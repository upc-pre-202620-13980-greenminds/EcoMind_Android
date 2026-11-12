package pe.greenminds.ecomind.community.domain.entity

data class CommunityPost(
    val id: Long,
    val communityId: Long,
    val authorId: Long?,
    val content: String,
    val postType: String?,
    val imageUrl: String?,
    val relatedEventId: Long?,
    val likes: Long
)