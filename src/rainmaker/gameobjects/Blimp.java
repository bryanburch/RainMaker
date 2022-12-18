package rainmaker.gameobjects;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import rainmaker.*;

public class Blimp extends TransientGameObject implements Updatable {
    private BlimpBody body;
    private BlimpBlade blade;
    private GameText fuelText;
    private BlimpState state;

    public static Blimp makeBlimp() {
        Blimp b = new Blimp(
                new Point2D(0, Game.randomInRange(0, Game.GAME_HEIGHT)),
                Game.randomInRange(Game.BLIMP_MIN_SPEED, Game.BLIMP_MAX_SPEED),
                Game.randomInRange(Game.BLIMP_MIN_SPEED_OFFSET,
                        Game.BLIMP_MAX_SPEED_OFFSET),
                Game.randomInRange(Game.BLIMP_MIN_FUEL, Game.BLIMP_MAX_FUEL));
        return b;
    }

    public Blimp(Point2D initialPosition, double speed, double speedOffset,
                 double fuel) {
        super(initialPosition, speed, speedOffset);
        buildShape();
        addFuelGauge(fuel);

        getTransforms().add(new Translate(initialPosition.getX(),
                initialPosition.getY()));
        state = new CreatedBlimp(fuel);
    }

    private void addFuelGauge(double fuel) {
        fuelText = new GameText(String.valueOf((int) fuel),
                Game.BLIMP_FUEL_TEXT_COLOR);
        fuelText.setSize(Game.BLIMP_TEXT_FONT_SIZE);
        StackPane fuelPane = new StackPane(fuelText);
        fuelPane.setAlignment(Pos.CENTER);
        fuelPane.setPrefSize(Game.BLIMP_TEXT_PANE_SIZE.getX(),
                Game.BLIMP_TEXT_PANE_SIZE.getY());
        fuelPane.getTransforms().add(
                new Translate(-Game.BLIMP_TEXT_PANE_SIZE.getX() / 2,
                        -Game.BLIMP_TEXT_PANE_SIZE.getY() / 2));
        getChildren().add(fuelPane);
    }

    private void buildShape() {
        body = new BlimpBody();
        blade = new BlimpBlade();
        blade.getTransforms().add(new Translate(Game.BLIMP_BLADE_XOFFSET, 0));
        this.getChildren().addAll(body, blade);
    }

    @Override
    public void update() {
        state = state.update(this, fuelText);
    }

    public double extractFuel() {
        return state.extractFuel();
    }

    public boolean isDead() {
        return state instanceof DeadBlimp;
    }

    public void stopAnimation() {
        blade.stopAnimation();
    }

    public void stopAudio() {
        state.stopAudio();
    }
}

class BlimpBody extends Group {

    public BlimpBody() {
        configureAndAddImage();
    }

    private void configureAndAddImage() {
        ImageView image = new ImageView(
                new Image("images/blimp_transparent_trimmed.png"));
        image.setFitHeight(Game.BLIMP_BODY_SIZE.getY());
        image.setFitWidth(Game.BLIMP_BODY_SIZE.getX());
        centerAboutOrigin(image);
        getChildren().add(image);
    }

    private void centerAboutOrigin(ImageView image) {
        image.getTransforms().add(
                new Translate(-Game.BLIMP_BODY_SIZE.getX() / 2,
                        -Game.BLIMP_BODY_SIZE.getY() / 2)
        );
    }
}

class BlimpBlade extends Group {
    private double angle = 0;
    private AnimationTimer animation;

    public BlimpBlade() {
        ImageView image = new ImageView(
                new Image("images/blimp_rotor_transparent.png"));
        image.setFitHeight(Game.BLIMP_ROTOR_SIZE);
        image.setFitWidth(Game.BLIMP_ROTOR_SIZE);
        centerAboutOrigin(image);
        getChildren().add(image);
        startAnimation();
    }

    private void centerAboutOrigin(ImageView image) {
        image.getTransforms().add(
                new Translate(-Game.BLIMP_ROTOR_SIZE / 2,
                        -Game.BLIMP_ROTOR_SIZE / 2));
    }

    private void startAnimation() {
        animation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                angle += Game.BLIMP_ROTOR_SPEED;
                getTransforms().clear();
                getTransforms().addAll(
                        new Translate(Game.BLIMP_BLADE_XOFFSET, 0),
                        new Scale(Game.BLIMP_ROTOR_XSCALE_FACTOR, 1),
                        new Rotate(angle)
                );
            }
        };
        animation.start();
    }

    public void stopAnimation() {
        animation.stop();
    }
}
