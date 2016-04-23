package dodert.cuentakilometros3;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
    private TextView _logTextView, _speedTextView, _distanceTotalHistoryTextView;
    private Context _context;
    private TrackingSaver _trakingFile;
    private List<DistanceChangeListener> listeners = new ArrayList<DistanceChangeListener>();

    private void Initialize(TextView log, TextView vel, TextView distanceHistory, Context context)
    {
        _distanceTotalHistoryTextView = distanceHistory;
        _logTextView = log;
        _speedTextView = vel;
        _context = context;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean trackEnabled = settings.getBoolean("enable_track", false);
        if (trackEnabled) {
            _trakingFile = TrackingSaver.GetInstance();
            // _trakingFile.CreateAndInitilizaFile();
        }

    }

    public static void Instance(TextView log, TextView vel, TextView distanceHistory, Context context)
    {
        if (instance == null)
        {
            instance = new MyLocationListener();
        }
        instance.Initialize(log, vel, distanceHistory, context);
    }

    public static MyLocationListener GetInstance()
    {
        return instance;
    }

    public MyLocationListener() {

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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean trackEnabled = settings.getBoolean("enable_track", false);

        if (trackEnabled) {
            //_trakingFile.addComment("Inicio");

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            Date outputDate = new Date(time);
            String timeFormated = format.format(outputDate);

            try {
                String coor = String.format("%f %f %f", lng, lat, alt);
                _trakingFile.addLine(coor, timeFormated);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
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
        SumTotalHistoryMeters(0.0F);
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

        //SetDistanceToView(_currentTotalMeters, _distanceTextView);
        //_distanceTotalHistoryTextView.setText(GetDistanceFormatted(_currentTotalMeters));
        //_currentTotalMeters
        for (DistanceChangeListener hl : listeners)
            hl.onChangeDistance((float) (meters / 1000), 0.0F, GetDistanceFormatted(_currentTotalMeters));
    }

    public void SumTotalHistoryMeters(float meters) {
        Log("TotalHistoryMeters");
        if(meters == 0) _totalHistoryMeters = 0;
        else _totalHistoryMeters += meters;
        //SetDistanceToView(_totalHistoryMeters, _distanceTotalHistoryTextView);
        _distanceTotalHistoryTextView.setText(GetDistanceFormatted(_totalHistoryMeters));
    }

    private String GetDistanceFormatted(float meters) {
        String number = String.format("%.2f", (float) (meters / 1000));
        String mask = _maskForKilometers;
        mask += number;
        return mask.substring(mask.length() - _maxLengthForKilometers);
    }
    /*private void SetDistanceToView(float meters, TextView textView) {
        String number = String.format("%.2f", (float) (meters / 1000));
        String mask = _maskForKilometers;
        mask += number;
        textView.setText(mask.substring(mask.length() - _maxLengthForKilometers));

        //_currentTotalMeters
        for (DistanceChangeListener hl : listeners)
            hl.onChangeDistance( (float) (meters / 1000), 0.0F, mask.substring(mask.length() - _maxLengthForKilometers));
    }*/

    private void Log(String logText) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean logEnabled = settings.getBoolean("enable_log", false);
        if (logEnabled) {
            Log.d(_logTag, logText);
            _logTextView.setText(logText + "\n" + _logTextView.getText());
        }
    }


    public void Stop() {
        _trakingFile.addComment("Fin");
    }


    public void addListener(DistanceChangeListener toAdd) {
        listeners.add(toAdd);
    }

}