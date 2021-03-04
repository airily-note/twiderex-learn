/*
 *  Twidere X
 *
 *  Copyright (C) 2020-2021 Tlaster <tlaster@outlook.com>
 * 
 *  This file is part of Twidere X.
 * 
 *  Twidere X is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Twidere X is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Twidere X. If not, see <http://www.gnu.org/licenses/>.
 */
package com.twidere.services.twitter

import com.twidere.services.http.Errors
import com.twidere.services.http.MicroBlogHttpException
import com.twidere.services.http.authorization.OAuth1Authorization
import com.twidere.services.http.retrofit
import com.twidere.services.microblog.LookupService
import com.twidere.services.microblog.MicroBlogService
import com.twidere.services.microblog.RelationshipService
import com.twidere.services.microblog.SearchService
import com.twidere.services.microblog.StatusService
import com.twidere.services.microblog.TimelineService
import com.twidere.services.microblog.model.IRelationship
import com.twidere.services.microblog.model.IStatus
import com.twidere.services.microblog.model.IUser
import com.twidere.services.microblog.model.Relationship
import com.twidere.services.twitter.api.TwitterResources
import com.twidere.services.twitter.api.UploadResources
import com.twidere.services.twitter.model.StatusV2
import com.twidere.services.twitter.model.TwitterPaging
import com.twidere.services.twitter.model.TwitterSearchResponseV1
import com.twidere.services.twitter.model.TwitterSearchResponseV2
import com.twidere.services.twitter.model.User
import com.twidere.services.twitter.model.UserV2
import com.twidere.services.twitter.model.exceptions.TwitterApiException
import com.twidere.services.twitter.model.exceptions.TwitterApiExceptionV2
import com.twidere.services.twitter.model.fields.Expansions
import com.twidere.services.twitter.model.fields.MediaFields
import com.twidere.services.twitter.model.fields.PlaceFields
import com.twidere.services.twitter.model.fields.PollFields
import com.twidere.services.twitter.model.fields.TweetFields
import com.twidere.services.twitter.model.fields.UserFields
import com.twidere.services.utils.Base64
import com.twidere.services.utils.copyToInLength
import com.twidere.services.utils.decodeJson
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal const val TWITTER_BASE_URL = "https://api.twitter.com/"
internal const val UPLOAD_TWITTER_BASE_URL = "https://upload.twitter.com/"

class TwitterService(
    private val consumer_key: String,
    private val consumer_secret: String,
    private val access_token: String,
    private val access_token_secret: String,
) : MicroBlogService,
    TimelineService,
    LookupService,
    RelationshipService,
    SearchService,
    StatusService {
    private val resources by lazy {
        retrofit<TwitterResources>(
            TWITTER_BASE_URL,
            OAuth1Authorization(
                consumer_key,
                consumer_secret,
                access_token,
                access_token_secret,
            ),
            { chain ->
                val response = chain.proceed(chain.request())
                if (response.code != 200) {
                    response.body?.string()?.takeIf {
                        it.isNotEmpty()
                    }?.let { content ->
                        content.decodeJson<TwitterApiException>().takeIf {
                            it.microBlogErrorMessage != null
                        }.let {
                            it ?: run {
                                content.decodeJson<TwitterApiExceptionV2>()
                            }
                        }.let {
                            throw it
                        }
                    } ?: run {
                        throw MicroBlogHttpException(httpCode = response.code)
                    }
                }
                response
            }
        )
    }
    private val uploadResources by lazy {
        retrofit<UploadResources>(
            UPLOAD_TWITTER_BASE_URL,
            OAuth1Authorization(
                consumer_key,
                consumer_secret,
                access_token,
                access_token_secret,
            ),
        )
    }

    override suspend fun homeTimeline(
        count: Int,
        since_id: String?,
        max_id: String?,
    ) = resources.homeTimeline(
        count,
        since_id,
        max_id,
        trim_user = false,
        exclude_replies = false,
        include_entities = true,
    )

    override suspend fun mentionsTimeline(
        count: Int,
        since_id: String?,
        max_id: String?
    ) = resources.mentionsTimeline(
        count,
        since_id,
        max_id,
        trim_user = false,
        exclude_replies = false,
        include_entities = true,
    )

    override suspend fun userTimeline(
        user_id: String,
        count: Int,
        since_id: String?,
        max_id: String?,
        exclude_replies: Boolean,
    ) = resources.userTimeline(
        user_id = user_id,
        count = count,
        since_id = since_id,
        max_id = max_id,
        trim_user = false,
        exclude_replies = exclude_replies,
        include_entities = true,
    )

    override suspend fun favorites(
        user_id: String,
        count: Int,
        since_id: String?,
        max_id: String?
    ) =
        resources.favoritesList(
            user_id = user_id,
            count = count,
            since_id = since_id,
            max_id = max_id,
            include_entities = true,
        )

    override suspend fun lookupUserByName(
        name: String
    ): UserV2 {
        val user = resources.lookupUserByName(
            name,
            tweetFields = TweetFields.values().joinToString(",") {
                it.value
            },
            userFields = UserFields.values().joinToString(",") {
                it.value
            }
        )
        if (user.data == null) {
            if (user.errors != null && user.errors.any()) {
                throw TwitterApiException(
                    errors = user.errors.map {
                        Errors(
                            code = null,
                            message = null,
                            detail = it.detail,
                            title = it.title,
                            resource_type = it.resourceType,
                            parameter = it.parameter,
                            value = it.value,
                            type = it.type,
                        )
                    }
                )
            } else {
                // Shouldn't happen?
                throw Exception()
            }
        }
        user.data.profileBanner = runCatching {
            resources.profileBanners(name)
        }.getOrNull()
        return user.data
    }

    override suspend fun lookupUsersByName(name: List<String>): List<IUser> {
        return resources.lookupUsersByName(
            names = name.joinToString(","),
            tweetFields = TweetFields.values().joinToString(",") {
                it.value
            },
            userFields = UserFields.values().joinToString(",") {
                it.value
            }
        ).data ?: emptyList()
    }

    override suspend fun lookupUser(id: String): UserV2 {
        val user = resources.lookupUser(
            id,
            tweetFields = TweetFields.values().joinToString(",") {
                it.value
            },
            userFields = UserFields.values().joinToString(",") {
                it.value
            }
        )
        if (user.data == null) {
            if (user.errors != null && user.errors.any()) {
                throw TwitterApiException(
                    errors = user.errors.map {
                        Errors(
                            code = null,
                            message = null,
                            detail = it.detail,
                            title = it.title,
                            resource_type = it.resourceType,
                            parameter = it.parameter,
                            value = it.value,
                            type = it.type,
                        )
                    }
                )
            } else {
                // Shouldn't happen?
                throw Exception()
            }
        }
        user.data.profileBanner = user.data.username?.let { userName ->
            runCatching {
                resources.profileBanners(userName)
            }.getOrNull()
        }
        return user.data
    }

    override suspend fun lookupStatus(id: String): StatusV2 {
        val response = resources.lookupTweet(
            id,
            userFields = UserFields.values().joinToString(",") { it.value },
            pollFields = PollFields.values().joinToString(",") { it.name },
            placeFields = PlaceFields.values().joinToString(",") { it.value },
            mediaFields = MediaFields.values()
                .joinToString(",") { it.name },
            expansions = Expansions.values().joinToString(",") { it.value },
            tweetFields = TweetFields.values().joinToString(",") { it.value },
        )
        val data = response.data ?: throw TwitterApiException("Status not found")
        response.includes?.let {
            data.setExtra(it)
        }
        return data
    }

    override suspend fun userPinnedStatus(userId: String): IStatus? {
        val user = lookupUser(userId)
        return user.pinnedTweetID?.let { lookupStatus(it) }
    }

    override suspend fun searchTweets(
        query: String,
        count: Int,
        since_id: String?,
        nextPage: String?,
    ): TwitterSearchResponseV2 {
        val result = resources.search(
            query,
            next_token = nextPage,
            max_results = count,
            since_id = since_id,
            userFields = UserFields.values().joinToString(",") { it.value },
            pollFields = PollFields.values().joinToString(",") { it.name },
            placeFields = PlaceFields.values().joinToString(",") { it.value },
            mediaFields = MediaFields.values()
                .joinToString(",") { it.name },
            expansions = Expansions.values().joinToString(",") { it.value },
            tweetFields = TweetFields.values().joinToString(",") { it.value },
        )
        result.data?.forEach { status ->
            result.includes?.let {
                status.setExtra(it)
            }
        }
        return result
    }

    suspend fun searchTweetsV1(
        query: String,
        count: Int,
        since_id: String? = null,
        max_id: String? = null,
    ): TwitterSearchResponseV1 {
        return resources.searchV1(query, count = count, max_id = max_id, since_id = since_id)
    }

    override suspend fun searchUsers(query: String, page: Int?, count: Int) =
        resources.searchUser(query, page, count)

    override suspend fun showRelationship(target_id: String): IRelationship {
        val response = resources.showFriendships(target_id)
        return Relationship(
            followedBy = response.relationship?.target?.followedBy ?: false,
            following = response.relationship?.target?.following ?: false,
        )
    }

    override suspend fun follow(user_id: String) {
        resources.follow(user_id)
    }

    override suspend fun unfollow(user_id: String) {
        resources.unfollow(user_id)
    }

    override suspend fun like(id: String) = resources.like(id)

    override suspend fun unlike(id: String) = resources.unlike(id)

    override suspend fun retweet(id: String) = resources.retweet(id)

    override suspend fun unRetweet(id: String) = resources.unretweet(id)

    override suspend fun compose(content: String) = update(content)

    override suspend fun delete(id: String) = resources.destroy(id)

    private val BULK_SIZE: Long = 512 * 1024 // 512 Kib

    suspend fun update(
        status: String,
        in_reply_to_status_id: String? = null,
        repost_status_id: String? = null,
        display_coordinates: Boolean? = null,
        lat: Double? = null,
        long: Double? = null,
        media_ids: List<String>? = null,
        attachment_url: String? = null,
        possibly_sensitive: Boolean? = null,
        exclude_reply_user_ids: List<String>? = null
    ) = resources.update(
        status = status,
        in_reply_to_status_id = in_reply_to_status_id,
        auto_populate_reply_metadata = in_reply_to_status_id?.let {
            true
        },
        exclude_reply_user_ids = exclude_reply_user_ids?.joinToString(","),
        repost_status_id = repost_status_id,
        display_coordinates = display_coordinates,
        lat = lat,
        long = long,
        media_ids = media_ids?.joinToString(","),
        attachment_url = attachment_url,
        possibly_sensitive = possibly_sensitive,
    )

    suspend fun uploadFile(stream: InputStream, type: String, length: Long): String {
        val response =
            uploadResources.initUpload(type, length)
        val mediaId = response.mediaIDString ?: throw Error()
        var streamReadLength = 0
        var segmentIndex = 0L
        while (streamReadLength < length) {
            val currentBulkSize = BULK_SIZE.coerceAtMost(length - streamReadLength).toInt()
            ByteArrayOutputStream().use { output ->
                stream.copyToInLength(output, currentBulkSize)
                val data = Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)
                uploadResources.appendUpload(mediaId, segmentIndex, data)
            }
            segmentIndex++
            streamReadLength += currentBulkSize
        }

        return uploadResources.finalizeUpload(mediaId).mediaIDString ?: throw Error()
    }

    override suspend fun followers(user_id: String, nextPage: String?) = resources.followers(
        user_id,
        pagination_token = nextPage,
        userFields = UserFields.values().joinToString(",") { it.value },
        expansions = UserFields.pinned_tweet_id.name,
        tweetFields = TweetFields.values().joinToString(",") { it.value },
    ).let {
        TwitterPaging(it.data ?: emptyList(), it.meta?.nextToken)
    }

    override suspend fun following(user_id: String, nextPage: String?) = resources.following(
        user_id,
        pagination_token = nextPage,
        userFields = UserFields.values().joinToString(",") { it.value },
        expansions = UserFields.pinned_tweet_id.name,
        tweetFields = TweetFields.values().joinToString(",") { it.value },
    ).let {
        TwitterPaging(it.data ?: emptyList(), it.meta?.nextToken)
    }

    suspend fun verifyCredentials(): User? {
        return resources.verifyCredentials()
    }
}
