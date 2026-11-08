package pe.greenminds.ecomind.community.domain.entity

import pe.greenminds.ecomind.community.domain.valueobjects.CommunityType

data class Community(
    val id: Long,
    val name: String,
    val description: String?,
    val type: CommunityType,
    val topic: String?,
    val locality: String?,
    val memberLimit: Int?,
    val iconUrl: String?,
    val createdBy: Long?
)