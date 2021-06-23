package main.java.model.world;

import javax.vecmath.Vector2f;

public class Util {
    public static Vector2f calculateIntersectionCoordinates(Vector2f targetStart, Vector2f targetVelocity, Vector2f interceptorStart, float interceptorSpeed) {
        Vector2f offset = new Vector2f(targetStart.x - interceptorStart.x, targetStart.y - interceptorStart.y);

        float a = (float) (targetVelocity.dot(targetVelocity) - Math.pow(interceptorSpeed, 2));
        float b = 2 * offset.dot(targetVelocity);
        float c = offset.dot(offset);

        final double radicand = Math.pow(b, 2) - 4 * a * c;
        float t1 = (float) ((-b + Math.sqrt(radicand)) / (2 * a));
        float t2 = (float) ((-b - Math.sqrt(radicand)) / (2 * a));

        float t = Math.max(t1, t2);

        Vector2f intersection = new Vector2f(targetStart);
        Vector2f travelled = new Vector2f(targetVelocity);
        travelled.scale(t);
        intersection.add(travelled);

        return intersection;
    }
}
