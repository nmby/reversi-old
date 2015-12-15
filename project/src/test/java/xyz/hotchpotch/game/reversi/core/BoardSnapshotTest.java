package xyz.hotchpotch.game.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static xyz.hotchpotch.jutaime.throwable.RaiseMatchers.*;
import static xyz.hotchpotch.jutaime.throwable.Testee.*;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import xyz.hotchpotch.jutaime.serializable.experimental.FailToDeserializeException;
import xyz.hotchpotch.jutaime.serializable.experimental.TestUtil;

public class BoardSnapshotTest {
    
    private static class PseudeBoardSnapshot extends BaseBoard implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private volatile Map<Point, Color> mapToSerialize;
        
        private PseudeBoardSnapshot(Board board, Map<Point, Color> mapToSerialize) {
            super(board);
            this.mapToSerialize = mapToSerialize;
        }
        
        @Override
        public void apply(Move move) {
            throw new UnsupportedOperationException();
        }
        
        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeObject(mapToSerialize);
        }
    }
    
    @Test
    public void testOf() {
        Board original = StrictBoard.initializedBoard();
        Board snapshot = BoardSnapshot.of(original);
        
        assertThat(snapshot, instanceOf(BoardSnapshot.class));
        assertThat(original.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        assertThat(snapshot.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        
        // オリジナルを変更してもスナップショットは元のまま
        original.apply(Move.of(Color.BLACK, Point.of("d3")));
        assertThat(original.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・●・・・・・・・●●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        assertThat(snapshot.toStringInLine(), is("・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
        
        assertThat(of(() -> BoardSnapshot.of(null)), raise(NullPointerException.class));
    }
    
    @Test
    public void testApply() {
        Board original = StrictBoard.initializedBoard();
        Board snapshot = BoardSnapshot.of(original);
        
        assertThat(of(() -> snapshot.apply(Move.of(Color.BLACK, Point.of("d3")))),
                raise(UnsupportedOperationException.class));
    }
    
    @Test
    public void testSerializable1() throws IOException {
        Board original = StrictBoard.initializedBoard();
        Board snapshot = BoardSnapshot.of(original);
        
        assertThat(TestUtil.writeAndRead(snapshot), instanceOf(BoardSnapshot.class));
        assertThat(TestUtil.writeAndRead(snapshot).toStringInLine(),
                is("・・・・・・・・・・・・・・・・・・・・・・・・・・・○●・・・・・・●○・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
    }
    
    @Test
    public void testSerializable2() throws IOException {
        Map<Point, Color> map = new HashMap<>();
        map.put(Point.of("a1"), Color.BLACK);
        map.put(Point.of("b1"), Color.WHITE);
        Board original = StrictBoard.initializedBoard();
        Board pseude01 = new PseudeBoardSnapshot(original, map);
        Board pseude02 = new PseudeBoardSnapshot(original, null);
        
        // PseudeBoardSnapshot のバイト配列を改竄して BoardSnapshot にデシリアライズできることの確認（テスト方法の妥当性確認）
        assertThat(TestUtil.writeModifyAndRead(
                pseude01,
                bytes -> TestUtil.replace(
                        bytes,
                        TestUtil.bytes(PseudeBoardSnapshot.class.getName()),
                        TestUtil.bytes(BoardSnapshot.class.getName()))),
                instanceOf(BoardSnapshot.class));
        assertThat(((Board) TestUtil.writeModifyAndRead(
                pseude01,
                bytes -> TestUtil.replace(
                        bytes,
                        TestUtil.bytes(PseudeBoardSnapshot.class.getName()),
                        TestUtil.bytes(BoardSnapshot.class.getName())))).toStringInLine(),
                is("●○・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
                
        // map==null に改竄されている場合はデシリアライズしないことの確認（テスト本番）
        assertThat(of(() -> TestUtil.writeModifyAndRead(
                pseude02,
                bytes -> TestUtil.replace(
                        bytes,
                        TestUtil.bytes(PseudeBoardSnapshot.class.getName()),
                        TestUtil.bytes(BoardSnapshot.class.getName())))),
                raise(FailToDeserializeException.class)
                        .rootCause(InvalidObjectException.class, "map cannot be null."));
    }
}
