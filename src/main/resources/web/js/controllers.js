function Main($scope, $location, $log, xltReports) {

	$scope.xltReports = [];

	$scope.startTime = function(report) {
		$log.log(report.startTime);
		return new Date(report.startTime).toLocaleString();
	}

	xltReports.getList().then(function(data) {

		$log.log(data);

		$scope.xltReports = data.reports;
	});

}