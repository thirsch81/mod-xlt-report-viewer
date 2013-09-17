import static org.vertx.testtools.VertxAssert.*

import org.vertx.groovy.core.http.HttpClientResponse
import org.vertx.groovy.testtools.VertxTests
import org.vertx.java.core.json.JsonObject


import thhi.vertx.mod.StarterVerticle
import thhi.vertx.mod.XltReportReaderVerticle
import thhi.vertx.mod.XltReportServerVerticle

public abstract class TestConfig {

	static def reader = [
		"xltReportDir" : "src/test/resources/reports"
	]

	static def server =  [
		"host" : "localhost",
		"port" : 8080,
		"xltReportDir" : "src/test/resources/reports"
	]

	static def module = [
		"server" :  server,
		"reader": reader
	]
}

def testDeployXltReportReader() {
	container.deployVerticle("groovy:" + XltReportReaderVerticle.class.name, TestConfig.reader) { result ->
		assertNotNull(result)
		assertTrue("${result.cause()}", result.succeeded)
		testComplete()
	}
}

def testXltReportReaderHandleUnknownAction() {
	container.deployVerticle("groovy:" + XltReportReaderVerticle.class.name, TestConfig.reader) { result ->
		assertNotNull(result)
		assertTrue("${result.cause()}", result.succeeded)
		vertx.eventBus.send("xlt-report-reader", ["action" : "unknown"]) { reply ->
			assertEquals("error", reply.body.status)
			println reply.body
			assertEquals("Unknown action unknown, known actions are [read, update]", reply.body.message.toString())
			testComplete()
		}
	}
}

def testDeployXltReportServer() {

	container.deployVerticle("groovy:" + XltReportServerVerticle.class.name, TestConfig.server) { result ->
		assertNotNull(result)
		assertTrue("${result.cause()}", result.succeeded)
		testComplete()
	}
}

def testDeployStarter() {

	container.deployVerticle("groovy:" + StarterVerticle.class.name, TestConfig.module) { result ->
		assertNotNull(result)
		assertTrue("${result.cause()}", result.succeeded)
		testComplete()
	}
}

VertxTests.initialize(this)
VertxTests.startTests(this)