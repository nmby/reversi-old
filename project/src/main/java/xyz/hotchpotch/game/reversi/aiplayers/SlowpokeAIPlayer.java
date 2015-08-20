package xyz.hotchpotch.game.reversi.aiplayers;

import java.util.Random;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;

/**
 * ランダムに手を選びますが、のろまな {@link Player} の実装です。<br>
 * 早晩、時間切れで敗退することでしょう。<br>
 * 
 * @author nmby
 */
public class SlowpokeAIPlayer implements Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final Random random;
    private final int slowest;
    
    /**
     * {@code SlowpokeAIPlayer} のインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム条件
     */
    public SlowpokeAIPlayer(Color color, GameCondition gameCondition) {
        // デバッグ用に各種パラメータ値を受け取れるようにしておく。
        Long seed = CommonUtil.getParameter(gameCondition, getClass(), "seed", Long::valueOf, null);
        random = seed == null ? new Random() : new Random(seed);
        
        int slowest = CommonUtil.getParameter(gameCondition, getClass(), "slowest", Integer::parseInt, 0);
        if (slowest == 0) {
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
     * この実装は、駒を置ける位置の中からランダムに手を選びます。ただし、のろまです。<br>
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
