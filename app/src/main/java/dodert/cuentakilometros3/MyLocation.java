package dodert.cuentakilometros3;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.location.Location;

/**
 * Created by doder on 23/03/2016.
 */
public class MyLocation extends BaseObservable {
    private float _totalMeters;

    @Bindable
    public float getTotalMeters() {
        return _totalMeters;
    }

    @Bindable
    public float getTotalKilometers() {
        return _totalMeters / 100;
    }

    public MyLocation(float meters) {
        _totalMeters = meters;
    }
}
