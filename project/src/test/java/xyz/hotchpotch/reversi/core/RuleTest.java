package xyz.hotchpotch.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static xyz.hotchpotch.jutaime.throwable.RaiseMatchers.*;
import static xyz.hotchpotch.jutaime.throwable.Testee.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import xyz.hotchpotch.reversi.core.BaseBoard;
import xyz.hotchpotch.reversi.core.Board;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Move;
import xyz.hotchpotch.reversi.core.Point;
import xyz.hotchpotch.reversi.core.Rule;

public class RuleTest {
    
    private static class TestBoard extends BaseBoard {
        
        private TestBoard(String colors) {
            assert colors != null;
            assert colors.length() == Point.HEIGHT * Point.WIDTH;
            assert colors.chars().allMatch(c -> c == '●' || c == '○' || c == '・');
            
            for (int i = 0; i < Point.HEIGHT; i++) {
                for (int j = 0; j < Point.WIDTH; j++) {
                    char c = colors.charAt(i * Point.WIDTH + j);
                    map.put(Point.of(i, j), c == '●' ? Color.BLACK : c == '○' ? Color.WHITE : null);
                }
            }
        }
        
        @Override
        public void apply(Move move) {
            throw new UnsupportedOperationException();
        }
    }
    
    @Test
    public void testIsGameOngoing() {
        // 黒白ともに置ける場合
        assertThat(Rule.isGameOngoing(new TestBoard(""
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・○●・・・"
                + "・・・●○・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・")),
                is(true));
                
        // 黒のみ置ける場合
        assertThat(Rule.isGameOngoing(new TestBoard(""
                + "●○・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・")),
                is(true));
                
        // 白のみ置ける場合
        assertThat(Rule.isGameOngoing(new TestBoard(""
                + "・・・・・・・○"
                + "・・・・・・・●"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・")),
                is(true));
                
        // どちらも置けない場合
        assertThat(Rule.isGameOngoing(new TestBoard(""
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・")),
                is(false));
                
        assertThat(Rule.isGameOngoing(new TestBoard(""
                + "●○●○●○●○"
                + "○●○●○●○●"
                + "●○●○●○●○"
                + "○●○●○●○●"
                + "●○●○●○●○"
                + "○●○●○●○●"
                + "●○●○●○●○"
                + "○●○●○●○●")),
                is(false));
    }
    
    @Test
    public void testCanApply() {
        Board board = new TestBoard(""
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・○●");
                
        // パラメータチェックのテスト
        assertThat(of(() -> Rule.canApply(board, null)), raise(NullPointerException.class));
        assertThat(of(() -> Rule.canApply(null, Move.of(Color.BLACK, Point.of("a1")))), raise(NullPointerException.class));
        assertThat(of(() -> Rule.canApply(null, null)), raise(NullPointerException.class));
        
        // 通常手に対する判定のテスト
        assertThat(Rule.canApply(board, Move.of(Color.BLACK, Point.of("f8"))), is(true));
        assertThat(Rule.canApply(board, Move.of(Color.WHITE, Point.of("f8"))), is(false));
        
        // パスに対する判定のテスト
        assertThat(Rule.canApply(board, Move.passOf(Color.BLACK)), is(false));
        assertThat(Rule.canApply(board, Move.passOf(Color.WHITE)), is(true));
    }
    
    @Test
    public void testCanPut() {
        Board board = new TestBoard(""
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "●・・・・・・・"
                + "○・・・・・・・");
                
        // パラメータチェックのテスト
        assertThat(of(() -> Rule.canPut(board, null)), raise(NullPointerException.class));
        assertThat(of(() -> Rule.canPut(null, Color.BLACK)), raise(NullPointerException.class));
        assertThat(of(() -> Rule.canPut(null, null)), raise(NullPointerException.class));
        
        // 判定機能のテスト
        assertThat(Rule.canPut(board, Color.BLACK), is(false));
        assertThat(Rule.canPut(board, Color.WHITE), is(true));
    }
    
    @Test
    public void testCanPutAt() {
        Board board1 = new TestBoard(""
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・○○○・・・"
                + "・・○●○・・・"
                + "・・○○○・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・");
                
        // パラメータチェックのテスト
        assertThat(of(() -> Rule.canPutAt(board1, Color.BLACK, null)), raise(NullPointerException.class));
        assertThat(of(() -> Rule.canPutAt(board1, null, Point.of("d4"))), raise(NullPointerException.class));
        assertThat(of(() -> Rule.canPutAt(null, Color.BLACK, Point.of("d4"))), raise(NullPointerException.class));
        assertThat(of(() -> Rule.canPutAt(null, null, null)), raise(NullPointerException.class));
        
        // 既に置かれている場所の確認
        assertThat(Rule.canPutAt(board1, Color.BLACK, Point.of("d4")), is(false));
        
        // 8方向それぞれ判定できることの確認
        Point.stream().forEach(p -> {
            assertThat(Rule.canPutAt(board1, Color.BLACK, p), is(
                    p == Point.of("b2") || p == Point.of("d2") || p == Point.of("f2") ||
                            p == Point.of("b4") || p == Point.of("f4") ||
                            p == Point.of("b6") || p == Point.of("d6") || p == Point.of("f6")));
        });
        
        Board board2 = new TestBoard(""
                + "・●●●●●●○"
                + "・●●●●●●●"
                + "・●●●●●●・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・");
                
        // 相手の駒が連続した場合の判定のテスト
        assertThat(Rule.canPutAt(board2, Color.WHITE, Point.of("a1")), is(true));
        assertThat(Rule.canPutAt(board2, Color.WHITE, Point.of("a2")), is(false));
        assertThat(Rule.canPutAt(board2, Color.WHITE, Point.of("a3")), is(false));
    }
    
    @Test
    public void testReversibles() {
        Board board1 = new TestBoard(""
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・○●・・・"
                + "・・・●○・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・");
                
        // パラメータチェックのテスト
        assertThat(of(() -> Rule.reversibles(board1, null)), raise(NullPointerException.class));
        assertThat(of(() -> Rule.reversibles(null, Move.of(Color.BLACK, Point.of("c3")))), raise(NullPointerException.class));
        assertThat(of(() -> Rule.reversibles(null, null)), raise(NullPointerException.class));
        
        // パスが指定された場合のテスト
        assertThat(Rule.reversibles(board1, Move.passOf(Color.BLACK)), is(Collections.EMPTY_SET));
        
        // 相手の駒をひとつもひっくりかえせない位置が指定された場合のテスト
        assertThat(Rule.reversibles(board1, Move.of(Color.BLACK, Point.of("d4"))), is(Collections.EMPTY_SET));
        assertThat(Rule.reversibles(board1, Move.of(Color.WHITE, Point.of("c4"))), is(Collections.EMPTY_SET));
        assertThat(Rule.reversibles(board1, Move.of(Color.BLACK, Point.of("a1"))), is(Collections.EMPTY_SET));
        
        // ひっくり返せる場所
        assertThat(Rule.reversibles(board1, Move.of(Color.BLACK, Point.of("d3"))).toArray(),
                is(new Point[] { Point.of("d4") }));
                
        Board board2 = new TestBoard(""
                + "○○○○○○○○"
                + "○●●●●●●○"
                + "○●●●●●●○"
                + "○●●・●●●○"
                + "○●●●●●●○"
                + "○●●●●●●○"
                + "○●●●●●●○"
                + "○○○○○○○○");
                
        Set<Point> expected = new HashSet<>(Arrays.asList(new Point[] {
                Point.of("d3"), Point.of("d2"),
                Point.of("e3"), Point.of("f2"),
                Point.of("e4"), Point.of("f4"), Point.of("g4"),
                Point.of("e5"), Point.of("f6"), Point.of("g7"),
                Point.of("d5"), Point.of("d6"), Point.of("d7"),
                Point.of("c5"), Point.of("b6"),
                Point.of("c4"), Point.of("b4"),
                Point.of("c3"), Point.of("b2")
        }));
        assertThat(Rule.reversibles(board2, Move.of(Color.WHITE, Point.of("d4"))), is(expected));
    }
    
    @Test
    public void testWinner() {
        // パラメータチェックのテスト
        assertThat(of(() -> Rule.winner(null)), raise(NullPointerException.class));
        
        // ゲーム継続中の場合のテスト
        assertThat(of(() -> Rule.winner(new TestBoard(""
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・○●・・・"
                + "・・・●○・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"))),
                raise(IllegalStateException.class, "game is ongoing."));
                
        // ゲーム終了時の判定テスト（駒が埋まった場合）
        assertThat(Rule.winner(new TestBoard(""
                + "●○●○●○●○"
                + "○●○●○●○●"
                + "●○●○●○●○"
                + "○●○●○●○●"
                + "●○●○●○●○"
                + "○●○●○●○●"
                + "●○●○●○●○"
                + "○●○●○●○●")),
                nullValue());
                
        assertThat(Rule.winner(new TestBoard(""
                + "●●●●●●●●"
                + "●●●●●●●●"
                + "●●●●●●●●"
                + "●●●●●●●●"
                + "●○○○○○○○"
                + "○○○○○○○○"
                + "○○○○○○○○"
                + "○○○○○○○○")),
                is(Color.BLACK));
                
        assertThat(Rule.winner(new TestBoard(""
                + "●●●●●●●●"
                + "●●●●●●●●"
                + "●●●●●●●●"
                + "●●●●●●●○"
                + "○○○○○○○○"
                + "○○○○○○○○"
                + "○○○○○○○○"
                + "○○○○○○○○")),
                is(Color.WHITE));
        
        // ゲーム終了時の判定テスト（空のマスがある場合）
        assertThat(Rule.winner(new TestBoard(""
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・")),
                nullValue());
        
        assertThat(Rule.winner(new TestBoard(""
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・●・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・")),
                is(Color.BLACK));
        
        assertThat(Rule.winner(new TestBoard(""
                + "・・・・・・・・"
                + "・・・・●●・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・○・・・・"
                + "・・・○・・・・"
                + "・・・○・・・・"
                + "・・・・・・・・")),
                is(Color.WHITE));
    }
}
