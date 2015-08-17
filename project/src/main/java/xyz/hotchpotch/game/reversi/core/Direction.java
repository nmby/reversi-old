package xyz.hotchpotch.game.reversi.core;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * リバーシ盤上における8方向（上、右上、右、右下、下、左下、左、左上）を表す列挙型です。<br>
 * 
 * @author nmby
 */
public enum Direction {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /** 上 */
    UPPER(-1, 0),
    
    /** 右上 */
    UPPER_RIGHT(-1, 1),
    
    /** 右 */
    RIGHT(0, 1),
    
    /** 右下 */
    LOWER_RIGHT(1, 1),
    
    /** 下 */
    LOWER(1, 0),
    
    /** 左下 */
    LOWER_LEFT(1, -1),
    
    /** 左 */
    LEFT(0, -1),
    
    /** 左上 */
    UPPER_LEFT(-1, -1);
    
    /**
     * この列挙型のすべての要素をソースとする順次ストリームを返します。<br>
     * 
     * @return 新しいストリーム
     */
    public static Stream<Direction> stream() {
        return Arrays.stream(values());
    }
    
    /**
     * この列挙型のすべての要素をソースとする並列ストリームを返します。<br>
     * 
     * @return 新しいストリーム
     */
    public static Stream<Direction> parallelStream() {
        return Arrays.stream(values()).parallel();
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    final int di;
    final int dj;
    
    Direction(int di, int dj) {
        this.di = di;
        this.dj = dj;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("<%d, %d>", di, dj);
    }
}
