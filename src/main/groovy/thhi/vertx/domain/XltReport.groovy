package thhi.vertx.domain

import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.util.regex.Pattern;

import org.vertx.java.core.shareddata.Shareable;

/**
 * 
 * @author Thomas Hirsch
 *
 */
abstract class XltReport {

	// implement the marker interface
	class ReportMap extends HashMap implements Shareable {
	}

	static final Pattern NAME_PATTERN = ~/^(\d{8}-\d{6}).*/
	static final String DATE_FORMAT = "yyyyMMdd-HHmmss"

	public static Map read(File reportRootDir, serverRoot = "") {

		def name = reportRootDir.name
		def rootPath = serverRoot ?: ""

		// either include more statistics below...
		def statistics = parseTestreportXml(reportRootDir)

		// ...or extend this map to include more data
		return statistics + [

			name : name,

			indexPage : "${rootPath}/${name}/index.html" as String,

			testreportXml : "${rootPath}/${name}/testreport.xml" as String,

			mainLoadGraphPath : "${rootPath}/${name}/charts/HitsPerSecond.png" as String,

			startTime : getStartTime(name),

			sut : getSut(reportRootDir)

		] as ReportMap
	}

	// extend this helper method
	private static Map parseTestreportXml(File rootDir) {

		def xml = new XmlSlurper().parse(new File(rootDir.path, "testreport.xml"))

		def total = 0
		def errors = 0

		xml.actions.action.each {

			total += it.count.text() as long
			errors += it.errors.text() as long
		}

		// parse the otto fragment
		Map otto = [:]
		try {
			xml.otto.childNodes().each {
				if("actionDetails"== it.name()) {
					otto[it.name()] = new JsonSlurper().parseText(it.text().replaceAll(/\"/,/"/))
				} else {
					otto[it.name()] = it.text()
				}
			}
		} catch (Exception e) {
			// ignore
		}

		def result = [
			totalActions : total,
			totalErrors: errors,
			errorRatio : errors / total
		]
		if(otto) {
			result.otto = otto
		}
		return result
	}

	private static Long getStartTime(name) {
		def matcher = NAME_PATTERN.matcher(name)
		if(matcher.matches()) {
			return new SimpleDateFormat(DATE_FORMAT).parse(matcher[0][1]).time
		}
	}

	private static String getSut(File rootDir) {
		def result = "unknown"
		rootDir.eachFileMatch(~/.*json/) {
			result = it.name - ".json"
		}
		return result
	}
}