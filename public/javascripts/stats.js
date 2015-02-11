var app = angular.module('app', ['infinite-scroll']);

app.filter('fromNow', function () {
    return function (date) {
        moment.lang('fr');
        return moment(moment(date, 'YYYY-MM-DD HH:mm').format()).fromNow();
    }
});

function StatsRenderer($scope, $http) {
    $scope.limite=100;
    $scope.periode='day';
    $scope.currentPeriode="Aujourd'hui";
    $scope.idType=-1;
    $scope.currentType="Tout";
    $scope.variable="apparitions";
    $scope.currentVariable="apparitions";
    $scope.currentPic="";
    $scope.currentName="";
    $scope.currentLikes="";
    $scope.currentId=0;
    $scope.currentEntityType="";
    $scope.showPic=false;
    $scope.lockPic=false;
    $scope.loading=false;

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


    $scope.loadMore = function (number) {
       $scope.loading=true;
        $scope.limite=$scope.limite+number;
        $scope.apply();
        $scope.loading=false;
    }

    $http.get('/getTypes').success(function (data) {
        var item = [], results = [] ;
        for (var i = data.liste.length - 1; i >= 0; i--) {
            item = data.liste[i];
            item.nom = item.nom.replace("DBpedia:", "");
            item.nom = item.nom.replace("Schema:", "");
            if(item.nom.indexOf('Http')==-1){
                results.push({id: item.id, nom: item.nom });
            }
        }

        for (var i =  results.length - 1; i >= 0; i--) {
          if(!$scope.types.items.indexOf(results[i])>-1){
              $scope.types.items.push(results[i]);
          }
        }
    }).error(function (err) {
            console.log("err : " + err);
        });

    $http.get('/statsAnnotationsJour/'+$scope.idType).success(function (data) {
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

    $scope.changeVariable = function(variable) {
        if(variable!=$scope.variable) {
            $scope.variable = variable;
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
        $scope.limite = 1000;
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

    $scope.hidePicture = function(){
        if(!$scope.lockPic){
            $scope.showPic=false;
        }
    }

    $scope.getPicture = function($id, $url){

        console.log("url : " +$url);
        $scope.showPic=true;
        $scope.lockPic=false;
        $scope.currentPic="wait";
        $scope.currentId=$id;

            $http.post("/getEntityInfos",{url: $url}).success(function (data) {
                console.log(" OK :"+data);
                $scope.currentPic=data.url;
                $scope.currentName=data.name;
                $scope.currentEntityType=data.type;
                $scope.currentLikes=data.likes;
                return $scope.currentPic;
            }).error(function (err) {
                    console.log("err : " + err);
                });

    }

}