package pe.greenminds.ecomind.quests.domain.valueobject

data class QuestReward (
    val gems: Int,
    val ecopoints: Int
){
    init {
        require(gems >= 0){
            "Gems cannot be negative."
        }
        require(ecopoints >=0){
            "Ecopoints cannot be negative"
        }
    }
}