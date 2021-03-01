package dodert.cuentakilometros3;
import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Set;

import dodert.tools.Helpers;


public class DistanceActivity extends AppCompatActivity implements DistanceChangeListener, NumberPicker.OnValueChangeListener {
    public static final int MY_PERMISSIONS_REQUEST_GPS = 111;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 112;
    public static final String CONTROL_BY_VOLUME = "10";
    public static final String CONTROL_BY_MEDIA = "20";
    public static final String IS_PROVIDER_ENABLE = "IsProviderEnable";
    public static final String TOTAL_HISTORY_DISTANCE = "TotalHistoryDistance";
    public static final String TOTAL_DISTANCE = "TotalDistance";
    public static final String IS_REVERSE = "IsReverse";
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
    private boolean _reverseCount = false;

    int _logType = 40;// 10;
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
            _areLocationUpdatesEnabled = savedInstanceState.getBoolean(IS_PROVIDER_ENABLE);
            if (_areLocationUpdatesEnabled) {
                onStartListening(null);
            }

            //todo: revisar si pasar la distancia guardad o no
            //UpdateCounter(0, savedInstanceState.getString(TOTAL_DISTANCE));
            UpdateCounter(savedInstanceState.getFloat(TOTAL_DISTANCE));
            _distanceHistoryTextView.setText(savedInstanceState.getString(TOTAL_HISTORY_DISTANCE));
            //_reverseCount = savedInstanceState.getBoolean(IS_REVERSE);
            setReversCount(savedInstanceState.getBoolean(IS_REVERSE));
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_PROVIDER_ENABLE, _areLocationUpdatesEnabled);
        //outState.putString(TOTAL_DISTANCE, GetCurrentDistanceFormatted());
        outState.putFloat(TOTAL_DISTANCE, _gpsListener.GetDistance());
        outState.putString(TOTAL_HISTORY_DISTANCE, _distanceHistoryTextView.getText().toString());

        outState.putBoolean(IS_REVERSE, _reverseCount);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        RefreshLayout();
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

                _lm.removeUpdates(MyLocationListener.Instance(_context));
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
                    StartListening();

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
    public void onChangeDistance(float totalDistance, float previousDistance, float distanceToAdd) {
        boolean isFix = IsCounterFix();
        Log(String.format("%b", isFix));
        if (isFix) {

            float previousDistance_truncated = Helpers.Truncate(previousDistance, 2);

            //new to convert to the same symbol to compare.
            if (_reverseCount){
                previousDistance_truncated = Math.abs(previousDistance_truncated) * -1;
            }
            else
            {
                previousDistance_truncated = Math.abs(previousDistance_truncated);
            }

            if(previousDistance_truncated != GetCounter()){

                float currentCounter = GetCounter();

                _gpsListener.OverrideTotalMeters((currentCounter * 1000) + distanceToAdd);

                totalDistance = _gpsListener.GetDistance();
            }
        } else {
            System.out.println("tocando counter");
        }

        UpdateCounter(totalDistance);
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

    private void RefreshLayout() {
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
        _gpsListener = MyLocationListener.Instance(_context);
        _lm = (LocationManager) _context.getSystemService(LOCATION_SERVICE);
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

    @SuppressLint("MissingPermission")
    private void StartListening() {

        boolean test = false;
        if (_areLocationUpdatesEnabled || test == true) {
            Log("Already started.");
            return;
        }

        Log("Monitor Location - Start Listening");
        try {
            CheckPermissionsWhenStartListening();
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

    private void CheckPermissionsWhenStartListening() {
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
        StartListening();
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
            MyLocationListener test = MyLocationListener.Instance(_context);

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

    private void setReversCount(boolean isReversCount){
        _reverseCount = isReversCount;
        _gpsListener._reversados = isReversCount;
        setTextReverseButton(!isReversCount);
    }

    private void setTextReverseButton(boolean isReversCount)
    {
        Button buttonReverseForward = (Button)findViewById(R.id.reverseButton);
        if (!isReversCount){
            buttonReverseForward.setText("Forward");
        }
        else
        {
            buttonReverseForward.setText("Reverse");
        }
    }
    public void onReverseCount(View view){

        setReversCount(!_reverseCount);

        Log("test onreverse");
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

        //String sValue = (String) String.format("%.2f", previousDistance);
        String sValue = String.format("%s%s%s%s.%s%s"
                ,_npThousands.getValue(), _npHundreds.getValue(), _npDozen.getValue()
                ,_npUnit.getValue(), _npTenth.getValue(),_npHundredth.getValue());

       // Utils.Truncate();

        float counters = Float.parseFloat(sValue);
/*
        float counters = (_npThousands.getValue() * 1000)
                + (_npHundreds.getValue() * 100)
                + (_npDozen.getValue() * 10)
                + (_npUnit.getValue())
                + ((float) _npTenth.getValue() / 10)
                + ((float) _npHundredth.getValue() / 100);
*/
        if (_reverseCount){
            counters = Math.abs(counters) * -1;
        }
        else
        {
            counters = Math.abs(counters);
        }



        return counters;
    }

    private String GetCounterString() {
        return _gpsListener.GetDistanceFormatted(GetCounter() * 1000);
    }

    private void UpdateCounter(float distance) {
        //distance = 123456.7890f;
        //String test = _gpsListener.GetDistanceFormatted();
        //String hundredthPart = "0";
        //hundredthPart = distanceString.substring(distanceString.length() - 1);
        //int distanceLenght = distanceString.length();
        int hundredth = 0, tenth = 0, unit = 0, dozen = 0, hundreds = 0, thousands = 0;

        float distanceAbs = Math.abs(distance);
        //Log(String.format("distanceAbs %s, distance %s, distanceString %s", distanceAbs, distance, distanceString), 20);
        if (distanceAbs<=100)
        {
            Log("adfasdfas");
        }
        // 0.80
        hundredth = (int) (distanceAbs/0.01) % 10;
        tenth = (int) (distanceAbs/0.1) % 10;
        unit =  (int) (distanceAbs/1) % 10;
        dozen =  (int) (distanceAbs/10) % 10;
        hundreds =  (int) (distanceAbs/100) % 10;
        thousands =  (int) (distanceAbs/1000) % 10;
        Log(String.format("distance %s", distance), 20);
        Log(String.format("tenth %s unit %s", tenth, unit),10);
        _npHundredth.setValue(hundredth);
        _npTenth.setValue(tenth);
        _npUnit.setValue(unit);
        _npDozen.setValue(dozen);
        _npHundreds.setValue(hundreds);
        _npThousands.setValue(thousands);

        //TODO review context of labes to fix bug aobut missing update after rotate screen, i thing this is already fixed
        /*_npHundredth.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 1)));
        _npTenth.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 2, distanceLenght - 1)));
        _npUnit.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 4, distanceLenght - 3)));
        _npDozen.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 5, distanceLenght - 4)));
        _npHundreds.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 6, distanceLenght - 5)));
        _npThousands.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 7, distanceLenght - 6)));
*/
    }

}
