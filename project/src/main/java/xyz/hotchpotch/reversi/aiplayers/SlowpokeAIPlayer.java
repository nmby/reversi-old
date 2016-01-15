package xyz.hotchpotch.reversi.aiplayers;

import java.util.Optional;
import java.util.Random;

import xyz.hotchpotch.reversi.core.Board;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Point;
import xyz.hotchpotch.reversi.core.Rule;
import xyz.hotchpotch.reversi.framework.GameCondition;
import xyz.hotchpotch.reversi.framework.Player;

/**
 * ランダムに手を選びますが、のろまな {@link Player} の実装です。<br>
 * 早晩、時間切れで敗退することでしょう。<br>
 * <br>
 * 動作制御のために、次のオプションパラメータを与えることができます。
 * <table border="1">
 *   <caption>指定可能なオプションパラメータ</caption>
 *   <tr><th>キー</th><th>型</th><th>内容</th><th>デフォルト値</th></tr>
 *   <tr><td>{@code seed}</td><td>{@code long}</td><td>乱数ジェネレータのシード値</td><td>（なし）</td></tr>
 *   <tr><td>{@code slowest}</td><td>{@code int}</td><td>思考にかける最大の時間（ミリ秒）（0ミリ秒からこの値の間でランダムに選択されます。）</td><td>一手あたりの持ち時間の1.25倍</td></tr>
 * </table>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class SlowpokeAIPlayer implements Player {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final Random random;
    private final int slowest;
    
    /**
     * このクラスのインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの石の色
     * @param gameCondition ゲーム実施条件
     */
    public SlowpokeAIPlayer(Color color, GameCondition gameCondition) {
        // デバッグ用に各種パラメータ値を受け取れるようにしておく。
        Optional<Long> seed = AIPlayerUtil.getLongParameter(gameCondition, "seed");
        random = seed.isPresent() ? new Random(seed.get()) : new Random();
        
        int slowest = AIPlayerUtil.getIntParameter(gameCondition, "slowest").orElse(0);
        if (slowest <= 0) {
            // 制限時間の 1.25 倍を上限とする。（5回に1回は制限時間オーバーになるはず）
            try {
                long tmp = Math.multiplyExact(gameCondition.givenMillisPerTurn / 4, 5);
                slowest = (int) Long.min(tmp, Integer.MAX_VALUE);
            } catch (ArithmeticException e) {
                slowest = Integer.MAX_VALUE;
            }
        }
        this.slowest = slowest;
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、石を置ける位置の中からランダムに手を選びます。ただし、のろまです。<br>
     * 早晩、時間切れで敗退することでしょう。<br>
     */
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        Point[] availables = Point.stream()
                .filter(p -> Rule.canPutAt(board, color, p))
                .toArray(Point[]::new);
                
        try {
            Thread.sleep(random.nextInt(slowest));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return availables.length == 0 ? null : availables[random.nextInt(availables.length)];
    }
}
