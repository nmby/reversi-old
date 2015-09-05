package xyz.hotchpotch.game.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class BoardTest {
    
    private static class TestBoard extends BaseBoard {
        
        private TestBoard(String str) {
            assert str != null;
            assert str.length() == Point.HEIGHT * Point.WIDTH;
            
            Point[] points = Point.values();
            for (int n = 0; n < Point.HEIGHT * Point.WIDTH; n++) {
                char c = str.charAt(n);
                Color color = c == '●' ? Color.BLACK : c == '○' ? Color.WHITE : null;
                map.put(points[n], color);
            }
        }
        
        @Override
        public void apply(Move move) {
            throw new UnsupportedOperationException();
        }
    }
    
    private String lineToBox(String line) {
        assert line != null;
        assert line.length() == Point.HEIGHT * Point.WIDTH;
        
        StringBuilder box = new StringBuilder("   ");
        for (int j = 0; j < Point.WIDTH; j++) {
            box.append(String.format("%c ", 'a' + j));
        }
        box.append(System.lineSeparator());
        for (int i = 0; i < Point.HEIGHT; i++) {
            box.append(String.format("%2c ", '1' + i));
            box.append(line.substring(Point.WIDTH * i, Point.WIDTH * (i + 1)));
            box.append(System.lineSeparator());
        }
        
        return box.toString();
    }
    
    private static final String board1 = ""
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・"
            + "・・・・・・・・";
    
    private static final String board2 = ""
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●"
            + "●●●●●●●●";
    
    private static final String board3 = ""
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○"
            + "○○○○○○○○";
    
    private static final String board4 = ""
            + "●●●●●●●○"
            + "●●●●●●○○"
            + "●●●●●○○○"
            + "・・・・○○○○"
            + "●●●●・・・・"
            + "●●●○○○○○"
            + "●●○○○○○○"
            + "●○○○○○○○";
    
    @Test
    public void testToStringKindly() {
        assertThat(new TestBoard(board1).toStringKindly(), is(lineToBox(board1)));
        assertThat(new TestBoard(board2).toStringKindly(), is(lineToBox(board2)));
        assertThat(new TestBoard(board3).toStringKindly(), is(lineToBox(board3)));
        assertThat(new TestBoard(board4).toStringKindly(), is(lineToBox(board4)));
    }
    
    @Test
    public void testToStringInLine() {
        assertThat(new TestBoard(board1).toStringInLine(), is(board1));
        assertThat(new TestBoard(board2).toStringInLine(), is(board2));
        assertThat(new TestBoard(board3).toStringInLine(), is(board3));
        assertThat(new TestBoard(board4).toStringInLine(), is(board4));
    }
}
