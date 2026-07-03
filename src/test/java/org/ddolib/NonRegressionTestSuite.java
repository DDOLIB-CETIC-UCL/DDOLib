package org.ddolib;

import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Non-Regression Tests Only")
@Tag("non-regression")
@SelectPackages("org.ddolib")
@IncludeTags("non-regression")
@ExcludeClassNamePatterns(".*TestSuite")
public class NonRegressionTestSuite {
}