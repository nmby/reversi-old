package xyz.hotchpotch.game.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static xyz.hotchpotch.jutaime.throwable.RaiseMatchers.*;
import static xyz.hotchpotch.jutaime.throwable.Testee.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class BaseBoardTest {
    
    private static class TestBoard1 extends BaseBoard {
        
        private TestBoard1() {
            super();
        }
        
        private TestBoard1(Map<Point, Color> map) {
            super(map);
        }
        
        private TestBoard1(Board board) {
            super(board);
        }
        
        @Override
        public void apply(Move move) {
            assert move != null;
            
            map.put(move.point, move.color);
        }
    }
    
    private static class TestBoard2 implements Board {
        private final String line;
        
        private TestBoard2(String line) {
            assert line != null;
            assert line.length() == Point.HEIGHT * Point.WIDTH;
            
            this.line = line;
        }
        
        @Override
        public Color colorAt(Point point) {
            assert point != null;
            
            char c = line.charAt(point.ordinal());
            return c == '●' ? Color.BLACK : c == '○' ? Color.WHITE : null;
        }
        
        @Override
        public void apply(Move move) {
            throw new UnsupportedOperationException();
        }
    }
    
    private static Map<Point, Color> lineToMap(String line) {
        assert line != null;
        assert line.length() == Point.HEIGHT * Point.WIDTH;
        
        Map<Point, Color> map = new HashMap<>();
        Point[] points = Point.values();
        
        for (int n = 0; n < Point.HEIGHT * Point.WIDTH; n++) {
            char c = line.charAt(n);
            map.put(points[n], c == '●' ? Color.BLACK : c == '○' ? Color.WHITE : null);
        }
        return map;
    }
    
    private static final String boardStr1 = ""
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・";
            
    private static final String boardStr2 = ""
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●";
            
    private static final String boardStr3 = ""
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○";
            
    private static final String boardStr4 = ""
            + "●●●●●●●○"
            + "●●●●●●○○"
            + "●●●●●○○○"
            + "・・・・○○○○"
            + "●●●●・・・・"
            + "●●●○○○○○"
            + "●●○○○○○○"
            + "●○○○○○○○";
            
    @Test
    public void testBaseBoard() {
        assertThat(new TestBoard1().toStringInLine(), is(boardStr1));
    }
    
    @Test
    public void testBaseBoardMap() {
        assertThat(new TestBoard1(lineToMap(boardStr1)).toStringInLine(), is(boardStr1));
        assertThat(new TestBoard1(lineToMap(boardStr2)).toStringInLine(), is(boardStr2));
        assertThat(new TestBoard1(lineToMap(boardStr3)).toStringInLine(), is(boardStr3));
        assertThat(new TestBoard1(lineToMap(boardStr4)).toStringInLine(), is(boardStr4));
        
        Map<Point, Color> map4 = new HashMap<>(lineToMap(boardStr4));
        BaseBoard board4 = new TestBoard1(map4);
        
        assertThat(board4.map, not(sameInstance(map4)));
        assertThat(board4.colorAt(Point.of(0, 0)), is(Color.BLACK));
        map4.put(Point.of(0, 0), Color.WHITE);
        assertThat(board4.colorAt(Point.of(0, 0)), is(Color.BLACK));
        
        assertThat(of(() -> new TestBoard1((Map<Point, Color>) null)), raise(NullPointerException.class));
    }
    
    @Test
    public void testBaseBoardBoard1() {
        assertThat(new TestBoard1(new TestBoard1(lineToMap(boardStr1))).toStringInLine(), is(boardStr1));
        assertThat(new TestBoard1(new TestBoard1(lineToMap(boardStr2))).toStringInLine(), is(boardStr2));
        assertThat(new TestBoard1(new TestBoard1(lineToMap(boardStr3))).toStringInLine(), is(boardStr3));
        assertThat(new TestBoard1(new TestBoard1(lineToMap(boardStr4))).toStringInLine(), is(boardStr4));
        
        Board board4 = new TestBoard1(lineToMap(boardStr4));
        Board copy = new TestBoard1(board4);
        
        assertThat(board4.colorAt(Point.of(0, 0)), is(Color.BLACK));
        assertThat(copy.colorAt(Point.of(0, 0)), is(Color.BLACK));
        board4.apply(Move.of(Color.WHITE, Point.of(0, 0)));
        assertThat(board4.colorAt(Point.of(0, 0)), is(Color.WHITE));
        assertThat(copy.colorAt(Point.of(0, 0)), is(Color.BLACK));
        
        assertThat(of(() -> new TestBoard1((Board) null)), raise(NullPointerException.class));
    }
    
    @Test
    public void testBaseBoardBoard2() {
        assertThat(new TestBoard1(new TestBoard2(boardStr1)).toStringInLine(), is(boardStr1));
        assertThat(new TestBoard1(new TestBoard2(boardStr2)).toStringInLine(), is(boardStr2));
        assertThat(new TestBoard1(new TestBoard2(boardStr3)).toStringInLine(), is(boardStr3));
        assertThat(new TestBoard1(new TestBoard2(boardStr4)).toStringInLine(), is(boardStr4));
    }
    
    @Test
    public void testColorAt() {
        Map<Point, Color> map1 = lineToMap(boardStr1);
        Map<Point, Color> map2 = lineToMap(boardStr2);
        Map<Point, Color> map3 = lineToMap(boardStr3);
        Map<Point, Color> map4 = lineToMap(boardStr4);
        Board board1 = new TestBoard1(map1);
        Board board2 = new TestBoard1(map2);
        Board board3 = new TestBoard1(map3);
        Board board4 = new TestBoard1(map4);
        
        Point.stream().forEach(p -> {
            assertThat(board1.colorAt(p), is(map1.get(p)));
            assertThat(board2.colorAt(p), is(map2.get(p)));
            assertThat(board3.colorAt(p), is(map3.get(p)));
            assertThat(board4.colorAt(p), is(map4.get(p)));
        });
        
        assertThat(of(() -> board1.colorAt(null)), raise(NullPointerException.class));
    }
    
    @Test
    public void testToString() {
        Board board1 = new TestBoard1(lineToMap(boardStr1));
        Board board2 = new TestBoard1(lineToMap(boardStr2));
        Board board3 = new TestBoard1(lineToMap(boardStr3));
        Board board4 = new TestBoard1(lineToMap(boardStr4));
        
        assertThat(board1.toString(), is(board1.toStringInLine()));
        assertThat(board2.toString(), is(board2.toStringInLine()));
        assertThat(board3.toString(), is(board3.toStringInLine()));
        assertThat(board4.toString(), is(board4.toStringInLine()));
    }
}