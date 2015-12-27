package xyz.hotchpotch.game.reversi.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// このクラスを Serializable にしてみようかとも思ったが、
// 自分の現状の理解では無理なので、今回は止める。
/*package*/ abstract class BaseBoard implements Board {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private static final char CHR_BLACK = Color.toString(Color.BLACK).charAt(0);
    private static final char CHR_WHITE = Color.toString(Color.WHITE).charAt(0);
    private static final char CHR_SPACE = Color.toString(null).charAt(0);
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    protected final Map<Point, Color> map;
    
    protected BaseBoard() {
        map = new HashMap<>();
    }
    
    protected BaseBoard(Map<Point, Color> map) {
        assert map != null;
        this.map = new HashMap<>(map);
    }
    
    protected BaseBoard(Board board) {
        assert board != null;
        
        if (board instanceof BaseBoard) {
            BaseBoard bBoard = (BaseBoard) board;
            map = new HashMap<>(bBoard.map);
        } else {
            map = new HashMap<>();
            for (Point p : Point.values()) {
                map.put(p, board.colorAt(p));
            }
        }
    }
    
    protected BaseBoard(String str) {
        assert str != null;
        assert str.length() == Point.HEIGHT * Point.WIDTH;
        assert str.chars().allMatch(c -> c == CHR_BLACK || c == CHR_WHITE || c == CHR_SPACE);
        
        map = new HashMap<>();
        for (Point p : Point.values()) {
            char c = str.charAt(p.ordinal());
            map.put(p, c == CHR_BLACK ? Color.BLACK : c == CHR_WHITE ? Color.WHITE : null);
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException {@code point} が {@code null} の場合
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
