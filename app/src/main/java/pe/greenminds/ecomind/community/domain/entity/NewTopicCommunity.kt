package pe.greenminds.ecomind.community.domain.entity

data class NewTopicCommunity(
    val name: String,
    val description: String?,
    val topic: String,
    val memberLimit: Int,
    val iconUrl: String?
)