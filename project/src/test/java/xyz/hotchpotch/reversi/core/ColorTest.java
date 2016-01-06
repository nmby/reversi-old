package xyz.hotchpotch.reversi.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import xyz.hotchpotch.reversi.core.Color;

public class ColorTest {
    
    @Test
    public void testValues() {
        assertThat(Color.values().length, is(2));
        
        assertThat(Color.values()[0], theInstance(Color.BLACK));
        assertThat(Color.values()[1], theInstance(Color.WHITE));
    }
    
    @Test
    public void testValueOf() {
        assertThat(Color.valueOf("BLACK"), theInstance(Color.BLACK));
        assertThat(Color.valueOf("WHITE"), theInstance(Color.WHITE));
    }
    
    @Test
    public void testStream() {
        assertThat(Color.stream().isParallel(), is(false));
        assertThat(Color.stream().toArray(Color[]::new), is(Color.values()));
    }
    
    @Test
    public void testParallelStream() {
        assertThat(Color.parallelStream().isParallel(), is(true));
        assertThat(Color.parallelStream().toArray(Color[]::new), is(Color.values()));
    }
    
    @Test
    public void testToStringColor() {
        assertThat(Color.toString(Color.BLACK), is(Color.BLACK.toString()));
        assertThat(Color.toString(Color.WHITE), is(Color.WHITE.toString()));
        assertThat(Color.toString(null), is("・"));
    }
    
    @Test
    public void testOpposite() {
        assertThat(Color.BLACK.opposite(), theInstance(Color.WHITE));
        assertThat(Color.WHITE.opposite(), theInstance(Color.BLACK));
    }
    
    @Test
    public void testToString() {
        assertThat(Color.BLACK.toString(), is("●"));
        assertThat(Color.WHITE.toString(), is("○"));
    }
}
