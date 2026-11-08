package pe.greenminds.ecomind.community.domain.repositories

import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.entity.CommunityMembership

interface CommunityDirectoryRepository {
    suspend fun getMyMemberships(): Result<List<CommunityMembership>>
    suspend fun getLocalCommunities(locality: String?): Result<List<Community>>
    suspend fun joinCommunity(communityId: Long): Result<CommunityMembership>
}