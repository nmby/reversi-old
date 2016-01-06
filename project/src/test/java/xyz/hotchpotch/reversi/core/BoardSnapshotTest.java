package xyz.hotchpotch.reversi.core;

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

import xyz.hotchpotch.jutaime.serializable.FailToDeserializeException;
import xyz.hotchpotch.jutaime.serializable.STUtil;
import xyz.hotchpotch.reversi.core.BaseBoard;
import xyz.hotchpotch.reversi.core.Board;
import xyz.hotchpotch.reversi.core.BoardSnapshot;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Move;
import xyz.hotchpotch.reversi.core.Point;
import xyz.hotchpotch.reversi.core.StrictBoard;

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
        
        assertThat(STUtil.writeAndRead(snapshot), instanceOf(BoardSnapshot.class));
        assertThat(STUtil.writeAndRead(snapshot).toStringInLine(),
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
        assertThat(STUtil.writeModifyAndRead(
                pseude01,
                bytes -> STUtil.replace(
                        bytes,
                        STUtil.bytes(PseudeBoardSnapshot.class.getName()),
                        STUtil.bytes(BoardSnapshot.class.getName()))),
                instanceOf(BoardSnapshot.class));
        assertThat(((Board) STUtil.writeModifyAndRead(
                pseude01,
                bytes -> STUtil.replace(
                        bytes,
                        STUtil.bytes(PseudeBoardSnapshot.class.getName()),
                        STUtil.bytes(BoardSnapshot.class.getName())))).toStringInLine(),
                is("●○・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・"));
                
        // map==null に改竄されている場合はデシリアライズしないことの確認（テスト本番）
        assertThat(of(() -> STUtil.writeModifyAndRead(
                pseude02,
                bytes -> STUtil.replace(
                        bytes,
                        STUtil.bytes(PseudeBoardSnapshot.class.getName()),
                        STUtil.bytes(BoardSnapshot.class.getName())))),
                raise(FailToDeserializeException.class)
                        .rootCause(InvalidObjectException.class, "map cannot be null."));
    }
}
