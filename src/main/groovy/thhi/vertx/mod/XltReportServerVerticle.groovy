package thhi.vertx.mod

import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.HttpServerRequest;
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.java.core.json.JsonArray
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

import thhi.vertx.base.GroovyVerticleBase

class XltReportServerVerticle extends GroovyVerticleBase {

	File xltReportDir
	String hostname
	Integer port

	Set getSharedReports() {
		getSharedSet("xltReports")
	}

	def start () {

		xltReportDir = new File(getMandatoryConfig("xltReportDir"))
		hostname = getMandatoryConfig("host")
		port = getMandatoryConfig("port") as Integer

		createWebServer()
	}

	def createWebServer() {

		RouteMatcher rm = new RouteMatcher()

		// index page
		rm.get("/") { HttpServerRequest request ->
			logDebug "Received request ${request.method} ${request.uri}"
			request.response.sendFile "web/index.html"
		}

		// line break separated list of reports
		rm.get("/list") { HttpServerRequest request ->
			logDebug "Received request ${request.method} ${request.uri}"
			request.response.end(getSharedReports().collect { it.name }.sort().join("\n"))
		}

		// update XLT directory
		rm.post("/reports/update") { HttpServerRequest request ->
			logDebug "Received request ${request.method} ${request.uri}"
			sendMessage("xlt-report-reader", ["action": "update"], { success ->
				request.response.end(success.body.toString())
			}, { error ->
				request.response
						.setStatusCode(500)
						.end(error.body.message.toString())
			})
		}

		// JSON object containing all reports
		rm.get("/reports/list") { HttpServerRequest request ->
			logDebug("Received request ${request.method} ${request.uri}")
			def response = new JsonObject().putArray("reports", new JsonArray(getSharedReports() as List)).encode()
			request.response.end(response)
		}

		// read a single XLT reports
		rm.getWithRegEx(/\/reports\/read/) { HttpServerRequest request ->
			logDebug("Received request ${request.method} ${request.uri}")
			sendMessage("xlt-report-reader", ["action": "read", name: request.params.name], { success ->
				request.response.end(success.body.toString())
			}, { error ->
				request.response
						.setStatusCode(500)
						.end(error.body.message.toString())
			})
		}

		// serve actual XLT reports
		rm.getWithRegEx(/\/reports\/\d{8}-\d{6}.*/) { HttpServerRequest request ->
			logDebug("Received request ${request.method} ${request.uri}")

			// use parent file, since /reports is part of request URI
			def path = xltReportDir.parentFile.path + request.uri

			// cache stuff
			request.response.putHeader("Cache-Control", "public")
			request.response.sendFile(URLDecoder.decode(path, "UTF-8"))
		}

		// everything else from web directory
		rm.getWithRegEx(".*") { request ->
			logDebug("Received request ${request.method} ${request.uri}")
			request.response.sendFile("web${request.uri}")
		}

		HttpServer server = vertx.createHttpServer()
		server.requestHandler(rm.asClosure())
		server.listen(port, hostname)
	}
}