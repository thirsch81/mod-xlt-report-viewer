package thhi.vertx.domain

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

	public static Map read(File rootDir) {

		def name = rootDir.name
		
		def stats = parseTestreportXml(rootDir)

		// extend this map to include more data
		return [

			name : name,

			indexPage : "${name}/index.html" as String,

			mainLoadGraphPath : "${name}/charts/HitsPerSecond.png" as String,

			startTime : getStartTime(name),

			sut : getSut(rootDir),

			totalActions : stats.actions,
			totalErrors : stats.errors,
			errorRatio :  stats.errorRatio

		] as ReportMap
	}

	// main helper method
	// extend this to include more data
	private static Map parseTestreportXml(File rootDir) {

		def xml = new XmlSlurper().parse(new File(rootDir.path, "testreport.xml"))

		def total = 0
		def errors = 0

		xml.actions.action.each {

			total += it.count.text() as long
			errors += it.errors.text() as long
		}

		return [
			actions: total,
			errors: errors,
			errorRatio : errors / total
		]
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
		return result.size() < 4 ? result.toUpperCase() : result
	}
}