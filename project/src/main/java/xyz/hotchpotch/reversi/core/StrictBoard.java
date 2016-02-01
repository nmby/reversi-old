package xyz.hotchpotch.reversi.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
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
 * @since 2.0.0
 * @author nmby
 */
public class StrictBoard extends BaseBoard implements Serializable {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    /**
     * {@link StrictBoard} のシリアライゼーションプロキシです。<br>
     * 
     * @serial include
     * @author nmby
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /**
         * @serial この {@code SerializationProxy} オブジェクトが代理となる {@link StrictBoard} オブジェクトに適用された手（パスの手も含む）
         *         が適用順に格納されたリスト
         */
        private final List<Move> moves;
        
        private SerializationProxy(StrictBoard board) {
            moves = board.moves;
        }
        
        /**
         * 復元された {@code SerializationProxy} に対応する {@link StrictBoard} オブジェクトを返します。<br>
         * 
         * @serialData ゲーム開始状態の {@code StrictBoard} オブジェクトに対して、
         *             復元された {@link #moves} に含まれる手を順に適用して返します。<br>
         *             {@link #moves} に不正な手が含まれる場合は例外をスローして復元を中止します。
         * @return 復元された {@code SerializationProxy} オブジェクトに対応する {@code StrictBoard} オブジェクト
         * @throws ObjectStreamException 復元された {@link #moves} に不正な手が含まれる場合
         */
        private Object readResolve() throws ObjectStreamException {
            Board board = initializedBoard();
            try {
                for (Move move : moves) {
                    board.apply(move);
                }
            } catch (IllegalArgumentException e) {
                throw new InvalidObjectException(e.getMessage());
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
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private transient Color next;
    private transient final List<Move> moves;
    
    private StrictBoard() {
        assert 2 <= Point.HEIGHT;
        assert 2 <= Point.WIDTH;
        
        int i = (Point.HEIGHT - 1) / 2;
        int j = (Point.WIDTH - 1) / 2;
        map.put(Point.of(i + 0, j + 1), Color.BLACK);
        map.put(Point.of(i + 1, j + 0), Color.BLACK);
        map.put(Point.of(i + 0, j + 0), Color.WHITE);
        map.put(Point.of(i + 1, j + 1), Color.WHITE);
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
    // 同期化する（synchronized を付ける）ためにオーバーライドする。
    @Override
    public synchronized Color colorAt(Point point) {
        return super.colorAt(point);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    // 同期化する（synchronized を付ける）ためにオーバーライドする。
    public synchronized String toStringKindly() {
        return super.toStringKindly();
    }
    
    /**
     * {@inheritDoc}
     */
    // 同期化する（synchronized を付ける）ためにオーバーライドする。
    @Override
    public synchronized String toStringInLine() {
        return super.toStringInLine();
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see #toStringInLine()
     */
    // 同期化する（synchronized を付ける）ためにオーバーライドする。
    @Override
    public synchronized String toString() {
        return super.toString();
    }
    
    /**
     * この {@code StrictBoard} オブジェクトの代わりに、{@link SerializationProxy StrictBoard.SerializationProxy} オブジェクトを直列化します。<br>
     * 
     * @return この {@code StrictBoard} オブジェクトの代理となる {@link SerializationProxy StrictBoard.SerializationProxy} オブジェクト
     */
    private synchronized Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    /**
     * {@code StrictBoard} オブジェクトを直接復元することはできません。<br>
     * {@code StrictBoard} オブジェクトの復元は {@link SerializationProxy StrictBoard.SerializationProxy} を通して行う必要があります。<br>
     * 
     * @serialData 例外をスローして復元を中止します。
     * @param stream オブジェクト入力ストリーム
     * @throws InvalidObjectException 直接 {@code StrictBoard} オブジェクトの復元が試みられた場合
     */
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
