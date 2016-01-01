package xyz.hotchpotch.game.reversi.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * リバーシ盤上の位置を表す不変クラスです。<br>
 * 同じ位置を表す {@code Point} インスタンスは同一であることが保証されます。<br>
 * <br>
 * {@code Point} オブジェクトは次の順に順序付けされます。
 * <pre>
 *     Point.of(0, 0), Point.of(0, 1), Point.of(0, 2), ... Point.of(7, 6), Point.of(7, 7)
 * </pre>
 * または、
 * <pre>
 *     Point.of("a1"), Point.of("b1"), Point.of("c1"), ... Point.of("g8"), Point.of("h8")
 * </pre>
 * 
 * @since 1.0.0
 * @author nmby
 */
// Point は本質的には列挙なので、64 個の要素を持つ enum としてもよいのだが、
// お勉強のため普通のクラスとして実装した。
public class Point implements Serializable, Comparable<Point> {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    /**
     * {@link Point} のシリアライゼーションプロキシです。<br>
     * 
     * @serial include
     * @since 1.0.0
     * @author nmby
     */
    // 同値オブジェクトの単一性を保証するためにシリアライズプロキシパターンを採用する。
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** @serial 縦座標 */
        private final int i;
        
        /** @serial 横座標 */
        private final int j;
        
        private SerializationProxy(Point point) {
            i = point.i;
            j = point.j;
        }
        
        /**
         * 復元された {@code SerializationProxy} に対応する {@link Point} インスタンスを返します。<br>
         * 
         * @serialData 復元された {@code (i, j)} に対応する {@code Point} インスタンスを返します。<br>
         *             {@code (i, j)} の値が不正な場合は例外をスローして復元を中止します。
         * @return 復元された {@code SerializationProxy} オブジェクトに対応する {@link Point} インスタンス
         * @throws ObjectStreamException 復元された {@code (i, j)} の値が不正な場合
         */
        private Object readResolve() throws ObjectStreamException {
            try {
                return of(i, j);
            } catch (IndexOutOfBoundsException e) {
                throw new InvalidObjectException("IndexOutOfBounds: " + desc(i, j));
            }
        }
    }
    
    /** 座標平面（リバーシ盤）の高さ */
    public static final int HEIGHT = 8;
    
    /** 座標平面（リバーシ盤）の幅 */
    public static final int WIDTH = 8;
    
    private static final Point[] points;
    
    static {
        points = new Point[HEIGHT * WIDTH];
        IntStream.range(0, HEIGHT).forEach(i -> IntStream.range(0, WIDTH).forEach(j -> {
            points[ordinal(i, j)] = new Point(i, j);
        }));
    }
    
    /**
     * 指定された位置を表す {@code Point} インスタンスを返します。<br>
     * 同じ座標には、常に同じインスタンスを返します。<br>
     * 
     * @param i 縦座標
     * @param j 横座標
     * @return 座標 {@code (i, j)} を表す {@code Point} インスタンス
     * @throws IndexOutOfBoundsException {@code (i, j)} が範囲外の場合
     */
    public static Point of(int i, int j) {
        if (!isValidIndex(i, j)) {
            throw new IndexOutOfBoundsException(desc(i, j));
        }
        return points[ordinal(i, j)];
    }
    
    /**
     * {@code "a1"}～{@code "h8"} 形式で指定された位置を表す {@code Point} インスタンスを返します。<br>
     * 同じ座標には、常に同じインスタンスを返します。<br>
     * 
     * @param str {@code "a1"}～{@code "h8"} 形式の位置
     * @return 指定された位置を表す {@code Point} インスタンス
     * @throws NullPointerException {@code str} が {@code null} の場合
     * @throws IllegalArgumentException {@code str} の形式が不正な場合
     */
    public static Point of(String str) {
        Objects.requireNonNull(str);
        if (!str.matches(String.format("[a-%c][1-%d]", 'a' + (WIDTH - 1), HEIGHT))) {
            throw new IllegalArgumentException("str=" + str);
        }
        
        int i = str.charAt(1) - '1';
        int j = str.charAt(0) - 'a';
        return points[ordinal(i, j)];
    }
    
    /**
     * すべての {@code Point} インスタンスを含む配列を返します。<br>
     * 
     * @return すべての {@code Point} インスタンスを含む配列
     */
    public static Point[] values() {
        return Arrays.copyOf(points, points.length);
    }
    
    /**
     * すべての {@code Point} インスタンスをソースとする順次ストリームを返します。<br>
     * 
     * @return 新しいストリーム
     */
    public static Stream<Point> stream() {
        return Arrays.stream(points);
    }
    
    /**
     * すべての {@code Point} インスタンスをソースとする並列ストリームを返します。<br>
     * 
     * @return 新しいストリーム
     */
    public static Stream<Point> parallelStream() {
        return Arrays.stream(points).parallel();
    }
    
    private static boolean isValidIndex(int i, int j) {
        return 0 <= i && i < HEIGHT && 0 <= j && j < WIDTH;
    }
    
    private static String desc(int i, int j) {
        return String.format("(%d, %d)", i, j);
    }
    
    private static int ordinal(int i, int j) {
        return WIDTH * i + j;
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // メンバ変数を public で公開するのに抵抗を感じるかもしれないが、
    // その変数参照や変数インスタンス自体が不変である場合、デメリットは少ない。
    // むしろ積極的に public にすべきだという主張もある。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** 縦座標 */
    public final transient int i;
    
    /** 横座標 */
    public final transient int j;
    
    private Point(int i, int j) {
        this.i = i;
        this.j = j;
    }
    
    /**
     * 指定された方向に次の {@code Point} があるか（盤上か）を返します。<br>
     * 
     * @param direction 次の {@code Point} の方向
     * @return 指定された方向に次の {@code Point} がある場合（盤上である場合）は {@code true}
     * @throws NullPointerException {@code direction} が {@code null} の場合
     */
    public boolean hasNext(Direction direction) {
        Objects.requireNonNull(direction);
        return isValidIndex(i + direction.di, j + direction.dj);
    }
    
    /**
     * 指定された方向の次の {@code Point} を返します。<br>
     * 
     * @param direction 次の {@code Point} の方向
     * @return 指定された方向の次の {@code Point}
     * @throws NullPointerException {@code direction} が {@code null} の場合
     * @throws NoSuchElementException 指定された方向の次の {@code Point} がない（盤上からはみ出る）場合
     */
    public Point next(Direction direction) {
        Objects.requireNonNull(direction);
        int ni = i + direction.di;
        int nj = j + direction.dj;
        if (isValidIndex(ni, nj)) {
            return points[ordinal(ni, nj)];
        } else {
            throw new NoSuchElementException(
                    String.format("%s + %s -> %s", desc(i, j), direction, desc(ni, nj)));
        }
    }
    
    /**
     * この {@code Point} の序数を返します。<br>
     * 
     * @return この {@code Point} の序数
     * @see Enum#ordinal()
     */
    public int ordinal() {
        return ordinal(i, j);
    }
    
    /**
     * この {@code Point} オブジェクトを指定された {@code Point} オブジェクトと比較します。<br>
     * 
     * @param p 比較対象の {@code Point} オブジェクト
     * @return この {@code Point} が {@code p} より小さい場合は {@code -1}、等しい場合は {@code 0}、大きい場合は {@code 1}
     * @throws NullPointerException {@code p} が {@code null} の場合
     */
    @Override
    public int compareTo(Point p) {
        Objects.requireNonNull(p);
        
        if (this == p) {
            return 0;
        } else if (ordinal() < p.ordinal()) {
            return -1;
        } else {
            return 1;
        }
    }
    
    /**
     * この {@code Point} の文字列表現を {@code (i, j)} 形式で返します。<br>
     * 
     * @return この {@code Point} の文字列表現（{@code (i, j)} 形式）
     */
    @Override
    public String toString() {
        return desc(i, j);
    }
    
    /**
     * この {@code Point} の文字列表現を {@code a1}～{@code h8} の形式で返します。<br>
     * 
     * @return この {@code Point} の文字列表現（{@code a1}～{@code h8} の形式）
     */
    public String toStringKindly() {
        return String.format("%c%d", 'a' + j, 1 + i);
    }
    
    /**
     * この {@code Point} インスタンスの代わりに、{@link SerializationProxy Point.SerializationProxy} オブジェクトを直列化します。<br>
     * 
     * @return この {@code Point} インスタンスの代理となる {@link SerializationProxy Point.SerializationProxy} オブジェクト
     * @see SerializationProxy Point.SerializationProxy
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    /**
     * {@code Point} インスタンスを直接復元することはできません。<br>
     * {@code Point} インスタンスの復元は {@link SerializationProxy Point.SerializationProxy} を通して行う必要があります。<br>
     * 
     * @serialData 例外をスローして復元を中止します。
     * @param stream オブジェクト入力ストリーム
     * @throws InvalidObjectException 直接 {@code Point} インスタンスの復元が試みられた場合
     * @see SerializationProxy Point.SerializationProxy
     */
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
