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
import android.widget.TextView;

public class DistanceActivity extends AppCompatActivity implements DistanceChangeListener, NumberPicker.OnValueChangeListener {

    public static final int MY_PERMISSIONS_REQUEST_GPS = 111;
    final String _logTag = "Monitor Location";
    public TextView _logTextView, _speedTextView, _distanceHistoryTextView;
    public CustomNumberPicker _npHundreds, _npThousands, _npDozen, _npUnit, _npTenth, _npHundredth;
    private MyLocationListener _gpsListener;
    protected LocationManager _lm;
    private boolean _areLocationUpdatesEnabled;
    final float _metersListener = 5;
    float _valueToAddOrSubtract = 100;

    int _logType = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context mContext = getApplicationContext();
        setContentView(R.layout.activity_kilometros);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InitializeDistanceCounter();

        _distanceHistoryTextView = (TextView) findViewById(R.id.DistanceTotalTextView);
        _logTextView = (TextView) findViewById(R.id.LogTextView);
        _speedTextView = (TextView) findViewById(R.id.VelocityTextView);
        _logTextView.setMovementMethod(new ScrollingMovementMethod());

        MyLocationListener.Instance(mContext);

        _lm = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        _gpsListener = MyLocationListener.GetInstance();

        _gpsListener.addListener(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("IsProviderEnable")) {
                onStartListening(null);
            }
            UpdateCounter(0, savedInstanceState.getString("TotalDistance"));
            _distanceHistoryTextView.setText(savedInstanceState.getString("TotalHistoryDistance"));
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String pref_meters_step = settings.getString("meters_steps", "100");

        _valueToAddOrSubtract = Float.parseFloat(pref_meters_step);
    }

    private void InitializeDistanceCounter() {
        _npThousands = (CustomNumberPicker) findViewById(R.id.npThousands);
        _npHundreds = (CustomNumberPicker) findViewById(R.id.npHundreds);
        _npDozen = (CustomNumberPicker) findViewById(R.id.npDozens);
        _npUnit = (CustomNumberPicker) findViewById(R.id.npUnits);
        _npTenth = (CustomNumberPicker) findViewById(R.id.npTenths);
        _npHundredth = (CustomNumberPicker) findViewById(R.id.npHundredth);

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

        //InitializeListenersDistanceCounter();
    }

    private void InitializeListenersDistanceCounter() {
        _npHundreds.setOnValueChangedListener(this);

        _npHundreds.setOnScrollListener(new CustomNumberPicker.OnScrollListener() {

            @Override
            public void onScrollStateChange(NumberPicker numberPicker, int scrollState) {

                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE && scrollState != SCROLL_STATE_TOUCH_SCROLL && scrollState != SCROLL_STATE_FLING) {
                    int value = numberPicker.getValue();
                    ((CustomNumberPicker) numberPicker).putoflag = true;
                    Log("NP true " + value + " - " + scrollState, 10);
                } else {
                    ((CustomNumberPicker) numberPicker).putoflag = false;
                    Log("NP false " + " - " + scrollState, 10);
                }

            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String pref_meters_step = settings.getString("meters_steps", "100");
        Float metersToSet = Float.parseFloat(pref_meters_step);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log("Volumne UP");
                _gpsListener.SumTotalMeters(metersToSet);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log("Volumne Down");
                _gpsListener.SumTotalMeters(-metersToSet);
                return true;
            default:
                Log("Other" + keyCode);
                return false;
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GPS: {
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

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public void onStartListening(MenuItem item) {
        if (_areLocationUpdatesEnabled) {
            Log("Already started.");
            return;
        }

        Log("Monitor Location - Start Listening");
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_GPS);
                }
                return;
            }
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

    public void onStopListening(MenuItem item) {
        Log("Monitor Location - Stop Listening");
        doStopListening();
    }

    public void onSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean logEnabled = settings.getBoolean("enable_log", false);
        getApplicationContext();
        if (logEnabled) {
            if (type == _logType) {
                Log.d(_logTag, logText);
                System.out.println(_logTag + " " + logText);
                _logTextView.setText(logText + "\n" + _logTextView.getText());
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

    private String GetCurrentDistanceFormatted() {
        return _gpsListener.GetDistanceFormatted();
    }

    private boolean IsCounterFix() {
        return _npThousands.putoflag && _npHundreds.putoflag && _npDozen.putoflag && _npUnit.putoflag && _npTenth.putoflag && _npHundredth.putoflag;
    }

    private float GetCounter() {
        return (_npThousands.getValue() * 1000) + (_npHundreds.getValue() * 100) + (_npDozen.getValue() * 10) + (_npUnit.getValue()) + ((float) _npTenth.getValue() / 10) + ((float) _npHundredth.getValue() / 100);
    }

    private String GetCounterString() {
        return _gpsListener.GetDistanceFormatted(GetCounter() * 1000);
    }

    private void UpdateCounter(float distance, String distanceString) {
        String test = _gpsListener.GetDistanceFormatted();
        String hundredthPart = "0";
        hundredthPart = distanceString.substring(distanceString.length() - 1);
        int distanceLenght = distanceString.length();
        _npHundredth.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 1)));
        _npTenth.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 2, distanceLenght - 1)));
        _npUnit.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 4, distanceLenght - 3)));
        _npDozen.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 5, distanceLenght - 4)));
        _npHundreds.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 6, distanceLenght - 5)));
        _npThousands.setValue(Integer.parseInt(distanceString.substring(distanceLenght - 7, distanceLenght - 6)));

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        String idStr = getResources().getResourceEntryName(picker.getId());
        Log("onValueChange " + idStr + ",o " + oldVal + ",n " + newVal + "p:" + String.format("%b", ((CustomNumberPicker) picker).putoflag));
    }
}
