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
package com.twidere.services.twitter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Source(
    val id: Double? = null,

    @SerialName("id_str")
    val idStr: String? = null,

    @SerialName("screen_name")
    val screenName: String? = null,

    val following: Boolean? = null,

    @SerialName("followed_by")
    val followedBy: Boolean? = null,

    @SerialName("live_following")
    val liveFollowing: Boolean? = null,

    @SerialName("following_received")
    val followingReceived: Boolean? = null,

    @SerialName("following_requested")
    val followingRequested: Boolean? = null,

    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean? = null,

    @SerialName("can_dm")
    val canDm: Boolean? = null,

    val blocking: Boolean? = null,

    @SerialName("blocked_by")
    val blockedBy: Boolean? = null,

    val muting: Boolean? = null,

    @SerialName("want_retweets")
    val wantRetweets: Boolean? = null,

    @SerialName("all_replies")
    val allReplies: Boolean? = null,

    @SerialName("marked_spam")
    val markedSpam: Boolean? = null
)
