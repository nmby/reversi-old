package xyz.hotchpotch.game.reversi.aiplayers;

import java.util.Random;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;

/**
 * ランダムに手を選ぶ Plyer の実装です。<br>
 * 
 * @author nmby
 */
public class RandomAIPlayer implements Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final Random random;
    
    public RandomAIPlayer(Color color, GameCondition condition) {
        // デバッグ用にシード値を受け取れるようにしておく。
        String seedStr = condition.getProperty("random.seed");
        Long seed;
        try {
            seed = Long.valueOf(seedStr);
        } catch (NumberFormatException e) {
            seed = null;
        }
        random = seed != null ? new Random(seed) : new Random();
    }
    
    @Override
    public Move move(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        Point[] availables = Point.stream()
                .filter(p -> Rule.canPutAt(board, color, p))
                .toArray(Point[]::new);
                
        if (availables.length == 0) {
            return Move.of(color, null);
        } else {
            return Move.of(color, availables[random.nextInt(availables.length)]);
        }
    }
}
