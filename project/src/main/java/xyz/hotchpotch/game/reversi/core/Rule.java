package xyz.hotchpotch.game.reversi.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * リバーシのルールに基づく各種判定メソッドを提供するユーティリティクラスです。<br>
 * 
 * @author nmby
 */
public class Rule {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * ゲームが継続中か（黒か白の少なくともどちらかが駒を置ける場所があるか）を返します。<br>
     * 
     * @param board リバーシ盤
     * @return ゲーム継続中の場合は true、どちらの色も置けず終了状態の場合は false
     * @NullPointerException board が null の場合
     */
    public static boolean isGameOngoing(Board board) {
        Objects.requireNonNull(board);
        return Color.stream().anyMatch(c -> canPut(board, c));
    }
    
    /**
     * リバーシ盤に指定した色の駒を置けるか（パスではないか）を返します。<br>
     * 
     * @param board リバーシ盤
     * @param color 置く駒の色
     * @return いずれかの位置に駒を置ける場合は true
     * @NullPointerException board, color のいずれかが null の場合
     */
    public static boolean canPut(Board board, Color color) {
        Objects.requireNonNull(board);
        Objects.requireNonNull(color);
        
        return Point.stream().anyMatch(p -> canPutAt(board, color, p));
    }
    
    /**
     * リバーシ盤の指定した位置に指定した色の駒を置けるかを返します。<br>
     * 
     * @param board リバーシ盤
     * @param color 置く駒の色
     * @param point 駒を置く位置
     * @return 駒を置ける場合は true
     * @NullPointerException board, color, point のいずれかが null の場合
     */
    public static boolean canPutAt(Board board, Color color, Point point) {
        Objects.requireNonNull(board);
        Objects.requireNonNull(color);
        Objects.requireNonNull(point);
        
        if (board.colorAt(point) != null) {
            return false;
        }
        return Direction.stream().anyMatch(d -> 0 < countReversibles(board, color, point, d));
    }
    
    private static int countReversibles(Board board, Color color, Point point, Direction direction) {
        assert board != null;
        assert color != null;
        assert point != null;
        assert direction != null;
        assert board.colorAt(point) == null;
        
        int n = 0;
        Point p = point.next(direction);
        while (p != null) {
            if (board.colorAt(p) == color.opposite()) {
                return n;
            } else if (board.colorAt(p) == null) {
                return 0;
            }
            n++;
            p = p.next(direction);
        }
        return 0;
    }
    
    /**
     * リバーシ盤上の指定した位置に指定した色の駒を置いた場合に、
     * それぞれの方向に相手の駒を何枚ひっくり返せるかを返します。<br>
     * 置くことのできない位置が指定された場合は、0 のみが格納されたマップを返します。<br>
     * 
     * @param board リバーシ盤
     * @param color 置く駒の色
     * @param point 駒を置く位置
     * @return 方向ごとにひっくり返せる枚数を格納した Map
     * @NullPointerException board, color, point のいずれかが null の場合
     */
    public static Map<Direction, Integer> counts(Board board, Color color, Point point) {
        Objects.requireNonNull(board);
        Objects.requireNonNull(color);
        Objects.requireNonNull(point);
        
        Map<Direction, Integer> counts = new EnumMap<>(Direction.class);
        // Direction#stream() が並列ストリームを生成する可能性を考慮して、Collections#synchronizedMap でラップする。
        // （この考え方で正しいんやろか...？）
        Map<Direction, Integer> wrappedCounts = Collections.synchronizedMap(counts);
        Direction.stream().forEach(d -> wrappedCounts.put(d, countReversibles(board, color, point, d)));
        // ラップしたまま返すのもなんなので、中身だけを返す。
        // （この考え方で正しいんやろか...？）
        return counts;
    }
    
    /**
     * 勝者の色を返します。
     * 引き分けの場合は null を返します。<br>
     * 
     * @param board リバーシ盤
     * @return 勝者の色（引き分けの場合は null）
     * @throws NullPointerException board が null の場合
     * @throws IllegalStateException ゲーム継続中の場合
     */
    public static Color winner(Board board) {
        Objects.requireNonNull(board);
        if (isGameOngoing(board)) {
            throw new IllegalStateException("game is ongoing.");
        }
        
        Map<Color, Integer> counts = board.counts();
        int black = counts.get(Color.BLACK);
        int white = counts.get(Color.WHITE);
        
        if (white < black) {
            return Color.BLACK;
        } else if (black < white) {
            return Color.WHITE;
        } else {
            return null;
        }
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private Rule() {
    }
}
