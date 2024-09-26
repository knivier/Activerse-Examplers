import ActiverseEngine.*;

public class Paddle extends Actor{

    public Paddle(){
        setImage(new ActiverseImage("paddle.png"));
        setHeight(10);
    }

    @Override
    public void act(){
        detectMovement();
        detectCollisions();
    }

    public void detectMovement(){
        if (keyIsDown('a')){
            setX(getX() - 10);
        }
        if (keyIsDown('d')){
            setX(getX() + 10);
        }
        stayInsideBoundary();
    }

    public void detectCollisions(){
        getWorld().getActors().forEach(actor -> {
            if (actor instanceof Ball ball) {
                if(this.intersects(ball)){
                    ball.handlePaddleCollision(this);
                }
            }
        });
    }

    public void stayInsideBoundary(){
        if (getX() < getWidth() / 2) {
            setX(getWidth() / 2);
        }
        if (getX() > getWorld().getWidth() - getWidth() / 2) {
            setX(getWorld().getWidth() - getWidth() / 2);
        }
    }

}

