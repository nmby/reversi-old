package xyz.hotchpotch.game.reversi.core;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * リバーシゲームにおける手を表す不変クラスです。<br>
 * Move は値ベースのクラスです。同値性を確認するときは Move#equals() メソッドを使用してください。<br>
 * 
 * @author nmby
 */
public class Move implements Serializable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 指定された手を表す Move オブジェクトを返します。<br>
     * 
     * @param color 駒の色
     * @param point 駒の位置（パスの場合は null）
     * @return Move オブジェクト
     * @throws NullPointerException color が null の場合
     */
    public static Move of(Color color, Point point) {
        Objects.requireNonNull(color);
        return new Move(color, point);
    }
    
    /**
     * パスを表す Move オブジェクトを返します。<br>
     * この呼び出しは次の呼び出しと同値です。<br>
     *     Move.of(color, null);
     * 
     * @param color 駒の色
     * @return Move オブジェクト
     * @throws NullPointerException color が null の場合
     */
    public static Move passOf(Color color) {
        Objects.requireNonNull(color);
        return new Move(color, null);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    // メンバ変数を public で公開するのに抵抗を感じるかもしれないが、
    // その変数やクラス自体が不変である場合、デメリットは少ない。
    // むしろ積極的に public にすべきだという主張もある。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** 駒の色 */
    public final Color color;
    
    /** 駒の位置（パスの場合は null） */
    public final Point point;
    
    private Move(Color color, Point point) {
        this.color = color;
        this.point = point;
    }
    
    /**
     * obj がこのオブジェクトと同じ内容を表すかを返します。<br>
     * 
     * @param obj 検査対象
     * @return obj がこのオブジェクトと同じ内容を表す Move の場合は true。obj が異なる内容や null の場合は false。
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Move) {
            Move other = (Move) obj;
            return color == other.color && point == other.point;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int o = (point == null ? 0 : point.ordinal() + 1) * 2;
        return color == Color.BLACK ? o : o + 1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s : %s", color, point == null ? "PASS" : point);
    }
    
    // Color も Point も不変であり、かつそれぞれきちんとシリアライゼーションの制御をしているので、
    // きっとこれだけで Move オブジェクトの正当性（常に color != null）を守れるはず...
    // シリアライゼーションは難しい...
    private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
        
        // Move オブジェクトは、常に color != null でなければならない。
        // バイトストリームが不正に改変されている場合はデシリアル化を防止する。
        if (color == null) {
            throw new InvalidObjectException("color cannot be null.");
        }
    }
}
