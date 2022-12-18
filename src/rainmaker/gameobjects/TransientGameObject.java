package rainmaker.gameobjects;

import javafx.geometry.Point2D;

public class TransientGameObject extends GameObject implements Updatable {
    private double speedOffset;
    private TransientState state;

    public TransientGameObject(Point2D startPosition, double speed,
                               double speedOffset) {
        super(startPosition);
        this.speedOffset = speedOffset;
        state = new Created(speed);
    }
    @Override
    public void update() {
        state.update(this);
    }

    public void impartSpeed(double speedToImpart) {
        state.impartSpeed(speedToImpart, speedOffset);
    }

    public double getSpeed() {
        return state.getSpeed();
    }
}
