package xyz.hotchpotch.game.reversi.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 単純な Board の実装です。<br>
 * この実装は、クライアントによる更新操作を無批判に受け入れます。ルールに基づく妥当性の検査は行いません。
 * また、周囲の駒をひっくりかえすこともしません。<br>
 * <br>
 * この実装は同期されません。
 * 複数のスレッドが並行してインスタンスにアクセスし、それらのスレッドの少なくとも1つが更新操作を行う場合は、
 * 外部で同期をとる必要があります。<br>
 * 
 * @author nmby
 */
public class SimpleBoard extends BaseBoard implements Serializable {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    private static final long serialVersionUID = 1L;
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    /**
     * 駒がひとつも置かれていないリバーシ盤を生成します。<br>
     */
    public SimpleBoard() {
        super();
    }
    
    /**
     * 指定されたリバーシ盤と同じ内容のリバーシ盤を生成します。<br>
     * 
     * @param board コピーするリバーシ盤
     * @throws NullPointerException board が null の場合
     */
    public SimpleBoard(Board board) {
        super(Objects.requireNonNull(board));
    }
    
    /**
     * このリバーシ盤に指定された手を適用します。<br>
     * この実装は、単純に java.lang.Map#put(point, color) のように振る舞います。
     * リバーシのルールに照らした妥当性検査は行いません。周囲の駒をひっくりかえすこともしません。
     * move.color == null の場合は指定された位置を null に更新します。<br>
     * 
     * @param move 適用する手
     * @throws NullPointerException move が null の場合
     */
    @Override
    public void apply(Move move) {
        Objects.requireNonNull(move);
        map.put(move.point, move.color);
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
