package xyz.hotchpotch.game.reversi.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ColorTest.class,
        DirectionTest.class,
        PointTest.class
})
public class AllTests {
}
