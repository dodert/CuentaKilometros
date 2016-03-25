package dodert.cuentakilometros3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Kilometros extends AppCompatActivity {

    final String _logTag = "Monitor Location";
    public TextView _distanceTextView, _logTextView, _speedTextView, _distanceHistoryTextView;
    private MyLocationListener _gpsListener;
    protected LocationManager _lm;
    private boolean _areLocationUpdatesEnabled;
    final float _metersListener = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kilometros);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _distanceTextView = (TextView) findViewById(R.id.DistanceTextView);
        _distanceHistoryTextView = (TextView) findViewById(R.id.DistanceTotalTextView);
        _logTextView = (TextView) findViewById(R.id.LogTextView);
        _speedTextView = (TextView) findViewById(R.id.VelocityTextView);
        _logTextView.setMovementMethod(new ScrollingMovementMethod());
        Context mContext = getApplicationContext();
        MyLocationListener.Instance(_distanceTextView, _logTextView, _speedTextView, _distanceHistoryTextView,  getApplicationContext());

        _lm = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        _gpsListener = MyLocationListener.GetInstance();

        if(savedInstanceState != null)
        {
            if(savedInstanceState.getBoolean("IsProviderEnable"))
            {
                onStartListening(null);
            }
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
    }

    @Override
    protected  void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepEnableGpsWhenBackground = settings.getBoolean("keep_enable_gps_when_background", true);
        if(!keepEnableGpsWhenBackground) {
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

    public void onStartListening(MenuItem item) {
        Log("Monitor Location - Start Listening");
        try {
            //_lm = (LocationManager) getSystemService(LOCATION_SERVICE);
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
            _lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, _metersListener, _gpsListener);
            _areLocationUpdatesEnabled = true;
            Log("success on requestLocationUpdates");
        } catch (Exception e) {
            Log("Error on requestLocationUpdates" + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            _lm.removeUpdates(MyLocationListener.GetInstance());

            _areLocationUpdatesEnabled = false;
            //_gpsListener = null;
        }
    }

    private void onMinusDistanceBy(float meters) {
        if (_gpsListener!= null)
            _gpsListener.SubstractTotalMeters(meters);
        else
            Log("gpsListener null");
    }

    private void onPlusDistance(float meters) {
        if (_gpsListener!= null)
            _gpsListener.SumTotalMeters(meters);
        else
            Log("gpsListener null");
    }

    public void onResetDistance(View view) {
        if (_gpsListener!= null) {
            _gpsListener.ResetTotalMeters();
        }
        else
            Log("gpsListener null");
    }

    public void onPlusDistance100(View view) {
        onPlusDistance(100.0F);
    }

    public void onPlusDistance200(View view) {
        onPlusDistance(200.0F);
    }

    public void onPlusDistance500(View view) {
        onPlusDistance(500.0F);
    }

    public void onMinusDistance100(View view) {
        onMinusDistanceBy(100.0F);
    }

    public void onMinusDistance200(View view) {
        onMinusDistanceBy(200.0F);
    }

    public void onMinusDistance500(View view) {
        onMinusDistanceBy(500.0F);
    }

    private void Log(String logText) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean logEnabled = settings.getBoolean("enable_log", false);
        getApplicationContext();
        if (logEnabled) {
            Log.d(_logTag, logText);
            _logTextView.setText(logText + "\n" + _logTextView.getText());
        }
    }
}
