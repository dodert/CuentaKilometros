package dodert.cuentakilometros3;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by doder on 19/03/2016.
 */
public class MyLocationListener implements LocationListener{
    private static MyLocationListener instance;

    final String _logTag = "Monitor Location";
    public float _totalMeters = 0.0F;
    Location previous;
    TextView textView, logTextView, velTextView;
    Context _context;

    private void Initialize(TextView tv, TextView log, TextView vel, Context context)
    {
        textView = tv;
        logTextView = log;
        velTextView = vel;
        _context = context;
    }

    public static void Instance(TextView tv, TextView log, TextView vel, Context context)
    {
        if (instance == null)
        {
            // Create the instance
            instance = new MyLocationListener();

        }
        instance.Initialize(tv, log, vel, context);
    }

    public static MyLocationListener GetInstance()
    {
        // Return the instance
        return instance;
    }

    @Override
    public void onLocationChanged(Location location) {
       //float meters = 0.0F;
        DecimalFormat df = new DecimalFormat("000.00");
        df.setRoundingMode(RoundingMode.CEILING);
        Location currentLocation = location;
        if(previous != null)
        {
            _totalMeters += previous.distanceTo(currentLocation);
        }
        velTextView.setText(df.format(currentLocation.getSpeed() * 3.6));

        String provider = currentLocation.getProvider();
        double lat = currentLocation.getLatitude();
        double lng = currentLocation.getLongitude();
        float accuracy = currentLocation.getAccuracy();
        long time = currentLocation.getTime();

        previous = currentLocation;

        String logMessage = LogHelper.FormatLocationInfo(provider, lat, lng, accuracy, time);
        Log("Monitor Locatio: " + logMessage);

        textView.setText(df.format(_totalMeters/1000));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log("Monitor Location - Provider enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log("Monitor Location - Provider Disabled: " + provider);
    }

    public void ResetTotalMeters (){
        _totalMeters = 0.0F;
    }

    public void SubstractTotalMeters(float meters)
    {
        Log("Restado - Current" + _totalMeters);
        //meters <= _totalMeters &&
        if( _totalMeters - meters >= 0) {
            _totalMeters -= meters;
            Log("Restado " + meters + " after " + _totalMeters);

        }
    }

    public void SumTotalMeters(float meters)
    {
        Log("Sumado - Current" + _totalMeters);
        _totalMeters += meters;
        Log("Sumado " + meters + " after " + _totalMeters);
    }

    private void Log(String logText) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean logEnabled = settings.getBoolean("enable_log", false);
        if (logEnabled) {
            Log.d(_logTag, logText);
            logTextView.setText(logText + "\n" + logTextView.getText());
        }
    }
}
