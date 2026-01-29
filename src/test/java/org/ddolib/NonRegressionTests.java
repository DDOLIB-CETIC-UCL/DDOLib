package org.ddolib;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Non-Regression Tests Only")
@SelectPackages("org.ddolib")
@IncludeTags("non-regression")
public class NonRegressionTests {
}