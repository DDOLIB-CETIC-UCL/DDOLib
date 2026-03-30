package org.ddolib.examples;

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All Examples")
@SelectPackages("org.ddolib.examples")
@ExcludeTags("non-regression")
public class AllExamples {
}
