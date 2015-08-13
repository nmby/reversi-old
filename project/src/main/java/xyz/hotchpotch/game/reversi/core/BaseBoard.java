package xyz.hotchpotch.game.reversi.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// このクラスを Serializable にしてみようかとも思ったが、
// 自分の現状の理解では無理なので、今回は止める。
abstract class BaseBoard implements Board {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    final Map<Point, Color> map;
    
    BaseBoard() {
        map = new HashMap<>();
    }
    
    BaseBoard(Map<Point, Color> map) {
        assert map != null;
        this.map = new HashMap<>(map);
    }
    
    BaseBoard(Board board) {
        assert board != null;
        map = new HashMap<>();
        Map<Point, Color> wrapped = Collections.synchronizedMap(map);
        Point.stream().forEach(p -> wrapped.put(p, board.colorAt(p)));
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException point が null の場合
     */
    @Override
    public Color colorAt(Point point) {
        Objects.requireNonNull(point);
        return map.get(point);
    }
    
    /**
     * このリバーシ盤の文字列表現を返します。<br>
     * ログファイルへの出力等に便利な、改行を含まない単一行形式です。<br>
     * 
     * @return このリバーシ盤の文字列表現（単一行形式）
     * @see #toStringInLine()
     */
    @Override
    public String toString() {
        return toStringInLine();
    }
}
