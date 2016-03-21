package dodert.cuentakilometros3;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
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
    private boolean _isProvderEnable;

    private void Initialize(TextView tv, TextView log, TextView vel)
    {
        textView = tv;
        logTextView = log;
        velTextView = vel;
    }
    public static void Instance(TextView  tv, TextView log, TextView vel)
    {
        if (instance == null)
        {
            // Create the instance
            instance = new MyLocationListener();

        }
        instance.Initialize(tv, log, vel);

    }

    public static MyLocationListener GetInstance()
    {
        // Return the instance
        return instance;
    }

    public boolean IsProviderEnable()
    {
        return _isProvderEnable;
    }
    /*private MyLocationListener (TextView  tv, TextView log, TextView vel){
        textView = tv;
        logTextView = log;
        velTextView = vel;
    }*/

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
        Log.d(_logTag, "Monitor Locatio: " + logMessage);
       // logTextView.append("\nMonitor Locatio: " + logMessage);
        logTextView.setText("Monitor Locatio: " + logMessage + "\n" + logTextView.getText());


        //DistanceTetxView.setText(String.format(Float.toString(5.1234F), "#"));
        textView.setText(df.format(_totalMeters/1000));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(_logTag, "Monitor Location - Provider enabled: " + provider);
        _isProvderEnable = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(_logTag, "Monitor Location - Provider Disabled: " + provider);
        _isProvderEnable = false;
    }

    public void ResetTotalMeters (){
        _totalMeters = 0.0F;
    }

    public void SubstractTotalMeters(float meters)
    {
        logTextView.setText("Restado - Current"  +_totalMeters + "\n" + logTextView.getText());
        //meters <= _totalMeters &&
        if( _totalMeters - meters >= 0) {
            _totalMeters -= meters;
            logTextView.setText("Restado " + meters + " after " + _totalMeters + "\n" + logTextView.getText());

        }
    }

    public void SumTotalMeters(float meters)
    {
        logTextView.setText("Sumado - Current"  + _totalMeters + "\n" + logTextView.getText());
        _totalMeters += meters;
        logTextView.setText("Sumado " + meters + " after " + _totalMeters + "\n" + logTextView.getText());
    }
}
