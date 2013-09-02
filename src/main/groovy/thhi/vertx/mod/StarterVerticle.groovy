package thhi.vertx.mod

import org.vertx.groovy.core.AsyncResult
import org.vertx.java.core.json.JsonObject
import thhi.vertx.base.GroovyVerticleBase

class StarterVerticle extends GroovyVerticleBase {

	def start() {

		def readerConfig = getMandatoryConfig("reader")
		def serverConfig = getMandatoryConfig("server")
		
		deployGroovyWorkerVerticle(XltReportReaderVerticle.class.name, readerConfig)
		deployGroovyVerticle(XltReportServerVerticle.class.name, serverConfig)
	}
}
