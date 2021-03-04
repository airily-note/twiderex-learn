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
package com.twidere.twiderex.paging.mediator.user

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import com.twidere.services.microblog.TimelineService
import com.twidere.services.microblog.model.IStatus
import com.twidere.twiderex.db.CacheDatabase
import com.twidere.twiderex.db.model.DbPagingTimelineWithStatus
import com.twidere.twiderex.db.model.UserTimelineType
import com.twidere.twiderex.db.model.pagingKey
import com.twidere.twiderex.model.MicroBlogKey
import com.twidere.twiderex.notification.InAppNotification
import com.twidere.twiderex.paging.MaxIdPagination
import com.twidere.twiderex.paging.PagingList
import com.twidere.twiderex.paging.mediator.paging.MaxIdPagingMediator

@OptIn(ExperimentalPagingApi::class)
class UserMediaMediator(
    private val userKey: MicroBlogKey,
    database: CacheDatabase,
    accountKey: MicroBlogKey,
    private val service: TimelineService,
    inAppNotification: InAppNotification,
) : MaxIdPagingMediator(accountKey, database, inAppNotification) {
    override val pagingKey: String
        get() = UserTimelineType.Media.pagingKey(userKey)

    override suspend fun load(pageSize: Int, paging: MaxIdPagination?): List<IStatus> {
        return service.userTimeline(
            user_id = userKey.id,
            count = pageSize,
            max_id = paging?.maxId,
            exclude_replies = false
        )
    }

    override fun provideNextPage(
        raw: List<IStatus>,
        result: List<DbPagingTimelineWithStatus>
    ): MaxIdPagination {
        if (result is PagingList<*, *>) {
            return result.nextPage as MaxIdPagination
        }
        return super.provideNextPage(raw, result)
    }

    override fun transform(
        type: LoadType,
        state: PagingState<Int, DbPagingTimelineWithStatus>,
        data: List<DbPagingTimelineWithStatus>
    ): List<DbPagingTimelineWithStatus> {
        return PagingList(
            data.filter {
                val content = it.status.status
                it.status.retweet == null && content.data.hasMedia && content.user.user.userKey == userKey
            },
            MaxIdPagination(data.lastOrNull()?.status?.status?.data?.statusId)
        )
    }
}
