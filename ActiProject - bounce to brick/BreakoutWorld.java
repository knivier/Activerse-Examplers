import ActiverseEngine.*;

public class BreakoutWorld extends World {
    

    public BreakoutWorld() {
        super(800, 600, 1);
        setBackgroundImage("background.png");
        Ball ball = new Ball(300,500);
        addObject(new Paddle(), 300,500);
        addObject(ball, 300, 500);
        addObject(new Brick(), 20 ,50 );
        addObject(new Brick(), 200 ,50 );
        addObject(new Brick(), 500 ,50 );
    }

    public void update(){
        super.update();
        if(getActors().stream().noneMatch(actor -> actor instanceof Brick)){
            showText(150, 150, "You  win!");
            javax.swing.JOptionPane.showMessageDialog(null, "You won!", "Game Over", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }
}
