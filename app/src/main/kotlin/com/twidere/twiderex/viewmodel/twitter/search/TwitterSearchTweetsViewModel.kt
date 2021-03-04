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
package com.twidere.twiderex.viewmodel.twitter.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.twidere.services.twitter.TwitterService
import com.twidere.twiderex.db.CacheDatabase
import com.twidere.twiderex.di.assisted.IAssistedFactory
import com.twidere.twiderex.model.AccountDetails
import com.twidere.twiderex.model.ui.UiStatus.Companion.toUi
import com.twidere.twiderex.notification.InAppNotification
import com.twidere.twiderex.paging.mediator.paging.pager
import com.twidere.twiderex.paging.mediator.search.SearchStatusMediator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.map

class TwitterSearchTweetsViewModel @AssistedInject constructor(
    val database: CacheDatabase,
    inAppNotification: InAppNotification,
    @Assisted private val account: AccountDetails,
    @Assisted keyword: String,
) : ViewModel() {
    @dagger.assisted.AssistedFactory
    interface AssistedFactory : IAssistedFactory {
        fun create(account: AccountDetails, keyword: String): TwitterSearchTweetsViewModel
    }

    private val service by lazy {
        account.service as TwitterService
    }
    val source by lazy {
        SearchStatusMediator(keyword, database, account.accountKey, service, inAppNotification).pager()
            .flow.map { it.map { it.status.toUi(account.accountKey) } }.cachedIn(viewModelScope)
    }
}
