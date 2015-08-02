package xyz.hotchpotch.game.reversi.aiplayers;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.GameCondition;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Player;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;

/**
 * リバーシ盤を左上から順に走査して最初に見つかった置ける場所を選択する Player の実装です。
 * 
 * @author nmby
 */
public class StupidAIPlayer implements Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    @Override
    public void init(Color color, GameCondition gameCondition) {
    }
    
    @Override
    public Move move(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        for (Point p : Point.values()) {
            if (Rule.canPutAt(board, color, p)) {
                return Move.of(color, p);
            }
        }
        return Move.of(color, null);
    }
}
