package thhi.vertx.mod

import java.util.regex.Pattern

class XltReportDir {

	static final EARLIEST = new Date(0).time
	static final LATEST = new Date(Long.MAX_VALUE).time

	File rootDir
	SortedSet reports = [] as SortedSet

	public XltReportDir(String rootDirName) {
		this.rootDir = new File(rootDirName)
	}

	void addReports(after = EARLIEST, before = LATEST) {
		rootDir.eachDirMatch(XltReport.NAME_PATTERN) { dir ->
			if( !(reports.find { it.name == dir.name && it.startTime in [after..before]})) {
				def xltReport = new XltReport(dir)
				reports.add(xltReport)
			}
		}
	}
}
