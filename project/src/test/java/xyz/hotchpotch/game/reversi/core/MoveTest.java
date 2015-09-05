package xyz.hotchpotch.game.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static xyz.hotchpotch.jutaime.throwable.RaiseMatchers.*;
import static xyz.hotchpotch.jutaime.throwable.Testee.*;

import java.io.InvalidObjectException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import xyz.hotchpotch.jutaime.serializable.experimental.FailToDeserializeException;
import xyz.hotchpotch.jutaime.serializable.experimental.TestUtil;

public class MoveTest {
    
    @Test
    public void testOf() {
        Color.stream().forEach(c -> {
            assertThat(Move.of(c, null), instanceOf(Move.class));
            assertThat(Move.of(c, null).color, theInstance(c));
            assertThat(Move.of(c, null).point, nullValue());
            
            Point.stream().forEach(p -> {
                assertThat(Move.of(c, p), instanceOf(Move.class));
                assertThat(Move.of(c, p).color, theInstance(c));
                assertThat(Move.of(c, p).point, theInstance(p));
                
                assertThat(of(() -> Move.of(null, p)), raise(NullPointerException.class));
            });
        });
    }
    
    @Test
    public void testPassOf() {
        Color.stream().forEach(c -> {
            assertThat(Move.passOf(c), instanceOf(Move.class));
            assertThat(Move.passOf(c).color, theInstance(c));
            assertThat(Move.passOf(c).point, nullValue());
            
            assertThat(Move.passOf(c), is(Move.of(c, null)));
        });
        
        assertThat(of(() -> Move.passOf(null)), raise(NullPointerException.class));
    }
    
    @Test
    public void testEquals() {
        Color.stream().forEach(c -> Point.stream().forEach(p -> {
            Move move = Move.of(c, p);
            
            assertThat(move, not(sameInstance(Move.of(c, p))));
            assertThat(move.equals(Move.of(c, p)), is(true));
            assertThat(move.equals(move), is(true));
            
            assertThat(move.equals(Move.of(c.opposite(), p)), is(false));
            assertThat(move.equals(Move.of(c, null)), is(false));
            assertThat(Move.of(c, null).equals(move), is(false));
            assertThat(move.equals(Move.of(c, p == Point.of(0, 0) ? Point.of(1, 1) : Point.of(0, 0))), is(false));
            
            assertThat(move.equals(null), is(false));
            assertThat(move.equals(c), is(false));
            assertThat(move.equals(p), is(false));
        }));
        
        Color.stream().forEach(c -> {
            assertThat(Move.of(c, null).equals(Move.of(c, null)), is(true));
            assertThat(Move.passOf(c).equals(Move.passOf(c)), is(true));
            assertThat(Move.of(c, null).equals(Move.passOf(c)), is(true));
            assertThat(Move.passOf(c).equals(Move.of(c, null)), is(true));
        });
    }
    
    @Test
    public void testHashCode1() {
        // 同じ内容のオブジェクトは常に同じ値を返すことの確認
        Color.stream().forEach(c -> {
            assertThat(Move.of(c, null).hashCode(), is(Move.of(c, null).hashCode()));
            
            Point.stream().forEach(p -> {
                assertThat(Move.of(c, p).hashCode(), is(Move.of(c, p).hashCode()));
            });
        });
    }
    
    @Test
    public void testHashCode2() {
        // 値がばらけていることの確認
        Set<Integer> hashCodes = new HashSet<>();
        
        for (Color c : Color.values()) {
            int hashCode = Move.of(c, null).hashCode();
            assertThat(hashCodes.contains(hashCode), is(false));
            hashCodes.add(hashCode);
            
            for (Point p : Point.values()) {
                hashCode = Move.of(c, p).hashCode();
                assertThat(hashCodes.contains(hashCode), is(false));
                hashCodes.add(hashCode);
            }
        }
        
        assertThat(hashCodes.size(), is(Color.values().length * (Point.values().length + 1)));
    }
    
    @Test
    public void testToString() {
        Color.stream().forEach(c -> {
            assertThat(Move.of(c, null).toString(), is(String.format("[%s : PASS]", c)));
            
            Point.stream().forEach(p -> {
                assertThat(Move.of(c, p).toString(), is(String.format("[%s : %s]", c, p.toStringKindly())));
            });
        });
    }
    
    @Test
    public void testSerializable1() {
        Color.stream().forEach(c -> {
            assertThat(TestUtil.writeAndRead(Move.of(c, null)), is(Move.of(c, null)));
            
            Point.stream().forEach(p -> {
                assertThat(TestUtil.writeAndRead(Move.of(c, p)), is(Move.of(c, p)));
            });
        });
    }
    
    @Test
    public void testSerializable2() {
        // 先ずは、Move の中身を改竄できることを確認（テスト方法の妥当性確認）
        assertThat(TestUtil.writeModifyAndRead(Move.of(Color.BLACK, Point.of(0, 0)),
                bytes -> TestUtil.replace(bytes, TestUtil.bytes(Color.BLACK), TestUtil.bytes(Color.WHITE))),
                is(Move.of(Color.WHITE, Point.of(0, 0))));
                
        // color == null に改竄されたオブジェクトのデシリアル化が抑止されることの確認
        assertThat(of(() -> TestUtil.writeModifyAndRead(Move.of(Color.BLACK, Point.of(0, 0)),
                bytes -> TestUtil.replace(bytes, TestUtil.bytes(Color.BLACK), TestUtil.bytes((Object) null)))),
                raise(FailToDeserializeException.class).rootCause(InvalidObjectException.class));
    }
}
