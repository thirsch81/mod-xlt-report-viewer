package thhi.vertx.mod

import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject

public class ReportServerVerticle extends Verticle {

	static XltReportDir reportDir = null

	def start () {

		def hostname = container.config.host
		def port = container.config.port as int
		def xltReportDir = container.config.reportDir

		reportDir = new XltReportDir(xltReportDir)
		reportDir.addReports()

		createWebServer(hostname, port)
	}

	def createWebServer(hostname, port) {

		RouteMatcher rm = new RouteMatcher()

		rm.get("/") { request ->
			logDebug("Received request ${request.method} ${request.uri}")
			request.response.sendFile("web/index.html")
		}

		rm.get("/reports/list") { request ->
			logDebug("Received request ${request.method} ${request.uri}")
			def result = reportDir.reports.collect { it.asMap() }
			request.response.end(new JsonObject(["reports": result]).toString())
		}

		rm.getWithRegEx(/\/reports\/\d{8}-\d{6}.*/) { request ->
			logDebug("Received request ${request.method} ${request.uri}")
			def root = reportDir.rootDir.path - (File.separator + "reports")
			def path = root + request.uri
			if(new File(path).isFile()) {
				request.response.sendFile(path)
			} else {
				request.response.end("Ressource not found")
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