var app = angular.module('app', []);

app.filter('fromNow', function () {
    return function (date) {
        moment.lang('fr');
        return moment(moment(date, 'YYYY-MM-DD HH:mm').format()).fromNow();
    }
});

function StatsRenderer($scope, $http) {
    $scope.limite = 100;
    $scope.periode='day';
    $scope.idType=-1;
    $scope.types = {
        "items": [
        ]
    }
    $scope.liste = {
        "items": [
        ]
    }
    $scope.tops = {
        "items": [
        ]
    }

    $http.get('/getTypes').success(function (data) {
        console.dir(data);
        $scope.types.items=data.liste;
    }).error(function (err) {
        console.log("err : " + err);
    });

    $http.get('/statsAnnotationsJour/'+$scope.idType).success(function (data) {
        console.dir(data);
        $scope.liste.items=data.liste;
    }).error(function (err) {
        console.log("err : " + err);
    });

    $scope.changePeriode = function(periode) {
        if(periode!=$scope.periode) {
            $scope.periode = periode;
            $scope.liste.items = [];
            $scope.refresh();
        }
    }

    $scope.changeType = function(idType) {
        if(idType!=$scope.idType) {
            $scope.idType = idType;
            $scope.liste.items = [];
            $scope.refresh();
        }
    }

    $scope.refresh = function(){
        var url ="";
        switch ($scope.periode) {
            case 'day':
                url ='/statsAnnotationsJour/'
                break;
            case 'week':
                url ='/statsAnnotationsSemaine/'
                break;
            case 'month':
                url ='/statsAnnotationsMois/'
                break;
            case 'all':
                url ='/statsAnnotations/'
                break;
        }

        $http.get(url+$scope.idType).success(function (data) {
            console.dir(data);
            $scope.liste.items = data.liste;
        }).error(function (err) {
            console.log("err : " + err);
        });
    }


}