package org.ddolib;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("All tests")
@SelectPackages("org.ddolib")
@ExcludeTags("non-regression")
@ExcludeClassNamePatterns(".*TestsSuite")
public class AllTestSuite {
}
