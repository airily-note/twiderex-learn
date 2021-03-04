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
package com.twidere.twiderex.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.twidere.twiderex.component.navigation.LocalNavigator
import com.twidere.twiderex.component.navigation.Navigator
import com.twidere.twiderex.ui.LocalNavController

@Composable
fun Router(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalNavigator provides Navigator(navController, context),
    ) {
        NavHost(navController = navController, startDestination = initialRoute) {
            route()
        }
    }
}
