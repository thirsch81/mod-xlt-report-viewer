package thhi.vertx.mod

import org.vertx.groovy.core.AsyncResult
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.core.file.AsyncFile
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

import thhi.vertx.base.GroovyVerticleBase
import thhi.vertx.domain.XltReport;

class XltReportReaderVerticle extends GroovyVerticleBase {

	File xltReportDir

	Set getSharedReports() {
		getSharedSet("xltReports")
	}

	def start () {

		xltReportDir = new File(getMandatoryConfig("xltReportDir"))

		def actionHandlers = [
			"read": handleRead,
			"update": handleUpdate,
		]

		registerActionHandlers("xlt-report-reader", actionHandlers)

		readAllReports()
	}

	def handleRead = { Message message ->

		def name = getMandatoryObject("name", message)
		if(name) {
			readXltReport(name, message)
		}
	}

	def handleUpdate = { Message message ->

		readDirectory(xltReportDir.path, { files ->

			def newReports = getDirectoryList(files)*.name
			def oldReports = getSharedReports().collect { it.name }

			def reportsToRemove = getSharedReports().findAll { (oldReports - newReports).contains(it.name) }
			if(reportsToRemove) {
				logDebug("Removing reports ${reportsToRemove*.name}")
				getSharedReports().removeAll(reportsToRemove)
			}

			(newReports - oldReports).each {
				logDebug("Adding new report $it")
				readXltReport(it)
			}

			replyOk(message)
		}, { cause ->
			replyError(message, "Couldn't read XLT report dir", cause)
		})
	}

	def getDirectoryList(files) {
		files.collect { new File(it) }.findAll { it.isDirectory() }
	}

	def readAllReports() {
		readDirectory(xltReportDir.path, { files ->
			getDirectoryList(files).each { File directory ->
				newXltReport(directory)
			}
		}, { cause ->
			logError("Couldn't read XLT report dir", cause)
		})
	}

	def readXltReport(name, message = null) {
		try {
			newXltReport(new File(xltReportDir, name))
			if(message) {
				replyOk(message, getSharedReports().find { it.name == name })
			}
		} catch (Exception e) {
			def errorMsg = "Error when reading XLT report dir ${name}" as String
			if(message) {
				replyError(message, errorMsg, e)
			} else {
				logError(errorMsg, e)
			}
		}
	}

	def newXltReport(File directory) {
		try {
			def present = getSharedReports().find { directory.name == it.name }
			if(present) {
				getSharedReports().remove(present)
			}
			def serverRootPath = "/${xltReportDir.name}"
			getSharedReports().add(XltReport.read(directory, serverRootPath))
		} catch(Exception e) {
			logError("Error when processing XLT report dir ${directory.name}" as String, e)
		}
	}
}