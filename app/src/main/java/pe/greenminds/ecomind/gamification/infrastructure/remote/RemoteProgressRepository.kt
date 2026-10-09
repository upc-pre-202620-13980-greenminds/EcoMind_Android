package pe.greenminds.ecomind.gamification.infrastructure.remote

import pe.greenminds.ecomind.gamification.domain.model.*
import pe.greenminds.ecomind.gamification.domain.repositories.ProgressRepository
import pe.greenminds.ecomind.gamification.domain.repositories.ShareRequestStore
import pe.greenminds.ecomind.gamification.domain.repositories.ShareRequest
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import retrofit2.HttpException
import javax.inject.Inject

class RemoteProgressRepository @Inject constructor(private val api: GamificationApi, private val access: RemoteAccess,
    private val shares: ShareRequestStore) : ProgressRepository {
    override val isSimulated = false
    override suspend fun getOverview() = access.authenticated { session, token ->
        val p = api.progress(token)
        check(p.userId == session.accountId) { "Unexpected progress owner" }
        val families = api.familyMembership(token, session.accountId).filter { it.userId == session.accountId }.distinctBy { it.familyId }
        val memberships = api.memberships(token).filter { it.user_id == session.accountId }.map { it.community_id }.toSet()
        val communities = api.communities(token).filter { it.id in memberships }
        val familyPoints = families.singleOrNull()?.let { api.familyScore(token, it.familyId).also { score ->
            check(score.familyId == it.familyId)
        }.totalEcopoints }
        GamificationOverview(UserProgress(p.userId, p.totalEcopoints, p.currentStreak, p.longestStreak, p.lastActivityDate, p.lastProtectedDate),
            families.map { AchievementGroup(it.familyId, "", "FAMILY") } + communities.map { AchievementGroup(it.id, it.name, "COMMUNITY") }, familyPoints, false)
    }
    override suspend fun getHistory(fromMillis: Long, toMillis: Long) = access.authenticated { session, token ->
        require(fromMillis < toMillis)
        collectPages { api.history(token, millisToInstant(fromMillis), millisToInstant(toMillis), it) }.map {
            check(it.beneficiary.type == "USER" && it.beneficiary.id == session.accountId) { "Unexpected reward owner" }
            RewardHistory(it.id, it.source.type, it.grantedReward.ecopoints, it.grantedReward.gems, it.occurredAt)
        }.distinctBy { it.id }
    }
    override suspend fun getGroupAchievements(group: AchievementGroup) = access.authenticated { _, token ->
        require(group.id > 0 && group.scope in setOf("FAMILY", "COMMUNITY"))
        val scopePath = if (group.scope == "FAMILY") "families" else "communities"
        val awards = collectArrayPages { api.groupAwards(token, scopePath, group.id, it) }.map { it.toDomain() }
        check(awards.all { it.scope == group.scope && (if (group.scope == "COMMUNITY") it.communityId == group.id && it.beneficiaryId == null else it.beneficiaryId == group.id) }) { "Unexpected achievement owner" }
        val definitions = collectArrayPages { api.catalog(token, it) }.map { it.toDomain() }.associateBy { it.id }
        AchievementCollection(awards.distinctBy { it.achievementId }.map { award ->
            AchievementEntry(checkNotNull(definitions[award.achievementId]), award)
        }, false)
    }
    override suspend fun getShare(awardId: String) = access.authenticated { session, token ->
        shares.read(session.accountId, awardId)?.let { request ->
            try { api.shareStatus(token, request.requestId).checked(session, request) }
            catch (e: HttpException) {
                if (e.code() != 404) throw e
                AchievementShare(request.requestId, awardId, request.communityId, "UNSENT", null)
            }
        }
    }
    override suspend fun share(awardId: String, communityId: Long) = access.authenticated { session, token ->
        require(communityId > 0)
        val request = shares.getOrCreate(session.accountId, awardId, communityId)
        api.share(token, ShareBody(request.requestId, request.awardId, request.communityId)).checked(session, request)
    }
    private fun ShareDto.checked(session: Session, request: ShareRequest): AchievementShare {
        check(requestedBy == session.accountId && requestId == request.requestId && awardId == request.awardId && communityId == request.communityId)
        check(status == "PENDING" || status == "PUBLISHED")
        check(status != "PUBLISHED" || publicationId != null)
        return AchievementShare(requestId, awardId, communityId, status, publicationId)
    }
}
