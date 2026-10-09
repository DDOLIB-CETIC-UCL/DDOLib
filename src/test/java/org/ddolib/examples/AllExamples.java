package org.ddolib.examples;

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/** Test suite running all the example tests (layered and no-layer), except non-regression ones. */
@Suite
@SuiteDisplayName("All Examples")
@SelectPackages({"org.ddolib.examples.layered", "org.ddolib.examples.nolayer"})
@ExcludeTags("non-regression")
public class AllExamples {}
