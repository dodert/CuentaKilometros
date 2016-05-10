package dodert.cuentakilometros3;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by dodert on 19/03/2016.
 */
public class LogHelper {
    static final String _timeStampFormat = "yyyy-MM-dd'T'HH:mm:ss";
    static final String _timeStampTimeZoneId = "UTC";

    /*public static String FormatLocationInfo(String provider, double lat, double lng, double alt,float accuracy, long time) {
        SimpleDateFormat timeStampFormatter = new SimpleDateFormat(_timeStampFormat);
        timeStampFormatter.setTimeZone(TimeZone.getTimeZone(_timeStampTimeZoneId));

        String timeStamp = timeStampFormatter.format(time);

        String logMessage = String.format("%s | lat/lng/alt=%f/%f/%f | accuracy=%f | Time=%s",
                provider, lat, lng, alt, accuracy, timeStamp);

        return logMessage;
    }

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
