/*
 * Example Groovy integration test that deploys the module that this project builds.
 *
 * Quite often in integration tests you want to deploy the same module for all tests and you don't want tests
 * to start before the module has been deployed.
 *
 * This test demonstrates how to do that.
 */

import static org.vertx.testtools.VertxAssert.*

// And import static the VertxTests script
import org.vertx.groovy.testtools.VertxTests;

// Make sure you initialize
VertxTests.initialize(this)

// Provide configuration for the module
def moduleTestConfig = [
	"server" :[
		"host" : "localhost",
		"port" : 8080,
		"xltReportDir" : "src/test/resources/reports"
	],
	"reader" : [
		"xltReportDir" : "src/test/resources/reports"
	]
]

// Start the module for testing
def moduleName = "thhi.vertx~xlt-report-viewer~0.5.0"
//def moduleName = System.getProperty("vertx.modulename")

container.deployModule(moduleName, moduleTestConfig, { result ->
	assertTrue("${result.cause()}", result.succeeded)
	assertNotNull("DeploymentID should not be null", result.result)

	// If deployed correctly then start the tests!
	VertxTests.startTests(this)
})

// The test methods must being with "test"
def testSomething() {
	container.logger.info("vertx is ${vertx.getClass().getName()}")
	testComplete()
}



