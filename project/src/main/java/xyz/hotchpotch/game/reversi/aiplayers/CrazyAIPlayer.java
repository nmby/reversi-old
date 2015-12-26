package xyz.hotchpotch.game.reversi.aiplayers;

import java.util.Optional;
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
 * <table border="1">
 *   <tr><th>キー</th><th>型</th><th>内容</th><th>デフォルト値</th></tr>
 *   <tr><td>seed</td><td>long</td><td>乱数ジェネレータのシード値</td><td>（なし）</td></tr>
 * </table>
 * 
 * @author nmby
 */
public class CrazyAIPlayer implements Player {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final Random random;
    
    /**
     * このクラスのインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム実施条件
     */
    public CrazyAIPlayer(Color color, GameCondition gameCondition) {
        // デバッグ用にシード値を受け取れるようにしておく。
        Optional<Long> seed = AIPlayerUtil.getLongParameter(gameCondition, "seed");
        random = seed.isPresent() ? new Random(seed.get()) : new Random();
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
