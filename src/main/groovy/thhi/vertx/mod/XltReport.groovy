package thhi.vertx.mod

import java.util.regex.Pattern;

class XltReport implements Comparable<XltReport> {

	static final Pattern NAME_PATTERN = ~/^(\d{8}-\d{6}).*/
	static final DATE_FORMAT = "yyyyMMdd-HHmmss"

	File rootDir
	String indexPage

	String name
	long testStart
	String sut
	String mainLoadGraphPath
	long totalActions
	long totalErrors

	public XltReport(File rootDir) {
		this.rootDir = rootDir
		this.name = rootDir.name
		this.indexPage = "${name}/index.html"
		this.mainLoadGraphPath = "${name}/charts/HitsPerSecond.png"
		this.testStart = getStartTime(name)
		this.sut = getSut(rootDir)
		def stats = getStatistics()
		this.totalActions = stats.actions
		this.totalErrors = stats.errors
	}

	int compareTo(XltReport other) {
		this.testStart <=> other.testStart
	}

	long getStartTime(name) {
		def matcher = NAME_PATTERN.matcher(name)
		if(matcher.matches()) {
			return new Date().parse(DATE_FORMAT, matcher[0][1]).time
		}
	}

	String getSut(File rootDir) {
		def result = ""
		rootDir.eachFileMatch(~/.*json/) {
			result = it.name - ".json"
		}
		result.size() < 4 ? result.toUpperCase() : result
	}

	Map getStatistics() {
		def xml = new XmlSlurper().parse(new File(rootDir.path, "testreport.xml"))
		def total = 0
		def errors = 0
		xml.actions.action.each {
			total += it.count.text() as long
			errors += it.errors.text() as long
		}
		[actions: total, errors: errors]
	}
}