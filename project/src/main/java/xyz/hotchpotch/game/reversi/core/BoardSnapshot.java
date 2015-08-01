package xyz.hotchpotch.game.reversi.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 他のリバーシ盤のある時点の状態を保持するスナップショットです。<br>
 * 元のリバーシ盤の内容が変更されても、スナップショットの内容は変更されません。<br>
 * スナップショットに対する変更オペレーションはサポートしません。<br>
 * 
 * @author nmby
 */
public class BoardSnapshot extends BaseBoard implements Serializable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 指定されたリバーシ盤のスナップショットを返します。<br>
     * 
     * @param board 元のリバーシ盤
     * @return 指定されたリバーシ盤のスナップショット
     * @throws NullPointerException board が null の場合
     */
    public static Board of(Board board) {
        Objects.requireNonNull(board);
        return new BoardSnapshot(board);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private BoardSnapshot(Board board) {
        super(board);
    }
    
    /**
     * このオペレーションはサポートされません。<br>
     * 
     * @throws UnsupportedOperationException このオペレーションを実行した場合
     */
    @Override
    public void apply(Move move) {
        throw new UnsupportedOperationException();
    }
    
    // シリアライゼーションは難しい...
    // なので何も考える必要のないシリアライズプロキシパターンが楽なのだが、
    // お勉強のためにシリアライズプロキシパターンを使わずに実装してみる。
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(map);
    }
    
    // このクラスのオブジェクトが満たすべき制約は super.map != null であることと、super.map が外部から参照されないことのみ。
    // きっとこの実装でうまくいくはず...
    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        
        @SuppressWarnings("unchecked")
        Map<Point, Color> map = (Map<Point, Color>) Objects.requireNonNull(s.readObject());
        super.map.putAll(map);
    }
}
