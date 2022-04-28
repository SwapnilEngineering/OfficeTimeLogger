package com.irs.timelogger;

import java.time.LocalTime;

public class Settings {
    LocalTime earliestIn = LocalTime.parse("07:45:00");
    LocalTime latestOut = LocalTime.parse("19:00:00");
}
