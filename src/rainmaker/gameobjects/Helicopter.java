package rainmaker.gameobjects;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import rainmaker.Game;

public class Helicopter extends GameObject implements Updatable {
    private HeliBody heliBody;
    private HeliBlade heliBlade;
    private HeliState state;
    private GameText fuelGauge;
    private double heading;
    private double speed;
    private double fuel;

    public Helicopter(Point2D initialPosition, int fuel) {
        super(initialPosition);
        makeAndAddHelicopterShape();
        makeFuelGauge(fuel);
        this.getChildren().addAll(fuelGauge);

        this.getTransforms().add(new Translate(initialPosition.getX(),
                initialPosition.getY()));

        this.heading = 0;
        this.speed = 0;
        this.fuel = fuel;

        state = new OffHeliState();
    }

    private void makeAndAddHelicopterShape() {
        heliBody = new HeliBody();
        heliBlade = new HeliBlade();
        this.getChildren().addAll(heliBody, heliBlade);
    }

    private void makeFuelGauge(int fuel) {
        fuelGauge = new GameText("F:" + fuel, Game.FUEL_GAUGE_COLOR,
                FontWeight.BOLD);
        fuelGauge.setTranslateY(-Game.HELIBODY_SIZE / 2);
        fuelGauge.setTranslateX(-25);
    }

    @Override
    public void update() {
        Point2D newPosition = state.updatePosition(this.getPosition(),
                heading, speed, fuel, heliBlade, this);

        if (newPosition != null) {
            this.updatePositionTo(newPosition);
            this.getTransforms().clear();
            this.getTransforms().addAll(
                    new Translate(this.getPosition().getX(),
                            this.getPosition().getY()),
                    new Rotate(-heading));
        }

        fuel = state.consumeFuel(fuel, speed);
        updateFuelGauge();
    }

    private void updateFuelGauge() {
        fuelGauge.setText("F:" + (int) fuel);
    }

    public void toggleIgnition() {
        state = state.toggleIgnition(heliBlade);
    }

    public void turnLeft() {
        state.turnLeft(this);
    }

    public void turnRight() {
        state.turnRight(this);
    }

    public void increaseSpeed() {
        state.increaseSpeed(this);
    }

    public void decreaseSpeed() {
        state.decreaseSpeed(this);
    }

    public boolean hasFuel() {
        return fuel > 0;
    }

    public boolean isEngineOff() {
        return state instanceof OffHeliState
                || state instanceof StoppingHeliState;
    }

    public double getRemainingFuel() {
        return fuel;
    }

    public void changeState(HeliState state) {
        this.state = state;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    // TODO: check if in rainmaker.OffHeliState instead of checking speed
    public boolean isStationary() {
        return Math.abs(speed) < 1e-3;
//        return state instanceof rainmaker.OffHeliState;
    }

    public void refuelBy(double fuel) {
        this.fuel += fuel;
    }

    public void stopAudio() {
        state.stopAudio();
    }
}

class HeliBody extends Group {
    public HeliBody() {
        loadAndSetImage();
    }

    private void loadAndSetImage() {
        ImageView image = new ImageView(
                new Image("images/helibody_2x_transparent.png"));
        image.setFitHeight(Game.HELIBODY_SIZE);
        image.setFitWidth(Game.HELIBODY_SIZE);
        centerAboutOriginAndFlip();
        this.getChildren().add(image);
    }

    private void centerAboutOriginAndFlip() {
        this.setTranslateX(-Game.HELIBODY_SIZE / 2);
        this.setTranslateY(-Game.HELIBODY_SIZE / 2);
        this.setRotate(180);
    }
}

class HeliBlade extends Group {
    private double rotationalSpeed;
    private boolean isSpinning;

    public HeliBlade() {
        loadAndSetImage();
        startAnimation();
    }

    private void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {

            @Override
            public void handle(long now) {
                determineAndUpdateSpeed();
                HeliBlade.super.setRotate(
                        HeliBlade.super.getRotate() + rotationalSpeed);
            }

            private void determineAndUpdateSpeed() {
                if (isSpinning && rotationalSpeed < Game.ROTOR_MAX_SPEED)
                    rotationalSpeed += Game.ROTOR_ACCELERATION;
                else if (!isSpinning && rotationalSpeed > Game.ROTOR_MIN_SPEED)
                    rotationalSpeed =
                        (rotationalSpeed - Game.ROTOR_ACCELERATION >= 0) ?
                            (rotationalSpeed - Game.ROTOR_ACCELERATION) : 0;
            }
        };
        loop.start();
    }

    private void loadAndSetImage() {
        ImageView image = new ImageView(
                new Image("images/heliblade_2wing_transparent.png"));
        image.setFitHeight(Game.ROTOR_LENGTH);
        image.setFitWidth(Game.ROTOR_LENGTH);
        centerAboutOrigin();
        this.getChildren().add(image);
    }

    private void centerAboutOrigin() {
        this.setTranslateX(-Game.ROTOR_LENGTH / 2);
        this.setTranslateY(-Game.ROTOR_LENGTH / 2);
    }

    public void spinUp() {
        isSpinning = true;
    }

    public void spinDown() {
        isSpinning = false;
    }

    public boolean isUpToSpeed() {
        return rotationalSpeed >= Game.ROTOR_MAX_SPEED;
    }

    public boolean isRotating() {
        return Math.abs(rotationalSpeed) > 1e-3;
    }
}
