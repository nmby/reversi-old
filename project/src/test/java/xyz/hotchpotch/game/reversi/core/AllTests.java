package xyz.hotchpotch.game.reversi.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        BaseBoardTest.class,
        BoardSnapshotTest.class,
        BoardTest.class,
        ColorTest.class,
        DirectionTest.class,
        MoveTest.class,
        PointTest.class,
        StrictBoardTest.class
})
public class AllTests {
}
