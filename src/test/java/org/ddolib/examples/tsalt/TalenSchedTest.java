package org.ddolib.examples.tsalt;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.lang.model.type.NullType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TalenSchedTest {

    @DisplayName("Talent Scheduling")
    @TestFactory
    public Stream<DynamicTest> testTS() {
        var dataSupplier =
                new TalenSchedTestDataSupplier(Path.of("src", "test", "resources",
                        "TalentScheduling"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }

}
