package rainmaker.gameobjects;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Translate;
import rainmaker.Game;

public class Helicopter extends GameObject implements Updatable {
    public static final Point2D FUEL_GAUGE_OFFSET =
            new Point2D(-Game.HELIBODY_SIZE / 2, -25);
    public static final double SPEED_ADJUSTMENT = 0.1;
    public static final double HEADING_ADJUSTMENT = 15;

    private HeliBody heliBody;
    private HeliBlade heliBlade;
    private GameText fuelGauge;
    private HeliState state;

    public Helicopter(Point2D initialPosition, int fuel) {
        super(initialPosition);
        makeAndAddHelicopterShape();
        makeAndAddFuelGauge(fuel);

        getTransforms().add(new Translate(initialPosition.getX(),
                initialPosition.getY()));
        state = new OffHeliState(fuel, 0);
    }

    private void makeAndAddHelicopterShape() {
        heliBody = new HeliBody();
        heliBlade = new HeliBlade();
        this.getChildren().addAll(heliBody, heliBlade);
    }

    private void makeAndAddFuelGauge(int fuel) {
        fuelGauge = new GameText("F:" + fuel, Game.FUEL_GAUGE_COLOR,
                FontWeight.BOLD);
        fuelGauge.setTranslateY(FUEL_GAUGE_OFFSET.getX());
        fuelGauge.setTranslateX(FUEL_GAUGE_OFFSET.getY());
        getChildren().addAll(fuelGauge);
    }

    public void toggleIgnition() {
        state = state.toggleIgnition(heliBlade);
    }

    @Override
    public void update() {
        state = state.update(this, heliBlade, fuelGauge);
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

    public void refuelBy(double fuel) {
        state.refuelBy(fuel);
    }

    public boolean hasFuel() {
        return state.getFuel() > 0;
    }

    public boolean isEngineOff() {
        return state instanceof OffHeliState
                || state instanceof StoppingHeliState;
    }

    public boolean isStationary() {
        return Math.abs(state.getSpeed()) < Game.EFFECTIVELY_ZERO;
    }

    public void stopAnimation() {
        heliBlade.stopAnimation();
    }

    public void stopAudio() {
        state.stopAudio();
    }

    public double getRemainingFuel() {
        return state.getFuel();
    }

    public double getSpeed() {
        return state.getSpeed();
    }
}

class HeliBody extends Group {
    public static final int IMAGE_ROTATION = 180;

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
        this.setRotate(IMAGE_ROTATION);
    }
}

class HeliBlade extends Group {
    private double rotationalSpeed;
    private boolean isSpinning;
    private AnimationTimer animation;

    public HeliBlade() {
        loadAndSetImage();
        startAnimation();
    }

    private void startAnimation() {
        animation = new AnimationTimer() {

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
        animation.start();
    }

    private void loadAndSetImage() {
        ImageView image = new ImageView(
                new Image("images/heliblade_2wing_transparent.png"));
        image.setFitHeight(Game.ROTOR_LENGTH);
        image.setFitWidth(Game.ROTOR_LENGTH);
        centerAboutOrigin();
        getChildren().add(image);
    }

    private void centerAboutOrigin() {
        setTranslateX(-Game.ROTOR_LENGTH / 2);
        setTranslateY(-Game.ROTOR_LENGTH / 2);
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
        return Math.abs(rotationalSpeed) > Game.EFFECTIVELY_ZERO;
    }

    public void stopAnimation() {
        animation.stop();
    }
}
