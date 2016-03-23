package dodert.cuentakilometros3;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Kilometros extends AppCompatActivity {

    final String _logTag = "Monitor Location";
    public TextView DistanceTetxView;
    public TextView textViewTestView;
    public TextView VelTestView;
    private MyLocationListener _gpsListener;
    private LocationManager _lm;
    private boolean _isProvderEnable;
    final float _metersLisener = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kilometros);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DistanceTetxView = (TextView) findViewById(R.id.DistanceLabel);
        textViewTestView = (TextView) findViewById(R.id.textViewTest);
        VelTestView = (TextView) findViewById(R.id.VelocityLabel);
        textViewTestView.setMovementMethod(new ScrollingMovementMethod());

        MyLocationListener.Instance(DistanceTetxView, textViewTestView, VelTestView, getApplicationContext());
        _gpsListener = MyLocationListener.GetInstance();

        if(savedInstanceState != null)
        {
            if(savedInstanceState.getBoolean("IsProviderEnable"))
            {
                onStartListening(null);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("IsProviderEnable", false);
        outState.putBoolean("IsProviderEnable", _isProvderEnable);
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
        //preferences.
        if (_lm != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            _lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            _lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, _metersLisener, _gpsListener);
            _isProvderEnable = true;
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
        // startActivityForResult(new Intent(SettingsActivity), 0);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        //Intent launchNewIntent = new Intent(CurrentClass.this,SettingsActivity.class);
        //startActivityForResult(launchNewIntent, 0);
       /* Log.d(_logTag, "Monitor - Recent Location");
        textViewTestView.setText("Monitor - Recent Location" + "\n" + textViewTestView.getText());

        Location gpsLocation;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        gpsLocation = _lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        String gpsLogMessage = LogHelper.FormatLocationInfo(gpsLocation);

        Log.d(_logTag, "Monitor Location" + gpsLogMessage);
        textViewTestView.setText("Monitor Location" + gpsLogMessage + "\n" + textViewTestView.getText());
*/
    }

    public void onExit(MenuItem item) {
        Log("Monitor Location Exit");

        doStopListening();

        finish();
    }

    void doStopListening() {
        if (_gpsListener != null && _lm != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            _lm.removeUpdates(_gpsListener);
            _isProvderEnable = false;
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
            Log("Reseted");
        }
        else
            Log("gpsListener null");
    }

    public void onPlusDistance100(View view) {
        onPlusDistance(100F);
    }

    public void onPlusDistance200(View view) {
        onPlusDistance(200F);
    }

    public void onPlusDistance500(View view) {
        onPlusDistance(500F);
    }

    public void onMinusDistance100(View view) {
        onMinusDistanceBy(100F);
    }

    public void onMinusDistance200(View view) {
        onMinusDistanceBy(200F);
    }

    public void onMinusDistance500(View view) {
        onMinusDistanceBy(500F);
    }

    private void Log(String logText) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean logEnabled = settings.getBoolean("enable_log", false);
        getApplicationContext();
        if (logEnabled) {
            Log.d(_logTag, logText);
            textViewTestView.setText(logText + "\n" + textViewTestView.getText());
        }
    }
}
