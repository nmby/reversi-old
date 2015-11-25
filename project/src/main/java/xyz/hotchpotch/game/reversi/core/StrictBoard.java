package xyz.hotchpotch.game.reversi.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * ルールを忠実に守るリバーシ盤の実装です。<br>
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
     * また、黒白の順番も忠実に守る必要があります。
     * 次の手番がパスの場合は、パスを表す {@link Move} オブジェクトを指定して本メソッドを実行しなければなりません。<br>
     * 
     * @param move 適用する手
     * @throws NullPointerException {@code move} が {@code null} の場合
     * @throws IllegalArgumentException 許可されない手や手番とは異なる色の手が指定された場合
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
            Set<Point> reversibles = Rule.reversibles(this, move);
            for (Point p : reversibles) {
                map.put(p, move.color);
            }
            map.put(move.point, move.color);
        }
        
        moves.add(move);
        next = Rule.isGameOngoing(this) ? next.opposite() : null;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException {@code point} が {@code null} の場合
     */
    @Override
    public synchronized Color colorAt(Point point) {
        return super.colorAt(point);
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
     * 
     * @see #toStringInLine()
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
