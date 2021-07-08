package main.java.model;

public class Vector2D {
    /**
     * The x (horizontal) component of this vector.
     */
    public final double x;

    /**
     * The y (vertical) component of this vector.
     */
    public final double y;

    /**
     * Creates a new Vector2D with the given x and y components.
     * @param x The x component.
     * @param y The y component.
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D vector) {
        this(vector.x, vector.y);
    }

    /**
     * Creates a new Vector2D where both components are zero.
     */
    public static Vector2D ZERO() {
        return new Vector2D(0, 0);
    }

    /**
     * Creates a new Vector2D pointing up with a length of 1.
     */
    public static Vector2D UP() {
        return new Vector2D(0, 1);
    }

    /**
     * Creates a new Vector2D pointing down with a length of 1.
     */
    public static Vector2D DOWN() {
        return new Vector2D(0, -1);
    }

    /**
     * Creates a new Vector2D pointing right with a length of 1.
     */
    public static Vector2D RIGHT() {
        return new Vector2D(1, 0);
    }

    /**
     * Creates a new Vector2D pointing left with a length of 1.
     */
    public static Vector2D LEFT() {
        return new Vector2D(-1, 0);
    }

    /**
     * Calculates the result of the addition of this vector with another.
     * @param b The vector to add.
     * @return The result of the addition as a new Vector2D.
     */
    public Vector2D add(Vector2D b) {
        return new Vector2D(this.x + b.x, this.y + b.y);
    }

    /**
     * Calculates the result of the subtraction of one vector from this one.
     * @param b The vector to subtract.
     * @return The result of the subtraction as a new Vector2D.
     */
    public Vector2D sub(Vector2D b) {
        return new Vector2D(this.x - b.x, this.y - b.y);
    }

    /**
     * Calculates the result of scaling this vector by a float.
     * @param factor The factor to scale by.
     * @return The result of the scaling as a new Vector2D.
     */
    public Vector2D scale(double factor) {
        return new Vector2D(this.x * factor, this.y * factor);
    }

    /**
     * Calculates the dot product of this vector with another.
     * @param b The vector to calculate the dot product to.
     * @return The dot product of the two vectors.
     */
    public double dot(Vector2D b) {
        return this.x * b.x + this.y * b.y;
    }

    /**
     * Calculates the length of this vector.
     * @return The length of this vector.
     */
    public double length() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    /**
     * Calculates the angle between this vector and another.
     * This is the basic angle calculation without directional information.
     * As such it ranges from 0 to PI.
     * @param b The vector to calculate the angle to.
     * @return The angle between the vectors
     */
    public double angle(Vector2D b) {
        return Math.acos(this.dot(b) / (this.length() * b.length()));
    }

    /**
     * Calculates the signed angle between this vector and another.
     * This is the advanced angle calculation containing directional information.
     * As such it ranges from -PI to PI.
     * @param b The vector to calculate the angle to.
     * @return The angle between the vectors
     */
    public double signedAngle(Vector2D b) {
        return Math.atan2(this.x * b.y - this.y * b.x, this.x * b.x + this.y * b.y);
    }

    /**
     * Calculates the normalized vector of this one.
     * This means that the direction is kept, but its length is scaled so it is 1.
     * @return The normalized vector.
     */
    public Vector2D normalize() {
        return this.scale(1 / this.length());
    }

    /**
     * Calculates the vector that is the result of rotating this vector by angle.
     * @return The rotated vector.
     */
    public Vector2D rotate(double angle) {
        return new Vector2D(
                Math.cos(angle) * this.x - Math.sin(angle) * this.y,
                Math.sin(angle) * this.x + Math.cos(angle) * this.y
        );
    }

    public double distanceTo(Vector2D b) {
        return b.sub(this).length();
    }

}
