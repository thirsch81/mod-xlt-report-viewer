function Main($scope, $location, $log, xltReports) {

	$scope.rowCollection = [];

	$scope.columnCollection = [ {
		label : "Report",
		map : "name",
		cellTemplateUrl : "/reportLink.html"
	}, {
		label : "Start Time",
		map : "startTime",
		formatFunction : function(value, formatParameter) {
			return new Date(value).toLocaleString();
		}
	}, {
		label : "SUT",
		map : "sut",
		formatFunction : function(value, formatParameter) {
			return value.length < 5 ? value.toUpperCase() : value;
		}
	}, {
		label : "Main Load Graph",
		map : "mainLoadGraphPath",
		cellTemplateUrl : "/loadGraph.html"
	}, {
		label : "TotalActions",
		map : "totalActions"
	}, {

		label : "Errors",
		map : "totalErrors",
		cellClass : "error-count"
	}, {
		label : "Error Ratio",
		map : "errorRatio",
		cellClass : "error-count",
		formatFunction : function(value, formatParameter) {
			return (value * 100).toFixed(2) + "%";
		}
	} ];

	$scope.tableConfig = {
		isGlobalSearchActivated : true,
		isPaginationEnabled : false
	}

	xltReports.getList().then(function(reports) {

		$scope.rowCollection = reports
	});

}