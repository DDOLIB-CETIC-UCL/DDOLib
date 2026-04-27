package org.ddolib;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("All tests")
@SelectPackages("org.ddolib")
@ExcludeTags("non-regression")
@ExcludeClassNamePatterns({".*NonRegressionTests", ".*AllSolversTests"})
public class AllTests {
}
