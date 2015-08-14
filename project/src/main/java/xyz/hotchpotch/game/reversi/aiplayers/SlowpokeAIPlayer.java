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
        String keySeed = getClass().getName() + ".seed";
        String strSeed = gameCondition.getProperty(keySeed);
        Long seed;
        try {
            seed = Long.valueOf(strSeed);
        } catch (NumberFormatException e) {
            seed = null;
        }
        random = seed == null ? new Random() : new Random(seed);
        
        String keySlowest = getClass().getName() + ".slowest";
        String strSlowest = gameCondition.getProperty(keySlowest);
        int slowest;
        try {
            slowest = Integer.parseInt(strSlowest);
        } catch (NumberFormatException e) {
            if (0 < Long.compare(gameCondition.givenMillisPerTurn, Integer.MAX_VALUE)) {
                slowest = Integer.MAX_VALUE;
            } else if (0 < Long.compare(gameCondition.givenMillisPerTurn * 2, Integer.MAX_VALUE)) {
                slowest = Integer.MAX_VALUE;
            } else {
                slowest = (int) gameCondition.givenMillisPerTurn * 2;
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
