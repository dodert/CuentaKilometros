package com.example.doder.cuentakilometros3;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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


        MyLocationListener.Instance(DistanceTetxView, textViewTestView, VelTestView);
        _gpsListener = MyLocationListener.GetInstance();

        if(savedInstanceState != null)
        {
            //savedInstanceState.get
            if(savedInstanceState.getBoolean("IsProviderEnable"))
            {
                onStartListening(null);
            }
        }
        // }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       // outState.putst
        outState.putBoolean("IsProviderEnable", false);
        outState.putBoolean("IsProviderEnable", _isProvderEnable);


        int test;
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
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

        int test;
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
     /*   if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void onStartListening(MenuItem item) {
        Log.d(_logTag, "Monitor Location - Start Listening");
        //textViewTestView.append("\nMonitor Location - Start Listening");
        textViewTestView.setText("Start Listening: " + "\n" + textViewTestView.getText());
        //_gpsListener = new MyLocationListener(DistanceTetxView, textViewTestView, VelTestView);
        //MyLocationListener.Instance(DistanceTetxView, textViewTestView, VelTestView);
        //_gpsListener = MyLocationListener.GetInstance();
        //_lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            _lm = (LocationManager) getSystemService(LOCATION_SERVICE);

            // _networkListener = new MyLocationListener();
            //lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, _networkListener);


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
            _lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 3, _gpsListener);
            _isProvderEnable = true;
            //textViewTestView.append("\nsuccess on requestLocationUpdates");
            textViewTestView.setText("success on requestLocationUpdates" + "\n" + textViewTestView.getText());
        } catch (Exception e) {
            //textViewTestView.append("\nError on requestLocationUpdates" + e.getMessage());
            textViewTestView.setText("Error on requestLocationUpdates" + e.getMessage() + "\n" + textViewTestView.getText());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public void onStopListening(MenuItem item) {
        Log.d(_logTag, "Monitor Location - Stop Listening");
        //textViewTestView.append("\nMonitor Location - Stop Listening");
        textViewTestView.setText("Monitor Location - Stop Listenings" + "\n" + textViewTestView.getText());
        doStopListening();
    }

    public void onRecentLocation(MenuItem item) {
        Log.d(_logTag, "Monitor - Recent Location");
        //textViewTestView.append("\nMonitor - Recent Location");
        textViewTestView.setText("Monitor - Recent Location" + "\n" + textViewTestView.getText());


        Location gpsLocation;

       // _lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        //networkLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
        gpsLocation = _lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //String networkLogMessage = LogHelper.FormatLocationInfo(networkLocation);
        String gpsLogMessage = LogHelper.FormatLocationInfo(gpsLocation);

        //Log.d(_logTag, "Monitor Location" + networkLogMessage);
        Log.d(_logTag, "Monitor Location" + gpsLogMessage);
        //textViewTestView.append("\n Monitor Location" + gpsLogMessage);
        textViewTestView.setText("Monitor Location" + gpsLogMessage + "\n" + textViewTestView.getText());

    }

    public void onSingleLocation(MenuItem item) {
        Log.d(_logTag, "Monitor - Single Location");
        textViewTestView.append("\nMonitor - Single Location");
        textViewTestView.setText("Monitor - Single Location" + "\n" + textViewTestView.getText());
       // LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        //_networkListener = new MyLocationListener();
        //lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, _networkListener, null);

      // _gpsListener = new MyLocationListener();
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

        _lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, _gpsListener, null);


    }

    public void onExit(MenuItem item) {
        Log.d(_logTag, "Monitor Location Exit");

        doStopListening();

        finish();
    }

    void doStopListening() {


        /*if (_networkListener != null) {
            lm.removeUpdates(_networkListener);
            _networkListener = null;
        }*/
        if (_gpsListener != null) {
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
            _lm.removeUpdates(_gpsListener);
            _isProvderEnable = false;
            //_gpsListener = null;
        }
    }

    public void onMinusDistanceByOne(View view) {
        onMinusDistanceBy(500F);
    }

    public void onPlusDistanceByOne(View view) {
        onPlusDistance(500F);
    }

    private void onMinusDistanceBy(float meters) {
        if (_gpsListener!= null)
            _gpsListener.SubstractTotalMeters(meters);
        else
            textViewTestView.setText("gpsListener null" + "\n" + textViewTestView.getText());
    }

    private void onPlusDistance(float meters) {
        if (_gpsListener!= null)
            _gpsListener.SumTotalMeters(meters);
        else
            textViewTestView.setText("gpsListener null" + "\n" + textViewTestView.getText());
    }

    public void onResetDistance(View view) {
        if (_gpsListener!= null)
            _gpsListener.ResetTotalMeters();
        else
            textViewTestView.setText("gpsListener null" + "\n" + textViewTestView.getText());
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
}
