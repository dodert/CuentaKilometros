package dodert.cuentakilometros3;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import dodert.tools.Helpers;

/**
 * Created by dodert on 19/03/2016.
 */
public class MyLocationListener implements LocationListener {
    private static MyLocationListener instance;

    final int _maxLengthForKilometers = 7;
    final String _maskForKilometers = "0000000000000000000000";
    private float _currentTotalMeters = 0.0F;
    private float _totalHistoryMeters = 0.0F;
    private Location _previousLocation;
    private Context _context;
    private TrackingSaver _trakingFile;
    private List<DistanceChangeListener> listeners = new ArrayList<DistanceChangeListener>();

    private void Initialize(Context context) {
        _context = context;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean trackEnabled = settings.getBoolean("enable_track", false);
        if (trackEnabled) {
            _trakingFile = TrackingSaver.GetInstance();
        }
    }

    public int CountListeners()
    {
        return listeners.size();
    }

    public static void Instance(Context context) {
        if (instance == null) {
            instance = new MyLocationListener();
        }
        instance.Initialize(context);
    }

    public static MyLocationListener GetInstance() {
        return instance;
    }

    private void ChangeSpeed(String speed) {
        for (DistanceChangeListener hl : listeners) {
            hl.onChangeSpeed(speed);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        float distanceTo;
        Location currentLocation = location;
        if (_previousLocation != null) {
            distanceTo = _previousLocation.distanceTo(currentLocation);
            SumTotalMeters(distanceTo);
            SumTotalHistoryMeters(distanceTo);
            ChangeSpeed(String.format("%.2f", (currentLocation.getSpeed() * 3.6)));
        }

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
            String timeFormatted = Helpers.FormatDateTimeTo_gxTrack(time);
            Log(timeFormatted);

            try {
                String coordinates = String.format(Locale.US, "%f %f %f", lng, lat, alt);
                _trakingFile.addTrackLine(coordinates, timeFormatted);
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
        String logMessage = Helpers.FormatLocationInfo(provider, lat, lng, alt, accuracy, time);
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

    public void ResetTotalMeters() {
        SumTotalMeters(0.0F);
        SumTotalHistoryMeters(0.0F);
    }

    public void OverrideTotalMeters(float meters) { //se llama una vez mas cada vez qeu se recrea la actividad distance
        Log("OTM: " + _currentTotalMeters + " - " + meters, 10);

        _currentTotalMeters = meters;
    }

    public void SumTotalMeters(float meters) {
        float previous = _currentTotalMeters;

        if (meters == 0) {
            _currentTotalMeters = 0.0F;
            Log("Reseted");
        } else if (_currentTotalMeters + meters >= 0) {
            _currentTotalMeters += meters;
            Log("STM: " + meters + " B " + previous + " A " + _currentTotalMeters, 10);
        }

        for (DistanceChangeListener hl : listeners) {
            hl.onChangeDistance((_currentTotalMeters / 1000), (previous / 1000), GetDistanceFormatted(_currentTotalMeters), GetDistanceFormatted(previous), meters);
        }
    }

    public void SumTotalHistoryMeters(float meters) {
        float previous = _totalHistoryMeters;
        if (meters == 0) _totalHistoryMeters = 0;
        else _totalHistoryMeters += meters;

        Log("STHM: " + meters + " B " + previous + " A " + _totalHistoryMeters, 10);

        for (DistanceChangeListener hl : listeners) {
            hl.onChangeHistoryDistance((_totalHistoryMeters / 1000), 0.0F, GetDistanceFormatted(_totalHistoryMeters));
        }
    }

    public String GetDistanceFormatted(float meters) {
        String number = String.format("%.2f", (float) (meters / 1000));
        String mask = _maskForKilometers;
        mask += number;
        return mask.substring(mask.length() - _maxLengthForKilometers);
    }

    public String GetDistanceFormatted() {
        String number = String.format("%.2f", (float) (_currentTotalMeters / 1000));
        String mask = _maskForKilometers;
        mask += number;
        return mask.substring(mask.length() - _maxLengthForKilometers);
    }

    public float GetDistance() {
        return (_currentTotalMeters / 1000);
    }

    private void Log(String logText) {
        for (DistanceChangeListener hl : listeners) {
            hl.onLog(logText, 0);
        }
    }

    private void Log(String logText, int type) {
        for (DistanceChangeListener hl : listeners) {
            hl.onLog(logText, type);
        }
    }

    public void Stop() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean trackEnabled = settings.getBoolean("enable_track", false);
        if (trackEnabled) {
            _trakingFile.addCommentLine("Fin");
        }
    }

    public void addListener(DistanceChangeListener toAdd) {
        if(CountListeners() == 0)
        listeners.add(toAdd);
    }


}