
import ActiverseEngine.Activerse;


/**
 * Instantiates a new myWorld class and starts the game engine
 *
 * @author Knivier
 */
public class Main {
    
    /** 
     * @param args
     */
    public static void main(String[] args) {
        Activerse.start(new BreakoutWorld());
    }
}
