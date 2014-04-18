var app = angular.module('app', []);

app.filter('fromNow', function () {
    return function (date) {
        moment.lang('fr');
        return moment(moment(date, 'YYYY-MM-DD HH:mm').format()).fromNow();
    }
});

function NewsRenderer($scope, $http) {

    $scope.gererCoeur = function($mailUser, $urlArticle) {
        console.log("Dans la méthode gererCoeur: " + $mailUser + " - " + $urlArticle);
        $http.post("/changerCoeur", {mailUser: $mailUser, urlArticle: $urlArticle});
    }

    $scope.news = {
        "items": [
//            {
//                "url": "http://www.leparisien.fr/faits-divers/boeing-disparu-deux-semaines-de-recherches-et-des-theories-en-pagaille-20-03-2014-3689851.php",
//                "titre": "Deux objets peut-être liés au vol MH370 repérés par des satellites ...",
//                "description": "L'Australie a prudemment relancé jeudi l'enquête sur la disparition du vol MH370 de la Malaysia Airlines en annonçant la détection de deux \"objets\" dans le sud de l'océan Indien, après bientôt deux semaines de fausses pistes et d'espoirs déçus. Les deux ...",
//                "site": "Le Parisien",
//                "image": "http://www.leparisien.fr/images/2014/03/20/3689649_befabbe0cc3d9e1275f4cd119506a585337d94af_640x280.jpg",
//                "consultationsJour": 5615,
//                "coeurs": 450,
//                "domaine": "International",
//                "tags": [
//                    "Australie",
//                    "Malaysia Airlines"
//                ],
//                "note": 2.9,
//                "date": "2014-03-23 9:36",
//                "lies": 15
//            },
//            {
//                "url": "http://www.lefigaro.fr/flash-actu/2014/03/20/97001-20140320FILWWW00051-ruquier-aux-commandes-des-grosses-tetes.php",
//                "titre": "Ruquier aux commandes des ''Grosses Têtes''",
//                "description": "L'animateur Laurent Ruquier remplacera Philippe Bouvard, 84 ans, aux commandes de l'émission phare de RTL, les \"Grosses Têtes\", à la rentrée prochaine, a annoncé RTL aujourd'hui dans un communiqué.",
//                "site": "Le Figaro",
//                "image": "data:image/jpeg;base64,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",
//                "consultationsJour": 2123,
//                "coeurs": 106,
//                "domaine": "France",
//                "tags": [
//                    "Laurent Ruquier",
//                    "Philippe Bouvard",
//                    "RTL"
//                ],
//                "note": 3.3,
//                "date": "2014-03-22 8:02",
//                "lies": 18
//            },
//            {
//                "url": "http://www.20minutes.fr/sport/1328606-ligue-des-champions-miracule-le-manchester-united-de-david-moyes-est-maintenant-capable-de-tout",
//                "titre": "Ligue des champions: Miraculé, Manchester United est désormais ...",
//                "description": "FOOTBALL - Les Anglais ont remonté leur handicap contre l'Olympiakos (3-0)… Il respire. Sous le feu des critiques pour la saison excessivement compliquée de Manchester United, David Moyes a gagné quelques jours de répit. Il le doit en grande partie à ...",
//                "site": "20 minutes",
//                "image": "http://cache.20minutes.fr/illustrations/2014/03/20/attaquant-manchester-united-robin-van-persie-18-mars-2014-old-trafford-1536130-616x0.jpg",
//                "consultationsJour": 1589,
//                "coeurs": "72",
//                "domaine": "Sport",
//                "tags": [
//                    "Manchester United",
//                    "Olympiakos",
//                    "David Moyes"
//                ],
//                "note": 3.4,
//                "date": "2014-03-22 11:50",
//                "lies": 9
//            },
//            {
//                "url": "http://www.lexpress.fr/culture/tele/game-of-thrones-george-r-r-martin-reve-d-un-film-pour-clore-la-serie_1501615.html",
//                "titre": "\"Game of Thrones\" : une dernière bande-annonce pour attendre la ...",
//                "description": "Quelles conséquences humaines et politiques auront les noces pourpres sur Westeros ? Pour ceux qui n'ont pas lu les romans épiques de George R.R. Martin, la saison 4 de la série Game of Thrones va apporter la réponse à cette question et à bien d'autres.",
//                "site": "TF1",
//                "image": "http://static.lexpress.fr/medias_9489/w_633,h_275,c_crop,x_0,y_16/w_605,h_270,c_fill,g_north/game-of-thrones-saison-4-2_4858837.jpg",
//                "consultationsJour": 18950,
//                "coeurs": 15658,
//                "domaine": "Séries",
//                "tags": [
//                    "Game of Thrones",
//                    "George R.R. Martin"
//                ],
//                "note": 4.6,
//                "date": "2014-03-23 11:00",
//                "lies": 5
//            },
//            {
//                "url": "http://www.cinemovies.fr/actu/star-wars-7-a-30-ans-de-l-empire-contre-attaque/26904",
//                "titre": "Star Wars VII : à 30 ans de L'Empire contre-attaque !",
//                "description": "Il y a peu d'informations officielles à se mettre sous la dent concernant Star Wars VII, mais en voici une : la compagnie LucasFilm a précisé que l'action de Star Wars VII se déroulait 30 ans après les événements de L'Empire contre-attaque. Star Wars 7 : 30 ...",
//                "site": "CinéMovies",
//                "image": "http://static1.cinemovies.fr/articles/4/26/90/4/@/366755-star-wars-7-sortira-a-l-ete-2015-620x0-9.jpg",
//                "consultationsJour": 156895,
//                "coeurs": 17564,
//                "domaine": "Cinéma",
//                "tags": [
//                    "Star Wars",
//                    "L'Empire contre-attaque"
//                ],
//                "note": 4.05,
//                "date": "2014-03-21 9:05",
//                "lies": 6
//            },
//            {
//                "url": "http://news.google.fr/news/url?sr=1&ct2=fr%2F1_0_s_0_1_a&sa=t&usg=AFQjCNGdceIWwjq7bCbE1idjs0Q4xtmh-w&cid=43982691178888&url=http%3A%2F%2Fwww.liberation.fr%2Fsociete%2F2014%2F03%2F20%2Fallergie-aux-pollens-pres-d-un-tiers-des-adultes-concernes_988521&ei=JccqU8jANoSgiQbMtQE&rt=SECTION&vm=STANDARD&bvm=section&did=-5917910466712704783&sid=fr_fr%3Am&ssid=m",
//                "titre": "Allergie aux pollens : près d'un tiers des adultes concernés",
//                "description": "La fréquence de l'allergie aux pollens varie avec l'âge : elle est plus élevée chez l'adulte jeune que chez les enfants et les personnes âgées. (Photo Philippe Huguen. AFP). Le nombre de personnes touchées par des allergies respiratoires aurait doublé ces ...",
//                "site": "Libération",
//                "image": "http://md1.libe.com/photo/629932--.jpg?modified_at=1395307022&amp;width=750",
//                "consultationsJour": 2250,
//                "coeurs": 28,
//                "domaine": "Santé",
//                "tags": [
//                    "pollens"
//                ],
//                "note": 3.7,
//                "date": "2014-03-23 8:15",
//                "lies": 1
//            }
//            ,
//            {
//                "url": "http://news.google.fr/news/url?sr=1&ct2=fr%2F1_0_s_0_1_a&sa=t&usg=AFQjCNGjcPx1mFHbkA40wwdJsqhPeI68CQ&cid=43982690435978&url=http%3A%2F%2Fwww.bfmtv.com%2Fplanete%2Fun-etrange-dinosaure-a-plumes-aux-allures-poulet-geant-decouvert-736668.html&ei=dscqU6DCHoSgiQbMtQE&rt=SECTION&vm=STANDARD&bvm=section&did=-9153348354648486053&sid=fr_fr%3At&ssid=t",
//                "titre": "Un étrange dinosaure à plumes aux allures de poulet géant découvert",
//                "description": "Des paléontologues sont parvenus à reconstituer un dinosaure très surprenant qui vivait en Amérique il y a 66 millions d'années: à plumes et à bec, il ressemblait un peu à un énorme poulet. A. D. avec AFP Le 20/03/2014 à 11:18. - +. Imprimer cet article.",
//                "site": "BFM",
//                "image": "http://www.bfmtv.com/i/580/290/1092472.jpg",
//                "consultationsJour": 254,
//                "coeurs": 23,
//                "domaine": "Sciences",
//                "tags": [
//                    "Dinosaure",
//                    "Amérique"
//                ],
//                "note": 4.1,
//                "date": "2014-03-23 10:55",
//                "lies": 0
//            }
        ]
    };


    $http.get('/getart').success(function (data) {
        console.log("data : " + data);
        console.dir(data);
        $scope.news.items=data.liste;

    }).error(function (err) {
            console.log("err : " + err);
    });
    $scope.limite = 5;
    $scope.filtrage = [];
    $scope.domaines = {
        "items": [
            ]
    }
    $scope.filtrage['domaine'] = "";

    $http.get('/getDomaines').success(function (data) {
        console.dir(data);
        $scope.domaines.items=data.liste;
    }).error(function (err) {
        console.log("err : " + err);
    });


    $scope.tops = {
        "items": [
        ]
    }

    $http.get('/getTop').success(function (data) {
        console.dir(data);
        $scope.tops.items=data.liste;
    }).error(function (err) {
            console.log("err : " + err);
        });


}