package com.itdontmatta.omniscience.api.entry;

import com.itdontmatta.omniscience.api.OmniApi;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DataAggregateEntry extends DataEntry {
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OmniApi.getSimpleDateFormat());

    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(Calendar calender) {
        this.date = simpleDateFormat.format(calender.getTime());
    }
}
