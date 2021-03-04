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
package com.twidere.twiderex.worker.status

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OverwritingInputMerger
import androidx.work.WorkerParameters
import androidx.work.setInputMerger
import com.twidere.twiderex.extensions.getNullableBoolean
import com.twidere.twiderex.extensions.getNullableLong
import com.twidere.twiderex.model.MicroBlogKey
import com.twidere.twiderex.repository.ReactionRepository
import com.twidere.twiderex.repository.StatusRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UpdateStatusWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: ReactionRepository,
    private val statusRepository: StatusRepository,
) : CoroutineWorker(appContext, params) {
    companion object {
        fun create(statusResult: StatusResult? = null) = OneTimeWorkRequestBuilder<UpdateStatusWorker>()
            .setInputMerger(OverwritingInputMerger::class)
            .apply {
                statusResult?.let {
                    setInputData(it.toWorkData())
                }
            }
            .build()
    }

    override suspend fun doWork(): Result {
        val accountKey = inputData.getString("accountKey")?.let {
            MicroBlogKey.valueOf(it)
        } ?: return Result.failure()
        val statusKey = inputData.getString("statusKey")?.let {
            MicroBlogKey.valueOf(it)
        } ?: return Result.failure()
        val liked = inputData.getNullableBoolean("liked")
        val retweeted = inputData.getNullableBoolean("retweeted")
        val retweetCount = inputData.getNullableLong("retweetCount")
        val likeCount = inputData.getNullableLong("likeCount")
        repository.updateReaction(accountKey = accountKey, statusKey = statusKey) {
            if (liked != null) {
                it.liked = liked
            }
            if (retweeted != null) {
                it.retweeted = retweeted
            }
        }
        statusRepository.updateStatus(statusKey = statusKey) {
            if (retweetCount != null) {
                it.retweetCount = retweetCount
            }
            if (likeCount != null) {
                it.likeCount = likeCount
            }
        }
        return Result.success()
    }
}
