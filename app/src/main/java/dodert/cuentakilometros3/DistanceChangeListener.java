package dodert.cuentakilometros3;

/**
 * Created by dodert on 23/04/2016.
 */
public interface DistanceChangeListener {
    void onChangeDistance(float totalDistance, float previousDistance, float distanceToAdd);

    void onChangeHistoryDistance(float totalDistance, float previousDistance, String totalDistanceFormatted);

    void onChangeSpeed(String speed);

    void onLog(String log, int type);
}

