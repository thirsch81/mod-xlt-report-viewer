function Main($scope, $location, $log, xltReports) {

	$scope.rows = [];

	$scope.startTime = function(report) {
		$log.log(report.startTime);
		return new Date(report.startTime).toLocaleString();
	}

	$scope.globalConfig = {
		isGlobalSearchActivated : true,
		isPaginationEnabled : false
	}

	xltReports.getList().then(function(data) {

		$scope.rows = data.reports
		
		console.log(data.reports instanceof Array)
		console.log(data.reports[0].name);
		
		console.log($scope.rows instanceof Array)
	});

}