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
package com.twidere.twiderex.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import com.twidere.twiderex.preferences.proto.AppearancePreferences
import com.twidere.twiderex.preferences.proto.DisplayPreferences
import com.twidere.twiderex.ui.LocalVideoPlayback
import javax.inject.Inject

val LocalAppearancePreferences = compositionLocalOf<AppearancePreferences> { error("No AppearancePreferences") }
val LocalDisplayPreferences = compositionLocalOf<DisplayPreferences> { error("No DisplayPreferences") }

data class PreferencesHolder @Inject constructor(
    val appearancePreferences: DataStore<AppearancePreferences>,
    val displayPreferences: DataStore<DisplayPreferences>,
)

@Composable
fun ProvidePreferences(
    holder: PreferencesHolder,
    content: @Composable () -> Unit,
) {
    val appearances by holder.appearancePreferences
        .data
        .collectAsState(initial = AppearancePreferences.getDefaultInstance())
    val display by holder.displayPreferences
        .data
        .collectAsState(initial = DisplayPreferences.getDefaultInstance())

    CompositionLocalProvider(
        LocalAppearancePreferences provides appearances,
        LocalDisplayPreferences provides display,
        LocalVideoPlayback provides display.autoPlayback,
    ) {
        content.invoke()
    }
}
