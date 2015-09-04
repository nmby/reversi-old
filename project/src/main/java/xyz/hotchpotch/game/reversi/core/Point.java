package xyz.hotchpotch.game.reversi.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 盤上の位置を表す不変クラスです。<br>
 * 同じ位置を表す {@code Point} インスタンスは同一であることが保証されます。<br>
 * 
 * @author nmby
 */
// Point は本質的には列挙なので、64 個の要素を持つ enum としてもよいのだが、
// お勉強のため普通のクラスとして実装した。
public class Point implements Serializable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    // 同値オブジェクトの単一性を保証するためにシリアライズプロキシパターンを採用する。
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final int i;
        private final int j;
        
        private SerializationProxy(Point point) {
            i = point.i;
            j = point.j;
        }
        
        private Object readResolve() throws ObjectStreamException {
            try {
                return of(i, j);
            } catch (IndexOutOfBoundsException e) {
                throw new InvalidObjectException("IndexOutOfBounds: " + desc(i,  j));
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
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                points[ordinal(i, j)] = new Point(i, j);
            }
        }
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
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
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
    
    int ordinal() {
        return ordinal(i, j);
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
        return String.format("%c%d", 'a' + j, i + 1);
    }
    
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
