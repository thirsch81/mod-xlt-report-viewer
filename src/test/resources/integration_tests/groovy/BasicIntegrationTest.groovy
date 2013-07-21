import static org.vertx.testtools.VertxAssert.*

import org.vertx.groovy.core.http.HttpClientResponse
import org.vertx.groovy.testtools.VertxTests

import thhi.vertx.mods.StarterVerticle;

// import thhi.vertx.mods.Starter

// The test methods must being with "test"

import thhi.vertx.mods.StarterVerticle

def testStart() {
	container.deployVerticle("groovy:" + StarterVerticle.class.name) { result ->
		assertNotNull(result)
		assertTrue("${result.cause()}", result.succeeded)
		vertx.createHttpClient().setHost("localhost").setPort(8080).getNow("/") { HttpClientResponse resp ->
			assertEquals(200, resp.statusCode)
			resp.bodyHandler {
				container.logger.info(it)
				testComplete()
			}
		}
	}
}

VertxTests.initialize(this)
VertxTests.startTests(this)