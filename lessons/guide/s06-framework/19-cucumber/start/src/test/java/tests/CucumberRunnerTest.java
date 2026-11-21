// Runs the feature files with TestNG, so Cucumber scenarios join the same Maven run, listeners and reports.
// Run:  mvn test -Dtest=CucumberRunnerTest -Denv=docker   (or campus)
package tests;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

// ---
// AbstractTestNGCucumberTests turns every scenario into one TestNG test (a data provider row).
// features: where the .feature files are; glue: the package with the step definitions;
// plugin: "pretty" prints each step, and an HTML report goes to target/cucumber.html.
