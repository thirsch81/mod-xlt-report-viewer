package thhi.vertx.mod

class XltReportDir {

	File rootDir
	List<XltReport> reports

	public XltReportDir(String rootDirName) {
		this.rootDir = new File(rootDirName)
		this.reports = getReports(this.rootDir)
	}

	List<XltReport> getReports(File rootDir) {
		List<XltReport> reports = []
		rootDir.eachDirMatch( ~/^\d{8}-\d{6}$/) { reports.add(new XltReport(it)) }
		return reports
	}
}
