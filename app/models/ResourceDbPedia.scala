package models


case class ResourceDbPedia(uri : String,
                           support : Int,
                           types: String,
                           surfaceForm: String,
                           offset: Int,
                           similarityScore: Double,
                           percentageOfSecondRank : Double) {

}
