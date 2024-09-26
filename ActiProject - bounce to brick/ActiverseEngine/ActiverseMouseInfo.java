package ActiverseEngine;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;

/**
 * Provides utility methods to retrieve information about the mouse pointer location
 * and detect mouse clicks.
 * 
 * @author Knivier 
 * @version 1.2.2
 */
public class ActiverseMouseInfo implements MouseListener {
    private static boolean leftClick = false;
    private static boolean rightClick = false;

    /**
     * @param component
     * Constructor for the ActiverseMouseInfo class.
     */
    private ActiverseMouseInfo(Component component) {
        addMouseListenerToComponent(component);
    }
    
    /**
     * @param component
     * Adds a mouse listener to the specified component.
     */
    private void addMouseListenerToComponent(Component component) {
        component.addMouseListener(this);
    }

    /**
     * Creates a new instance of the ActiverseMouseInfo class with the specified component.
     * @param component
     * @return
     */
    public static ActiverseMouseInfo createInstance(Component component) {
        ActiverseMouseInfo instance = new ActiverseMouseInfo(component);
        component.addMouseListener(instance);
        return instance;
    }

    /**
     * Retrieves the current location of the mouse pointer on the screen.
     *
     * @return A Point object representing the coordinates of the mouse pointer.
     */
    public static Point getMouseLocation() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    /**
     * Checks if the left mouse button is clicked.
     *
     * @return true if the left mouse button is clicked, false otherwise.
     */
    public static boolean isLeftClick() {
        return leftClick;
    }

    /**
     * Checks if the right mouse button is clicked.
     *
     * @return true if the right mouse button is clicked, false otherwise.
     */
    public static boolean isRightClick() {
        return rightClick;
    }

    
    /** 
     * Not in use method (built on in the future)
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        // Not used
    }

    
    /** 
     * Detects if a mouse button is pressed.
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftClick = true;
        } else if (SwingUtilities.isRightMouseButton(e)) {
            rightClick = true;
        }
    }

    
    /**
     * Detects if a mouse button is released.
     * Limitations for this method are greater than mousePressed 
     * @param e
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftClick = false;
        } else if (SwingUtilities.isRightMouseButton(e)) {
            rightClick = false;
        }
    }

    
    /** 
     * To be built in the future
     * @param e
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        // Not used
    }

    /*
     * @param e
     * To be built in the future
     */
    @Override
    public void mouseExited(MouseEvent e) {
        // Not used
    }
}