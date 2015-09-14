package xyz.hotchpotch.game.reversi.aiplayers;

import java.util.Random;

import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Direction;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;

/**
 * その位置に自分の駒を置けるか否かも考えず、既存の駒に隣接する空きセルの中からランダムに手を選ぶ
 * {@link Player} の実装です。<br>
 * 早晩、ルール違反で敗退することでしょう。<br>
 * <br>
 * 動作制御のために、次のオプションパラメータを与えることができます。
 * <ul>
 *   <li>seed : 乱数ジェネレータのシード値（long）</li>
 * </ul>
 * 
 * @author nmby
 */
public class CrazyAIPlayer implements Player {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final Random random;
    
    /**
     * このクラスのインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム実施条件
     */
    public CrazyAIPlayer(Color color, GameCondition gameCondition) {
        // デバッグ用にシード値を受け取れるようにしておく。
        Long seed = CommonUtil.getParameter(gameCondition, getClass(), "seed", Long::valueOf, null);
        random = seed == null ? new Random() : new Random(seed);
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、駒を置ける位置か否かを考慮せず、既存の駒に隣接する空きセルの中からランダムに手を選びます。<br>
     * 早晩、ルール違反で敗退することでしょう。<br>
     */
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        Point[] neighborAndBlankPoints = Point.stream()
                .filter(p -> board.colorAt(p) == null)
                .filter(p -> Direction.stream().anyMatch(d -> p.hasNext(d) && board.colorAt(p.next(d)) != null))
                .toArray(Point[]::new);
                
        return neighborAndBlankPoints[random.nextInt(neighborAndBlankPoints.length)];
    }
}
