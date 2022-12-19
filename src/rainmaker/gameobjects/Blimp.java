package rainmaker.gameobjects;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import rainmaker.*;

public class Blimp extends TransientGameObject implements Updatable {
    public static final int BLIMP_TEXT_FONT_SIZE = 16;
    public static final Color BLIMP_FUEL_TEXT_COLOR = Color.rgb(44, 235, 242);

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
                BLIMP_FUEL_TEXT_COLOR);
        fuelText.setSize(BLIMP_TEXT_FONT_SIZE);
        StackPane fuelPane = new StackPane(fuelText);
        fuelPane.setAlignment(Pos.CENTER);
        fuelPane.setPrefSize(BlimpBody.BLIMP_TEXT_PANE_SIZE.getX(),
                BlimpBody.BLIMP_TEXT_PANE_SIZE.getY());
        fuelPane.getTransforms().add(
                new Translate(-BlimpBody.BLIMP_TEXT_PANE_SIZE.getX() / 2,
                        -BlimpBody.BLIMP_TEXT_PANE_SIZE.getY() / 2));
        getChildren().add(fuelPane);
    }

    private void buildShape() {
        body = new BlimpBody();
        blade = new BlimpBlade();
        blade.getTransforms().add(new Translate(
                BlimpBlade.BLIMP_BLADE_XOFFSET, 0));
        this.getChildren().addAll(body, blade);
    }

    @Override
    public void update() {
        super.update();
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
    public static final Point2D BLIMP_BODY_SIZE = new Point2D(200, 68);
    public static final Point2D BLIMP_TEXT_PANE_SIZE =
            new Point2D(BLIMP_BODY_SIZE.getX() / 2,
                    BLIMP_BODY_SIZE.getY() / 2);

    public BlimpBody() {
        configureAndAddImage();
    }

    private void configureAndAddImage() {
        ImageView image = new ImageView(
                new Image("images/blimp_transparent_trimmed.png"));
        image.setFitHeight(BLIMP_BODY_SIZE.getY());
        image.setFitWidth(BLIMP_BODY_SIZE.getX());
        centerAboutOrigin(image);
        getChildren().add(image);
    }

    private void centerAboutOrigin(ImageView image) {
        image.getTransforms().add(
                new Translate(-BLIMP_BODY_SIZE.getX() / 2,
                        -BLIMP_BODY_SIZE.getY() / 2)
        );
    }
}

class BlimpBlade extends Group {
    public static final double BLIMP_ROTOR_SPEED = 7.5;
    public static final double BLIMP_ROTOR_XSCALE_FACTOR = 0.25;
    public static final int BLIMP_BLADE_XOFFSET = -90;
    public static final int BLIMP_ROTOR_SIZE = 70;

    private double angle = 0;
    private AnimationTimer animation;

    public BlimpBlade() {
        ImageView image = new ImageView(
                new Image("images/blimp_rotor_transparent.png"));
        image.setFitHeight(BLIMP_ROTOR_SIZE);
        image.setFitWidth(BLIMP_ROTOR_SIZE);
        centerAboutOrigin(image);
        getChildren().add(image);
        startAnimation();
    }

    private void centerAboutOrigin(ImageView image) {
        image.getTransforms().add(
                new Translate(-BLIMP_ROTOR_SIZE / 2,
                        -BLIMP_ROTOR_SIZE / 2));
    }

    private void startAnimation() {
        animation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                angle += BLIMP_ROTOR_SPEED;
                getTransforms().clear();
                getTransforms().addAll(
                        new Translate(BLIMP_BLADE_XOFFSET, 0),
                        new Scale(BLIMP_ROTOR_XSCALE_FACTOR, 1),
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
