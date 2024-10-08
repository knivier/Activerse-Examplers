package ActiverseEngine;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * The base class for all actors in the world.
 */
public abstract class Actor {
    protected double direction; // Changed access to protected
    private int x, y;
    private World world;
    private ActiverseImage image;
    private double velocityX, velocityY;
    private int height; // Added height field
    private int width; // Added width field

    /**
     * Performs the actor's action.
     */
    public abstract void act();

    /**
     * Sets the location of the actor.
     *
     * @param x The x-coordinate of the new location.
     * @param y The y-coordinate of the new location.
     */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate of the actor's location.
     *
     * @return The x-coordinate of the actor's location.
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of the actor's location.
     *
     * @param x The x-coordinate to set.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gets the y-coordinate of the actor's location.
     *
     * @return The y-coordinate of the actor's location.
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of the actor's location.
     *
     * @param y The y-coordinate to set.
     */
    public void setY(int y) {
        this.y = y;
    }
    public int getWidth() {
        return width;
    }
    /**
     * Gets the image of the actor.
     *
     * @return The image of the actor, or null if no image is set.
     */
    public ActiverseImage getImage() {
        if (image == null) {
            System.err.println("Warning: Image not set for actor.");
            return null;
        }
        return image;
    }

    /**
     * Sets the image of the actor.
     *
     * @param image The image to set for the actor.
     */
    public void setImage(ActiverseImage image) {
        this.image = image;
    }

    /**
     * Draws the actor on the given graphics context.
     *
     * @param g The graphics context to draw on.
     */
    public void paint(Graphics g) {
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g;
            width = image.getImage().getWidth(null);
            int height = image.getImage().getHeight(null);

            AffineTransform old = g2d.getTransform();

            g2d.rotate(direction, x + (double) width / 2, y + (double) height / 2);
            g2d.drawImage(image.getImage(), x, y, null);

            g2d.setTransform(old);
        } else {
            System.err.println("Warning: Image not set for actor.");
        }
    }

    /**
     * Gets the world that the actor belongs to.
     *
     * @return The world that the actor belongs to.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Sets the world that the actor belongs to.
     *
     * @param world The world that the actor belongs to.
     */
    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Checks if this actor intersects with another actor.
     *
     * @param other The other actor to check for intersection.
     * @return true if this actor intersects with the other actor, false otherwise.
     */
    public boolean intersects(Actor other) {
        Rectangle r1 = getBoundingBox();
        Rectangle r2 = other.getBoundingBox();
        return r1.intersects(r2);
    }

    /**
     * Gets the bounding box of the actor.
     *
     * @return The bounding box of the actor.
     */
    protected Rectangle getBoundingBox() {
        ActiverseImage img = getImage();
        if (img != null) {
            return new Rectangle(x, y, img.getImage().getWidth(null), img.getImage().getHeight(null));
        } else {
            return new Rectangle(x, y, 0, 0);
        }
    }

    /**
     * Moves the actor in its current direction by the specified distance.
     *
     * @param distance The distance to move the actor.
     */
    public void move(int distance) {
        int dx = (int) (distance * Math.cos(direction));
        int dy = (int) (distance * Math.sin(direction));
        int newX = x + dx;
        int newY = y + dy;
        // Check if the new position exceeds the world limits
        if (newX >= 0 && newX + getImage().getImage().getWidth(null) <= world.getWidth() &&
                newY >= 0 && newY + getImage().getImage().getHeight(null) <= world.getHeight()) {
            x = newX;
            y = newY;
        }
    }

    /**
     * Turns the actor by the specified angle.
     *
     * @param angle The angle to turn the actor (in radians).
     */
    public void turn(double angle) {
        direction = (direction + angle) % (2 * Math.PI);
    }

    /**
     * Finds the intesrsectiuon of the actor with another actor then returns it
     * @return The intersecting actor, or null if no intersection is found.
     */
    public Actor getOneIntersectingObject() {
        List<Actor> actors = getWorld().getActors(); // Assume getActors() returns a List<Actor>
        for (Actor other : actors) {
            // Avoid checking against itself
            if (other != this && CollisionManager.intersects(this, other)) {
                return other; // Return the first intersecting actor
            }
        }

        return null; // No intersection found
    }
    /**
     * Delays the execution of the next action by the specified milliseconds.
     *
     * @param ms The delay in milliseconds.
     */
    public void delayNext(int ms) {
        new Thread(() -> {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                System.out.println("An unidentified exception occurred while delaying the next action. Please see the stack trace for more information.");
                System.out.println("Contact ActiverseEngine support for bugs, and provide the stack trace.");
                e.printStackTrace();
            }
            act();
        }).start();
    }

    /**
     * Checks if a specific key is currently pressed.
     *
     * @param key The character representing the key to check.
     * @return true if the key is currently pressed, false otherwise.
     */
    public boolean keyIsDown(char key) {
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(key);
        return KeyboardInfo.isKeyDown(keyCode);
    }

    /**
     * Gets the horizontal velocity of the actor.
     *
     * @return The horizontal velocity of the actor.
     */
    public double getVelocityX() {
        return velocityX;
    }

    /**
     * Sets the horizontal velocity of the actor.
     *
     * @param velocityX The horizontal velocity to set.
     */
    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    /**
     * Gets the vertical velocity of the actor.
     *
     * @return The vertical velocity of the actor.
     */
    public double getVelocityY() {
        return velocityY;
    }

    /**
     * Sets the vertical velocity of the actor.
     *
     * @param velocityY The vertical velocity to set.
     */
    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    /**
     * Gets the height of the actor.
     *
     * @return The height of the actor.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the actor.
     *
     * @param height The height to set.
     */
    public void setHeight(int height) {
        this.height = height;
    }
}