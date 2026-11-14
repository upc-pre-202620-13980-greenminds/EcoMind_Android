package pe.greenminds.ecomind.community.infrastructure.mapper

import java.time.Instant
import pe.greenminds.ecomind.community.domain.entity.CommunityPost
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityPostDto

fun CommunityPostDto.toDomain() = CommunityPost(
    id = id,
    communityId = communityId,
    authorId = authorId,
    content = content,
    postType = postType,
    imageUrl = imageUrl,
    relatedEventId = relatedEventId,
    likes = likes,
    createdAt = createdAt?.let {
        runCatching { Instant.parse(it) }.getOrNull()
    }
)