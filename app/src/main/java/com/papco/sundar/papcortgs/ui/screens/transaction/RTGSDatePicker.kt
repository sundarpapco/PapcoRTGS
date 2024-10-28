package com.papco.sundar.papcortgs.ui.screens.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.dayId
import com.papco.sundar.papcortgs.common.getCalendarInstance
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RTGSDatePickerDialog(
    onDateSelected: (Long) -> Unit, onDismiss: () -> Unit
) {

    val datePickerState: DatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = getCalendarInstance().run {
            add(Calendar.DATE, 1)
            dayId()
        }, selectableDates = selectableDateRange()
    )

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(
            onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(it)
                }
            }, enabled = datePickerState.selectedDateMillis != null
        ) {
            Text(text = stringResource(id = R.string.ok))
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss
        ) {
            Text(text = stringResource(id = R.string.cancel))
        }
    }) {
        DatePicker(state = datePickerState,
            showModeToggle = false
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
private fun selectableDateRange(): SelectableDates {

    return object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val from = getCalendarInstance().dayId()
            val to = getCalendarInstance().run {
                add(Calendar.DATE, 7)
                dayId()
            }
            return utcTimeMillis in from..to
        }
    }

}

@Preview
@Composable
private fun PreviewDatePicker() {

    RTGSTheme {
        var dialogOpen by remember {
            mutableStateOf(false)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { dialogOpen = true }) {
                Text(text = "OPEN DATE PICKER")
            }
        }

        if (dialogOpen) RTGSDatePickerDialog(onDateSelected = {},
            onDismiss = { dialogOpen = false })

    }

}
