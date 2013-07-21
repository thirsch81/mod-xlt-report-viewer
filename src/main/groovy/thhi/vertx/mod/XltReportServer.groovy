package thhi.vertx.mod

import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject

public class XltReportServer extends Verticle {

	static XltReportDir reportDir = null

	def start () {

		def hostname = container.config.host
		def port = container.config.port as int
		def xltReportDir = container.config.reportDir

		reportDir = new XltReportDir(xltReportDir)

		createReportServer(hostname, port)
	}

	def createReportServer(hostname, port) {

		RouteMatcher rm = new RouteMatcher()

		rm.get("/") { request ->
			logDebug("Received request ${request.method} ${request.uri}")
			request.response.sendFile("web/index.html")
		}

		rm.get("/list") { request ->
			def result =  []
			reportDir.reports.each { xltReport ->
				result.add(["name": xltReport.name, "sut": xltReport.sut])
			}
			request.response.end(new JsonObject(["reports": result]).toString())
		}

		rm.getWithRegEx(/.*\d{8}-\d{6}\/.*/) { request ->
			logInfo("Received request ${request.method} ${request.uri}")

			def nameMatcher = (request.uri =~ /.*(\d{8}-\d{6}).*/)
			if(nameMatcher.matches()) {
				def name = nameMatcher[0][1]
				if(request.uri =~ /HitsPerSecond.png/) {
					def graphFile =  reportDir.reports.find { name == it.name }.hitsPerSecondChart
					request.response.sendFile(graphFile.path)
				}
			}
		}


		rm.getWithRegEx(".*") { request ->
			logDebug("Received request ${request.method} ${request.uri}")
			request.response.sendFile("web${request.uri}")
		}

		HttpServer server = vertx.createHttpServer()
		server.requestHandler(rm.asClosure())
		server.listen(port, hostname)
	}

	def logInfo(msg, err = null) {
		if(container.logger.infoEnabled) {
			container.logger.info(msg, err)
		}
	}

	def logError(msg, err = null) {
		container.logger.error(msg, err)
	}

	def logDebug(msg, err = null) {
		if(container.logger.debugEnabled) {
			container.logger.debug(msg, err)
		}
	}

	def now = { System.currentTimeMillis() }
}