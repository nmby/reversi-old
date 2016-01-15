package xyz.hotchpotch.reversi.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * リバーシのルールに基づく各種判定メソッドを提供するユーティリティクラスです。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class Rule {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /**
     * ゲームが継続中か（黒か白の少なくともどちらかの石を置ける場所があるか）を返します。<br>
     * 
     * @param board リバーシ盤
     * @return ゲーム継続中の場合は {@code true}、どちらの色も置けず終了状態の場合は {@code false}
     * @throws NullPointerException {@code board} が {@code null} の場合
     */
    public static boolean isGameOngoing(Board board) {
        Objects.requireNonNull(board);
        return Color.stream().anyMatch(c -> canPut(board, c));
    }
    
    /**
     * リバーシ盤に指定した手を適用できるかを返します。<br>
     * 
     * @param board リバーシ盤
     * @param move 手
     * @return 手を適用できる場合は {@code true}
     * @throws NullPointerException {@code board}, {@code move} のいずれかが {@code null} の場合
     */
    public static boolean canApply(Board board, Move move) {
        Objects.requireNonNull(board);
        Objects.requireNonNull(move);
        
        if (move.point == null) {
            return !canPut(board, move.color);
        } else {
            return canPutAt(board, move.color, move.point);
        }
    }
    
    /**
     * リバーシ盤に指定した色の石を置けるか（パスではないか）を返します。<br>
     * 
     * @param board リバーシ盤
     * @param color 石の色
     * @return いずれかの位置に石を置ける場合は {@code true}
     * @throws NullPointerException {@code board}, {@code color} のいずれかが {@code null} の場合
     */
    public static boolean canPut(Board board, Color color) {
        Objects.requireNonNull(board);
        Objects.requireNonNull(color);
        
        return Point.stream().anyMatch(p -> canPutAt(board, color, p));
    }
    
    /**
     * リバーシ盤の指定した位置に指定した色の石を置けるかを返します。<br>
     * 
     * @param board リバーシ盤
     * @param color 石の色
     * @param point 石を置く位置
     * @return 石を置ける場合は {@code true}
     * @throws NullPointerException {@code board}, {@code color}, {@code point} のいずれかが {@code null} の場合
     */
    public static boolean canPutAt(Board board, Color color, Point point) {
        Objects.requireNonNull(board);
        Objects.requireNonNull(color);
        Objects.requireNonNull(point);
        
        if (board.colorAt(point) != null) {
            return false;
        }
        return Direction.stream().anyMatch(d -> canReverse(board, color, point, d));
    }
    
    private static boolean canReverse(Board board, Color color, Point point, Direction direction) {
        assert board != null;
        assert color != null;
        assert point != null;
        assert direction != null;
        assert board.colorAt(point) == null;
        
        // ひとつ隣は相手の石でなければならない
        if (!point.hasNext(direction)) {
            return false;
        }
        Point p = point.next(direction);
        if (board.colorAt(p) != color.opposite()) {
            return false;
        }
        
        // 隣のさらに隣以降
        while (p.hasNext(direction)) {
            p = p.next(direction);
            
            if (board.colorAt(p) == color) {
                return true;
            } else if (board.colorAt(p) == null) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * リバーシ盤に指定した手を適用した場合にひっくり返すことのできる石の位置を返します。<br>
     * 適用できない手やパスが指定された場合は、空の {@code Set} を返します。<br>
     * 
     * @param board リバーシ盤
     * @param move 手
     * @return ひっくり返すことのできる石の位置を格納した {@code Set}
     * @throws NullPointerException {@code board}, {@code move} のいずれかが {@code null} の場合
     */
    public static Set<Point> reversibles(Board board, Move move) {
        Objects.requireNonNull(board);
        Objects.requireNonNull(move);
        
        if (move.point == null) {
            return Collections.emptySet();
        }
        if (board.colorAt(move.point) != null) {
            return Collections.emptySet();
        }
        
        return Direction.stream()
                .flatMap(d -> reversibles(board, move.color, move.point, d).stream())
                .collect(Collectors.toSet());
    }
    
    private static Set<Point> reversibles(Board board, Color color, Point point, Direction direction) {
        assert board != null;
        assert color != null;
        assert point != null;
        assert direction != null;
        assert board.colorAt(point) == null;
        
        Set<Point> reversibles = new HashSet<>();
        Point p = point;
        
        while (p.hasNext(direction)) {
            p = p.next(direction);
            
            if (board.colorAt(p) == color) {
                return reversibles;
            } else if (board.colorAt(p) == null) {
                return Collections.emptySet();
            }
            reversibles.add(p);
        }
        
        return Collections.emptySet();
    }
    
    /**
     * 勝者の色を返します。
     * 引き分けの場合は {@code null} を返します。<br>
     * 
     * @param board リバーシ盤
     * @return 勝者の色（引き分けの場合は {@code null}）
     * @throws NullPointerException {@code board} が {@code null} の場合
     * @throws IllegalStateException ゲーム継続中の場合
     */
    public static Color winner(Board board) {
        Objects.requireNonNull(board);
        if (isGameOngoing(board)) {
            throw new IllegalStateException("game is ongoing.");
        }
        
        int black = (int) Point.stream().filter(p -> board.colorAt(p) == Color.BLACK).count();
        int white = (int) Point.stream().filter(p -> board.colorAt(p) == Color.WHITE).count();
        
        if (white < black) {
            return Color.BLACK;
        } else if (black < white) {
            return Color.WHITE;
        } else {
            return null;
        }
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private Rule() {
    }
}
