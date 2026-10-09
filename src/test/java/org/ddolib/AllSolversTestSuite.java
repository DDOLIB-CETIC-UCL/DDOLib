package org.ddolib;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/** Test suite running the tests of all the solvers (ACS, A*, AWA*, DDO and LNS). */
@Suite
@SuiteDisplayName("All solvers tests")
@SelectPackages({
    "org.ddolib.layered.solving.acs.core.solver",
    "org.ddolib.nolayer.solving.acs.core.solver",
    "org.ddolib.layered.solving.astar.core.solver",
    "org.ddolib.nolayer.solving.astar.core.solver",
    "org.ddolib.layered.solving.awastar.core.solver",
    "org.ddolib.nolayer.solving.awastar.core.solver",
    "org.ddolib.layered.solving.ddo.core.solver",
    "org.ddolib.nolayer.solving.ddo.core.solver",
    "org.ddolib.layered.solving.lns.core.solver"
})
public class AllSolversTestSuite {}
