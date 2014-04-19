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
    $scope.currentPeriode="Aujourd'hui";
    $scope.currentType="Tout";
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
            $scope.refresh();
        }
    }

    $scope.changeType = function(idType, nomType) {
        if(idType!=$scope.idType) {
            $scope.idType = idType;
            $scope.currentType=nomType;
            $scope.refresh();
        }
    }

    $scope.refresh = function(){
        $scope.limite = 100;
        var url ="";
        switch ($scope.periode) {
            case 'day':
                $scope.currentPeriode="Aujourd'hui";
                url ='/statsAnnotationsJour/'
                break;
            case 'week':
                $scope.currentPeriode="Cette semaine";
                url ='/statsAnnotationsSemaine/'
                break;
            case 'month':
                $scope.currentPeriode="Ce mois-ci";
                url ='/statsAnnotationsMois/'
                break;
            case 'all':
                $scope.currentPeriode="Depuis le début";
                url ='/statsAnnotations/'
                break;
        }

        $scope.liste.items = [];

        $http.get(url+$scope.idType).success(function (data) {
            console.dir(data);
            $scope.liste.items = data.liste;
        }).error(function (err) {
            console.log("err : " + err);
        });
    }


}