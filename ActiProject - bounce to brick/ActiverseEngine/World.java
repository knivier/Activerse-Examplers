package ActiverseEngine;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;

/**
 * Represents the world where actors interact.
 * This class extends JPanel and implements ActionListener, KeyListener.
 * The world has a fixed size and a black border, and can display a background image.
 * @author Knivier
 * @version 1.2.2
 */
public class World extends JPanel implements ActionListener, KeyListener {
    private static int fps;
    private final int fixedWidth;
    private final int fixedHeight;
    private final Timer timer;
    private final List<Actor> actors;
    private final List<String> loadedImages;
    private final List<ActiverseSound> sounds;
    private final MemoryTracker memoryTracker;
    private Image backgroundImage;
    private String displayText;
    private int textX;
    private int textY;
    private JButton debugButton;
    private boolean debugMode = false;
    private boolean dynamicLighting = false;

    /**
     * Constructor for the World class.
     * Initializes the world with the given width, height, and cell size.
     * Sets the preferred size, background color, and border.
     * Initializes the list of actors, images, and sounds.
     * Creates a timer for updating the world.
     * Loads properties and sets debug mode and dynamic lighting.
     * Adds a debug button and a terminate button to the world.
     * Adds key listener and sets focus to the world.
     * @param width The width of the world in cells.
     * @param height The height of the world in cells.
     * @param cellSize The size of each cell in pixels.
     * @return none valid
     * @throws none
     * @see Actor
     */
    public World(int width, int height, int cellSize) {
        this.fixedWidth = width * cellSize;
        this.fixedHeight = height * cellSize;
        setPreferredSize(new Dimension(this.fixedWidth, this.fixedHeight));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        actors = new CopyOnWriteArrayList<>();
        loadedImages = new CopyOnWriteArrayList<>();
        sounds = new CopyOnWriteArrayList<>();
        timer = new Timer(1000 / 60, this);

        displayText = null;

        memoryTracker = new MemoryTracker();

        Properties props = loadProperties();
        boolean showDebug = Boolean.parseBoolean(props.getProperty("show_debug", "true"));
        dynamicLighting = Boolean.parseBoolean(props.getProperty("dynamic_lighting", "false"));

        if (showDebug) {
            debugButton = new JButton("Debug");
            debugButton.addActionListener(_ -> {
                debugMode = !debugMode;
                requestFocusInWindow();
                repaint();
            });
            setLayout(null);
            int buttonWidth = 80;
            debugButton.setBounds(this.fixedWidth - buttonWidth - 10, 10, buttonWidth, 30);
            add(debugButton);
        }

        JButton terminateButton = new JButton("End");
        terminateButton.setFont(new Font("Arial", Font.PLAIN, 10));
        terminateButton.setPreferredSize(new Dimension(60, 20));
        terminateButton.setBounds(this.fixedWidth - 90, 50, 60, 20);
        terminateButton.addActionListener(_ -> {
            stop();
            System.exit(0);
        });
        add(terminateButton);

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    
    /** 
     * @return int
     */
    public static int getFPS() {
        return fps;
    }

    
    /** 
     * @param fpsValue
     */
    public static void setFPS(int fpsValue) {
        fps = fpsValue;
    }

    
    /** 
     * @return Properties
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream("Activerse.properties")) {
            if (inStream != null) {
                props.load(inStream);
            } else {
                System.err.println("Activerse.properties not found or could not be loaded.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Activerse.properties: " + e.getMessage());
        }
        return props;
    }

    
    /** 
     * Sets background image of world subclasses
     * @param imagePath
     */
    public void setBackgroundImage(String imagePath) {
        backgroundImage = Toolkit.getDefaultToolkit().getImage(imagePath);
        loadedImages.add(imagePath);
    }

    /**
     * Updates the world by calling the act method of each actor at a tick interval
     * Updates memory tracker
     * @see MemoryTracker
     */
    public void update() {
        for (Actor actor : actors) {
            actor.act();
        }
        memoryTracker.update();
    }

    /**
     * Adds an actor to the world at the specified location
     * @param actor The actor to add to the world
     * @param x The x-coordinate of the actor
     * @param y The y-coordinate of the actor
     * @see Actor
     */
    public void addObject(Actor actor, int x, int y) {
        actor.setLocation(x, y);
        actor.setWorld(this);
        actors.add(actor);
        if (actor.getImage() != null) {
            loadedImages.add(actor.getImage().getPath());
        }
    }

    /**
     * Removes an actor from the world
     * @param actor The actor to remove from the world
     * @see Actor
     */
    public void removeObject(Actor actor) {
        actors.remove(actor);
        if (actor.getImage() != null) {
            loadedImages.remove(actor.getImage().getPath());
        }
    }

    /**
     * Adds a sound to the world
     * @param sound The sound to add to the world
     * @see ActiverseSound
     */
    public void addSound(ActiverseSound sound) {
        sounds.add(sound);
    }

    /**
     * Starts the world timer
     * @see Timer
     */
    public void start() {
        timer.start();
    }

    /**
     * Stops the world timer
     * @see Timer
     */
    public void stop() {
        timer.stop();
    }

    /**
     * Shows text on the world at the specified location
     * @param x The x-coordinate of the text
     * @param y The y-coordinate of the text
     * @param text The text to display
     */
    public void showText(int x, int y, String text) {
        this.textX = x;
        this.textY = y;
        this.displayText = text;
    }

    /**
     * Paints the world by drawing the background image, actors, and debug info
     * Applies dynamic lighting if enabled
     * @param g The graphics object to draw on
     * @see Graphics
     * @see Actor
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, fixedWidth, fixedHeight, this);
        }

        else if(backgroundImage == null){
            throw new NullPointerException("Background image not found");
        }

        if (dynamicLighting) {
            applyDynamicLighting(g);
        }

        for (Actor actor : actors) {
            actor.paint(g);
        }

        if (displayText != null) {
            g.setColor(Color.BLACK);
            g.drawString(displayText, textX, textY);
        }

        if (debugMode) {
            drawDebugInfo(g);
        }
    }

    /**
     * Applies dynamic lighting to the world
     * Draws light sources and shadows
     * @param g The graphics object to draw on
     */
    private void applyDynamicLighting(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Temporary buffer to avoid multiple drawing operations on the main Graphics object
        BufferedImage lightingBuffer = new BufferedImage(fixedWidth, fixedHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gLighting = lightingBuffer.createGraphics();

        // Clear the buffer
        gLighting.setComposite(AlphaComposite.Clear);
        gLighting.fillRect(0, 0, fixedWidth, fixedHeight);
        gLighting.setComposite(AlphaComposite.SrcOver);

        // Define multiple light sources
        List<LightSource> lightSources = List.of(
                new LightSource(fixedWidth / 2, fixedHeight / 2, Color.YELLOW, 300),
                new LightSource(fixedWidth / 3, fixedHeight / 3, Color.CYAN, 200),
                new LightSource(fixedWidth * 2 / 3, fixedHeight * 2 / 3, Color.MAGENTA, 250)
        );

        for (LightSource light : lightSources) {
            RadialGradientPaint gradient = new RadialGradientPaint(
                    light.x, light.y, light.radius,
                    new float[]{0f, 1f},
                    new Color[]{new Color(light.color.getRed(), light.color.getGreen(), light.color.getBlue(), 150), new Color(0, 0, 0, 0)}
            );
            gLighting.setPaint(gradient);
            gLighting.fillRect(0, 0, fixedWidth, fixedHeight);
        }

        // Draw shadows
        for (Actor actor : actors) {
            int actorX = actor.getX();
            int actorY = actor.getY();
            int shadowLength = 50; // Example shadow length

            for (LightSource light : lightSources) {
                int dx = actorX - light.x;
                int dy = actorY - light.y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < light.radius) {
                    double angle = Math.atan2(dy, dx);
                    int shadowX = (int) (actorX + shadowLength * Math.cos(angle));
                    int shadowY = (int) (actorY + shadowLength * Math.sin(angle));
                    gLighting.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    gLighting.setColor(new Color(0, 0, 0, 100));
                    gLighting.drawLine(actorX, actorY, shadowX, shadowY);
                }
            }
        }

        // Draw the lighting buffer onto the main Graphics object
        g2d.drawImage(lightingBuffer, 0, 0, null);

        gLighting.dispose();
        g2d.dispose();
    }

    /**
     * Calculates the distance between two points
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    private float calculateDistance(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Draws debug information on the world
     * Debug information can be relayed to logs.log if enabled
     * Displays FPS, memory usage, actor positions, loaded images, playing sounds, and current keys
     * Only occurs if property file allows for logging
     * @see logs.log
     * @param g The graphics object to draw on
     */
    private void drawDebugInfo(Graphics g) {
        g.setColor(Color.BLACK);
        int y = 50;

        g.drawString("FPS: " + fps + " @target " + memoryTracker.getTargetFPS(), 10, y);
        y += 20;

        g.drawString(memoryTracker.getMemoryUsagePerSecond(), 10, y);
        y += 20;

        for (Actor actor : actors) {
            String info = String.format("Actor at (%d, %d)", actor.getX(), actor.getY());
            boolean isColliding = checkCollision(actor);
            info += isColliding ? " - Colliding" : " - Not colliding";
            g.drawString(info, 10, y);
            y += 20;
        }

        StringBuilder imagesInfo = new StringBuilder("Loaded Images: ");
        for (String imagePath : loadedImages) {
            imagesInfo.append(imagePath).append(" ");
        }
        g.drawString(imagesInfo.toString(), 10, y);

        y += 20;
        g.drawString("Playing Sounds:", 10, y);
        y += 20;
        for (ActiverseSound sound : sounds) {
            if (sound.isPlaying()) {
                g.drawString("Sound: " + sound.getFilename(), 10, y);
                y += 20;
            }
        }

        y += 20;
        g.drawString("Current Keys:", 10, y);
        y += 20;
        StringBuilder keysInfo = new StringBuilder();
        for (int i = 0; i < KeyboardInfo.keys.length; i++) {
            if (KeyboardInfo.keys[i]) {
                keysInfo.append(KeyEvent.getKeyText(i)).append(" ");
            }
        }
        g.drawString(keysInfo.toString(), 10, y);
    }

    /**
     * Checks if an actor is colliding with another actor
     * @param actor The actor to check for collision
     * @return true if the actor is colliding with another actor, false otherwise
     * @see Actor
     */
    private boolean checkCollision(Actor actor) {
        for (Actor other : actors) {
            if (actor != other && CollisionManager.intersects(actor, other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the world and repaints it
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() != debugButton) {
            update();
            repaint();
        }
    }

    /**
     * Handles key presses
     */
    @Override
    public void keyPressed(KeyEvent e) {
        KeyboardInfo.keys[e.getKeyCode()] = true;
    }

    /**
     * Handles key releases
     */
    @Override
    public void keyReleased(KeyEvent e) {
        KeyboardInfo.keys[e.getKeyCode()] = false;
    }

    /**
     * Handles key typing
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // To be implemented if needed
    }
    /**
     * Returns the list of actors in the world
     * @return The list of actors in the world
     * @see Actor
     */
    public List<Actor> getActors() {
        return actors;
    }

    /**
     * Returns the list of loaded images in the world
     * @return The list of loaded images in the world
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(fixedWidth, fixedHeight);
    }

    /**
     * Light source class for dynamic lighting
     */
    private static class LightSource {
        int x, y, radius;
        Color color;

        LightSource(int x, int y, Color color, int radius) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.radius = radius;
        }
    }
}
