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
package com.twidere.twiderex.scenes.mastodon

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.twidere.twiderex.R
import com.twidere.twiderex.component.foundation.SignInButton
import com.twidere.twiderex.component.foundation.SignInScaffold
import com.twidere.twiderex.component.navigation.LocalNavigator
import com.twidere.twiderex.extensions.navViewModel
import com.twidere.twiderex.extensions.navigateForResult
import com.twidere.twiderex.extensions.setResult
import com.twidere.twiderex.ui.LocalNavController
import com.twidere.twiderex.ui.standardPadding
import com.twidere.twiderex.viewmodel.mastodon.MastodonSignInViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MastodonSignInScene() {
    val viewModel = navViewModel<MastodonSignInViewModel>()
    val host by viewModel.host.observeAsState(initial = "")
    val loading by viewModel.loading.observeAsState(initial = false)
    val navController = LocalNavController.current
    val navigator = LocalNavigator.current

    SignInScaffold {
        if (loading == true) {
            CircularProgressIndicator()
        } else {
            val focusRequester = remember { FocusRequester() }
            DisposableEffect(focusRequester) {
                focusRequester.requestFocus()
                onDispose { }
            }
            OutlinedTextField(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth(),
                value = host,
                onValueChange = { viewModel.setHost(it) },

            )
            Spacer(modifier = Modifier.height(standardPadding * 2))
            SignInButton(
                onClick = {
                    GlobalScope.launch {
                        withContext(Dispatchers.Main) {
                            // TODO: dynamic key && secret
                            viewModel.beginOAuth(
                                host,
                            ) { target ->
                                navController.navigateForResult("code") {
                                    navigator.mastodonSignInWeb(target)
                                }
                            }.let { success ->
                                if (success) {
                                    navController.setResult("success", success)
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            ) {
                ListItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mastodon_logo_white),
                            contentDescription = stringResource(
                                id = R.string.accessibility_common_logo_mastodon
                            )
                        )
                    },
                    text = {
                        Text(text = stringResource(id = R.string.scene_sign_in_sign_in_with_mastodon))
                    },
                    trailing = {
                        IconButton(
                            enabled = false,
                            onClick = {},
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = stringResource(
                                    id = R.string.scene_sign_in_sign_in_with_mastodon
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}
