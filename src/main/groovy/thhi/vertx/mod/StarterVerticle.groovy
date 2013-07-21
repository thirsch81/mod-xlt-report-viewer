package thhi.vertx.mod

import org.vertx.groovy.platform.Verticle

public class StarterVerticle extends Verticle {

	def start() {

		def serverConfig = container.config.server

		container.deployWorkerVerticle("groovy:" + XltReportServer.class.name, serverConfig) { result ->
			if(result.succeeded) {
				container.logger.info("Deployed XltReportServer ${result.result}")
			} else {
				container.logger.info("Deployed XltReportServer ${result.cause()}")
			}
		}
	}
}
