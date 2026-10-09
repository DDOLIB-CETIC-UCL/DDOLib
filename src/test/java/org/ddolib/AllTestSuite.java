package org.ddolib;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/** Test suite running all the tests of the library, except the non-regression ones. */
@Suite
@SuiteDisplayName("All tests")
@SelectPackages("org.ddolib")
@ExcludeTags("non-regression")
@ExcludeClassNamePatterns(".*TestsSuite")
public class AllTestSuite {}
