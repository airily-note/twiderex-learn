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
package com.twidere.twiderex.scenes.home

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.navigate
import com.twidere.twiderex.R
import com.twidere.twiderex.component.TimelineComponent
import com.twidere.twiderex.component.foundation.InAppNotificationScaffold
import com.twidere.twiderex.di.assisted.assistedViewModel
import com.twidere.twiderex.navigation.Route
import com.twidere.twiderex.ui.LocalActiveAccount
import com.twidere.twiderex.ui.LocalNavController
import com.twidere.twiderex.viewmodel.compose.ComposeType
import com.twidere.twiderex.viewmodel.timeline.HomeTimelineViewModel

class HomeTimelineItem : HomeNavigationItem() {

    @Composable
    override fun name(): String = stringResource(R.string.scene_timeline_title)

    override val route: String
        get() = "home"

    @Composable
    override fun icon(): Painter = painterResource(id = R.drawable.ic_home)

    @Composable
    override fun content() {
        val account = LocalActiveAccount.current ?: return
        val viewModel = assistedViewModel<HomeTimelineViewModel.AssistedFactory, HomeTimelineViewModel>(
            account
        ) {
            it.create(account)
        }
        InAppNotificationScaffold(
            floatingActionButton = {
                val navController = LocalNavController.current
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Route.Compose(ComposeType.New))
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_feather),
                        contentDescription = stringResource(
                            id = R.string.accessibility_scene_home_compose
                        )
                    )
                }
            }
        ) {
            TimelineComponent(viewModel = viewModel)
        }
    }
}
