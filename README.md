# Semantag

Semantag fetches and semantically structures articles from any RSS feeds.

## Contributors

* Romain De Oliveira
* Benjamin Brabant
* Maxime Gautré
* Thibault Goustat
* David Dogneton

## About

Semantag annotates RSS feeds titles and descriptions as DBpedia entities thanks to [DBpedia Spotlight](https://github.com/dbpedia-spotlight/dbpedia-spotlight).

Articles are then semantically linked together by tags, making a strong and smart recommandation engine.

## Installation

**Important** : this has not been tested for a while so it may not work ¯\_(ツ)_/¯

Start by the beginning : 

`git clone git@github.com:david-dogneton/pfe-semantic-news.git`

Then retrieve the dependencies :

 * Get and run Neo4j on default port (**7474**). Semantag is currently using version **2.0.1**. https://neo4j.com/download/other-releases/#releases
 * Get DBpedia Spotlight running at port **http://localhost:2222/** rest https://github.com/dbpedia-spotlight/dbpedia-spotlight
 * Get Play and run the app https://www.playframework.com/. Semantag is currently using version **2.2.1**.

You're all set. Enjoy !
