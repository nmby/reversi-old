package xyz.hotchpotch.reversi.aiplayers;

import java.time.Instant;

import xyz.hotchpotch.reversi.aiplayers.AIPlayerUtil.LightweightBoard;
import xyz.hotchpotch.reversi.core.Board;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Move;
import xyz.hotchpotch.reversi.core.Point;
import xyz.hotchpotch.reversi.core.Rule;
import xyz.hotchpotch.reversi.framework.GameCondition;
import xyz.hotchpotch.reversi.framework.Player;

/**
 * 深さ優先探索により必勝手を探す {@link Player} の実装です。<br>
 * 探索時間が足りない場合はランダムに手を選択します。<br>
 * <br>
 * 動作制御のために、次のオプションパラメータを与えることができます。<br>
 * <table border="1">
 *   <caption>指定可能なオプションパラメータ</caption>
 *   <tr><th>キー</th><th>型</th><th>内容</th><th>デフォルト値</th></tr>
 *   <tr><td>{@code seed}</td><td>{@code long}</td><td>乱数ジェネレータのシード値</td><td>（なし）</td></tr>
 *   <tr><td>{@code skip}</td><td>{@code int}</td><td>ゲーム序盤で探索を行わない自ターン数</td><td>{@code 12}</td></tr>
 *   <tr><td>{@code rounds}</td><td>{@code int}</td><td>最低何手に一回、探索を試みるか</td><td>{@code 3}</td></tr>
 *   <tr><td>{@code margin1}</td><td>{@code long}</td><td>探索を実施する最少の残り持ち時間（ミリ秒）</td><td>{@code 100}</td></tr>
 *   <tr><td>{@code weight}</td><td>{@code float}</td><td>ゲーム終盤よりも中盤に時間を費やすためのウェイト</td><td>{@code 3.5}</td></tr>
 *   <tr><td>{@code debug}</td><td>{@code boolean}</td><td>デバッグ出力の有無</td><td>{@code false}</td></tr>
 * </table>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class DepthFirstAIPlayer implements Player {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * 試行時間切れを表す例外です。<br>
     * 
     * @author nmby
     */
    private static class TimeUpException extends RuntimeException {
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final Player proxy;
    private final long margin1;
    private final int rounds;
    private final float weight;
    private final boolean debug;
    
    private int searchableTurns = Point.HEIGHT * Point.WIDTH;
    private int round = -1;
    private int skip;
    private Instant deadline;
    
    /**
     * このクラスのインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの石の色
     * @param gameCondition ゲーム実施条件
     */
    public DepthFirstAIPlayer(Color color, GameCondition gameCondition) {
        proxy = new RandomAIPlayer(null, gameCondition);
        
        // 動作制御用パラメータの取得
        skip = AIPlayerUtil.getIntParameter(gameCondition, "skip").filter(v -> 0 <= v).orElse(12);
        rounds = AIPlayerUtil.getIntParameter(gameCondition, "rounds").filter(v -> 0 < v).orElse(3);
        margin1 = AIPlayerUtil.getLongParameter(gameCondition, "margin1").filter(v -> 0 < v).orElse(100L);
        weight = AIPlayerUtil.getFloatParameter(gameCondition, "weight").filter(v -> 1.0f <= v).orElse(3.5f);
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
        if (0 < skip) {
            skip--;
            return proxy.decide(board, color, 0, 0);
        }
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
        
        // 前回の探索で読み切れた深さよりも現時点での残りターン数の方が 2 以上多い場合は、
        // 今回もどうせ読み切れずに時間切れとなり時間の無駄なので、探索を行わずにランダムに返す。
        // 但し、rounds 回に 1 回は探索を行う。
        int blankCells = (int) Point.stream().filter(p -> board.colorAt(p) == null).count();
        if (searchableTurns + 1 < blankCells && 0 < round) {
            return proxy.decide(board, color, 0, 0);
        }
        
        int myTurns = (blankCells + 1) / 2;
        long millisForThisTurn = Long.min(givenMillisPerTurn, (long) (weight * remainingMillisInGame / myTurns));
        
        // 残り時間が少ない場合は探索を行わずにランダムに返す。
        if (millisForThisTurn < margin1) {
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
            selected = searchCertainPoint(board, color, candidates);
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
    
    private Point searchCertainPoint(Board board, Color color, Point[] candidates) {
        int remainingTurns = (int) Point.stream().filter(p -> board.colorAt(p) == null).count() - 1;
        Point drawable = null;
        
        for (Point candidate : candidates) {
            LightweightBoard nextBoard = new LightweightBoard(board);
            nextBoard.apply(Move.of(color, candidate));
            Color winner = searchWinnerDeeply(nextBoard, color.opposite(), remainingTurns);
            
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
     * 但し、必勝手の探索においては勝ち負けのみを考慮し、石数の差は考慮しない。<br>
     * 
     * @param board リバーシ盤
     * @param currColor 現在の手番
     * @param remainingTurns 空のマスの数（どの程度の深さまで読めたかの記録に使用）
     * @return 勝者の色
     */
    private Color searchWinnerDeeply(LightweightBoard board, Color currColor, int remainingTurns) {
        // 時間切れの場合は諦める
        if (Instant.now().isAfter(deadline)) {
            throw new TimeUpException();
        }
        
        Color winner;
        Point[] availables = Point.stream()
                .filter(p -> Rule.canPutAt(board, currColor, p))
                .toArray(Point[]::new);
        
        if (availables.length == 0 && !Rule.canPut(board, currColor.opposite())) {
            winner = Rule.winner(board);
            
        } else if (availables.length == 0) {
            winner = searchWinnerDeeply(board, currColor.opposite(), remainingTurns);
            
        } else if (availables.length == 1) {
            board.apply(Move.of(currColor, availables[0]));
            winner = searchWinnerDeeply(board, currColor.opposite(), remainingTurns - 1);
            
        } else {
            winner = currColor.opposite();
            for (Point p : availables) {
                LightweightBoard nextBoard = new LightweightBoard(board);
                nextBoard.apply(Move.of(currColor, p));
                Color tmp = searchWinnerDeeply(nextBoard, currColor.opposite(), remainingTurns - 1);
                
                if (tmp == currColor) {
                    winner = currColor;
                    break;
                    
                } else if (tmp == null) {
                    winner = null;
                }
            }
        }
        
        // 終了から何手前まで読めたのかを記録する。（high-water mark）
        if (searchableTurns < remainingTurns) {
            searchableTurns = remainingTurns;
        }
        return winner;
    }
}
