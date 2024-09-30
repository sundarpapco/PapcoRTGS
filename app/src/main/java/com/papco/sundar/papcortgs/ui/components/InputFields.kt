package com.papco.sundar.papcortgs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

class TextInputFieldState {
    var text by mutableStateOf("")
    var error: String? by mutableStateOf(null)
    var label: String by mutableStateOf("")

    fun clearError() {
        error = null
    }
}

@Composable
fun TextInputField(
    modifier: Modifier = Modifier,
    text: String,
    label: String,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation=VisualTransformation.None,
    onChange: (String) -> Unit,
    ) {

    OutlinedTextField(modifier = modifier,
        value = text,
        onValueChange = onChange,
        isError = error != null,
        singleLine = true,
        label = {
            Text(label)
        },
        supportingText = error?.let {
            {
                Text(text = error)
            }
        },
        keyboardActions=keyboardActions,
        keyboardOptions=keyboardOptions,
        visualTransformation=visualTransformation
    )

}

@Preview
@Composable
private fun TestTextInputField() {

    var text by rememberSaveable {
        mutableStateOf("")
    }

    var error by rememberSaveable {
        mutableStateOf("")
    }

    RTGSTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            TextInputField(text = text, label = "Name", error = error.ifBlank { null }) {
                error = if (it == "Madhana") it else ""
                text = it
            }
        }
    }

}