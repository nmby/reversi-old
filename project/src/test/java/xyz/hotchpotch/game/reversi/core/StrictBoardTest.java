package xyz.hotchpotch.game.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static xyz.hotchpotch.jutaime.throwable.RaiseMatchers.*;
import static xyz.hotchpotch.jutaime.throwable.Testee.*;

import java.io.InvalidObjectException;
import java.io.Serializable;

import org.junit.Test;

import xyz.hotchpotch.jutaime.serializable.FailToDeserializeException;
import xyz.hotchpotch.jutaime.serializable.STUtil;

public class StrictBoardTest {
    
    private static class DummyBoard extends BaseBoard implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Override
        public void apply(Move move) {
        }
    }
    
    @Test
    public void testInitializedBoard() {
        Board board = StrictBoard.initializedBoard();
        
        assertThat(board, instanceOf(StrictBoard.class));
        assertThat(board.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
    }
    
    @Test
    public void testApply1() {
        // 正常ケース
        Board board = StrictBoard.initializedBoard();
        
        board.apply(Move.of(Color.BLACK, Point.of("c4")));
        assertThat(board.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・●●●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        
        board.apply(Move.of(Color.WHITE, Point.of("c3")));
        assertThat(board.toStringInLine(), is("・・・・・・・・・・・・・・・・・・○・・・・・・・●○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
    }
    
    @Test
    public void testApply2() {
        // 正常ケース : パスの適用
        Board board = StrictBoard.initializedBoard();
        board.apply(Move.of(Color.BLACK, Point.of("d3")));
        board.apply(Move.of(Color.WHITE, Point.of("c3")));
        board.apply(Move.of(Color.BLACK, Point.of("b3")));
        board.apply(Move.of(Color.WHITE, Point.of("b2")));
        board.apply(Move.of(Color.BLACK, Point.of("b1")));
        board.apply(Move.of(Color.WHITE, Point.of("a1")));
        board.apply(Move.of(Color.BLACK, Point.of("c4")));
        board.apply(Move.of(Color.WHITE, Point.of("c1")));
        board.apply(Move.of(Color.BLACK, Point.of("c2")));
        board.apply(Move.of(Color.WHITE, Point.of("d2")));
        board.apply(Move.of(Color.BLACK, Point.of("d1")));
        board.apply(Move.of(Color.WHITE, Point.of("e1")));
        board.apply(Move.of(Color.BLACK, Point.of("a2")));
        board.apply(Move.of(Color.WHITE, Point.of("a3")));
        board.apply(Move.of(Color.BLACK, Point.of("f5")));
        board.apply(Move.of(Color.WHITE, Point.of("e2")));
        board.apply(Move.of(Color.BLACK, Point.of("f1")));
        board.apply(Move.of(Color.WHITE, Point.of("g1")));
        
        assertThat(board.toStringInLine(), is(""
                + "○○○○○○○・"
                + "○○○○●・・・"
                + "○●●●・・・・"
                + "・・●●●・・・"
                + "・・・●●●・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"));
                
        assertThat(of(() -> board.apply(Move.passOf(Color.BLACK))), raiseNothing());
        assertThat(board.toStringInLine(), is(""
                + "○○○○○○○・"
                + "○○○○●・・・"
                + "○●●●・・・・"
                + "・・●●●・・・"
                + "・・・●●●・・"
                + "・・・・・・・・"
                + "・・・・・・・・"
                + "・・・・・・・・"));
    }
    
    @Test
    public void testApply3() {
        // 異常ケース ： 手番違い
        Board board = StrictBoard.initializedBoard();
        
        assertThat(of(() -> board.apply(Move.of(Color.WHITE, Point.of("e3")))),
                raise(IllegalArgumentException.class, "本来の手番とは異なる色が指定されました。期待=●, 実際=○"));
                
        board.apply(Move.of(Color.BLACK, Point.of("d3")));
        
        assertThat(of(() -> board.apply(Move.of(Color.BLACK, Point.of("e6")))),
                raise(IllegalArgumentException.class, "本来の手番とは異なる色が指定されました。期待=○, 実際=●"));
    }
    
    @Test
    public void testApply4() {
        // 異常ケース ： 置けない位置
        Board board = StrictBoard.initializedBoard();
        
        assertThat(of(() -> board.apply(Move.of(Color.BLACK, Point.of("e3")))),
                raise(IllegalArgumentException.class,
                        "許可されない手が指定されました。move=[● : e3], board=・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
                        
        board.apply(Move.of(Color.BLACK, Point.of("d3")));
        
        assertThat(of(() -> board.apply(Move.of(Color.WHITE, Point.of("a1")))),
                raise(IllegalArgumentException.class,
                        "許可されない手が指定されました。move=[○ : a1], board=・・・・・・・・・・・・・・・・・・・●・・・・・・・●●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        assertThat(of(() -> board.apply(Move.of(Color.WHITE, Point.of("d4")))),
                raise(IllegalArgumentException.class,
                        "許可されない手が指定されました。move=[○ : d4], board=・・・・・・・・・・・・・・・・・・・●・・・・・・・●●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
    }
    
    @Test
    public void testApply5() {
        // 異常ケース ： 置けるのにパス
        Board board = StrictBoard.initializedBoard();
        
        assertThat(of(() -> board.apply(Move.passOf(Color.BLACK))),
                raise(IllegalArgumentException.class,
                        "許可されない手が指定されました。move=[● : PASS], board=・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
                        
        board.apply(Move.of(Color.BLACK, Point.of("d3")));
        
        assertThat(of(() -> board.apply(Move.passOf(Color.WHITE))),
                raise(IllegalArgumentException.class,
                        "許可されない手が指定されました。move=[○ : PASS], board=・・・・・・・・・・・・・・・・・・・●・・・・・・・●●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
    }
    
    @Test
    public void testApply6() {
        // ゲーム完了まで進められることの確認＋さらに指そうとした場合はエラーになることの確認
        Board board = StrictBoard.initializedBoard();
        board.apply(Move.of(Color.BLACK, Point.of("d3")));
        board.apply(Move.of(Color.WHITE, Point.of("c3")));
        board.apply(Move.of(Color.BLACK, Point.of("b3")));
        board.apply(Move.of(Color.WHITE, Point.of("b2")));
        board.apply(Move.of(Color.BLACK, Point.of("b1")));
        board.apply(Move.of(Color.WHITE, Point.of("a1")));
        board.apply(Move.of(Color.BLACK, Point.of("c4")));
        board.apply(Move.of(Color.WHITE, Point.of("c1")));
        board.apply(Move.of(Color.BLACK, Point.of("c2")));
        board.apply(Move.of(Color.WHITE, Point.of("d2")));
        board.apply(Move.of(Color.BLACK, Point.of("d1")));
        board.apply(Move.of(Color.WHITE, Point.of("e1")));
        board.apply(Move.of(Color.BLACK, Point.of("a2")));
        board.apply(Move.of(Color.WHITE, Point.of("a3")));
        board.apply(Move.of(Color.BLACK, Point.of("f5")));
        board.apply(Move.of(Color.WHITE, Point.of("e2")));
        board.apply(Move.of(Color.BLACK, Point.of("f1")));
        board.apply(Move.of(Color.WHITE, Point.of("g1")));
        board.apply(Move.of(Color.BLACK, null));
        board.apply(Move.of(Color.WHITE, Point.of("f2")));
        board.apply(Move.of(Color.BLACK, null));
        board.apply(Move.of(Color.WHITE, Point.of("e3")));
        board.apply(Move.of(Color.BLACK, null));
        board.apply(Move.of(Color.WHITE, Point.of("b5")));
        board.apply(Move.of(Color.BLACK, Point.of("b4")));
        board.apply(Move.of(Color.WHITE, Point.of("a5")));
        board.apply(Move.of(Color.BLACK, Point.of("a4")));
        board.apply(Move.of(Color.WHITE, Point.of("c5")));
        board.apply(Move.of(Color.BLACK, Point.of("a6")));
        board.apply(Move.of(Color.WHITE, Point.of("f4")));
        board.apply(Move.of(Color.BLACK, Point.of("f3")));
        board.apply(Move.of(Color.WHITE, Point.of("g3")));
        board.apply(Move.of(Color.BLACK, Point.of("g2")));
        board.apply(Move.of(Color.WHITE, Point.of("h2")));
        board.apply(Move.of(Color.BLACK, Point.of("h1")));
        board.apply(Move.of(Color.WHITE, Point.of("h3")));
        board.apply(Move.of(Color.BLACK, Point.of("h4")));
        board.apply(Move.of(Color.WHITE, Point.of("g4")));
        board.apply(Move.of(Color.BLACK, Point.of("c6")));
        board.apply(Move.of(Color.WHITE, Point.of("g5")));
        board.apply(Move.of(Color.BLACK, Point.of("h5")));
        board.apply(Move.of(Color.WHITE, Point.of("b6")));
        board.apply(Move.of(Color.BLACK, Point.of("c7")));
        board.apply(Move.of(Color.WHITE, Point.of("d6")));
        board.apply(Move.of(Color.BLACK, Point.of("e6")));
        board.apply(Move.of(Color.WHITE, Point.of("f6")));
        board.apply(Move.of(Color.BLACK, Point.of("g6")));
        board.apply(Move.of(Color.WHITE, Point.of("h6")));
        board.apply(Move.of(Color.BLACK, Point.of("h7")));
        board.apply(Move.of(Color.WHITE, Point.of("a7")));
        board.apply(Move.of(Color.BLACK, null));
        board.apply(Move.of(Color.WHITE, Point.of("b7")));
        board.apply(Move.of(Color.BLACK, Point.of("a8")));
        board.apply(Move.of(Color.WHITE, Point.of("d7")));
        board.apply(Move.of(Color.BLACK, Point.of("e7")));
        board.apply(Move.of(Color.WHITE, Point.of("f7")));
        board.apply(Move.of(Color.BLACK, Point.of("g7")));
        board.apply(Move.of(Color.WHITE, Point.of("g8")));
        board.apply(Move.of(Color.BLACK, Point.of("b8")));
        board.apply(Move.of(Color.WHITE, Point.of("c8")));
        board.apply(Move.of(Color.BLACK, Point.of("d8")));
        board.apply(Move.of(Color.WHITE, Point.of("e8")));
        board.apply(Move.of(Color.BLACK, Point.of("f8")));
        
        assertThat(board.toStringInLine(), is(""
                + "○○○○○○○●"
                + "○○○○○○●●"
                + "○○○○○●○●"
                + "○○○○●○○●"
                + "○○○○●○○●"
                + "○○○●○●○●"
                + "○○○○●●●●"
                + "●●●●●●○・"));
                
        // 最後の一手！
        assertThat(of(() -> board.apply(Move.of(Color.WHITE, Point.of("h8")))), raiseNothing());
        
        assertThat(board.toStringInLine(), is(""
                + "○○○○○○○●"
                + "○○○○○○●●"
                + "○○○○○●○●"
                + "○○○○●○○●"
                + "○○○○○○○●"
                + "○○○●○○○●"
                + "○○○○●●○●"
                + "●●●●●●○○"));
                
        // 余計な一手！
        assertThat(of(() -> board.apply(Move.passOf(Color.BLACK))),
                raise(IllegalArgumentException.class, "本来の手番とは異なる色が指定されました。期待=null, 実際=●"));
    }
    
    @Test
    public void testColorAt() {
        Board board = StrictBoard.initializedBoard();
        Point.stream().forEach(p -> {
            if (p == Point.of("d4") || p == Point.of("e5")) {
                assertThat(board.colorAt(p), is(Color.WHITE));
            } else if (p == Point.of("d5") || p == Point.of("e4")) {
                assertThat(board.colorAt(p), is(Color.BLACK));
            } else {
                assertThat(board.colorAt(p), nullValue());
            }
        });
    }
    
    @Test
    public void testToString() {
        Board board = StrictBoard.initializedBoard();
        assertThat(board.toString(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
    }
    
    @Test
    public void testToStringKindly() {
        Board board = StrictBoard.initializedBoard();
        assertThat(board.toStringKindly(), is(""
                + "   a b c d e f g h " + System.lineSeparator()
                + " 1 ・・・・・・・・" + System.lineSeparator()
                + " 2 ・・・・・・・・" + System.lineSeparator()
                + " 3 ・・・・・・・・" + System.lineSeparator()
                + " 4 ・・・○●・・・" + System.lineSeparator()
                + " 5 ・・・●○・・・" + System.lineSeparator()
                + " 6 ・・・・・・・・" + System.lineSeparator()
                + " 7 ・・・・・・・・" + System.lineSeparator()
                + " 8 ・・・・・・・・" + System.lineSeparator()
                ));
    }
    
    @Test
    public void testToStringInLine() {
        Board board = StrictBoard.initializedBoard();
        assertThat(board.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
    }
    
    @Test
    public void testSerializable1() {
        // シリアライズ・デシリアライズでき、デシリアライズされたオブジェクトに対して正常に操作できることの確認
        Board board = StrictBoard.initializedBoard();
        assertThat(board.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        
        Board board2 = STUtil.writeAndRead(board);
        assertThat(board2, instanceOf(StrictBoard.class));
        assertThat(board2.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        
        board2.apply(Move.of(Color.BLACK, Point.of("c4")));
        assertThat(board2.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・●●●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        
        Board board3 = STUtil.writeAndRead(board2);
        
        assertThat(board3, instanceOf(StrictBoard.class));
        assertThat(board3.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・●●●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        
        board3.apply(Move.of(Color.WHITE, Point.of("c3")));
        assertThat(board3.toStringInLine(), is("・・・・・・・・・・・・・・・・・・○・・・・・・・●○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
    }
    
    @Test
    public void testSerializable2() {
        // バイトストリームが許容されない内容に改竄されていた場合はデシリアライズが抑止されることの確認
        Board board = StrictBoard.initializedBoard();
        board.apply(Move.of(Color.BLACK, Point.of("c4")));
        assertThat(board.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・●●●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        
        byte[] bytes = STUtil.replace(
               STUtil.write(board),
               STUtil.bytes(Point.of("c4")),
               STUtil.bytes(Point.of("a1")));
        
        assertThat(of(() -> STUtil.read(bytes)),
                raiseExact(FailToDeserializeException.class)
                .rootCause(InvalidObjectException.class, "許可されない手が指定されました。move=[● : a1], board=・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
    }
    
    @Test
    public void testSerializable3() {
        // serialization proxy を迂回できないことの確認
        byte[] bytes = STUtil.replace(
                STUtil.write(new DummyBoard()),
                STUtil.bytes(DummyBoard.class.getName()),
                STUtil.bytes(StrictBoard.class.getName()));
        
        assertThat(of(() -> STUtil.read(bytes)),
                raiseExact(FailToDeserializeException.class)
                .rootCause(InvalidObjectException.class, "Proxy required"));
    }
}
