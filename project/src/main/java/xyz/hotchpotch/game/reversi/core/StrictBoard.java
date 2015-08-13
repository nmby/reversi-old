package xyz.hotchpotch.game.reversi.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * リバーシのルールを忠実に守るリバーシ盤の実装です。<br>
 * このリバーシ盤に対してルールに反する手が指定された場合は例外がスローされます。<br>
 * <br>
 * この実装はスレッドセーフです。<br>
 * 
 * @author nmby
 */
public class StrictBoard extends BaseBoard implements Serializable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final List<Move> moves;
        
        private SerializationProxy(StrictBoard board) {
            moves = board.moves;
        }
        
        private Object readResolve() {
            Board board = initializedBoard();
            for (Move move : moves) {
                board.apply(move);
            }
            return board;
        }
    }
    
    /**
     * ゲーム開始時の状態に初期化されたリバーシ盤を返します。<br>
     * 
     * @return ゲーム開始時の状態に初期化されたリバーシ盤
     */
    public static Board initializedBoard() {
        return new StrictBoard();
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private transient Color next;
    private transient final List<Move> moves;
    
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
     * 次の手番がパスの場合は、パスを表す {@link Move} オブジェクトを指定して本メソッドを実行する必要があります。<br>
     * 
     * @param move 適用する手
     * @throws NullPointerException {@code move} が {@code null} の場合
     * @throws IllegalArgumentException 許可されない手が指定された場合
     */
    @Override
    public synchronized void apply(Move move) {
        Objects.requireNonNull(move);
        if (move.color != next) {
            throw new IllegalArgumentException(String.format(
                    "本来の手番とは異なる色が指定されました。期待=%s, 実際=%s", next, move.color));
        }
        if (!Rule.canApply(this, move)) {
            throw new IllegalArgumentException(String.format(
                    "許可されない手が指定されました。move=%s, board=%s", move, this.toStringInLine()));
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
    public synchronized String toStringInLine() {
        return super.toStringInLine();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String toString() {
        return super.toString();
    }
    
    private synchronized Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
