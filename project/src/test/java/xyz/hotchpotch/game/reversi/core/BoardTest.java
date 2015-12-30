package xyz.hotchpotch.game.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.function.IntFunction;

import org.junit.Test;

public class BoardTest {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private static class TestBoard extends BaseBoard {
        private TestBoard(String str) {
            super(str);
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
            
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    @Test
    public void testEquals() {
        Board board1 = new TestBoard(boardStr1);
        Board board4 = new TestBoard(boardStr4);
        Board board4b = new TestBoard(boardStr4);
        
        assertThat(Board.equals(null, null), is(true));
        assertThat(Board.equals(null, board1), is(false));
        assertThat(Board.equals(board1, null), is(false));
        assertThat(Board.equals(board1, board1), is(true));
        assertThat(Board.equals(board1, board4), is(false));
        assertThat(Board.equals(board4, board4), is(true));
        assertThat(Board.equals(board4, board4b), is(true));
    }
    
    @Test
    public void testHashCode() {
        IntFunction<Color> converter = (c -> c == '●' ? Color.BLACK : c == '○' ? Color.WHITE : null);
        Color[] array1 = boardStr1.chars().<Color> mapToObj(converter).toArray(Color[]::new);
        Color[] array2 = boardStr2.chars().<Color> mapToObj(converter).toArray(Color[]::new);
        Color[] array3 = boardStr3.chars().<Color> mapToObj(converter).toArray(Color[]::new);
        Color[] array4 = boardStr4.chars().<Color> mapToObj(converter).toArray(Color[]::new);
        
        assertThat(Board.hashCode(new TestBoard(boardStr1)), is(Arrays.hashCode(array1)));
        assertThat(Board.hashCode(new TestBoard(boardStr2)), is(Arrays.hashCode(array2)));
        assertThat(Board.hashCode(new TestBoard(boardStr3)), is(Arrays.hashCode(array3)));
        assertThat(Board.hashCode(new TestBoard(boardStr4)), is(Arrays.hashCode(array4)));
        
        assertThat(Board.hashCode(null), is(0));
    }
    
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
