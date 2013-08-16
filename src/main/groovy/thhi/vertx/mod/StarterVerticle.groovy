package thhi.vertx.mod

import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject

public class StarterVerticle extends Verticle {

	def start() {

		def serverConfig = container.config.server
		
		container.deployWorkerVerticle("groovy:" + ReportServerVerticle.class.name, serverConfig) { result ->
			if(result.succeeded) {
				container.logger.info("Deployed ReportServerVerticle ${result.result}")
			} else {
				container.logger.info("Error when deploying ReportServerVerticle: ${result.cause()}")
			}
		}
	}
}
