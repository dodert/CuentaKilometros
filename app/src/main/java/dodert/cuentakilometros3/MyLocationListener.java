package dodert.cuentakilometros3;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by dodert on 19/03/2016.
 */
public class MyLocationListener implements LocationListener{
    private static MyLocationListener instance;

    final String _logTag = "Monitor Location";
    final int _maxLengthForKilometers = 7;
    final String _maskForKilometers = "0000000000000000000000";
    public float _totalMeters = 0.0F;
    private Location _previousLocation;
    private TextView _distanceTextView, _logTextView, _speedTextView;
    private Context _context;

    private void Initialize(TextView tv, TextView log, TextView vel, Context context)
    {
        _distanceTextView = tv;
        _logTextView = log;
        _speedTextView = vel;
        _context = context;
    }

    public static void Instance(TextView tv, TextView log, TextView vel, Context context)
    {
        if (instance == null)
        {
            instance = new MyLocationListener();
        }
        instance.Initialize(tv, log, vel, context);
    }

    public static MyLocationListener GetInstance()
    {
        return instance;
    }

    @Override
    public void onLocationChanged(Location location) {
        Location currentLocation = location;
        if (_previousLocation != null)
        {
            SumTotalMeters(_previousLocation.distanceTo(currentLocation));
        }
        _speedTextView.setText(String.format("%.2f", (currentLocation.getSpeed() * 3.6)));

        String provider = currentLocation.getProvider();
        double lat = currentLocation.getLatitude();
        double lng = currentLocation.getLongitude();
        double alt = currentLocation.getAltitude();
        float accuracy = currentLocation.getAccuracy();
        long time = currentLocation.getTime();

        _previousLocation = currentLocation;

        String logMessage = LogHelper.FormatLocationInfo(provider, lat, lng, alt, accuracy, time);
        Log("Monitor Location: " + logMessage);
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
        SumTotalMeters(0.0F);
    }

    public void SubstractTotalMeters(float meters)
    {
        SumTotalMeters(-meters);
    }

    public void SumTotalMeters(float meters)
    {
        float current = _totalMeters;
        if (meters == 0) {
            _totalMeters = 0.0F;
            Log("Reseted");
        } else if (_totalMeters + meters >= 0) {
            _totalMeters += meters;
            Log(meters + " Before " + current + " after " + _totalMeters);
        }

        SetDistanceToView(_totalMeters);
    }

    private void SetDistanceToView(float meters) {
        String number = String.format("%.2f", (float) (meters / 1000));
        number += _maskForKilometers;
        _distanceTextView.setText(number.substring(number.length() - _maxLengthForKilometers));
    }

    private void Log(String logText) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean logEnabled = settings.getBoolean("enable_log", false);
        if (logEnabled) {
            Log.d(_logTag, logText);
            _logTextView.setText(logText + "\n" + _logTextView.getText());
        }
    }
}