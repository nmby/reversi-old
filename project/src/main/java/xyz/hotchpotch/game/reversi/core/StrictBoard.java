package xyz.hotchpotch.game.reversi.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * リバーシのルールを忠実に守るリバーシ盤の実装です。<br>
 * この実装はスレッドセーフです。<br>
 * 
 * @author nmby
 */
public class StrictBoard extends BaseBoard implements Serializable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private Color next;
    private final List<Move> moves;
    
    private StrictBoard() {
        map.put(Point.of(3, 4), Color.BLACK);
        map.put(Point.of(4, 3), Color.BLACK);
        map.put(Point.of(3, 3), Color.WHITE);
        map.put(Point.of(4, 4), Color.WHITE);
        next = Color.BLACK;
        moves = new ArrayList<>();
    }
    
    /**
     * このリバーシ盤に指定された手を適用します。<br>
     * この実装は、リバーシのルールに忠実に従います。ルール上許可されない手が指定された場合は、例外がスローされます。<br>
     * 次の手番がパスの場合は、パスを表す Move で本メソッドを実行する必要があります。<br>
     * 
     * @param move 適用する手
     * @throws NullPointerException move が null の場合
     * @throws IllegalMoveException 許可されない手が指定された場合
     */
    @Override
    public synchronized void apply(Move move) {
        Objects.requireNonNull(move);
        if (move.color != next) {
            // ルールに違反したのがどちらのプレーヤーかは分からないので、violator=null として投げる。
            throw new IllegalTurnException(null, next, move, this);
        }
        if (!Rule.canApply(this, move)) {
            if (move.point == null) {
                throw new IllegalPassException(move, this);
            } else {
                throw new IllegalPointException(move, this);
            }
        }
        
        if (move.point != null) {
            Map<Direction, Integer> reversibles = Rule.counts(this, move);
            Map<Point, Color> wrapped = Collections.synchronizedMap(map);
            Direction.stream().forEach(d -> {
                int n = reversibles.get(d);
                Point p = move.point;
                while (0 < n--) {
                    p = p.next(d);
                    wrapped.put(p, move.color);
                }
            });
            wrapped.put(move.point, move.color);
        }
        
        moves.add(move);
        next = Rule.isGameOngoing(this) ? next.opposite() : null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Color colorAt(Point point) {
        return super.colorAt(point);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Map<Color, Integer> counts() {
        return super.counts();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String toStringKindly() {
        return super.toStringKindly();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String toString() {
        return super.toString();
    }
}
