package org.ddolib;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All solvers tests")
@SelectPackages({
        "org.ddolib.solving.acs.core.solver",
        "org.ddolib.solving.astar.core.solver",
        "org.ddolib.solving.awastar.core.solver",
        "org.ddolib.solving.ddo.core.solver",
        "org.ddolib.solving.lns.core.solver"
})
public class AllSolversTestSuite {
}
