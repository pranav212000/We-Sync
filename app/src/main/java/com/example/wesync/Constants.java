package com.example.wesync;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Constants {
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String NAME = "name";
    public static final String LOGIN = "login";
    public static final String ROOM_ID = "roomId";
    public static final String ROOM = "room";
    public static final String SONG = "song";
    public static final String ROOMS = "rooms";
    public static final String HOST = "host";
    public static final String MEMBERS = "members";
    public static final String USERS_COLLECTION = "users";
    public static final String ROOMS_COLLECTION = "rooms";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String PREFERENCES = "prefs";
    public static final String CLIENT_ID = "3d7fabbd1e03480aa9ac4c68f0a7c360";
    public static final String CLIENT_SECRET = "ae0df2e9591f45b19a83a984e5feabfc";
    public static final String REDIRECT_URI = "http://localhost:8888/callback";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String REFRESH_TIME = "refresh_time";
    public static final String TOKEN_TYPE = "Bearer";


    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Date getUTCdatetimeAsDate() {
        // note: doesn't check for null
        return getStringDateToDate(getUTCdatetimeAsString());
    }

    public static String getUTCdatetimeAsString() {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());

        return utcTime;
    }

    public static Date getStringDateToDate(String StrDate) {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);

        try {
            dateToReturn = (Date) dateFormat.parse(StrDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateToReturn;
    }

    public static String getTimeFormLong(long millis) {
        String str = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        return str;
    }
}
