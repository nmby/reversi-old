package xyz.hotchpotch.game.reversi.aiplayers;

import java.util.Random;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;

/**
 * ランダムに手を選ぶ {@link Player} の実装です。<br>
 * 
 * @author nmby
 */
public class RandomAIPlayer implements Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final Random random;
    
    /**
     * {@code RandomAIPlayer} のインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム条件
     */
    public RandomAIPlayer(Color color, GameCondition gameCondition) {
        // デバッグ用にシード値を受け取れるようにしておく。
        String key = getClass().getName() + ".seed";
        String seedStr = gameCondition.getProperty(key);
        Long seed;
        try {
            seed = Long.valueOf(seedStr);
        } catch (NumberFormatException e) {
            seed = null;
        }
        random = seed == null ? new Random() : new Random(seed);
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、駒を置ける位置の中からランダムに手を選びます。<br>
     */
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        Point[] availables = Point.stream()
                .filter(p -> Rule.canPutAt(board, color, p))
                .toArray(Point[]::new);
                
        return availables.length == 0 ? null : availables[random.nextInt(availables.length)];
    }
}
