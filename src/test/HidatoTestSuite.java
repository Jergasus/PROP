package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * JUnit 4 test suite — runs all algorithm tests in one command.
 *
 * Usage:
 *   javac -cp out:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar \
 *         -d out src/test/*.java
 *   java  -cp out:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar \
 *         org.junit.runner.JUnitCore test.HidatoTestSuite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ValidatorTest.class,
    SolverTest.class,
    GeneratorTest.class
})
public class HidatoTestSuite {
    // Intentionally empty — suite configuration is in the annotations above.
}
