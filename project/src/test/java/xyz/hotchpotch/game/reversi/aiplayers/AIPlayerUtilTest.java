package xyz.hotchpotch.game.reversi.aiplayers;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static xyz.hotchpotch.jutaime.throwable.RaiseMatchers.*;
import static xyz.hotchpotch.jutaime.throwable.Testee.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.Test;

import xyz.hotchpotch.game.reversi.aiplayers.AIPlayerUtil.LightweightBoard;
import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.StrictBoard;
import xyz.hotchpotch.game.reversi.framework.GameCondition;

public class AIPlayerUtilTest {
    
    private static boolean enableAssertions;
    
    @BeforeClass
    public static void init() {
        // VMオプションに「-ea」が付いているか否かを調べる。
        // 非常によろしくないことをしているような気がするが、他の方法を知らない...
        try {
            assert false;
            enableAssertions = false;
        } catch (AssertionError e) {
            enableAssertions = true;
        }
    }
    
    @Test
    public void testLightweightBoard1() {
        Board original = StrictBoard.initializedBoard();
        
        Board test1 = new LightweightBoard(original);
        assertThat(test1, instanceOf(LightweightBoard.class));
        assertThat(test1.toStringInLine(), is(original.toStringInLine()));
        
        Board test2 = new LightweightBoard(test1);
        assertThat(test2, instanceOf(LightweightBoard.class));
        assertThat(test2.toStringInLine(), is(test1.toStringInLine()));
        
        if (enableAssertions) {
            assertThat(of(() -> new LightweightBoard((Board) null)), raise(AssertionError.class));
        } else {
            assertThat(of(() -> new LightweightBoard((Board) null)), raise(NullPointerException.class));
        }
    }
    
    @Test
    public void testLightweightBoard2() {
        Map<Point, Color> map = new HashMap<>();
        map.put(Point.of("d4"), Color.WHITE);
        map.put(Point.of("e5"), Color.WHITE);
        map.put(Point.of("d5"), Color.BLACK);
        map.put(Point.of("e4"), Color.BLACK);
        map.put(Point.of("a1"), null);
        map.put(Point.of("b2"), null);
        
        Board test = new LightweightBoard(map);
        assertThat(test, instanceOf(LightweightBoard.class));
        assertThat(test.toStringInLine(), is(StrictBoard.initializedBoard().toStringInLine()));
        
        if (enableAssertions) {
            assertThat(of(() -> new LightweightBoard((Map<Point, Color>) null)), raise(AssertionError.class));
        } else {
            assertThat(of(() -> new LightweightBoard((Map<Point, Color>) null)), raise(NullPointerException.class));
        }
    }
    
    @Test
    public void testLightweightBoardColorAt() {
        Board original = StrictBoard.initializedBoard();
        Board test = new LightweightBoard(original);
        
        Point.stream().forEach(p -> {
            assertThat(test.colorAt(p), is(original.colorAt(p)));
        });
        
        if (enableAssertions) {
            assertThat(of(() -> test.colorAt(null)), raise(AssertionError.class));
        } else {
            assertThat(of(() -> test.colorAt(null)), raise(NullPointerException.class));
        }
    }
    
    @Test
    public void testLightweightBoardApply() {
        Board original = StrictBoard.initializedBoard();
        Board test1 = new LightweightBoard(original);
        Board test2 = new LightweightBoard(new HashMap<>());
        
        if (enableAssertions) {
            assertThat(of(() -> test1.apply(null)), raise(AssertionError.class));
            assertThat(of(() -> test2.apply(Move.passOf(Color.BLACK))), raise(AssertionError.class));
            assertThat(of(() -> test1.apply(Move.of(Color.BLACK, Point.of("a1")))), raise(AssertionError.class));
        }
        
        original.apply(Move.of(Color.BLACK, Point.of("d3")));
        test1.apply(Move.of(Color.BLACK, Point.of("d3")));
        assertThat(test1.toStringInLine(), is(original.toStringInLine()));
    }
    
    @Test
    public void testLightweightBoardEquals() {
        Board original = StrictBoard.initializedBoard();
        Board test1 = new LightweightBoard(original);
        Board test2 = new LightweightBoard(test1);
        
        assertThat(test1.equals(test1), is(true));
        assertThat(test1.equals(test2), is(true));
        assertThat(test1.equals(original), is(true));
        
        assertThat(test1.equals(null), is(false));
        assertThat(test1.equals(Point.of("a1")), is(false));
    }
    
    @Test
    public void testLightweightBoardHashCode() {
        Board original = StrictBoard.initializedBoard();
        Board test1 = new LightweightBoard(original);
        
        assertThat(test1.hashCode(), is(Board.hashCode(test1)));
        
        test1.apply(Move.of(Color.BLACK, Point.of("c4")));
        test1.apply(Move.of(Color.WHITE, Point.of("e3")));
        assertThat(test1.hashCode(), is(Board.hashCode(test1)));
    }
    
    @Test
    public void testGetParameterGameConditionString() {
        Map<String, String> params = new HashMap<>();
        params.put("testKey1", "testVal1");
        params.put(getClass().getName() + ".testKey2", "testVal2");
        params.put("", "");
        GameCondition condition = GameCondition.of(SimplestAIPlayer.class, RandomAIPlayer.class, 100L, 3000L, params);
                
        // パラメータチェックのテスト
        assertThat(of(() -> AIPlayerUtil.getParameter(condition, null)), raise(NullPointerException.class));
        assertThat(of(() -> AIPlayerUtil.getParameter(null, "testKey")), raise(NullPointerException.class));
        
        // 値取得のテスト - 共有パラメータは「key」では取得できるが「className + key」では取得できない。
        Optional<String> result1 = AIPlayerUtil.getParameter(condition, "testKey1");
        assertThat(result1.isPresent(), is(true));
        assertThat(result1.get(), is("testVal1"));
        
        Optional<String> result2 = AIPlayerUtil.getParameter(condition, getClass().getName() + ".testKey1");
        assertThat(result2.isPresent(), is(false));
        
        // 値取得のテスト - 個別クラス向けパラメータは「key」でも「className + key」でも取得できる。
        Optional<String> result3 = AIPlayerUtil.getParameter(condition, "testKey2");
        assertThat(result3.isPresent(), is(true));
        assertThat(result3.get(), is("testVal2"));
        
        Optional<String> result4 = AIPlayerUtil.getParameter(condition, getClass().getName() + ".testKey2");
        assertThat(result4.isPresent(), is(true));
        assertThat(result4.get(), is("testVal2"));
        
        // 値取得のテスト - 誤ったキーの場合
        Optional<String> result5 = AIPlayerUtil.getParameter(condition, "wrongKey");
        assertThat(result5.isPresent(), is(false));
        
        // 値取得のテスト - その他
        Optional<String> result6 = AIPlayerUtil.getParameter(condition, "");
        assertThat(result6.isPresent(), is(true));
        assertThat(result6.get(), is(""));
    }
    
    @Test
    public void testGetParameterGameConditionStringFunction() {
        Map<String, String> params = new HashMap<>();
        params.put("testKey1", "a1");
        params.put("testKey2", "xyz");
        GameCondition condition = GameCondition.of(SimplestAIPlayer.class, RandomAIPlayer.class, 100L, 3000L, params);
                
        // パラメータチェックのテスト
        assertThat(of(() -> AIPlayerUtil.getParameter(condition, "testKey", null)), raise(NullPointerException.class));
        assertThat(of(() -> AIPlayerUtil.getParameter(condition, null, Function.identity())), raise(NullPointerException.class));
        assertThat(of(() -> AIPlayerUtil.getParameter(null, "testKey", Function.identity())), raise(NullPointerException.class));
        
        // 値を取得でき、変換できる場合
        Optional<Point> result1 = AIPlayerUtil.getParameter(condition, "testKey1", Point::of);
        assertThat(result1.isPresent(), is(true));
        assertThat(result1.get(), is(Point.of("a1")));
        
        // 値を取得でき、変換できない場合
        Optional<Point> result2 = AIPlayerUtil.getParameter(condition, "testKey2", Point::of);
        assertThat(result2.isPresent(), is(false));
        
        // 値を取得できない場合
        Optional<Point> result3 = AIPlayerUtil.getParameter(condition, "wrongKey", Point::of);
        assertThat(result3.isPresent(), is(false));
    }
    
    @Test
    public void testGetIntParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("int0", "0");
        params.put("int1", "1");
        params.put("int-1", "-1");
        params.put("intMAX", String.valueOf(Integer.MAX_VALUE));
        params.put("intMIN", String.valueOf(Integer.MIN_VALUE));
        params.put("notInt", "xyz");
        GameCondition condition = GameCondition.of(SimplestAIPlayer.class, RandomAIPlayer.class, 100L, 3000L, params);
        
        assertThat(AIPlayerUtil.getIntParameter(condition, "int0"), is(Optional.of(0)));
        assertThat(AIPlayerUtil.getIntParameter(condition, "int1"), is(Optional.of(1)));
        assertThat(AIPlayerUtil.getIntParameter(condition, "int-1"), is(Optional.of(-1)));
        assertThat(AIPlayerUtil.getIntParameter(condition, "intMAX"), is(Optional.of(Integer.MAX_VALUE)));
        assertThat(AIPlayerUtil.getIntParameter(condition, "intMIN"), is(Optional.of(Integer.MIN_VALUE)));
        
        assertThat(AIPlayerUtil.getIntParameter(condition, "notInt"), is(Optional.empty()));
        assertThat(AIPlayerUtil.getIntParameter(condition, "wrongKey"), is(Optional.empty()));
    }
    
    @Test
    public void testGetLongParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("long0", "0");
        params.put("long1", "1");
        params.put("long-1", "-1");
        params.put("longMAX", String.valueOf(Long.MAX_VALUE));
        params.put("longMIN", String.valueOf(Long.MIN_VALUE));
        params.put("notLong", "xyz");
        GameCondition condition = GameCondition.of(SimplestAIPlayer.class, RandomAIPlayer.class, 100L, 3000L, params);
        
        assertThat(AIPlayerUtil.getLongParameter(condition, "long0"), is(Optional.of(0L)));
        assertThat(AIPlayerUtil.getLongParameter(condition, "long1"), is(Optional.of(1L)));
        assertThat(AIPlayerUtil.getLongParameter(condition, "long-1"), is(Optional.of(-1L)));
        assertThat(AIPlayerUtil.getLongParameter(condition, "longMAX"), is(Optional.of(Long.MAX_VALUE)));
        assertThat(AIPlayerUtil.getLongParameter(condition, "longMIN"), is(Optional.of(Long.MIN_VALUE)));
        
        assertThat(AIPlayerUtil.getLongParameter(condition, "notLong"), is(Optional.empty()));
        assertThat(AIPlayerUtil.getLongParameter(condition, "wrongKey"), is(Optional.empty()));
    }
    
    @Test
    public void testGetFloatParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("float0", "0");
        params.put("float3.14", "3.14");
        params.put("float-2.71", "-2.71");
        params.put("floatMAX", String.valueOf(Float.MAX_VALUE));
        params.put("floatMIN", String.valueOf(Float.MIN_VALUE));
        params.put("notFloat", "xyz");
        GameCondition condition = GameCondition.of(SimplestAIPlayer.class, RandomAIPlayer.class, 100L, 3000L, params);
        
        assertThat(AIPlayerUtil.getFloatParameter(condition, "float0"), is(Optional.of(0f)));
        assertThat(AIPlayerUtil.getFloatParameter(condition, "float3.14"), is(Optional.of(3.14f)));
        assertThat(AIPlayerUtil.getFloatParameter(condition, "float-2.71"), is(Optional.of(-2.71f)));
        assertThat(AIPlayerUtil.getFloatParameter(condition, "floatMAX"), is(Optional.of(Float.MAX_VALUE)));
        assertThat(AIPlayerUtil.getFloatParameter(condition, "floatMIN"), is(Optional.of(Float.MIN_VALUE)));
        
        assertThat(AIPlayerUtil.getFloatParameter(condition, "notFloat"), is(Optional.empty()));
        assertThat(AIPlayerUtil.getFloatParameter(condition, "wrongKey"), is(Optional.empty()));
    }
    
    @Test
    public void testGetDoubleParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("double0", "0");
        params.put("double3.14", "3.14");
        params.put("double-2.71", "-2.71");
        params.put("doubleMAX", String.valueOf(Double.MAX_VALUE));
        params.put("doubleMIN", String.valueOf(Double.MIN_VALUE));
        params.put("notDouble", "xyz");
        GameCondition condition = GameCondition.of(SimplestAIPlayer.class, RandomAIPlayer.class, 100L, 3000L, params);
        
        assertThat(AIPlayerUtil.getDoubleParameter(condition, "double0"), is(Optional.of(0d)));
        assertThat(AIPlayerUtil.getDoubleParameter(condition, "double3.14"), is(Optional.of(3.14d)));
        assertThat(AIPlayerUtil.getDoubleParameter(condition, "double-2.71"), is(Optional.of(-2.71d)));
        assertThat(AIPlayerUtil.getDoubleParameter(condition, "doubleMAX"), is(Optional.of(Double.MAX_VALUE)));
        assertThat(AIPlayerUtil.getDoubleParameter(condition, "doubleMIN"), is(Optional.of(Double.MIN_VALUE)));
        
        assertThat(AIPlayerUtil.getDoubleParameter(condition, "notDouble"), is(Optional.empty()));
        assertThat(AIPlayerUtil.getDoubleParameter(condition, "wrongKey"), is(Optional.empty()));
    }
    
    @Test
    public void testGetBooleanParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("booltrue", "true");
        params.put("boolTrue", "True");
        params.put("boolTRUE", "TRUE");
        params.put("booltRuE", "tRuE");
        params.put("boolFalse", "false");
        params.put("notBool", "xyz");
        GameCondition condition = GameCondition.of(SimplestAIPlayer.class, RandomAIPlayer.class, 100L, 3000L, params);
        
        assertThat(AIPlayerUtil.getBooleanParameter(condition, "booltrue"), is(Optional.of(true)));
        assertThat(AIPlayerUtil.getBooleanParameter(condition, "boolTrue"), is(Optional.of(true)));
        assertThat(AIPlayerUtil.getBooleanParameter(condition, "boolTRUE"), is(Optional.of(true)));
        assertThat(AIPlayerUtil.getBooleanParameter(condition, "booltRuE"), is(Optional.of(true)));
        
        assertThat(AIPlayerUtil.getBooleanParameter(condition, "boolFalse"), is(Optional.of(false)));
        assertThat(AIPlayerUtil.getBooleanParameter(condition, "notBool"), is(Optional.of(false)));
        
        assertThat(AIPlayerUtil.getBooleanParameter(condition, "wrongKey"), is(Optional.empty()));
    }
}
