package com.example.hiddenboss;

import java.text.DateFormatSymbols;

public class Converters {
    //Converters contains custom methods for converting values

    public String DateToString(int year, int month, int day){
        //Converts a collection of ints for year, month, day into the desired string to be displayed
        String dayAdj;
        if (day%10 == 1){
            dayAdj = "st";
        } else if (day%10 == 2) {
            dayAdj = "nd";
        } else if (day%10 == 3){
            dayAdj = "rd";
        } else {
            dayAdj = "th";
        }
        return day + dayAdj + " of " + new DateFormatSymbols().getMonths()[month] + " " + year;
    }

    public String TimeToString(int hour, int minute){
        //Converts a collection of ints for hour minute into the desired string to be displayed
        if (hour < 10){
            if (minute < 10){return "0" + hour + ":0" + minute;}

            return "0" + hour + ":" + minute;
        } else
            {if (minute < 10) {return hour + ":0" + minute;}

            return hour+":"+ minute;
            }
        }

}
