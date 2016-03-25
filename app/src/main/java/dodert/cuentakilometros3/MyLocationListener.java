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
    private float _currentTotalMeters = 0.0F;
    private float _totalHistoryMeters = 0.0F;
    private Location _previousLocation;
    private TextView _distanceTextView, _logTextView, _speedTextView, _distanceTotalHistoryTextView;
    private Context _context;

    private void Initialize(TextView tv, TextView log, TextView vel, TextView distanceHistory, Context context)
    {
        _distanceTextView = tv;
        _distanceTotalHistoryTextView = distanceHistory;
        _logTextView = log;
        _speedTextView = vel;
        _context = context;
    }

    public static void Instance(TextView tv, TextView log, TextView vel, TextView distanceHistory, Context context)
    {
        if (instance == null)
        {
            instance = new MyLocationListener();
        }
        instance.Initialize(tv, log, vel, distanceHistory, context);
    }

    public static MyLocationListener GetInstance()
    {
        return instance;
    }

    @Override
    public void onLocationChanged(Location location) {
        float distanceTo;
        Location currentLocation = location;
        if (_previousLocation != null)
        {
            distanceTo = _previousLocation.distanceTo(currentLocation);
            SumTotalMeters(distanceTo);
            SumTotalHistoryMeters(distanceTo);
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
        float current = _currentTotalMeters;
        if (meters == 0) {
            _currentTotalMeters = 0.0F;
            Log("Reseted");
        } else if (_currentTotalMeters + meters >= 0) {
            _currentTotalMeters += meters;
            Log(meters + " Before " + current + " after " + _currentTotalMeters);
        }

        SetDistanceToView(_currentTotalMeters, _distanceTextView);
    }

    public void SumTotalHistoryMeters(float meters) {
        Log("TotalHistoryMeters");
        _totalHistoryMeters += meters;
        SetDistanceToView(_totalHistoryMeters, _distanceTotalHistoryTextView);
    }

    private void SetDistanceToView(float meters, TextView textView) {
        String number = String.format("%.2f", (float) (meters / 1000));
        String mask = _maskForKilometers;
        mask += number;
        textView.setText(mask.substring(mask.length() - _maxLengthForKilometers));
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