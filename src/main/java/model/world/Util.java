package main.java.model.world;

import lombok.AllArgsConstructor;
import main.java.model.Vector2D;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

@AllArgsConstructor
class BatchState {
    public long lastExecution;
    public boolean isTimerSet;
}

public class Util {
    public static Timer batchTimer = new Timer(true);
    public static Hashtable<String, BatchState> batchStates = new Hashtable<>();

    public static void batch(String batchKey, Runnable task, int executionsPerSecond) {
        String key = null;

        for (String mapKey :
                batchStates.keySet()) {
            if (mapKey.equals(batchKey)) {
                //TODO Fix Error
                key = mapKey;
            }
        }

        if (key == null) {
            key = batchKey;
            batchStates.put(key, new BatchState(0, false));
        }

        BatchState batchState = batchStates.get(key);

        long now = System.nanoTime();
        long timeSinceLastExecution = now - batchState.lastExecution;

        if (timeSinceLastExecution < 1000000000f / executionsPerSecond) {
            if (!batchState.isTimerSet) {
                batchState.isTimerSet = true;
                batchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        task.run();
                        batchState.lastExecution = System.nanoTime();
                        batchState.isTimerSet = false;
                    }
                }, (long) ((1000f / executionsPerSecond) - timeSinceLastExecution / 1000000f));
            }
            return;
        }

        task.run();
        batchState.lastExecution = System.nanoTime();
    }

    public static Vector2D calculateIntersectionCoordinates(Vector2D targetStart, Vector2D targetVelocity, Vector2D interceptorStart, double interceptorSpeed) {
        Vector2D offset = targetStart.sub(interceptorStart);

        double a = targetVelocity.dot(targetVelocity) - Math.pow(interceptorSpeed, 2);
        double b = 2 * offset.dot(targetVelocity);
        double c = offset.dot(offset);

        final double discriminant = Math.pow(b, 2) - 4 * a * c;

        if (discriminant < 0) {
            return null;
        }

        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);

        double t;
        if (Math.min(t1, t2) >= 0) {
            t = Math.min(t1, t2);
        } else {
            t = Math.max(t1, t2);
        }

        Vector2D travelled = targetVelocity.scale(t);

        return targetStart.add(travelled);
    }
}
