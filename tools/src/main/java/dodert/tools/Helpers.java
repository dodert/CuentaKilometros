package dodert.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Helpers {

    private static final String DateFormatForGxTrack = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";

    public static String FormatDateTimeTo_gxTrack(long time) {
        SimpleDateFormat format = new SimpleDateFormat(DateFormatForGxTrack);
        Date outputDate = new Date(time);
        return format.format(outputDate);
    }

    public static String FormatDateTimeTo_gxTrack(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DateFormatForGxTrack);
        return format.format(date);
    }
}
