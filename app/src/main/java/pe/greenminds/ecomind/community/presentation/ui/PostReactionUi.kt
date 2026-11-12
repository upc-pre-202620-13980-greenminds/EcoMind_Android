package pe.greenminds.ecomind.community.presentation.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.valueobjects.PostReactionType

@DrawableRes
internal fun PostReactionType.imageRes(): Int = when (this) {
    PostReactionType.LIKE -> R.drawable.like
    PostReactionType.LOVE -> R.drawable.love
    PostReactionType.FUNNY -> R.drawable.funny
    PostReactionType.SURPRISE -> R.drawable.surprise
    PostReactionType.SAD -> R.drawable.sad
    PostReactionType.ANGRY -> R.drawable.angry
}

@StringRes
internal fun PostReactionType.labelRes(): Int = when (this) {
    PostReactionType.LIKE -> R.string.news_reaction_like
    PostReactionType.LOVE -> R.string.news_reaction_love
    PostReactionType.FUNNY -> R.string.news_reaction_funny
    PostReactionType.SURPRISE -> R.string.news_reaction_surprise
    PostReactionType.SAD -> R.string.news_reaction_sad
    PostReactionType.ANGRY -> R.string.news_reaction_angry
}