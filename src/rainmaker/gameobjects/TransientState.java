package rainmaker.gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.transform.Translate;
import rainmaker.Game;

public interface TransientState {
    TransientState update(TransientGameObject object);

    void impartSpeed(double speed, double speedOffset);

    double getSpeed();
}

class Created implements TransientState {
    private double speed;

    public Created(double speed) {
        this.speed = speed;
    }

    @Override
    public TransientState update(TransientGameObject object) {
        updatePosition(object);

        if (object.getPosition().getX()
                + (object.getBoundsInLocal().getWidth() / 2) > 0)
            return new InView(speed);
        return this;
    }

    @Override
    public void impartSpeed(double speedToImpart, double speedOffset) {
        speed = speedToImpart + speedOffset;
    }

    private static void updatePosition(TransientGameObject object) {
        Point2D newPosition = new Point2D(
                object.getPosition().getX() + object.getSpeed(),
                object.getPosition().getY());
        object.updatePositionTo(newPosition);

        object.getTransforms().clear();
        object.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));
    }

    @Override
    public double getSpeed() {
        return speed;
    }
}

class InView implements TransientState {
    private double speed;

    public InView(double speed) {
        this.speed = speed;
    }

    @Override
    public TransientState update(TransientGameObject object) {
        updatePosition(object);

        if (object.getPosition().getX()
            - (object.getBoundsInLocal().getWidth() / 2) > Game.GAME_WIDTH) {
            return new Dead();
        }
        return this;
    }

    private static void updatePosition(TransientGameObject object) {
        Point2D newPosition = new Point2D(
                object.getPosition().getX() + object.getSpeed(),
                object.getPosition().getY());
        object.updatePositionTo(newPosition);

        object.getTransforms().clear();
        object.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));
    }

    @Override
    public void impartSpeed(double speedToImpart, double speedOffset) {
        speed = speedToImpart + speedOffset;
    }

    @Override
    public double getSpeed() {
        return speed;
    }
}

class Dead implements TransientState {

    @Override
    public TransientState update(TransientGameObject object) {
        return this;
    }

    @Override
    public void impartSpeed(double speed, double speedOffset) {
        /* pointless */
    }

    @Override
    public double getSpeed() {
        return 0;
    }
}
