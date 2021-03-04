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
package com.twidere.twiderex.repository

import com.twidere.twiderex.db.CacheDatabase
import com.twidere.twiderex.db.model.DbStatusReaction
import com.twidere.twiderex.model.MicroBlogKey
import java.util.UUID

class ReactionRepository(
    private val database: CacheDatabase,
) {
    suspend fun updateReaction(
        accountKey: MicroBlogKey,
        statusKey: MicroBlogKey,
        action: (DbStatusReaction) -> Unit,
    ) {
        database.reactionDao().findWithStatusKey(statusKey, accountKey).let {
            it ?: DbStatusReaction(
                _id = UUID.randomUUID().toString(),
                statusKey = statusKey,
                accountKey = accountKey,
                liked = false,
                retweeted = false,
            )
        }.let {
            action.invoke(it)
            database.reactionDao().insertAll(listOf(it))
        }
    }
}
