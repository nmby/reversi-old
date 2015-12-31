package xyz.hotchpotch.game.reversi.aiplayers;

import java.time.Instant;

import xyz.hotchpotch.game.reversi.aiplayers.AIPlayerUtil.LightweightBoard;
import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;

/**
 * 深さ優先探索により必勝手を探す {@link Player} の実装です。<br>
 * 探索時間が足りない場合はランダムに手を選択します。<br>
 * <br>
 * 動作制御のために、次のオプションパラメータを与えることができます。<br>
 * <table border="1">
 *   <tr><th>キー</th><th>型</th><th>内容</th><th>デフォルト値</th></tr>
 *   <tr><td>seed</td><td>long</td><td>乱数ジェネレータのシード値</td><td>（なし）</td></tr>
 *   <tr><td>margin1</td><td>long</td><td>探索を実施する最少の残り持ち時間（ミリ秒）</td><td>100</td></tr>
 *   <tr><td>rounds</td><td>int</td><td>ゲームの序盤において、何手に一度、探索を試みるか</td><td>3</td></tr>
 *   <tr><td>debug</td><td>boolean</td><td>デバッグ出力の有無</td><td>false</td></tr>
 * </table>
 * 
 * @since 1.0.0
 * @author nmby
 */
public class DepthFirstAIPlayer implements Player {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * 試行時間切れを表す例外です。<br>
     * 
     * @since 1.0.0
     * @author nmby
     */
    private static class TimeUpException extends RuntimeException {
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final Player proxy;
    private final long margin1;
    private final int rounds;
    private final boolean debug;
    
    private int searchableTurns = Point.HEIGHT * Point.WIDTH;
    private int round = 0;
    private Instant deadline;
    
    /**
     * このクラスのインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム実施条件
     */
    public DepthFirstAIPlayer(Color color, GameCondition gameCondition) {
        proxy = new RandomAIPlayer(null, gameCondition);
        
        // 動作制御用パラメータの取得
        margin1 = AIPlayerUtil.getLongParameter(gameCondition, "margin1").filter(v -> 0 < v).orElse(100L);
        rounds = AIPlayerUtil.getIntParameter(gameCondition, "rounds").filter(v -> 0 < v).orElse(3);
        debug = AIPlayerUtil.getBooleanParameter(gameCondition, "debug").orElse(false);
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、深さ優先探索により必勝手を探します。<br>
     * 探索時間が足りない場合はランダムに手を選択します。<br>
     */
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        round++;
        round %= rounds;
        
        Point[] candidates = Point.stream()
                .filter(p -> Rule.canPutAt(board, color, p))
                .toArray(Point[]::new);
                
        if (candidates.length == 0) {
            // パスの場合
            return null;
        } else if (candidates.length == 1) {
            return candidates[0];
        }
        
        int blankCells = (int) Point.stream().filter(p -> board.colorAt(p) == null).count();
        int myTurns = (blankCells + 1) / 2;
        long millisForThisTurn = Long.min(givenMillisPerTurn, remainingMillisInGame / myTurns);
        
        if (millisForThisTurn < margin1) {
            // 残り時間が少ない場合は探索を行わずにランダムに返す。
            return proxy.decide(board, color, 0, 0);
        }
        
        // 前回の探索で読み切れた深さよりも現時点での残りターン数の方が 2 以上多い場合は、
        // 今回もどうせ読み切れずに時間切れとなり時間の無駄なので、探索を行わずにランダムに返す。
        // 但し、rounds 回に 1 回は探索を行う。
        if (searchableTurns + 1 < blankCells && 0 < round) {
            return proxy.decide(board, color, 0, 0);
        }
        
        // 探索を行い、必勝手を探索する。
        // 指定時間内に探索が終了しない場合は、ランダムに手を選択する。
        //
        // 本来、深さ優先探索はマルチスレッドと相性のよいアルゴリズムだが、
        // ここではソースコードの分かり易さと堅牢性を重視し、シングルスレッドでの処理とする。
        searchableTurns = 0;
        deadline = Instant.now().plusMillis(millisForThisTurn - margin1);
        
        Point selected;
        try {
            selected = searchDeeply(board, color, candidates);
            if (debug) {
                System.out.println(String.format("残り %d 手を読み切りました。", searchableTurns));
            }
        } catch (TimeUpException e) {
            selected = proxy.decide(board, color, 0, 0);
            if (debug) {
                System.out.println(String.format("%d 手まで読めましたが、時間不足です。", searchableTurns));
            }
        }
        return selected;
    }
    
    private Point searchDeeply(Board board, Color color, Point[] candidates) {
        int remainingTurns = (int) Point.stream().filter(p -> board.colorAt(p) == null).count() - 1;
        Point drawable = null;
        
        for (Point candidate : candidates) {
            LightweightBoard nextBoard = new LightweightBoard(board);
            nextBoard.apply(Move.of(color, candidate));
            Color winner = searchWinner(nextBoard, color.opposite(), remainingTurns);
            
            if (winner == color) {
                if (debug) {
                    System.out.print("勝ち確信！：");
                }
                return candidate;
            } else if (winner == null && drawable == null) {
                drawable = candidate;
            }
        }
        if (debug) {
            System.out.print(drawable != null ? "引き分け可能：" : "負け確定：");
        }
        return drawable != null ? drawable : proxy.decide(board, color, 0, 0);
    }
    
    /**
     * 深さ優先でゲーム木の末端まで再帰的に探索し、黒白それぞれが必勝手を指した場合の勝者を返す。<br>
     * 但し、必勝手の探索においては勝ち負けのみを考慮し、駒数の差は考慮しない。<br>
     * 
     * @param board リバーシ盤
     * @param currColor 現在の手番
     * @param remainingTurns 空のマスの数（どの程度の深さまで読めたかの記録に使用）
     * @return 勝者の色
     */
    private Color searchWinner(LightweightBoard board, Color currColor, int remainingTurns) {
        // 時間切れの場合は諦める
        if (Instant.now().isAfter(deadline)) {
            throw new TimeUpException();
        }
        
        if (!Rule.isGameOngoing(board)) {
            // このメソッド内で結果を返す際は、何手先まで読めたのかを記録する。（high-water mark）
            if (searchableTurns < remainingTurns) {
                searchableTurns = remainingTurns;
            }
            return Rule.winner(board);
        }
        
        Point[] availables = Point.stream().filter(p -> Rule.canPutAt(board, currColor, p)).toArray(Point[]::new);
        
        if (availables.length == 0) {
            Color winner = searchWinner(board, currColor.opposite(), remainingTurns);
            if (searchableTurns < remainingTurns) {
                searchableTurns = remainingTurns;
            }
            return winner;
        } else if (availables.length == 1) {
            board.apply(Move.of(currColor, availables[0]));
            Color winner = searchWinner(board, currColor.opposite(), remainingTurns - 1);
            if (searchableTurns < remainingTurns) {
                searchableTurns = remainingTurns;
            }
            return winner;
        }
        
        boolean drawable = false;
        for (Point p : availables) {
            LightweightBoard nextBoard = new LightweightBoard(board);
            nextBoard.apply(Move.of(currColor, p));
            Color winner = searchWinner(nextBoard, currColor.opposite(), remainingTurns - 1);
            
            if (winner == currColor) {
                if (searchableTurns < remainingTurns) {
                    searchableTurns = remainingTurns;
                }
                return currColor;
            } else if (winner == null) {
                drawable = true;
            }
        }
        if (searchableTurns < remainingTurns) {
            searchableTurns = remainingTurns;
        }
        return drawable ? null : currColor.opposite();
    }
}
