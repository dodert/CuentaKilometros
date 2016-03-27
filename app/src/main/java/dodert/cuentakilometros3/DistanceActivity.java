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
import android.widget.TextView;

public class DistanceActivity extends AppCompatActivity {

    final String _logTag = "Monitor Location";
    public TextView _distanceTextView, _logTextView, _speedTextView, _distanceHistoryTextView;
    private MyLocationListener _gpsListener;
    protected LocationManager _lm;
    private boolean _areLocationUpdatesEnabled;
    final float _metersListener = 5;
    float _valueToAddorsubtract = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context mContext = getApplicationContext();
        setContentView(R.layout.activity_kilometros);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _distanceTextView = (TextView) findViewById(R.id.DistanceTextView);
        _distanceHistoryTextView = (TextView) findViewById(R.id.DistanceTotalTextView);
        _logTextView = (TextView) findViewById(R.id.LogTextView);
        _speedTextView = (TextView) findViewById(R.id.VelocityTextView);
        _logTextView.setMovementMethod(new ScrollingMovementMethod());

        MyLocationListener.Instance(_distanceTextView, _logTextView, _speedTextView, _distanceHistoryTextView, mContext);

        _lm = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        _gpsListener = MyLocationListener.GetInstance();

        if(savedInstanceState != null)
        {
            if(savedInstanceState.getBoolean("IsProviderEnable"))
            {
                onStartListening(null);
            }
            _distanceTextView.setText(savedInstanceState.getString("TotalDistance"));
            _distanceHistoryTextView.setText(savedInstanceState.getString("TotalHistoryDistance"));

        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);


        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String pref_meters_step = settings.getString("meters_steps", "100");
        Button test = (Button) findViewById(R.id.Plus100m);
        Button test2 = (Button) findViewById(R.id.Minus100m);
        test.setText("+ " + pref_meters_step + "m");
        test2.setText("- " + pref_meters_step + "m");
        _valueToAddorsubtract = Float.parseFloat(pref_meters_step);

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
        outState.putString("TotalDistance", _distanceTextView.getText().toString());
        outState.putString("TotalHistoryDistance", _distanceHistoryTextView.getText().toString());

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
            //check if the GPS is enabled

            if ( !_lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                buildAlertMessageNoGps();

            }
            else {
                _lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, _metersListener, _gpsListener);
                _areLocationUpdatesEnabled = true;
                Log("success on requestLocationUpdates");
            }

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
        onPlusDistance(_valueToAddorsubtract);
    }
    public void onMinusDistance100(View view) {
        onMinusDistanceBy(_valueToAddorsubtract);
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

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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
}
