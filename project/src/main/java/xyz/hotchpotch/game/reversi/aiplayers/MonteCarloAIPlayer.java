package xyz.hotchpotch.game.reversi.aiplayers;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import xyz.hotchpotch.game.reversi.aiplayers.AIPlayerUtil.LightweightBoard;
import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;

/**
 * モンテカルロ・シミュレーションにより最善手を選択する {@link Player} の実装です。<br>
 * 試行時間が足りない場合はランダムに手を選択します。<br>
 * <br>
 * 動作制御のために、次のオプションパラメータを与えることができます。<br>
 * <table border="1">
 *   <caption>指定可能なオプションパラメータ</caption>
 *   <tr><th>キー</th><th>型</th><th>内容</th><th>デフォルト値</th></tr>
 *   <tr><td>seed</td><td>long</td><td>乱数ジェネレータのシード値</td><td>（なし）</td></tr>
 *   <tr><td>margin1</td><td>long</td><td>シミュレーション結果の評価のために確保する時間（ミリ秒）</td><td>100</td></tr>
 *   <tr><td>margin2</td><td>long</td><td>シミュレーションを実施する最少の残り持ち時間（ミリ秒）</td><td>50</td></tr>
 *   <tr><td>debug</td><td>boolean</td><td>デバッグ出力の有無</td><td>false</td></tr>
 * </table>
 * 
 * @since 1.0.0
 * @author nmby
 */
public class MonteCarloAIPlayer implements Player {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * ある候補手についての試行結果を保持するためのクラスです。<br>
     * 
     * @since 1.0.0
     * @author nmby
     */
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
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final Player proxy;
    private final long margin1;
    private final long margin2;
    private final boolean debug;
    
    /**
     * このクラスのインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム実施条件
     */
    public MonteCarloAIPlayer(Color color, GameCondition gameCondition) {
        proxy = new RandomAIPlayer(null, gameCondition);
        
        // 動作制御用パラメータの取得
        margin1 = AIPlayerUtil.getLongParameter(gameCondition, "margin1").filter(v -> 0 < v).orElse(100L);
        margin2 = AIPlayerUtil.getLongParameter(gameCondition, "margin2").filter(v -> 0 < v).orElse(50L);
        debug = AIPlayerUtil.getBooleanParameter(gameCondition, "debug").orElse(false);
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、モンテカルロ・シミュレーションにより最善手を選択します。<br>
     * 試行時間が足りない場合はランダムに手を選択します。<br>
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
            return proxy.decide(board, color, 0, 0);
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
        Map<Point, LightweightBoard> nextBoards = new HashMap<>();
        
        for (Point candidate : candidates) {
            records.put(candidate, new Record(color, candidate));
            
            // 後続の simulateOneGame の中の処理で
            // new LightweightBoard(Board) ではなく new LightweightBoard(LightweightBoard) が選ばれるように
            // 敢えて LightweightBoard 型の参照で受けている。
            LightweightBoard nextBoard = new LightweightBoard(board);
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
    private Color simulateOneGame(LightweightBoard origin, Color nextColor) {
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
     * 今回のターンの思考（試行）に何ミリ秒費やすかを決める。<br>
     * 
     * @param board 現在のリバーシ盤
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param remainingMillisInGame ゲーム内での残り持ち時間（ミリ秒）
     * @return 今回のターンに費やせる時間（ミリ秒）
     */
    private long millisForThisTurn(Board board, long givenMillisPerTurn, long remainingMillisInGame) {
        int blankCells = (int) Point.stream().filter(p -> board.colorAt(p) == null).count();
        int myTurns = (blankCells + 1) / 2;
        float weight;
        
        // 序盤～中盤に時間を割けるよう、残り手数に応じて配分を変える。
        if (30 <= myTurns) {
            // 黒の初手はどこを選んでも同じなので考えるだけ無駄
            // 白の初手もランダムに選ぶことにする
            return 0;
        } else if (24 <= myTurns) {
            weight = 1.5f;
        } else if (18 <= myTurns) {
            weight = 2.0f;
        } else if (12 <= myTurns) {
            weight = 1.5f;
        } else if (6 <= myTurns) {
            weight = 1.0f;
        } else {
            weight = 1.0f;
        }
        
        long millisPerTurn = (long) ((float) (remainingMillisInGame - margin1) * weight) / myTurns;
        return Long.min(millisPerTurn, givenMillisPerTurn - margin1);
    }
}
