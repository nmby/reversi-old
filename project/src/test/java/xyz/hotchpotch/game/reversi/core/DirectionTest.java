package xyz.hotchpotch.game.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class DirectionTest {
    
    @Test
    public void testValues() {
        Direction[] directions = Direction.values();
        
        assertThat(directions.length, is(8));
        
        assertThat(directions[0], theInstance(Direction.UPPER));
        assertThat(directions[1], theInstance(Direction.UPPER_RIGHT));
        assertThat(directions[2], theInstance(Direction.RIGHT));
        assertThat(directions[3], theInstance(Direction.LOWER_RIGHT));
        assertThat(directions[4], theInstance(Direction.LOWER));
        assertThat(directions[5], theInstance(Direction.LOWER_LEFT));
        assertThat(directions[6], theInstance(Direction.LEFT));
        assertThat(directions[7], theInstance(Direction.UPPER_LEFT));
    }
    
    @Test
    public void testValueOf() {
        assertThat(Direction.valueOf("UPPER"), theInstance(Direction.UPPER));
        assertThat(Direction.valueOf("UPPER_RIGHT"), theInstance(Direction.UPPER_RIGHT));
        assertThat(Direction.valueOf("RIGHT"), theInstance(Direction.RIGHT));
        assertThat(Direction.valueOf("LOWER_RIGHT"), theInstance(Direction.LOWER_RIGHT));
        assertThat(Direction.valueOf("LOWER"), theInstance(Direction.LOWER));
        assertThat(Direction.valueOf("LOWER_LEFT"), theInstance(Direction.LOWER_LEFT));
        assertThat(Direction.valueOf("LEFT"), theInstance(Direction.LEFT));
        assertThat(Direction.valueOf("UPPER_LEFT"), theInstance(Direction.UPPER_LEFT));
    }
    
    @Test
    public void testStream() {
        assertThat(Direction.stream().isParallel(), is(false));
        assertThat(Direction.stream().toArray(Direction[]::new), is(Direction.values()));
    }
    
    @Test
    public void testParallelStream() {
        assertThat(Direction.parallelStream().isParallel(), is(true));
        assertThat(Direction.parallelStream().toArray(Direction[]::new), is(Direction.values()));
    }
    
    @Test
    public void testDI() {
        assertThat(Direction.UPPER.di, is(-1));
        assertThat(Direction.UPPER_LEFT.di, is(-1));
        assertThat(Direction.UPPER_RIGHT.di, is(-1));
        
        assertThat(Direction.LEFT.di, is(0));
        assertThat(Direction.RIGHT.di, is(0));
        
        assertThat(Direction.LOWER.di, is(1));
        assertThat(Direction.LOWER_LEFT.di, is(1));
        assertThat(Direction.LOWER_RIGHT.di, is(1));
    }
    
    @Test
    public void testDJ() {
        assertThat(Direction.LEFT.dj, is(-1));
        assertThat(Direction.UPPER_LEFT.dj, is(-1));
        assertThat(Direction.LOWER_LEFT.dj, is(-1));
        
        assertThat(Direction.UPPER.dj, is(0));
        assertThat(Direction.LOWER.dj, is(0));
        
        assertThat(Direction.RIGHT.dj, is(1));
        assertThat(Direction.UPPER_RIGHT.dj, is(1));
        assertThat(Direction.LOWER_RIGHT.dj, is(1));
    }
    
    @Test
    public void testToString() {
        Direction.stream().forEach(d -> {
            assertThat(d.toString(), is(String.format("<%d, %d>", d.di, d.dj)));
        });
    }
}
