package thhi.vertx.base

import org.vertx.groovy.core.AsyncResult
import org.vertx.groovy.core.eventbus.Message;
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.Future
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

abstract class VerticleBase extends Verticle {

	protected Map actionHandlers = [:]

	def handleActions = { Message message ->

		def action = getAction(message)
		def knownActions = actionHandlers.keySet()
		if(knownActions.contains(action)) {
			actionHandlers[action](message)
		} else {
			sendUnknownAction(message, knownActions)
		}
	}

	def getAction(Message message) {
		getMandatoryObject("action", message)
	}

	void sendUnknownAction(Message message, expected = []) {
		def error =  "Unknown action ${message.body.action}, known actions are $expected" as String
		replyError(message, error)
	}

	def getMandatoryObject(field, Message message) {
		def val = message.body[field]
		if(!val) {
			replyError(message, "$field must be specified" as String)
		}
		return val
	}

	def getMandatoryConfig(field) {
		def val = container.config.get(field)
		if(!val) {
			throw new IllegalArgumentException("$field must be specified in verticle config")
		}
		return val
	}

	def registerHandler(address, handler) {
		vertx.eventBus.registerHandler(address, handler)
	}

	def readDirectory(String path, Closure successHandler, Closure errorHander = {}) {
		vertx.fileSystem.readDir(path) { AsyncResult ar ->
			if(ar.succeeded) {
				successHandler.call(ar.result)
			} else {
				errorHander.call(ar.cause)
			}
		}
	}

	def deployGroovyWorkerVerticle(name, config, instances = 1) {
		container.deployWorkerVerticle("groovy:" + name, config, instances, handleDeploymentResult.curry(name))
	}

	def deployGroovyVerticle(name, config, instances = 1) {
		container.deployVerticle("groovy:" + name, config, instances, handleDeploymentResult.curry(name))
	}

	def handleDeploymentResult = { String name, Future result ->
		if(result.succeeded) {
			logInfo("Deployed $name ${result.result}")
		} else {
			logError("Error when deploying $name", result.cause())
		}
	}

	ConcurrentSharedMap getSharedMap(name) {
		vertx.sharedData.getMap(name)
	}

	void logInfo(msg, err = null) {
		if(container.logger.infoEnabled) {
			container.logger.info(msg, err)
		}
	}

	void logError(msg, err = null) {
		container.logger.error(msg, err)
	}

	void logDebug(msg, err = null) {
		if(container.logger.debugEnabled) {
			container.logger.debug(msg, err)
		}
	}

	void sendMessage(address, message, Closure statusOkHander, Closure statusErrorHandler = {}) {
		vertx.eventBus.send(address, message) { reply ->
			switch(getMandatoryObject("status", reply)) {
				case "ok":
					statusOkHander.call(reply)
					break
				default:
					statusErrorHandler.call(reply)
			}
		}
	}

	void replyOk(Message message, json = [:]) {
		replyStatus("ok", message, json)
	}

	void replyError(Message message, error, Exception e = null) {
		logError(error, e)
		replyStatus("error", message, ["message": error])
	}

	void replyStatus(status, Message message, Map json = [:]) {
		json.put("status", status)
		logDebug("Sending " + json)
		message.reply(json)
	}

	def now = { System.currentTimeMillis() }
}
