package models

/**
 * Created by Administrator on 27/03/2014.
 */
case class ResourceDbPedia(uri : String,
                           support : Int,
                           types: String,
                           surfaceForm: String,
                           offset: Int,
                           similarityScore: Double,
                           percentageOfSecondRank : Double) {

}
