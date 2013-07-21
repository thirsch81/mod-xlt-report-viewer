function Main($scope, $location, $log, xltReports) {

	$scope.headers = ["Name", "SUT", "Main Load Graph"]
	$scope.xltReports = [];

	xltReports.getList().then(function(data) {
		$log.log(data)
		
		var reports = data.reports;
		$scope.xltReports = reports;
	});
	
	
}