package xyz.hotchpotch.game.reversi.aiplayers;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.Player;

/**
 * リバーシ盤を左上から順に走査して最初に見つかった置ける位置を選択する {@link Player} の実装です。<br>
 * 恐らくは最も単純な {@code Player} の実装でしょう。<br>
 * 
 * @author nmby
 */
public class SimplestAIPlayer implements Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、リバーシ盤を左上から順に走査し、最初に見つかった置ける位置を選択します。<br>
     */
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        for (Point p : Point.values()) {
            if (Rule.canPutAt(board, color, p)) {
                return p;
            }
        }
        return null;
    }
}