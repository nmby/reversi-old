package xyz.hotchpotch.game.reversi.core;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * 駒の色を表す列挙型です。<br>
 * 
 * @author nmby
 */
public enum Color {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /** 黒 */
    BLACK("●"),
    
    /** 白 */
    WHITE("○");
    
    /**
     * この列挙型のすべての要素をソースとする順次ストリームを返します。<br>
     * 
     * @return 新しいストリーム
     */
    public static Stream<Color> stream() {
        // MEMO: コメントアウトした並列ストリームを返す実装にしたところ、実行が止まる事象が発生。
        // MEMO: 已む無く順次ストリームを返す仕様に変更。
        // MEMO: マルチスレッドプログラミングと Spliterator について要お勉強
        // return EnumSet.allOf(Color.class).parallelStream();
        return Arrays.stream(values());
    }
    
    /**
     * {@code color} の文字列表現を返します。<br>
     * 
     * @param color 色
     * @return {@code color} の文字列表現（{@code "●"} または {@code "○"}）。
     *         {@code color} が {@code null} の場合は {@code "・"}
     */
    public static String toString(Color color) {
        return color == null ? "・" : color.toString();
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final String symbol;
    
    Color(String symbol) {
        this.symbol = symbol;
    }
    
    /**
     * 自身と反対の色を返します。<br>
     * 
     * @return 自身と反対の色
     */
    public Color opposite() {
        return this == BLACK ? WHITE : BLACK;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return symbol;
    }
}
