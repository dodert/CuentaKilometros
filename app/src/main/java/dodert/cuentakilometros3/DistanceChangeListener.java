package dodert.cuentakilometros3;

/**
 * Created by dodert on 23/04/2016.
 */
public interface DistanceChangeListener {
    void onChangeDistance(float totalDistance, float previousDistance, String totalDistanceFormatted);
}
