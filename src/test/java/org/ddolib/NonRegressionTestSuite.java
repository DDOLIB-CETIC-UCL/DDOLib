package org.ddolib;

import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/** Test suite running only the tests tagged as {@code non-regression}. */
@Suite
@SuiteDisplayName("Non-Regression Tests Only")
@Tag("non-regression")
@SelectPackages("org.ddolib")
@IncludeTags("non-regression")
@ExcludeClassNamePatterns(".*TestSuite")
public class NonRegressionTestSuite {}
