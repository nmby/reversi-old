package xyz.hotchpotch.game.reversi.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 盤上の位置を表す不変クラスです。<br>
 * 同じ位置を表す Point インスタンスは同一であることが保証されます。<br>
 * 
 * @author nmby
 */
// [実装メモ]
// Point は本質的には列挙なので、64 個の要素を持つ enum としてもよいのだが、
// お勉強のためフツーのクラスとして実装した。
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
        
        private Object readResolve() {
            return of(i, j);
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
     * 指定された位置を表す Point インスタンスを返します。<br>
     * 同じ座標には、常に同じインスタンスを返します。<br>
     * 
     * @param i 縦座標
     * @param j 横座標
     * @return 座標 (i, j) を表す Point インスタンス
     * @throws IndexOutOfBoundsException (i, j) が範囲外の場合
     */
    public static Point of(int i, int j) {
        if (!isValidIndex(i, j)) {
            throw new IndexOutOfBoundsException(desc(i, j));
        }
        return points[ordinal(i, j)];
    }
    
    /**
     * すべての Point インスタンスを含む配列を返します。<br>
     * 
     * @return すべての Point インスタンスを含む配列
     */
    public static Point[] values() {
        return Arrays.copyOf(points, points.length);
    }
    
    /**
     * すべての Point インスタンスをソースとする並列ストリームを返します。<br>
     * 
     * @return 新しいストリーム
     */
    public static Stream<Point> stream() {
        // MEMO: ホントに並列ストリームになり得るのか要お勉強
        // MEMO: Spliterator について要お勉強
        return new HashSet<>(Arrays.asList(points)).parallelStream();
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
    // その変数やクラス自体が不変である場合、デメリットは少ない。
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
     * 指定された方向に次の Point があるか（盤上か）を返します。<br>
     * 
     * @param direction 次の Point の方向
     * @return 指定された方向に次の Point がある場合（盤上である場合）は true
     * @throws NullPointerException direction が null の場合
     */
    public boolean hasNext(Direction direction) {
        Objects.requireNonNull(direction);
        return isValidIndex(i + direction.di, j + direction.dj);
    }
    
    /**
     * 指定された方向の次の Point を返します。<br>
     * 
     * @param direction 次の Point の方向
     * @return 指定された方向の次の Point
     * @throws NullPointerException direction が null の場合
     * @throws NoSuchElementException 指定された方向の次の Point がない（盤上からはみ出る）場合
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return desc(i, j);
    }
    
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
