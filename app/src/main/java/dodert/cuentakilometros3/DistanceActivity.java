package dodert.cuentakilometros3;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Console;
import java.util.Set;

public class DistanceActivity extends AppCompatActivity implements DistanceChangeListener, NumberPicker.OnValueChangeListener {
    public static final int MY_PERMISSIONS_REQUEST_GPS = 111;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 112;
    public static final String CONTROL_BY_VOLUME = "10";
    public static final String CONTROL_BY_MEDIA = "20";
    final String _logTag = "Monitor Location";
    public TextView _logTextView, _speedTextView, _distanceHistoryTextView;
    public TextView _speedLabelTextView, _distanceLabelTextView;
    public CustomNumberPicker _npHundreds, _npThousands, _npDozen, _npUnit, _npTenth, _npHundredth;
    private MyLocationListener _gpsListener;
    protected LocationManager _lm;
    private boolean _areLocationUpdatesEnabled;
    final float _metersListener = 5;
    float _valueToAddOrSubtract = 100;
    RelativeLayout _relativeLayout;

    int _logType = 10;
    private Context _context;

    private SharedPreferences _sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kilometros);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        InitializeMain();
        InitializeDistanceCounter();
        InitializeLocationListener();
        InitializeFromSharedSettings();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("IsProviderEnable")) {
                onStartListening(null);
            }
            UpdateCounter(0, savedInstanceState.getString("TotalDistance"));
            _distanceHistoryTextView.setText(savedInstanceState.getString("TotalHistoryDistance"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("IsProviderEnable", false);
        outState.putBoolean("IsProviderEnable", _areLocationUpdatesEnabled);
        outState.putString("TotalDistance", GetCurrentDistanceFormatted());
        outState.putString("TotalHistoryDistance", _distanceHistoryTextView.getText().toString());
    }

    @Override
    protected void onResume() {
        rerefreshLayaout();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        boolean keepEnableGpsWhenBackground = settings.getBoolean("keep_enable_gps_when_background", true);
        if (!keepEnableGpsWhenBackground) {
            if (_lm != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                _lm.removeUpdates(MyLocationListener.GetInstance());
            }
        }
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String pref_meters_step = settings.getString("meters_steps", "100");

        Set<String> selections = settings.getStringSet("increment_types", null);

        Float metersToSet = Float.parseFloat(pref_meters_step);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                if(selections == null || (selections != null && !selections.contains(CONTROL_BY_MEDIA))) return  false;
                Log("Media Next");
                _gpsListener.SumTotalMeters(metersToSet);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(selections == null || (selections != null && !selections.contains(CONTROL_BY_VOLUME))) return  false;
                Log("Volumne UP");
                _gpsListener.SumTotalMeters(metersToSet);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                if(selections == null || (selections != null && !selections.contains(CONTROL_BY_MEDIA))) return  false;
                Log("Media Previous");
                _gpsListener.SumTotalMeters(-metersToSet);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(selections == null || (selections != null && !selections.contains(CONTROL_BY_VOLUME))) return  false;
                Log("Volumne Down");
                _gpsListener.SumTotalMeters(-metersToSet);
                return true;
            default:
                Log("Other" + keyCode);
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kilometros, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startListening();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public void onChangeDistance(float totalDistance, float previousDistance, String totalDistanceFormatted, String previousDistanceFormatted, float distanceToAdd) {
        /*Log("test benja - " + totalDistanceFormatted);
        String test = GetCounterString();

        System.out.println("previous: " + previousDistanceFormatted + " - Counter: " + GetCounterString());*/
        //  _npHundreds.putoflag = true;
        boolean isFix = IsCounterFix();
        Log(String.format("%b", isFix));
        if (isFix) {
            if (previousDistanceFormatted.compareTo(GetCounterString()) != 0) {

                //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                //Log("cambiar");
                float currentCounter = GetCounter();

                //problema con la distancia y la suma con decimales, depende de los decimas cambia un poco la distancia. ya lo arreglere
                _gpsListener.OverrideTotalMeters(currentCounter * 1000 + distanceToAdd);
                float newcount = _gpsListener.GetDistance();
                String newcountstring = _gpsListener.GetDistanceFormatted();

                totalDistance = newcount;
                totalDistanceFormatted = newcountstring;
            }
        } else {
            System.out.println("tocando counter");
        }


        UpdateCounter(totalDistance, totalDistanceFormatted);
    }
    @Override
    public void onChangeHistoryDistance(float totalDistance, float previousDistance, String totalDistanceFormatted) {
        _distanceHistoryTextView.setText(totalDistanceFormatted);
    }

    @Override
    public void onChangeSpeed(String speed) {
        _speedTextView.setText(speed);
    }

    @Override
    public void onLog(String log, int type) {
        Log(log, type);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        String idStr = getResources().getResourceEntryName(picker.getId());
        Log("onValueChange " + idStr + ",o " + oldVal + ",n " + newVal + "p:" + String.format("%b", ((CustomNumberPicker) picker).putoflag));
    }

    private void rerefreshLayaout() {
        setSpeedAndDistanceLabels();
    }

    private void InitializeDistanceCounter() {

        _npThousands = findViewById(R.id.npThousands);
        _npHundreds = findViewById(R.id.npHundreds);
        _npDozen = findViewById(R.id.npDozens);
        _npUnit = findViewById(R.id.npUnits);
        _npTenth = findViewById(R.id.npTenths);
        _npHundredth = findViewById(R.id.npHundredth);
        _speedLabelTextView = findViewById(R.id.speedLabel);
        _distanceLabelTextView = findViewById(R.id.distanceLabel);

        _npThousands.setMinValue(0);
        _npThousands.setMaxValue(9);
        _npHundreds.setMinValue(0);
        _npHundreds.setMaxValue(9);
        _npDozen.setMinValue(0);
        _npDozen.setMaxValue(9);
        _npUnit.setMinValue(0);
        _npUnit.setMaxValue(9);
        _npTenth.setMinValue(0);
        _npTenth.setMaxValue(9);
        _npHundredth.setMinValue(0);
        _npHundredth.setMaxValue(9);


        _distanceHistoryTextView = findViewById(R.id.DistanceTotalTextView);
        _logTextView = findViewById(R.id.LogTextView);
        _speedTextView = findViewById(R.id.VelocityTextView);
        _logTextView.setMovementMethod(new ScrollingMovementMethod());
        //InitializeListenersDistanceCounter();
    }

    public void InitializeLocationListener() {
        MyLocationListener.Instance(_context);
        _lm = (LocationManager) _context.getSystemService(LOCATION_SERVICE);
        _gpsListener = MyLocationListener.GetInstance();
        _gpsListener.addListener(this);

        Log("Listeners" + _gpsListener.CountListeners(), 10);
    }

    private void InitializeMain() {
        _relativeLayout = findViewById(R.id.relativeLayout);
        _context = getApplicationContext();
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
    }

    private void InitializeFromSharedSettings() {
        // SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
        String pref_meters_step = _sharedPreferences.getString("meters_steps", "100");
        _valueToAddOrSubtract = Float.parseFloat(pref_meters_step);
    }

    private void setSpeedAndDistanceLabels() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean useMiles = settings.getBoolean("use_miles", false);

        if(useMiles)
        {
            _speedLabelTextView.setText(R.string.pref_speedLabel_miles);
            _distanceLabelTextView.setText(R.string.pref_distanceLabel_miles);
        }
        else
        {
            _speedLabelTextView.setText(R.string.pref_speedLabel_kilometres);
            _distanceLabelTextView.setText(R.string.pref_distanceLabel_kilometres);
        }
    }

    private void startListening() {

        boolean test = false;
        if (_areLocationUpdatesEnabled || test == true) {
            Log("Already started.");
            return;
        }
        Log("Monitor Location - Start Listening");
        try {
            checkPermissionsWhenStartListening();
            //check if the GPS is enabled

            if (!_lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            } else {

                _lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, _metersListener, _gpsListener);
                _areLocationUpdatesEnabled = true;
                this.setTitle(this.getTitle());
                Log("success on requestLocationUpdates");
            }

        } catch (Exception e) {
            Log("Error on requestLocationUpdates" + e.getMessage());
            e.printStackTrace();
        }

    }

    private void checkPermissionsWhenStartListening() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_GPS);
            return;
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        boolean trackEnabled = settings.getBoolean("enable_track", false);

        if (trackEnabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return;
            }
        }
    }

    public void onStartListening(MenuItem item) {
        startListening();
    }

    public void onStopListening(MenuItem item) {
        Log("Monitor Location - Stop Listening");
        doStopListening();
    }

    public void onSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra( SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName() );
        intent.putExtra( SettingsActivity.EXTRA_NO_HEADERS, true );
        startActivity(intent);
    }

    public void onExit(MenuItem item) {
        Log("Monitor Location Exit");

        doStopListening();
        _gpsListener = null;

        finish();
    }

    void doStopListening() {
        if (_gpsListener != null && _lm != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            MyLocationListener test = MyLocationListener.GetInstance();

            _lm.removeUpdates(test);
            test.Stop();

            _areLocationUpdatesEnabled = false;
            //_gpsListener = null;
        }
    }

    public void onResetDistance(View view) {
        if (_gpsListener!= null) {
            _gpsListener.ResetTotalMeters();
        }
        else
            Log("gpsListener null");
    }

    private void Log(String logText, int type) {
        //getApplicationContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean logEnabled = settings.getBoolean("enable_log", false);

        if (logEnabled) {
            if (type == _logType) {
                Log.d(_logTag, logText);
                System.out.println(_logTag + " " + logText);
                _logTextView.setText(String.format("%s\n%s", logText, _logTextView.getText()));
            }
        }
    }

    private void Log(String logText) {
        Log(logText, 0);
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.messages_GPS_disabled)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private String GetCurrentDistanceFormatted() {
        return _gpsListener.GetDistanceFormatted();
    }

    private boolean IsCounterFix() {
        return _npThousands.putoflag && _npHundreds.putoflag && _npDozen.putoflag && _npUnit.putoflag && _npTenth.putoflag && _npHundredth.putoflag;
    }

    private float GetCounter() {
        return (_npThousands.getValue() * 1000)
                + (_npHundreds.getValue() * 100)
                + (_npDozen.getValue() * 10)
                + (_npUnit.getValue()) + ((float) _npTenth.getValue() / 10)
                + ((float) _npHundredth.getValue() / 100);
    }

    private String GetCounterString() {
        return _gpsListener.GetDistanceFormatted(GetCounter() * 1000);
    }

    private void UpdateCounter(float distance, String distanceString) {
        String test = _gpsListener.GetDistanceFormatted();
        String hundredthPart = "0";
        hundredthPart = distanceString.substring(distanceString.length() - 1);
        int distanceLenght = distanceString.length();
        //TODO review context of labes to fix bug aobut missing update after rotate screen
        _npHundredth.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 1)));
        _npTenth.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 2, distanceLenght - 1)));
        _npUnit.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 4, distanceLenght - 3)));
        _npDozen.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 5, distanceLenght - 4)));
        _npHundreds.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 6, distanceLenght - 5)));
        _npThousands.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 7, distanceLenght - 6)));

    }

}
