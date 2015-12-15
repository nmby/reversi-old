package xyz.hotchpotch.game.reversi.core;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 他のリバーシ盤のある時点の状態を保持するスナップショットです。<br>
 * 元のリバーシ盤の内容が変更されても、スナップショットの内容は変更されません。<br>
 * このクラスは不変です。変更オペレーションはサポートされません。<br>
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
     * @throws NullPointerException {@code board} が {@code null} の場合
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
    
    // シリアライゼーションは難しい... なので何も考える必要のないシリアライズプロキシパターンが楽なのだが、
    // お勉強のためにシリアライズプロキシパターンを使わずに実装してみる。
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(map);
    }
    
    // このクラスのオブジェクトが満たすべき制約は次の2点のみ。
    //     ・super.map != null であること
    //     ・super.map に外部からの参照リンクが無いこと
    // この2点ともに、BaseBoard のコンストラクタにより保証されているはず。
    // BoardSnapshot 独自に満たすべき制約（恒等式）は特にないため、この実装で良いはず...
    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        
        @SuppressWarnings("unchecked")
        Map<Point, Color> map = (Map<Point, Color>) s.readObject();
        if (map == null) {
            throw new InvalidObjectException("map cannot be null.");
        }
        super.map.putAll(map);
    }
}
