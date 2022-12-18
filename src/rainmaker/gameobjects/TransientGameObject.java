package rainmaker.gameobjects;

import javafx.geometry.Point2D;

// TODO: figure out how to generalize rainmaker.gameobjects.Cloud and rainmaker.gameobjects.Blimp to rainmaker.TransientGameObject.
//  Will also involve figuring out how state pattern will work
//  (idea: generalized state pattern for TGO, rainmaker.gameobjects.Cloud and rainmaker.gameobjects.Blimp maintain their
//  current state classes but just inherit and extend with their necessary
//  behavior?)
public class TransientGameObject extends GameObject {
    private double speed;
    private double speedOffset;

    public TransientGameObject(Point2D startPosition, double speed,
                               double speedOffset) {
        super(startPosition);
        this.speed = speed;
        this.speedOffset = speedOffset;
    }

    public void impartSpeed(double speed) {
        this.speed = speed + speedOffset;
    }

    public double getSpeed() {
        return speed;
    }
}
