import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.Optional;

/**
 * Sets up key event handlers that invoke Game class methods.
 */
public class GameApp extends Application {
    private Game game;
    private Scene scene;
    
    @Override
    public void start(Stage primaryStage) {
        game = new Game();
        scene = new Scene(game, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        primaryStage.setScene(scene);
        setupEventHandlers();
        primaryStage.setResizable(false);
        primaryStage.setTitle("RainMaker");
        primaryStage.show();
    }

    private void setupEventHandlers() {
        scene.setOnKeyPressed(e -> {
            switch(e.getCode()) {
                case LEFT -> game.handleLeftKeyPressed();
                case RIGHT -> game.handleRightKeyPressed();
                case UP -> game.handleUpKeyPressed();
                case DOWN -> game.handleDownKeyPressed();
                case SPACE -> game.handleSpaceKeyPressed();
                case I -> game.handleIKeyPressed();
                case R -> game.handleRKeyPressed();
                case B -> game.handleBKeyPressed();
            }
        });
    }
}

/**
 * Is a Pane to serve as the container for all game objects.
 */
class Game extends Pane {
    final static int GAME_HEIGHT = 800;
    final static int GAME_WIDTH = 800;

    final static Color POND_COLOR = Color.BLUE;
    final static Color POND_TEXT_COLOR = Color.WHITE;
    final static int MAX_POND_RADIUS = 50;
    final static int MAX_STARTING_POND_RADIUS = (int) (MAX_POND_RADIUS * 0.30);
    final static int MIN_POND_RADIUS = 5;

    final static Color CLOUD_COLOR = Color.WHITE;
    final static Color CLOUD_TEXT_COLOR = Color.BLUE;
    final static int MAX_CLOUD_RADIUS = 70;
    final static int MIN_CLOUD_RADIUS = 30;
    final static double RAIN_FREQUENCY = 0.6;

    final static Point2D HELIPAD_SIZE = new Point2D(100, 100);
    final static Point2D HELIPAD_POSITION =
            new Point2D((GAME_WIDTH / 2) - (HELIPAD_SIZE.getX() / 2),
                    GAME_HEIGHT / 25);

    final static Color HELICOPTER_COLOR = Color.YELLOW;
    final static int ROTOR_LENGTH = 80;
    final static Point2D HELICOPTER_START = new Point2D(GAME_WIDTH / 2,
            GAME_HEIGHT / 10);
    final static int HELICOPTER_MAX_SPEED = 10;
    final static int HELICOPTER_MIN_SPEED = -2;
    final static double ROTOR_ACCELERATION = 0.1;
    final static int HELICOPTER_START_FUEL = 25000;

    final static double NANOS_PER_SEC = 1e9;

    private Pond pond;
    private Cloud cloud;
    private Helipad helipad;
    private Helicopter helicopter;
    private boolean allowSeeding;
    private AnimationTimer loop;

    public static double randomInRange(double min, double max) {
        return (Math.random() * (max - min) + min);
    }

    public static Point2D randomPositionInBound(
            Point2D lowerLeft, Point2D upperRight) {
        return new Point2D(randomInRange(lowerLeft.getX(), upperRight.getX()),
                randomInRange(lowerLeft.getY(), upperRight.getY()));
    }

    public Game() {
        /* image credit: https://earthobservatory.nasa.gov/images/51341/
        two-views-of-the-painted-desert */
        BackgroundImage background = new BackgroundImage(
                new Image("desert_background_large.png"),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        this.setBackground(new Background(background));
        this.setScaleY(-1);
        init();
    }

    private void init() {
        this.getChildren().clear();

        this.pond = makePond();
        this.cloud = makeCloud();
        this.helipad = makeHelipad();
        this.helicopter = makeHelicopter();

        this.getChildren().addAll(pond, cloud, helipad, helicopter);

        loop = makeGameLoop();
        loop.start();
    }

    private static Helicopter makeHelicopter() {
        Helicopter helicopter = new Helicopter(HELICOPTER_START_FUEL,
                HELICOPTER_START);
        return helicopter;
    }

    private static Helipad makeHelipad() {
        Helipad helipad = new Helipad(HELIPAD_SIZE);
        return helipad;
    }

    private static Cloud makeCloud() {
        Cloud cloud = new Cloud(randomInRange(MIN_CLOUD_RADIUS,
                MAX_CLOUD_RADIUS), CLOUD_COLOR, CLOUD_TEXT_COLOR);
        Point2D position =
                randomPositionInBound(new Point2D(0, (GAME_HEIGHT * (0.33))),
                        new Point2D(GAME_WIDTH, GAME_HEIGHT));
        cloud.setTranslateX(position.getX());
        cloud.setTranslateY(position.getY());
        return cloud;
    }

    private static Pond makePond() {
        Pond pond = new Pond(MAX_POND_RADIUS, randomInRange(MIN_POND_RADIUS,
                MAX_STARTING_POND_RADIUS), POND_COLOR, POND_TEXT_COLOR);
        Point2D position =
                randomPositionInBound(new Point2D(0, (GAME_HEIGHT * (0.33))),
                        new Point2D(GAME_WIDTH, GAME_HEIGHT));
        pond.setTranslateX(position.getX());
        pond.setTranslateY(position.getY());
        return pond;
    }

    private AnimationTimer makeGameLoop() {
        AnimationTimer gameLoop = new AnimationTimer() {
            private double old = -1;
            private double timer = 0;

            @Override
            public void handle(long now) {
                double delta = calculateDelta(now);
                timer += delta;

                helicopter.update();
                cloud.update();
                pond.update();

                seedIfNearCloud();
                rainAndFillPond();

                showLoseDialogIfConditionsMet();
                showWinDialogIfConditionsMet();
            }

            private void showWinDialogIfConditionsMet() {
                if (hasMetWinConditions()) {
                    Alert winDialog = makeWinDialog();
                    ButtonType yes = winDialog.getButtonTypes().get(0);
                    ButtonType no = winDialog.getButtonTypes().get(1);

                    displayDialogAndStopGameLoop(winDialog, yes, no);
                }
            }

            private void displayDialogAndStopGameLoop(
                    Alert winDialog, ButtonType yes, ButtonType no) {
                Platform.runLater(() -> {
                    Optional<ButtonType> result = winDialog.showAndWait();
                    System.out.println(result);
                    if (result.get() == yes)
                        init();
                    else if (result.get() == no)
                        Platform.exit();
                });
                this.stop();
            }

            private boolean hasMetWinConditions() {
                return pond.isFull() && helicopter.hasFuel() &&
                    !helicopter.isEngineOn() && !Shape.intersect(
                        helicopter.getBoundingCircle(), helipad.getBoundingBox()).
                        getBoundsInLocal().isEmpty();
            }

            private Alert makeWinDialog() {
                DecimalFormat decimalFormat =
                        new DecimalFormat("###,###");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "You have won! You scored " +
                            decimalFormat.format(helicopter.getRemainingFuel())
                            + ". Play again?");
                alert.setTitle("Confirmation");
                alert.setHeaderText("Confirmation");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                alert.getButtonTypes().setAll(yesButton, noButton);
                return alert;
            }

            private void showLoseDialogIfConditionsMet() {
                if (!helicopter.hasFuel()) {
                    Alert loseDialog = makeLoseDialog();
                    ButtonType yes = loseDialog.getButtonTypes().get(0);
                    ButtonType no = loseDialog.getButtonTypes().get(1);

                    displayDialogAndStopGameLoop(loseDialog, yes, no);
                }
            }

            private Alert makeLoseDialog() {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "You have lost! Play again?");
                alert.setTitle("Confirmation");
                alert.setHeaderText("Confirmation");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                alert.getButtonTypes().setAll(yesButton, noButton);
                return alert;
            }

            private void rainAndFillPond() {
                if (timer >= RAIN_FREQUENCY) {
                    boolean hasRained = cloud.rain();
                    if (hasRained) {
                        pond.fill();
                    }
                    timer = 0;
                }
            }

            private void seedIfNearCloud() {
                if (!Shape.intersect(helicopter.getBoundingCircle(),
                        cloud.getBoundingBox()).getBoundsInLocal().isEmpty() &&
                        allowSeeding) {
                    cloud.seed();
                }
                allowSeeding = false;
            }

            private double calculateDelta(long now) {
                if (old < 0)
                    old = now;
                double delta = (now - old) / NANOS_PER_SEC;
                old = now;
                return delta;
            }
        };
        return gameLoop;
    }

    public void handleLeftKeyPressed() {
        helicopter.turnLeft();
    }

    public void handleRightKeyPressed() {
        helicopter.turnRight();
    }

    public void handleUpKeyPressed() {
        helicopter.increaseSpeed();
    }

    public void handleDownKeyPressed() {
        helicopter.decreaseSpeed();
    }

    public void handleSpaceKeyPressed() {
        allowSeeding = true;
    }

    public void handleIKeyPressed() {
        // TODO update containment logic
        Shape helicopterHelipadIntersection = Shape.intersect(
                helicopter.getBoundingCircle(), helipad.getBoundingBox());
        double widthDifference = Math.abs(
                helicopterHelipadIntersection.getBoundsInLocal().getWidth() -
                helicopter.getBoundingCircle().getBoundsInLocal().getWidth());
        double heightDifference = Math.abs(
                helicopterHelipadIntersection.getBoundsInLocal().getHeight()
                - helicopter.getBoundingCircle().getBoundsInLocal().getHeight());

        if (widthDifference < 1e-3 && heightDifference < 1e-3)
            helicopter.toggleIgnition();
    }

    public void handleRKeyPressed() {
        loop.stop();
        init();
    }

    public void handleBKeyPressed() {
        cloud.toggleBoundingBox();
        helipad.toggleBoundingBox();
        helicopter.toggleBoundingBox();
    }
}

/**
 * Is-a Group to treat game objects as Node objects to be put straight onto
 * scene graph.
 */
abstract class GameObject extends Group {

}

class Pond extends GameObject implements Updatable {
    private Circle pondCircle;
    private double maxRadius, currentRadius;
    private GameText percentFullText;
    private int percentFull;

    public Pond(double maxRadius, double currentRadius,
                final Color fill, final Color textFill) {
        this.maxRadius = maxRadius;
        this.currentRadius = currentRadius;
        pondCircle = new Circle(currentRadius, fill);

        makePercentFullText(maxRadius, currentRadius, textFill);

        this.getChildren().addAll(pondCircle, percentFullText);
    }

    private void makePercentFullText(double maxRadius, double currentRadius,
                                     Color textFill) {
        percentFull = (int) ((currentRadius / maxRadius) * 100);
        percentFullText = new GameText(percentFull + "%",
                textFill);

        Bounds fpBounds = percentFullText.getBoundsInParent();
        percentFullText.setTranslateX(
                percentFullText.getTranslateX() - fpBounds.getWidth() / 2);
        percentFullText.setTranslateY(
                percentFullText.getTranslateY() + fpBounds.getHeight() / 2);
    }

    @Override
    public void update() {
        if (pondCircle.getRadius() != currentRadius) {
            pondCircle.setRadius(currentRadius);
            percentFullText.setText(percentFull + "%");
        }
    }

    public void fill() {
        if (percentFull < 100) {
            currentRadius = calcNextRadiusForPercent(++percentFull);
        }
    }

    private double calcNextRadiusForPercent(int percent) {
        return (maxRadius * ((double) percent / 100));
    }

    public boolean isFull() {
        return percentFull >= 100;
    }
}

class Cloud extends GameObject implements Updatable {
    private Circle cloudCircle;
    private Color fill;
    private GameText percentSaturatedText;
    private int seedPercentage;
    private Rectangle bounds;
    private boolean showBounds;

    public Cloud(double radius, Color fill, Color textFill) {
        this.fill = fill;
        cloudCircle = new Circle(radius, fill);

        seedPercentage = 0;
        makePercentSaturatedText(textFill);

        this.getChildren().addAll(cloudCircle, percentSaturatedText);

        makeBoundingBox();
    }

    private void makePercentSaturatedText(Color textFill) {
        percentSaturatedText =
                new GameText(seedPercentage + "%", textFill);

        Bounds fpBounds = percentSaturatedText.getBoundsInParent();
        percentSaturatedText.setTranslateX(
                percentSaturatedText.getTranslateX()
                        - fpBounds.getWidth() / 2);
        percentSaturatedText.setTranslateY(percentSaturatedText.getTranslateY()
                + fpBounds.getHeight() / 2);
    }

    private void makeBoundingBox() {
        bounds = new Rectangle(this.getBoundsInParent().getWidth(),
                this.getBoundsInParent().getHeight());
        bounds.setTranslateX(bounds.getTranslateX() - bounds.getWidth() / 2);
        bounds.setTranslateY(bounds.getTranslateY() - bounds.getHeight() / 2);
        bounds.setFill(Color.TRANSPARENT);
        bounds.setStrokeWidth(1);
        bounds.setStroke(Color.YELLOW);
        bounds.setVisible(false);
        this.getChildren().add(bounds);
    }

    @Override
    public void update() {
        if (cloudCircle.getFill() != fill) {
            cloudCircle.setFill(fill);
            percentSaturatedText.setText(seedPercentage + "%");
        }
    }

    public void toggleBoundingBox() {
        showBounds = !showBounds;
        bounds.setVisible(showBounds);
    }

    public Rectangle getBoundingBox() {
        return bounds;
    }

    public void seed() {
        if (seedPercentage < 100) {
            seedPercentage++;
            int red = (int) (255 * ((Color) cloudCircle.getFill()).getRed());
            int green =
                (int) (255 * ((Color) cloudCircle.getFill()).getGreen());
            int blue = (int) (255 * ((Color) cloudCircle.getFill()).getBlue());
            fill = Color.rgb(--red, --green, --blue);
        }
    }

    public boolean rain() {
        if (seedPercentage >= 30) {
            seedPercentage--;
            int red = (int) (255 * ((Color) cloudCircle.getFill()).getRed());
            int green =
                (int) (255 * ((Color) cloudCircle.getFill()).getGreen());
            int blue = (int) (255 * ((Color) cloudCircle.getFill()).getBlue());
            fill = Color.rgb(++red, ++green, ++blue);
            return true;
        }
        return false;
    }
}

/**
 * Represents starting/ending location for helicopter.
 */
class Helipad extends GameObject {
    private Rectangle bounds;
    private boolean showBounds;

    public Helipad(Point2D size) {
        ImageView image = new ImageView(new Image("helipad_textured.png"));
        image.setFitHeight(size.getY());
        image.setFitWidth(size.getX());
        this.getChildren().add(image);

        this.getTransforms().add(new Translate(Game.HELIPAD_POSITION.getX(),
                Game.HELIPAD_POSITION.getY()));

        makeBoundingBox(size);
    }

    private void makeBoundingBox(Point2D size) {
        bounds = new Rectangle(size.getX(), size.getY());
        bounds.setFill(Color.TRANSPARENT);
        bounds.setStroke(Color.YELLOW);
        bounds.setStrokeWidth(1);
        bounds.setVisible(false);
        this.getChildren().add(bounds);
    }

    public void toggleBoundingBox() {
        showBounds = !showBounds;
        bounds.setVisible(showBounds);
    }

    public Rectangle getBoundingBox() {
        return bounds;
    }
}

class Helicopter extends GameObject implements Updatable {
    final static int FUEL_CONSUMPTION_RATE = 5;
    private GameText fuelGauge;
    private Circle bounds;
    private Point2D position;
    private double heading;
    private double speed;
    private int fuel;
    private boolean isActive;
    private boolean showBounds;

    private HeliBody heliBody;
    private HeliBlade heliBlade;
    private HeliState state;

    public Helicopter(int fuel, Point2D position) {
        makeHelicopterShape();
        makeFuelGauge(fuel);
        makeBoundingBox();
        this.getChildren().addAll(fuelGauge, bounds);

        this.getTransforms().add(new Translate(position.getX(),
                position.getY()));

        this.heading = 0;
        this.speed = 0;
        this.fuel = fuel;
        this.position = position;
        this.isActive = false;

        state = new OffHeliState();
    }

    private void makeHelicopterShape() {
        heliBody = new HeliBody();
        heliBlade = new HeliBlade();
        this.getChildren().addAll(heliBody, heliBlade);
    }

    private void makeFuelGauge(int fuel) {
        fuelGauge = new GameText("F:" + fuel, Game.HELICOPTER_COLOR);
        fuelGauge.setTranslateY(-15);
        fuelGauge.setTranslateX(-25);
    }

    private void makeBoundingBox() {
        bounds = new Circle(Game.ROTOR_LENGTH / 2);
        bounds.setFill(Color.TRANSPARENT);
        bounds.setStrokeWidth(1);
        bounds.setStroke(Color.YELLOW);
        bounds.setVisible(false);
    }

    @Override
    public void update() {
        Point2D newPosition = state.update(position, heading, speed,
                heliBlade, this);

        if (newPosition != null) {
            position = newPosition;
            this.getTransforms().clear();
            this.getTransforms().addAll(
                    new Translate(position.getX(), position.getY()),
                    new Rotate(-heading));
        }

        consumeFuel();
    }

    private void consumeFuel() {
        if (isActive && fuel <= Math.abs(speed) + FUEL_CONSUMPTION_RATE) {
            fuel = 0;
            fuelGauge.setText("F:" + fuel);
        } else if (isActive) {
            fuel -= Math.abs(speed) + FUEL_CONSUMPTION_RATE;
            fuelGauge.setText("F:" + fuel);
        }
    }

    public void toggleIgnition() {
        state = state.toggleIgnition(heliBlade);
//        if (Math.abs(speed) < 1e-3)
//            isActive = !isActive;
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

    public void toggleBoundingBox() {
        showBounds = !showBounds;
        bounds.setVisible(showBounds);
    }

    public Circle getBoundingCircle() {
        return bounds;
    }

    public boolean hasFuel() {
        return fuel > 0;
    }

    public boolean isEngineOn() {
        return isActive;
    }

    public int getRemainingFuel() {
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
}

class HeliBody extends GameObject {

    public static final int SIZE = 75;

    public HeliBody() {
        ImageView image = new ImageView(
                new Image("helibody_2x_transparent.png"));
        image.setFitHeight(SIZE);
        image.setFitWidth(SIZE);
        this.setTranslateX(-SIZE/2);
        this.setTranslateY(-SIZE/2);
        this.setRotate(180);
        this.getChildren().add(image);
    }
}

class HeliBlade extends GameObject {
    public static final int MIN_SPEED = 0;
    public static final int MAX_SPEED = 15;

    private double rotationalSpeed;
    private boolean isActive;

    public HeliBlade() {
        loadAndSetImage();
        rotationalSpeed = 0;
        startAnimation();
    }

    private void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (isActive && rotationalSpeed < MAX_SPEED)
                    rotationalSpeed += Game.ROTOR_ACCELERATION;
                else if (!isActive && rotationalSpeed > MIN_SPEED) {
                    rotationalSpeed -= Game.ROTOR_ACCELERATION;
                }
                HeliBlade.super.setRotate(
                        HeliBlade.super.getRotate() + rotationalSpeed);
            }
        };
        loop.start();
    }

    private void loadAndSetImage() {
        ImageView image = new ImageView(
                new Image("heliblade_2wing_transparent.png"));
        image.setFitHeight(Game.ROTOR_LENGTH);
        image.setFitWidth(Game.ROTOR_LENGTH);
        this.setTranslateX(-Game.ROTOR_LENGTH / 2);
        this.setTranslateY(-Game.ROTOR_LENGTH / 2);
        this.getChildren().add(image);
    }

    public void spinUp() {
        isActive = true;
    }

    public void spinDown() {
        isActive = false;
    }

    public boolean isUpToSpeed() {
        return rotationalSpeed >= MAX_SPEED;
    }
}

interface HeliState {
    Point2D update(Point2D position, double heading, double speed, HeliBlade heliBlade, Helicopter helicopter);

    HeliState toggleIgnition(HeliBlade heliBlade);

    void turnLeft(Helicopter helicopter);

    void turnRight(Helicopter helicopter);

    void increaseSpeed(Helicopter helicopter);

    void decreaseSpeed(Helicopter helicopter);

}

class OffHeliState implements HeliState {
    @Override
    public Point2D update(Point2D position, double heading, double speed, HeliBlade heliBlade, Helicopter helicopter) {
        // nothing to update in this state
        return null;
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinUp();
        return new StartingHeliState();
    }

    @Override
    public void turnLeft(Helicopter helicopter) {
        // not possible in this state
    }

    @Override
    public void turnRight(Helicopter helicopter) {
        // not possible in this state
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) {
        // not possible in this state
    }

    @Override
    public void decreaseSpeed(Helicopter helicopter) {
        // not possible in this state
    }

}

class StartingHeliState implements HeliState {

    @Override
    public Point2D update(Point2D position, double heading, double speed, HeliBlade heliBlade, Helicopter helicopter) {
        if (heliBlade.isUpToSpeed())
            helicopter.changeState(new ReadyHeliState());
        return null;
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinDown();
        return new StoppingHeliState();
    }

    @Override
    public void turnLeft(Helicopter helicopter) {
        // not possible in this state
    }

    @Override
    public void turnRight(Helicopter helicopter) {
        // not possible in this state
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) {
        // not possible in this state
    }

    @Override
    public void decreaseSpeed(Helicopter helicopter) {
        // not possible in this state
    }

}

class StoppingHeliState implements HeliState {

    @Override
    public Point2D update(Point2D position, double heading, double speed, HeliBlade heliBlade, Helicopter helicopter) {
        // nothing to update in this state
        return null;
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinUp();
        return new StartingHeliState();
    }

    @Override
    public void turnLeft(Helicopter helicopter) {
        // not possible in this state
    }

    @Override
    public void turnRight(Helicopter helicopter) {
        // not possible in this state
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) {
        // not possible in this state
    }

    @Override
    public void decreaseSpeed(Helicopter helicopter) {
        // not possible in this state
    }

}

class ReadyHeliState implements HeliState {

    @Override
    public Point2D update(Point2D position, double heading, double speed, HeliBlade heliBlade, Helicopter helicopter) {
        return new Point2D(
                position.getX() + (Math.sin(Math.toRadians(heading)) * speed),
                position.getY() + (Math.cos(Math.toRadians(heading)) * speed));
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinDown();
        return new StoppingHeliState();
    }

    @Override
    public void turnLeft(Helicopter helicopter) {
        if (Math.abs(helicopter.getSpeed()) > 1e-3)
            helicopter.setHeading(helicopter.getHeading() - 15);
    }

    @Override
    public void turnRight(Helicopter helicopter) {
        if (Math.abs(helicopter.getSpeed()) > 1e-3)
            helicopter.setHeading(helicopter.getHeading() + 15);
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) {
        if (helicopter.getSpeed() < Game.HELICOPTER_MAX_SPEED)
            helicopter.setSpeed(helicopter.getSpeed() + 0.1);
    }

    @Override
    public void decreaseSpeed(Helicopter helicopter) {
        if (helicopter.getSpeed() > Game.HELICOPTER_MIN_SPEED)
            helicopter.setSpeed(helicopter.getSpeed() - 0.1);
    }
}

/**
 * Objects will be updated from the main game timer.
 */
interface Updatable {
    void update();

}

/**
 * Since the game coordinate space is being scaled by -1 (to convert to
 * quadrant I), any text will have to be scaled accordingly.
 */
class GameText extends GameObject {
    private Text text;

    public GameText(final String string, final Color fill) {
        text = new Text(string);
        text.setFill(fill);

        this.setScaleY(-1);
        this.getChildren().add(text);
    }

    public void setText(String string) {
        text.setText(string);
    }

    public String getText() {
        return text.getText();
    }
}
