import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    PositionTest.class,
    CellShapeTest.class,
    CellTest.class,
    BoardTest.class,
    SquareAdjacencyStrategyTest.class,
    SquareFullAdjacencyStrategyTest.class,
    HexagonalAdjacencyStrategyTest.class,
    TriangleAdjacencyStrategyTest.class,
    ValidatorTest.class,
    SolverTest.class,
    GeneratorTest.class
})
public class HidatoTestSuite {}
