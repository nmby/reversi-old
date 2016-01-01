package xyz.hotchpotch.game.reversi.core;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * リバーシゲームにおける手を表す不変クラスです。<br>
 * <ul>
 *   <li>{@code Move.color} ： 手を指したプレーヤーの色を表します。</li>
 *   <li>{@code Move.point} ： 指定された駒の位置を表します。{@code null} の場合はパスを表します。</li>
 * </ul>
 * <br>
 * {@code Move} は値ベースのクラスです。
 * 同値性を確認するときは {@code Move.}{@link #equals(Object)} メソッドを使用してください。<br>
 * 
 * @since 1.0.0
 * @author nmby
 */
public class Move implements Serializable {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 指定された手を表す {@code Move} オブジェクトを返します。<br>
     * 
     * @param color 手を指したプレーヤーの色
     * @param point 指定された駒の位置（パスの場合は {@code null}）
     * @return Move オブジェクト
     * @throws NullPointerException {@code color} が {@code null} の場合
     */
    public static Move of(Color color, Point point) {
        Objects.requireNonNull(color);
        return new Move(color, point);
    }
    
    /**
     * パスを表す {@code Move} オブジェクトを返します。<br>
     * この呼び出しは次の呼び出しと同値です。<br>
     * <pre>
     *     Move.of(color, null);
     * </pre>
     * 
     * @param color 駒の色
     * @return Move オブジェクト
     * @throws NullPointerException {@code color} が {@code null} の場合
     */
    public static Move passOf(Color color) {
        Objects.requireNonNull(color);
        return new Move(color, null);
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // メンバ変数を public で公開するのに抵抗を感じるかもしれないが、
    // その変数参照や変数インスタンス自体が不変である場合、デメリットは少ない。
    // むしろ積極的に public にすべきだという主張もある。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** 手を指したプレーヤーの色 */
    public final Color color;
    
    /** 指定された駒の位置（パスの場合は {@code null}） */
    public final Point point;
    
    private Move(Color color, Point point) {
        this.color = color;
        this.point = point;
    }
    
    /**
     * {@code obj} がこのオブジェクトと同じ内容を表すかを返します。<br>
     * 
     * @param obj 検査対象
     * @return {@code obj} がこのオブジェクトと同じ内容を表す {@code Move} の場合は {@code true}。
     *         {@code obj} の内容がこのオブジェクトと異なる場合や {@code null} の場合は {@code false}。
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Move) {
            Move other = (Move) obj;
            return color == other.color && point == other.point;
        }
        return false;
    }
    
    /**
     * このオブジェクトのハッシュコードを返します。<br>
     * 
     * @return このオブジェクトのハッシュコード
     */
    @Override
    public int hashCode() {
        int n = (point == null ? 0 : point.ordinal() + 1) * 2;
        return color == Color.BLACK ? n : n + 1;
    }
    
    /**
     * このオブジェクトの文字列表現を返します。<br>
     * 
     * @return このオブジェクトの文字列表現
     */
    @Override
    public String toString() {
        return String.format("[%s : %s]", color, point == null ? "PASS" : point.toStringKindly());
    }
    
    /**
     * ストリームから {@code Move} インスタンスを復元します。<br>
     * 
     * @serialData デフォルトの形式で {@code Move} オブジェクトを復元します。
     *             {@link #color} が {@code null} の場合は例外をスローして復元を中止します。
     * @param stream オブジェクト入力ストリーム
     * @throws ClassNotFoundException 直列化されたオブジェクトのクラスが見つからなかった場合
     * @throws IOException 入出力例外が発生した場合
     * @throws InvalidObjectException 復元された {@link #color} が {@code null} の場合
     */
    // Move オブジェクトの恒等式（color != null）を保証するために readObject を実装する。
    // Color も Point も不変であり、かつそれぞれきちんとシリアライゼーションの制御をしているので、
    // きっとこれだけで大丈夫なはず... シリアライゼーションは難しい...
    private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
        
        // Move オブジェクトは、常に color != null でなければならない。
        // バイトストリームが不正に改変されている場合はデシリアル化を防止する。
        if (color == null) {
            throw new InvalidObjectException("color cannot be null.");
        }
    }
}
