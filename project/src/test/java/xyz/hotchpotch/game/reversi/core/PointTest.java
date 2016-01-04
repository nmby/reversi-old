package xyz.hotchpotch.game.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static xyz.hotchpotch.jutaime.serializable.STUtil.*;
import static xyz.hotchpotch.jutaime.throwable.RaiseMatchers.*;
import static xyz.hotchpotch.jutaime.throwable.Testee.*;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import org.junit.Test;

import xyz.hotchpotch.jutaime.serializable.FailToDeserializeException;

public class PointTest {
    
    @Test
    public void testConstants() {
        assertThat(Point.HEIGHT, is(8));
        assertThat(Point.WIDTH, is(8));
    }
    
    @Test
    public void testValues() {
        Point[] points = Point.values();
        
        assertThat(points.length, is(Point.HEIGHT * Point.WIDTH));
    }
    
    @Test
    public void testOfIntInt1() {
        Point[] points = Point.values();
        
        IntStream.range(0, Point.HEIGHT).forEach(i -> IntStream.range(0, Point.WIDTH).forEach(j -> {
            assertThat(Point.of(i, j), theInstance(points[Point.WIDTH * i + j]));
        }));
    }
    
    @Test
    public void testOfIntInt2() {
        IntStream.range(0, Point.HEIGHT).forEach(i -> {
            assertThat(of(() -> Point.of(i, -1)), raise(IndexOutOfBoundsException.class));
            assertThat(of(() -> Point.of(i, Point.WIDTH)), raise(IndexOutOfBoundsException.class));
        });
        
        IntStream.range(0, Point.WIDTH).forEach(j -> {
            assertThat(of(() -> Point.of(-1, j)), raise(IndexOutOfBoundsException.class));
            assertThat(of(() -> Point.of(Point.HEIGHT, j)), raise(IndexOutOfBoundsException.class));
        });
        
        assertThat(of(() -> Point.of(-1, -1)), raise(IndexOutOfBoundsException.class));
        assertThat(of(() -> Point.of(Point.HEIGHT, Point.WIDTH)), raise(IndexOutOfBoundsException.class));
        
        assertThat(of(() -> Point.of(Integer.MIN_VALUE, Integer.MIN_VALUE)), raise(IndexOutOfBoundsException.class));
        assertThat(of(() -> Point.of(Integer.MAX_VALUE, Integer.MAX_VALUE)), raise(IndexOutOfBoundsException.class));
    }
    
    @Test
    public void testOfString1() {
        IntStream.range('a', 'a' + Point.WIDTH).forEach(c -> IntStream.range('1', '1' + Point.HEIGHT).forEach(n -> {
            assertThat(Point.of(String.format("%c%c", c, n)), theInstance(Point.of(n - '1', c - 'a')));
        }));
    }
    
    @Test
    public void testOfString2() {
        assertThat(of(() -> Point.of(null)), raise(NullPointerException.class));
        assertThat(of(() -> Point.of("")), raise(IllegalArgumentException.class));
        assertThat(of(() -> Point.of("a1")), raiseNothing());
        assertThat(of(() -> Point.of("h8")), raiseNothing());
        assertThat(of(() -> Point.of(" a1")), raise(IllegalArgumentException.class));
        assertThat(of(() -> Point.of("h8 ")), raise(IllegalArgumentException.class));
        assertThat(of(() -> Point.of("A1")), raise(IllegalArgumentException.class));
        assertThat(of(() -> Point.of("H8")), raise(IllegalArgumentException.class));
        assertThat(of(() -> Point.of("a0")), raise(IllegalArgumentException.class));
        assertThat(of(() -> Point.of("h9")), raise(IllegalArgumentException.class));
        assertThat(of(() -> Point.of("i8")), raise(IllegalArgumentException.class));
        assertThat(of(() -> Point.of("Hello, World !!")), raise(IllegalArgumentException.class));
    }
    
    @Test
    public void testStream() {
        assertThat(Point.stream().isParallel(), is(false));
        assertThat(Point.stream().toArray(Point[]::new), is(Point.values()));
    }
    
    @Test
    public void testParallelStream() {
        assertThat(Point.parallelStream().isParallel(), is(true));
        assertThat(Point.parallelStream().toArray(Point[]::new), is(Point.values()));
    }
    
    @Test
    public void testIJ() {
        IntStream.range(0, Point.HEIGHT).forEach(i -> IntStream.range(0, Point.WIDTH).forEach(j -> {
            assertThat(Point.of(i, j).i, is(i));
            assertThat(Point.of(i, j).j, is(j));
        }));
    }
    
    @Test
    public void testHasNext() {
        IntStream.range(0, Point.HEIGHT).forEach(i -> IntStream.range(0, Point.WIDTH).forEach(j -> {
            Point p = Point.of(i, j);
            
            assertThat(p.hasNext(Direction.UPPER), is(0 < i));
            assertThat(p.hasNext(Direction.UPPER_RIGHT), is(0 < i && j < Point.WIDTH - 1));
            assertThat(p.hasNext(Direction.RIGHT), is(j < Point.WIDTH - 1));
            assertThat(p.hasNext(Direction.LOWER_RIGHT), is(i < Point.HEIGHT - 1 && j < Point.WIDTH - 1));
            assertThat(p.hasNext(Direction.LOWER), is(i < Point.HEIGHT - 1));
            assertThat(p.hasNext(Direction.LOWER_LEFT), is(i < Point.HEIGHT - 1 && 0 < j));
            assertThat(p.hasNext(Direction.LEFT), is(0 < j));
            assertThat(p.hasNext(Direction.UPPER_LEFT), is(0 < i && 0 < j));
            
            assertThat(of(() -> p.hasNext(null)), raise(NullPointerException.class));
        }));
    }
    
    @Test
    public void testNext() {
        IntStream.range(0, Point.HEIGHT).forEach(i -> IntStream.range(0, Point.WIDTH).forEach(j -> {
            Point p = Point.of(i, j);
            assertThat(of(() -> p.next(null)), raise(NullPointerException.class));
            
            Direction.stream().forEach(d -> {
                if (p.hasNext(d)) {
                    assertThat(p.next(d), theInstance(Point.of(i + d.di, j + d.dj)));
                } else {
                    assertThat(of(() -> p.next(d)), raise(NoSuchElementException.class));
                }
            });
        }));
    }
    
    @Test
    public void testOrdinal() {
        Point[] points = Point.values();
        
        IntStream.range(0, points.length).forEach(n -> {
            assertThat(points[n].ordinal(), is(n));
        });
    }
    
    @Test
    public void testCompareTo() {
        Point[] points = Point.values();
        IntStream.range(0, points.length).forEach(n -> {
            IntStream.range(0, n).forEach(o -> assertThat(points[n].compareTo(points[o]), is(1)));
            assertThat(points[n].compareTo(points[n]), is(0));
            IntStream.range(n + 1, points.length).forEach(o -> assertThat(points[n].compareTo(points[o]), is(-1)));
        });
        
        assertThat(of(() -> Point.of("a1").compareTo(null)), raise(NullPointerException.class));
    }
    
    @Test
    public void testToString() {
        IntStream.range(0, Point.HEIGHT).forEach(i -> IntStream.range(0, Point.WIDTH).forEach(j -> {
            assertThat(Point.of(i, j).toString(), is(String.format("(%d, %d)", i, j)));
        }));
    }
    
    @Test
    public void testToStringKindly() {
        IntStream.range('a', 'a' + Point.WIDTH).forEach(c -> IntStream.range('1', '1' + Point.HEIGHT).forEach(n -> {
            String str = String.format("%c%c", c, n);
            
            assertThat(Point.of(str).toStringKindly(), is(str));
        }));
    }
    
    @Test
    public void testSerializable1() throws IOException {
        Point.stream().forEach(p -> {
            assertThat(writeAndRead(p), theInstance(p));
        });
    }
    
    @Test
    public void testSerializable2() {
        // 先ずは、(i, j) を改竄できることを確認（テスト方法の妥当性を確認）
        assertThat(writeModifyAndRead(Point.of(0, 0), bytes -> modifyIJ(bytes, 1, 2)),
                theInstance(Point.of(1, 2)));
                
        // (i, j) が正常範囲ならば正常にデシリアル化できることの確認
        IntStream.range(0, Point.HEIGHT).forEach(i -> IntStream.range(0, Point.WIDTH).forEach(j -> {
            assertThat(writeModifyAndRead(Point.of(0, 0), bytes -> modifyIJ(bytes, i, j)),
                    theInstance(Point.of(i, j)));
        }));
        
        // (i, j) が不正な範囲ならばデシリアル化が抑止されることの確認
        IntStream.range(0, Point.HEIGHT).forEach(i -> {
            assertThat(of(() -> writeModifyAndRead(Point.of(0, 0), bytes -> modifyIJ(bytes, i, -1))),
                    raise(FailToDeserializeException.class).rootCause(InvalidObjectException.class));
            assertThat(of(() -> writeModifyAndRead(Point.of(0, 0), bytes -> modifyIJ(bytes, i, Point.WIDTH))),
                    raise(FailToDeserializeException.class).rootCause(InvalidObjectException.class));
        });
        IntStream.range(0, Point.WIDTH).forEach(j -> {
            assertThat(of(() -> writeModifyAndRead(Point.of(0, 0), bytes -> modifyIJ(bytes, -1, j))),
                    raise(FailToDeserializeException.class).rootCause(InvalidObjectException.class));
            assertThat(of(() -> writeModifyAndRead(Point.of(0, 0), bytes -> modifyIJ(bytes, Point.HEIGHT, j))),
                    raise(FailToDeserializeException.class).rootCause(InvalidObjectException.class));
        });
    }
    
    private byte[] modifyIJ(byte[] bytes, int i, int j) {
        int len = bytes.length;
        byte[] modified = Arrays.copyOf(bytes, len);
        
        byte[] iBytes = bytes(i);
        modified[len - 8] = iBytes[0];
        modified[len - 7] = iBytes[1];
        modified[len - 6] = iBytes[2];
        modified[len - 5] = iBytes[3];
        
        byte[] jBytes = bytes(j);
        modified[len - 4] = jBytes[0];
        modified[len - 3] = jBytes[1];
        modified[len - 2] = jBytes[2];
        modified[len - 1] = jBytes[3];
        
        return modified;
    }
    
    @Test
    public void testSerializable3() throws IOException {
        byte[] bytesOfPoint = bytes(Point.class.getName());
        byte[] bytesOfPointProxy = bytes(Point.class.getName() + "$SerializationProxy");
        byte[] bytesOfInstance = write(Point.of(0, 0));
        
        // （Point$SerializationProxyではなく）Pointのデシリアル化が抑止されることの確認
        byte[] modified = replace(bytesOfInstance, bytesOfPointProxy, bytesOfPoint);
        assertThat(of(() -> read(modified)),
                raise(FailToDeserializeException.class).rootCause(ObjectStreamException.class, "Proxy required"));
                
        // Point$SerializationProxyであればデシリアル化できることの確認
        byte[] modified2 = replace(modified, bytesOfPoint, bytesOfPointProxy);
        assertThat(read(modified2), theInstance(Point.of(0, 0)));
    }
}
