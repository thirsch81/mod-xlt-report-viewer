var app = angular.module("xltReportViewer", []);

app.config(["$routeProvider", function($routeProvider) {
}]);

app.factory("xltReports", function($http, $q) {

	this.getList = function() {
		var defer = $q.defer();
		$http.get("/list").success(function(data, status) {
			defer.resolve(data)
		});
		return defer.promise;
	}

	return this;
});