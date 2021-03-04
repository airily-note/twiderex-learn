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
package com.twidere.twiderex.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.twidere.twiderex.db.model.DbStatusV2
import com.twidere.twiderex.db.model.DbStatusWithReference
import com.twidere.twiderex.model.MicroBlogKey

@Dao
interface StatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(status: List<DbStatusV2>)

    @Query("SELECT * FROM status")
    suspend fun getAll(): List<DbStatusV2>

    @Query("SELECT * FROM status WHERE statusKey == :key")
    suspend fun findWithStatusKey(key: MicroBlogKey): DbStatusV2?

    @Query("SELECT * FROM status WHERE retweetStatusKey == :key")
    suspend fun findWithReplyStatusKey(key: MicroBlogKey): DbStatusV2?

    @Transaction
    @Query("SELECT * FROM status WHERE statusKey == :key")
    suspend fun findWithStatusKeyWithReference(key: MicroBlogKey): DbStatusWithReference?

    @Transaction
    @Query("SELECT * FROM status WHERE statusKey == :key")
    fun findWithStatusKeyWithReferenceLiveData(key: MicroBlogKey): LiveData<DbStatusWithReference?>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(status: List<DbStatusV2>)

    @Delete
    suspend fun delete(status: List<DbStatusV2>)
}
