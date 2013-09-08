function Main($scope, $location, $log, xltReports) {

	$scope.rowCollection = [];
	$scope.columnCollection = [ {
		label : "Report",
		map : "name", 
		cellTemplateUrl: "/reportLink.html"
	}, {
		label : "Start Time",
		map : "startTime",
		formatFunction : function(value, formatParameter) {
			return new Date(value).toLocaleString();
		}
	}, {
		label : "TotalActions",
		map : "totalActions"
	}, {
		label : "SUT",
		map : "sut"
	}, {
		label : "Main Load Graph",
		map : "mainLoadGraphPath",
		cellTemplateUrl: "/loadGraph.html"
	}, {
		
		
		label : "Errors",
		map : "totalErrors", 
		cellClass : "error-count" 
	}, {
		label : "Error Ratio",
		map : "errorRatio", 
		cellClass : "error-count"
	} ];

	$scope.tableConfig = {
		isGlobalSearchActivated : true,
		isPaginationEnabled : false
	}

	xltReports.getList().then(function(reports) {

		$scope.rowCollection = reports
	});

}