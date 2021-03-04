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
package com.twidere.twiderex.component.foundation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextInput(
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    color: Color = Color.Unspecified,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = Int.MAX_VALUE,
    alignment: Alignment = Alignment.TopStart,
    imeAction: ImeAction = ImeAction.Default,
    onImeActionPerformed: (ImeAction, SoftwareKeyboardController?) -> Unit = { _, _ -> },
    value: String,
    onValueChange: (String) -> Unit,
    onClicked: (() -> Unit)? = null,
) {

    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    val textFieldValue = textFieldValueState.copy(text = value)

    TextInput(
        autoFocus = autoFocus,
        modifier = modifier,
        textStyle = textStyle,
        color = color,
        placeholder = placeholder,
        keyboardType = keyboardType,
        maxLines = maxLines,
        alignment = alignment,
        imeAction = imeAction,
        onImeActionPerformed = onImeActionPerformed,
        value = textFieldValue,
        onValueChange = {
            textFieldValueState = it
            if (value != it.text) {
                onValueChange(it.text)
            }
        },
        onClicked = onClicked,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextInput(
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    color: Color = Color.Unspecified,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = Int.MAX_VALUE,
    alignment: Alignment = Alignment.TopStart,
    imeAction: ImeAction = ImeAction.Default,
    onImeActionPerformed: (ImeAction, SoftwareKeyboardController?) -> Unit = { _, _ -> },
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onClicked: (() -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val textColor = color.takeOrElse {
        LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    }
    DisposableEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
            keyboardController?.showSoftwareKeyboard()
        }
        onDispose { }
    }
    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .clickable(interactionSource = interactionSource, indication = null) {
                onClicked?.invoke()
                focusRequester.requestFocus()
                // TODO(b/163109449): Showing and hiding keyboard should be handled by BaseTextField.
                //  The requestFocus() call here should be enough to trigger the software keyboard.
                //  Investiate why this is needed here. If it is really needed, instead of doing
                //  this in the onClick callback, we should move this logic to the focusObserver
                //  so that it can show or hide the keyboard based on the focus state.
                keyboardController?.showSoftwareKeyboard()
            },
        contentAlignment = alignment,
    ) {
        BasicTextField(
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            keyboardActions =
                KeyboardActions(
                    onDone = { onImeActionPerformed.invoke(ImeAction.Done, keyboardController) },
                    onGo = { onImeActionPerformed.invoke(ImeAction.Go, keyboardController) },
                    onNext = { onImeActionPerformed.invoke(ImeAction.Next, keyboardController) },
                    onPrevious = { onImeActionPerformed.invoke(ImeAction.Previous, keyboardController) },
                    onSearch = { onImeActionPerformed.invoke(ImeAction.Search, keyboardController) },
                    onSend = { onImeActionPerformed.invoke(ImeAction.Send, keyboardController) }
                ),
            // cursorColor = textColor,
            textStyle = textStyle.copy(color = textColor),
            // onTextInputStarted = {
            //     keyboardController.value = it
            //     onTextInputStarted?.invoke(it)
            // },
            value = value,
            onValueChange = {
                onValueChange(it)
            },

        )
        if (value.text.isEmpty()) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium
            ) {
                placeholder?.invoke()
            }
        }
    }
}
