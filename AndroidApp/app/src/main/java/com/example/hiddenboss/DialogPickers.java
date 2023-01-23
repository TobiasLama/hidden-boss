package com.example.hiddenboss;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class DialogPickers implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    private Converters converters = new Converters();

    public void datePicker(View view, final TextView dateTextView, final Calendar date){
        //Opens a dialog to pick a date
        DatePickerDialog DateDialog = new DatePickerDialog(
                view.getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date.set(year,month,dayOfMonth);
                        dateTextView.setText(converters.DateToString(year,month,dayOfMonth));
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        DateDialog.show();
    }

    public void timePicker(View view, final TextView timeTextView, final Calendar date){
        //Opens a dialog to pick a time
        TimePickerDialog timePickerDialog = new TimePickerDialog(

                view.getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date.set(Calendar.MINUTE,minute);
                        timeTextView.setText(converters.TimeToString(hourOfDay,minute));
                    }
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        //Necessary method for implementing datepicker
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        //Necessary method for implementing timepicker
    }
}
