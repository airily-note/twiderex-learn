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
package com.twidere.twiderex.component.lazy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import com.twidere.twiderex.component.foundation.loading

fun <T : Any> LazyListScope.itemsPaging(
    lazyPagingItems: LazyPagingItems<T>,
    key: ((index: Int) -> Any) = { lazyPagingItems[it]?.hashCode() ?: 0 },
    itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
    loadState(lazyPagingItems.loadState.refresh) {
        lazyPagingItems.retry()
    }
    items(lazyPagingItems = lazyPagingItems, key = key, itemContent = itemContent)
    loadState(lazyPagingItems.loadState.append) {
        lazyPagingItems.retry()
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun LazyListScope.loadState(
    state: LoadState,
    onReloadRequested: () -> Unit = {},
) {
    when (state) {
        is LoadState.Loading -> loading()
        is LoadState.Error -> item {
            ListItem(
                modifier = Modifier.clickable(onClick = { onReloadRequested.invoke() }),
                text = {
//                        Text(text = stringResource(id = R.string.list_load_state_error))
                }
            )
        }
        else -> {
        }
    }
}
