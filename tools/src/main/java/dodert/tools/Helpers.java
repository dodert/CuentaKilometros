package dodert.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public static String FormatLocationInfo(String provider, double lat, double lng, double alt, float accuracy, long time) {
        SimpleDateFormat timeStampFormatter = new SimpleDateFormat(DateFormatForGxTrack);

        String timeStamp = timeStampFormatter.format(time);

        String logMessage = String.format(Locale.US, "%s | lat/lng/alt=%f/%f/%f | accuracy=%f | Time=%s", provider, lat, lng, alt, accuracy, timeStamp);

        return logMessage;
    }
/*
    public static String FormatLocationInfo(Location location) {

        if (location == null)
            return "<NULL Location Value>";

        String provider = location.getProvider();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        double alt = location.getAltitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();

        return LogHelper.FormatLocationInfo(provider, lat, lng, alt, accuracy, time);
    }*/
}
