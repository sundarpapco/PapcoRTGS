package com.papco.sundar.papcortgs.common

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import android.widget.Toast
import com.papco.sundar.papcortgs.R
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {


    companion object {

        const val TAG = "papco.payroll.DatePickerDialogFragment"
        private const val KEY_STARTING_DATE = "key:DatePicker:StartingDate"
        private const val KEY_MINIMUM_DATE="key:DatePicker:include_past"
        private const val KEY_MAXIMUM_DATE="key:DatePicker:include_future"

        fun getInstance(): DatePickerFragment {
            val args = Bundle()
            val today= getCalendarInstance().dayId()
            args.putLong(KEY_STARTING_DATE, today)
            args.putLong(KEY_MINIMUM_DATE,today)
            args.putLong(KEY_MAXIMUM_DATE,0L)
            return DatePickerFragment().also { it.arguments = args }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar= calendarWithTime(getDayId())
        val dialog=DatePickerDialog(
            requireContext(),
            R.style.MyTimePickerDialogTheme,
            this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )


        if(getMinumumDate()!=0L)
            dialog.datePicker.minDate=getMinumumDate()

        if(getMaximumDate()!=0L)
            dialog.datePicker.maxDate=getMaximumDate()

        return dialog

    }
    

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

        val calendar= getCalendarInstance()
        calendar.setDateAndTime(year,month,dayOfMonth)

        deliverResult(calendar.dayId())
        dismiss()
    }

    private fun getDayId(): Long = arguments?.getLong(KEY_STARTING_DATE) ?: getCurrentDayId()

    private fun getMinumumDate() = arguments?.getLong(KEY_MINIMUM_DATE) ?: 0L

    private fun getMaximumDate() = arguments?.getLong(KEY_MAXIMUM_DATE) ?: 0L

    private fun getCurrentDayId(): Long {
        return getCalendarInstance().dayId()
    }

    private fun deliverResult(dayId: Long) {

        var callback: OnDatePickedListener? = null

        try {
            when {
                parentFragment != null -> {
                    callback = parentFragment as OnDatePickedListener
                }
                activity != null -> {
                    callback = requireActivity() as OnDatePickedListener
                }
            }
        } catch (exception: Exception) {
            Toast.makeText(
                    requireContext(),
                    "Caller should implement OnDatePickedListener interface",
            Toast.LENGTH_SHORT).show()
        }

        callback?.onDatePicked(dayId)
    }

    interface OnDatePickedListener {
        fun onDatePicked(selectedDayId:Long)
    }
}