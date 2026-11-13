package pe.greenminds.ecomind.community.domain.entity

import java.time.Instant

data class CommunityPost(
    val id: Long,
    val communityId: Long,
    val authorId: Long?,
    val content: String,
    val postType: String?,
    val imageUrl: String?,
    val relatedEventId: Long?,
    val likes: Long,
    val createdAt: Instant? = null,
    val authorName: String? = null,
    val authorAvatar: String? = null
)