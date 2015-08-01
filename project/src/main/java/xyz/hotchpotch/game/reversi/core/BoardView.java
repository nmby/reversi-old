package xyz.hotchpotch.game.reversi.core;

import java.util.Map;
import java.util.Objects;

/**
 * 他の Board への変更不可能なビューを提供するクラスです。<br>
 * 
 * @author nmby
 */
public class BoardView implements Board {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * 指定された Board への変更不可能なビューを返します。<br>
     * 
     * @param board 参照対象のリバーシ盤
     * @return board への変更不可能なビュー
     * @throws NullPointerException board が null の場合
     */
    public static Board of(Board board) {
        Objects.requireNonNull(board);
        return new BoardView(board);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final Board target;
    
    private BoardView(Board board) {
        target = board;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Color colorAt(Point point) {
        return target.colorAt(point);
    }
    
    /**
     * 更新操作はサポートされません。<br>
     * 
     * @throws UnsupportedOperationException このメソッドを実行した場合
     */
    @Override
    public void apply(Move move) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Color, Integer> counts() {
        return target.counts();
    }
}
