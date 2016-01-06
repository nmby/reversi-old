package xyz.hotchpotch.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static xyz.hotchpotch.jutaime.serializable.STUtil.*;
import static xyz.hotchpotch.jutaime.throwable.RaiseMatchers.*;
import static xyz.hotchpotch.jutaime.throwable.Testee.*;

import java.io.InvalidObjectException;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import xyz.hotchpotch.jutaime.serializable.FailToDeserializeException;
import xyz.hotchpotch.reversi.core.Color;
import xyz.hotchpotch.reversi.core.Move;
import xyz.hotchpotch.reversi.core.Point;

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
            });
        });
        
        Point.stream().forEach(p -> {
            assertThat(of(() -> Move.of(null, p)), raise(NullPointerException.class));
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
            Move move1 = Move.of(c, p);
            Move move2 = Move.of(c, p);
            
            assertThat(move1, not(sameInstance(move2)));
            assertThat(move1.equals(move1), is(true));
            assertThat(move1.equals(move2), is(true));
            
            assertThat(move1.equals(Move.of(c.opposite(), p)), is(false));
            assertThat(move1.equals(Move.of(c, null)), is(false));
            assertThat(Move.of(c, null).equals(move1), is(false));
            assertThat(move1.equals(Move.of(c, p == Point.of(0, 0) ? Point.of(1, 1) : Point.of(0, 0))), is(false));
            
            assertThat(move1.equals(null), is(false));
            assertThat(move1.equals(c), is(false));
            assertThat(move1.equals(p), is(false));
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
        // ハッシュ値がばらけていることの確認
        Set<Integer> hashCodes = Color.stream()
                .flatMap(c -> Point.stream().map(p -> Move.of(c, p).hashCode()))
                .collect(Collectors.toSet());
        hashCodes.add(Move.passOf(Color.BLACK).hashCode());
        hashCodes.add(Move.passOf(Color.WHITE).hashCode());
        
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
            assertThat(writeAndRead(Move.of(c, null)), is(Move.of(c, null)));
            
            Point.stream().forEach(p -> {
                assertThat(writeAndRead(Move.of(c, p)), is(Move.of(c, p)));
            });
        });
    }
    
    @Test
    public void testSerializable2() {
        // 先ずは、Move の中身を改竄できることを確認（テスト方法の妥当性確認）
        assertThat(writeModifyAndRead(
                Move.of(Color.BLACK, Point.of(0, 0)),
                bytes -> replace(bytes, bytes(Color.BLACK), bytes(Color.WHITE))),
                is(Move.of(Color.WHITE, Point.of(0, 0))));
                
        // color == null に改竄されたオブジェクトのデシリアル化が抑止されることの確認
        assertThat(of(() -> writeModifyAndRead(
                Move.of(Color.BLACK, Point.of(0, 0)),
                bytes -> replace(bytes, bytes(Color.BLACK), bytes((Object) null)))),
                raise(FailToDeserializeException.class).rootCause(InvalidObjectException.class));
    }
}
