package org.main.smartmirror.smartmirror;

import java.util.Date;

public class CalendarEvent {

    public int calendarId = 0;

    public Date start;
    public Date end;
    public String startString = "";
    public String endString = "";

    public String name = "";
    public String description = "";
    public String location = "";
    public String account = "";

    public CalendarEvent() {
        description = "Event";
    }

    public CalendarEvent(String desc) {
        description = desc;
    }
}
