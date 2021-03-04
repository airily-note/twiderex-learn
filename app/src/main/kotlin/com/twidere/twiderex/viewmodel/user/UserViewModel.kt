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
package com.twidere.twiderex.viewmodel.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twidere.services.http.MicroBlogException
import com.twidere.services.microblog.LookupService
import com.twidere.services.microblog.RelationshipService
import com.twidere.services.microblog.model.IRelationship
import com.twidere.twiderex.di.assisted.IAssistedFactory
import com.twidere.twiderex.model.AccountDetails
import com.twidere.twiderex.model.MicroBlogKey
import com.twidere.twiderex.notification.InAppNotification
import com.twidere.twiderex.repository.UserRepository
import com.twidere.twiderex.utils.notify
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class UserViewModel @AssistedInject constructor(
    private val factory: UserRepository.AssistedFactory,
    private val inAppNotification: InAppNotification,
    @Assisted private val account: AccountDetails,
    @Assisted private val userKey: MicroBlogKey,
) : ViewModel() {

    @dagger.assisted.AssistedFactory
    interface AssistedFactory : IAssistedFactory {
        fun create(
            account: AccountDetails,
            initialUserKey: MicroBlogKey?,
        ): UserViewModel
    }

    private val repository by lazy {
        account.service.let {
            factory.create(account.accountKey, it as LookupService, it as RelationshipService)
        }
    }
    val refreshing = MutableLiveData(false)
    val loadingRelationship = MutableLiveData(false)
    val user = repository.getUserLiveData(userKey)
    val relationship = MutableLiveData<IRelationship>()
    val isMe = userKey == account.accountKey

    fun refresh() = viewModelScope.launch {
        refreshing.postValue(true)
        try {
            repository.lookupUserById(userKey.id)
        } catch (e: MicroBlogException) {
            e.notify(inAppNotification)
        } catch (e: IOException) {
            e.message?.let { inAppNotification.show(it) }
        } catch (e: HttpException) {
            e.message?.let { inAppNotification.show(it) }
        }
        refreshing.postValue(false)
    }

    fun follow() = viewModelScope.launch {
        loadingRelationship.postValue(true)
        repository.follow(userKey.id)
        loadRelationShip()
    }

    fun unfollow() = viewModelScope.launch {
        loadingRelationship.postValue(true)
        repository.unfollow(userKey.id)
        loadRelationShip()
    }

    private fun loadRelationShip() = viewModelScope.launch {
        loadingRelationship.postValue(true)
        try {
            repository.showRelationship(userKey.id).let {
                relationship.postValue(it)
            }
        } catch (e: MicroBlogException) {
        } catch (e: IOException) {
        } catch (e: HttpException) {
        }
        loadingRelationship.postValue(false)
    }

    init {
        refresh()
        loadRelationShip()
    }
}
