package xyz.hotchpotch.game.reversi.aiplayers;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;

/**
 * モンテカルロ・シミュレーションにより最善の手を選択する {@link Player} の実装です。<br>
 * 
 * @author nmby
 */
public class MonteCarloAIPlayer implements Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * パラメータチェック等を省き必要最小限の機能に絞った軽量リバーシ盤。<br>
     */
    private static class LightweightBoard implements Board {
        
        private final Map<Point, Color> map;
        
        private LightweightBoard(Board board) {
            map = new HashMap<>();
            for (Point p : Point.values()) {
                map.put(p, board.colorAt(p));
            }
        }
        
        @Override
        public Color colorAt(Point point) {
            return map.get(point);
        }
        
        /**
         * {@inheritDoc}
         * 
         * ルールに基づく防御的なチェックは省く。
         * クライアント側でルール妥当性を保証する必要がある。
         */
        @Override
        public void apply(Move move) {
            assert move != null;
            assert move.point != null;
            assert Rule.canApply(this, move);
            
            Set<Point> reversibles = Rule.reversibles(this, move);
            for (Point p : reversibles) {
                map.put(p, move.color);
            }
            map.put(move.point, move.color);
        }
    }
    
    private static class Record {
        private final Color myColor;
        private final Point candidate;
        private int wins = 0;
        private int losts = 0;
        
        private Record(Color myColor, Point candidate) {
            this.myColor = myColor;
            this.candidate = candidate;
        }
        
        private void increment(Color winner) {
            if (winner == myColor) {
                wins++;
            } else if (winner == myColor.opposite()) {
                losts++;
            }
            // 引き分けの回数は数えても使わないため、数えない。
        }
    }
    
    private static final Comparator<Record> comparator = (r1, r2) -> {
        if (r2.wins < r1.wins) {
            return 1;
        } else if (r1.wins < r2.wins) {
            return -1;
        } else if (r1.losts < r2.losts) {
            return 1;
        } else if (r2.losts < r1.losts) {
            return -1;
        }
        return 0;
    };
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final Player proxy;
    private final long margin1;
    private final long margin2;
    private final boolean debug;
    
    /**
     * {@code MonteCarloAIPlayer} のインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム条件
     */
    public MonteCarloAIPlayer(Color color, GameCondition gameCondition) {
        proxy = new RandomAIPlayer(null, gameCondition);
        
        // 動作制御用パラメータの取得
        margin1 = CommonUtil.getParameter(gameCondition, getClass().getName() + ".margin1", Long::valueOf, 30L);
        margin2 = CommonUtil.getParameter(gameCondition, getClass().getName() + ".margin2", Long::valueOf, 15L);
        debug = CommonUtil.getParameter(gameCondition, getClass().getName() + ".debug", Boolean::valueOf, false);
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、モンテカルロ・シミュレーションにより最善の手を選択します。<br>
     */
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        Point[] candidates = Point.stream()
                .filter(p -> Rule.canPutAt(board, color, p))
                .toArray(Point[]::new);
                
        if (candidates.length == 0) {
            // パスの場合
            return null;
        } else if (candidates.length == 1) {
            return candidates[0];
        }
        
        long millisForThisTurn = millisForThisTurn(board, givenMillisPerTurn, remainingMillisInGame);
        if (millisForThisTurn < margin2) {
            // 残り時間が少ない場合はシミュレーションを行わずにランダムに返す。
            return proxy.decide(board, color, givenMillisPerTurn, remainingMillisInGame);
        }
        
        // シミュレーションを行い、候補箇所ごとの結果を受け取る。
        Collection<Record> records =
                simulate(board, color, candidates, Instant.now().plusMillis(millisForThisTurn));
                
        // デバッグモードの場合、候補箇所ごとのスコアを出力する。
        if (debug) {
            System.out.println("＜候補箇所: 勝ち / 負け＞");
            System.out.println(records.stream().sorted(comparator.reversed())
                    .map(r -> String.format("%s: %3d / %3d", r.candidate.toStringKindly(), r.wins, r.losts))
                    .collect(Collectors.joining(System.lineSeparator())));
            System.out.println();
        }
        
        // 得られた結果の中から最もスコアの高いものを選ぶ。
        return Collections.max(records, comparator).candidate;
    }
    
    /**
     * 終了時刻になるまで、候補箇所それぞれに対してシミュレーションを行い、
     * 候補箇所それぞれに対する結果を返す。<br>
     * 
     * @param board 現在のリバーシ盤
     * @param color 自身の駒の色
     * @param candidates 今回のターンで置ける場所（候補箇所）
     * @param deadline シミュレーション終了時刻
     * @return 候補箇所ごとの結果が格納された {@code Set}
     */
    private Collection<Record> simulate(Board board, Color color, Point[] candidates, Instant deadline) {
        
        // 下準備
        Map<Point, Record> records = new HashMap<>();
        Map<Point, Board> nextBoards = new HashMap<>();
        
        for (Point candidate : candidates) {
            records.put(candidate, new Record(color, candidate));
            
            Board nextBoard = new LightweightBoard(board);
            nextBoard.apply(Move.of(color, candidate));
            nextBoards.put(candidate, nextBoard);
        }
        
        // 本処理
        // 本来、モンテカルロ・シミュレーションは並行プログラミングと相性のよいアルゴリズムだが、
        // ここでは分かり易さとコードの堅牢さを優先し、シングルスレッドでの順次処理として実装する。
        do {
            
            for (Point candidate : candidates) {
                
                // 本来はある程度の回数まとめてシミュレーションを行った方がオーバーヘッドを減らせるのだが、
                // ここでは簡略さを優先することにする。
                Color winner = simulateOneGame(nextBoards.get(candidate), color.opposite());
                records.get(candidate).increment(winner);
            }
            
        } while (Instant.now().isBefore(deadline));
        
        return records.values();
    }
    
    /**
     * ゲーム終了までランダムに手を進め、どちらが勝つかシミュレートする。<br>
     * 
     * @param origin シミュレーション開始時点のリバーシ盤
     * @param nextColor 次のターンの色
     * @return 勝者の色
     */
    private Color simulateOneGame(Board origin, Color nextColor) {
        Board board = new LightweightBoard(origin);
        Color currColor = nextColor;
        
        while (Rule.isGameOngoing(board)) {
            Point point = proxy.decide(board, currColor, 0, 0);
            if (point != null) {
                board.apply(Move.of(currColor, point));
            }
            currColor = currColor.opposite();
        }
        
        return Rule.winner(board);
    }
    
    /**
     * 今回のターンの思考に何ミリ秒費やすかを決める。<br>
     * 
     * @param board 現在のリバーシ盤
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param remainingMillisInGame ゲーム内での残り持ち時間（ミリ秒）
     * @return 今回のターンに費やせる時間（ミリ秒）
     */
    private long millisForThisTurn(Board board, long givenMillisPerTurn, long remainingMillisInGame) {
        int blankCells = (int) Point.stream().filter(p -> board.colorAt(p) == null).count();
        
        // 末尾の「+ 1」は、相手がパスすることにより自分のターンが増える可能性があるため、
        // １ターン分余裕を持っておくもの。
        int myTurns = (blankCells + 1) / 2 + 1;
        
        // ゲームの序盤/中盤/終盤で時間配分を変える戦略もあり得るが、ここでは単純に按分することとする。
        long millisPerTurn = remainingMillisInGame / myTurns;
        
        return Long.min(millisPerTurn, givenMillisPerTurn) - margin1;
    }
}
