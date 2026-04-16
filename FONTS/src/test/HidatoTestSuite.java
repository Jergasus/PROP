package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    CellTest.class,
    BoardTest.class,
    ValidatorTest.class,
    SolverTest.class
})
public class HidatoTestSuite {}
