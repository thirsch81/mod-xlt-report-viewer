package thhi.vertx.mod

import org.vertx.groovy.core.AsyncResult
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.core.file.AsyncFile
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

import thhi.vertx.base.VerticleBase

class XltReportReaderVerticle extends VerticleBase {

	File xltReportDir
	
	ConcurrentSharedMap xltReports() {
		getSharedMap("xltReports")
	}

	def start () {

		xltReportDir = new File(getMandatoryConfig("xltReportDir"))

		def actionHandlers = [
			"read": handleRead,
			"update": handleUpdate
		]
		
		registerActionHandlers("xlt-report-reader", actionHandlers)

		readAllReports()
	}

	def handleRead = { Message message ->
		def name = getMandatoryObject("name", message)
		if(name) {
			try {
				newXltReport(new File(xltReportDir, name))
			} catch (Exception e) {
				def errorMsg = "Error when reading XLT report $name"
				replyError(message, errorMsg)
			}
		}
	}

	def handleUpdate = { Message message ->
		try {
			readDirectory(xltReportDir.path, { files ->
				def newReports = getDirectoryList(files)*.name
				def oldReports = xltReports().collect { it.key }
				(oldReports - newReports).each {
					xltReports().remove(it)
				}
				(newReports - oldReports).each {
					newXltReport(new File(xltReportDir, it))
				}
				replyOk(message)
			}, { cause ->
				replyError(message, "Couldn't read XLT report dir", cause)
			})
		} catch (Exception e) {
			def errorMsg = "Error when reading XLT report dir ${xltReportDir.path}" as String
			replyError(message, errorMsg, e)
		}
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

	def newXltReport(File directory) {
		xltReports().put(directory.name, new XltReport(directory))
	}
}