package thhi.vertx.mod

class XltReport {

	File rootDir

	String name
	String sut

	public XltReport(File rootDir) {
		this.rootDir = rootDir
		this.name = rootDir.name
		this.sut = getSut(rootDir)
		this.hitsPerSecondChart = getHitsPerSecondChart(rootDir)
	}

	String getSut(File rootDir) {
		def result = ""
		rootDir.eachFileMatch(~/.*json/) {
			result = it.name - ".json"
		}
		return result.toUpperCase()
	}
}
