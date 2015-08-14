package xyz.hotchpotch.game.reversi.aiplayers;

import java.util.Random;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;

/**
 * その位置に自分の駒を置けるか否かも考えず、完全にランダムに手を選ぶ {@link Player} の実装です。<br>
 * 早晩、ルール違反で敗退することでしょう。<br>
 * 
 * @author nmby
 */
public class CrazyAIPlayer implements Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final Random random;
    
    /**
     * {@code CrazyAIPlayer} のインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム条件
     */
    public CrazyAIPlayer(Color color, GameCondition gameCondition) {
        // デバッグ用にシード値を受け取れるようにしておく。
        String keySeed = getClass().getName() + ".seed";
        String strSeed = gameCondition.getProperty(keySeed);
        Long seed;
        try {
            seed = Long.valueOf(strSeed);
        } catch (NumberFormatException e) {
            seed = null;
        }
        random = seed == null ? new Random() : new Random(seed);
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、駒を置けるか否かを考慮せず、完全にランダムに手を選びます。<br>
     * 早晩、ルール違反で敗退することでしょう。<br>
     */
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        Point[] points = Point.values();
        return points[random.nextInt(points.length)];
    }
}