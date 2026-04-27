package org.ddolib;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All solvers tests")
@SelectPackages({
        "org.ddolib.acs.core.solver",
        "org.ddolib.astar.core.solver",
        "org.ddolib.awastar.core.solver",
        "org.ddolib.ddo.core.solver",
        "org.ddolib.lns.core.solver"
})
public class AllSolversTests {
}
