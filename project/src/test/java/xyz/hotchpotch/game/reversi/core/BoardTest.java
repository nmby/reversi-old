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
                map.put(points[n], c == '●' ? Color.BLACK : c == '○' ? Color.WHITE : null);
            }
        }
        
        @Override
        public void apply(Move move) {
            throw new UnsupportedOperationException();
        }
    }
    
    private static String lineToBox(String line) {
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
    public void testToStringKindly() {
        assertThat(new TestBoard(boardStr1).toStringKindly(), is(lineToBox(boardStr1)));
        assertThat(new TestBoard(boardStr2).toStringKindly(), is(lineToBox(boardStr2)));
        assertThat(new TestBoard(boardStr3).toStringKindly(), is(lineToBox(boardStr3)));
        assertThat(new TestBoard(boardStr4).toStringKindly(), is(lineToBox(boardStr4)));
    }
    
    @Test
    public void testToStringInLine() {
        assertThat(new TestBoard(boardStr1).toStringInLine(), is(boardStr1));
        assertThat(new TestBoard(boardStr2).toStringInLine(), is(boardStr2));
        assertThat(new TestBoard(boardStr3).toStringInLine(), is(boardStr3));
        assertThat(new TestBoard(boardStr4).toStringInLine(), is(boardStr4));
    }
}
