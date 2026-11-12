package pe.greenminds.ecomind.community.domain.repositories

import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.entity.CommunityMembership
import pe.greenminds.ecomind.community.domain.entity.NewTopicCommunity

interface CommunityDirectoryRepository {
    suspend fun getMyMemberships(): Result<List<CommunityMembership>>
    suspend fun getLocalCommunities(locality: String?): Result<List<Community>>
    suspend fun getTopicCommunities(): Result<List<Community>>
    suspend fun joinCommunity(communityId: Long): Result<CommunityMembership>
    suspend fun leaveCommunity(communityId: Long): Result<Unit>
    suspend fun createTopicCommunity(community: NewTopicCommunity): Result<Community>
}