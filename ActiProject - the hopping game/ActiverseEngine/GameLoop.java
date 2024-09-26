package ActiverseEngine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Represents the main game loop for updating and rendering the game.
 */
public class GameLoop implements Runnable {
    private final World world; // The game world to be updated and rendered
    private final long FRAME_TIME; // Time per frame in nanoseconds
    private int TARGET_FPS; // Target frames per second
    private int frames; // Frame counter for FPS calculation
    private long lastFpsTime; // Last time FPS was calculated
    private boolean dynamicLighting; // Flag for dynamic lighting
    private volatile boolean running = true; // Flag to control the game loop

    /**
     * Constructor to initialize the game loop with the given world.
     * Loads properties and sets initial values.
     *
     * @param world The game world to be updated and rendered.
     */
    public GameLoop(World world) {
        this.world = world;
        loadProperties();
        FRAME_TIME = 1000000000 / TARGET_FPS;
        frames = 0;
        lastFpsTime = System.nanoTime();
    }

    /**
     * Loads properties from the Activerse.properties file.
     * Sets the target FPS and dynamic lighting flag.
     */
    private void loadProperties() {
        Properties props = new Properties();
        String propertiesFile = "Activerse.properties";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input == null) {
                System.err.println("Sorry, unable to find " + propertiesFile);
                TARGET_FPS = 60;
                dynamicLighting = false;
                return;
            }
            props.load(input);
            TARGET_FPS = Integer.parseInt(props.getProperty("fps", "60"));
            dynamicLighting = Boolean.parseBoolean(props.getProperty("dynamicLighting", "false"));
        } catch (IOException e) {
            e.printStackTrace();
            TARGET_FPS = 60;
            dynamicLighting = false;
        }
    }

    /**
     * The main game loop that updates and renders the game.
     * It runs until the running flag is set to false.
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / (double) FRAME_TIME;
            lastTime = now;

            // Update the game state if enough time has passed
            while (delta >= 1) {
                update();
                delta--;
            }

            // Render the game
            render();

            // Increment the frame counter
            frames++;
            calculateFPS(now);

            // Calculate the time taken for the frame and sleep if necessary
            long frameTime = System.nanoTime() - lastTime;
            long sleepTime = FRAME_TIME - frameTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000, (int) (sleepTime % 1000000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Thread.yield();
            }
        }
    }

    /**
     * Stops the game loop by setting the running flag to false.
     */
    public void stop() {
        running = false;
    }

    /**
     * Updates the game world.
     * This method is called once per frame.
     */
    private void update() {
        world.update();
    }

    /**
     * Renders the game world.
     * This method is called once per frame.
     */
    private void render() {
        world.repaint();
    }

    /**
     * Calculates the frames per second (FPS) and updates the world with the new FPS value.
     * This method is called once per second.
     *
     * @param now The current time in nanoseconds.
     */
    private void calculateFPS(long now) {
        if (now - lastFpsTime >= 1_000_000_000) {
            World.setFPS(frames);
            frames = 0;
            lastFpsTime += 1_000_000_000;
        }
    }

    public int getTargetFps() {
        return TARGET_FPS;
    }

    public boolean isDynamicLighting() {
        return dynamicLighting;
    }
}