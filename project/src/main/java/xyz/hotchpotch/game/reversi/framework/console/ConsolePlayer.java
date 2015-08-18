package xyz.hotchpotch.game.reversi.framework.console;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;
import xyz.hotchpotch.util.ConsoleScanner;

/**
 * 標準入力からの入力により手を指定する {@link Player} の実装です。<br>
 * 
 * @author nmby
 */
public class ConsolePlayer implements Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final boolean safety;
    
    private final ConsoleScanner<String> sc = ConsoleScanner
            .stringBuilder("[a-h][1-8]|PASS")
            .prompt("手を選択してください（\"a1\"～\"h8\" または \"PASS\"）" + System.lineSeparator() + "> ")
            .build();
    
    /**
     * {@code ConsolePlayer} のインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム条件
     */
    public ConsolePlayer(Color color, GameCondition gameCondition) {
        String keySafety = getClass().getName() + ".safety";
        String strSafety = gameCondition.getParam(keySafety);
        if (strSafety == null) {
            safety = true;
        } else {
            safety = Boolean.valueOf(strSafety);
        }
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、標準入力からの入力により手を選びます。<br>
     */
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        String strPoint = sc.get();
        Point point = "PASS".equals(strPoint) ? null : Point.of(strPoint);
        
        while (safety && !Rule.canApply(board, Move.of(color, point))) {
            System.out.print("不正な手です。");
            strPoint = sc.get();
            point = "PASS".equals(strPoint) ? null : Point.of(strPoint);
        }
        return point;
    }
}
