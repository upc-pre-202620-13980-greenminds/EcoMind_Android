package pe.greenminds.ecomind.community.domain.entity

import pe.greenminds.ecomind.community.domain.valueobjects.PostReactionType

data class PostReactions(
    val counts: Map<PostReactionType, Int>,
    val ownReaction: PostReactionType?
) {
    val total: Long get() = counts.values.sumOf { it.toLong() }
}