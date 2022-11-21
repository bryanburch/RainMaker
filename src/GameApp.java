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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
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

    final static int NUM_PONDS = 3;
    final static Color POND_COLOR = Color.BLUE;
    final static Color POND_TEXT_COLOR = Color.WHITE;
    final static int MAX_POND_RADIUS = 50;
    final static int MAX_STARTING_POND_RADIUS = (int) (MAX_POND_RADIUS * 0.30);
    final static int MIN_POND_RADIUS = 5;
    public static final int TOTAL_POND_CAPACITY_TO_WIN = 80;

    final static Color CLOUD_COLOR = Color.WHITE;
    final static Color CLOUD_TEXT_COLOR = Color.BLUE;
    final static int MAX_CLOUD_RADIUS = 70;
    final static int MIN_CLOUD_RADIUS = 30;
    final static double RAIN_FREQUENCY = 0.6;

    final static Point2D HELIPAD_DIMENSIONS = new Point2D(100, 100);
    final static Point2D HELIPAD_POSITION =
            new Point2D((GAME_WIDTH / 2),
                    (GAME_HEIGHT / 25) + (HELIPAD_DIMENSIONS.getY() / 2));

    final static Color HELICOPTER_COLOR = Color.MAROON;
    final static int HELIBODY_SIZE = 75;
    final static int ROTOR_LENGTH = 80;
    final static int HELICOPTER_MAX_SPEED = 10;
    final static int HELICOPTER_MIN_SPEED = -2;
    final static int ROTOR_MIN_SPEED = 0;
    final static int ROTOR_MAX_SPEED = 15;
    final static double ROTOR_ACCELERATION = 0.075;
    final static int HELICOPTER_START_FUEL = 25000;
    final static int FUEL_CONSUMPTION_RATE = 5;

    final static Color BOUND_FILL = Color.TRANSPARENT;
    final static Color BOUND_STROKE = Color.YELLOW;
    final static int BOUND_STROKE_WIDTH = 1;

    final static double NANOS_PER_SEC = 1e9;

    private Ponds ponds;
    private Clouds clouds;
    private BoundsPane bounds;

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

        initPonds();

        /* Init clouds */
        this.cloud = makeCloud();

        /* Init helipad */
        this.helipad = makeHelipad();

        /* Init helicopter */
        this.helicopter = makeHelicopter();

        /* Init bounds */
        bounds = new BoundsPane();
        bounds.add(cloud, new Circle(
                cloud.getBoundsInParent().getWidth() / 2));
        bounds.add(helipad,
                new Rectangle(helipad.getBoundsInParent().getWidth(),
                        helipad.getBoundsInParent().getHeight()));
        bounds.add(helicopter, new Circle(ROTOR_LENGTH / 2));
        bounds.setVisible(false);

        this.getChildren().addAll(ponds, cloud, helipad, helicopter, bounds);

        loop = makeGameLoop();
        loop.start();
    }

    private void initPonds() {
        ponds = new Ponds();
        for (int i = 0; i < NUM_PONDS; i++)
            ponds.add(makePond());
    }

    private static Helicopter makeHelicopter() {
        Helicopter helicopter = new Helicopter(HELIPAD_POSITION,
                HELICOPTER_START_FUEL);
        return helicopter;
    }

    private static Helipad makeHelipad() {
        Helipad helipad = new Helipad(HELIPAD_POSITION, HELIPAD_DIMENSIONS);
        return helipad;
    }

    private static Cloud makeCloud() {
        Point2D position =
                randomPositionInBound(new Point2D(0, (GAME_HEIGHT * (0.33))),
                        new Point2D(GAME_WIDTH, GAME_HEIGHT));
        Cloud cloud = new Cloud(position, randomInRange(MIN_CLOUD_RADIUS,
                MAX_CLOUD_RADIUS), CLOUD_COLOR, CLOUD_TEXT_COLOR);
        cloud.setTranslateX(position.getX());
        cloud.setTranslateY(position.getY());
        return cloud;
    }

    private static Pond makePond() {
        Point2D position =
                randomPositionInBound(new Point2D(0, (GAME_HEIGHT * (0.33))),
                        new Point2D(GAME_WIDTH, GAME_HEIGHT));
        Pond pond = new Pond(position, MAX_POND_RADIUS,
                randomInRange(MIN_POND_RADIUS,
                MAX_STARTING_POND_RADIUS), POND_COLOR, POND_TEXT_COLOR);
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
                ponds.update();

                bounds.update();

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
                return ponds.getTotalCapacity() >= TOTAL_POND_CAPACITY_TO_WIN
                        && helicopter.hasFuel()
                        && helicopter.isEngineOff()
                        && isHelicopterWithinHelipad();
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
                        ponds.fill();
                    }
                    timer = 0;
                }
            }

            private void seedIfNearCloud() {
                var helicopterBounds = bounds.getFor(helicopter);
                var cloudBounds = bounds.getFor(cloud);
                if (helicopterBounds.collidesWith(cloudBounds) &&
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

    private boolean isHelicopterWithinHelipad() {
        Bound helipadBounds = bounds.getFor(helipad);
        Bound helicopterBounds = bounds.getFor(helicopter);

        return helicopterBounds.containedIn(helipadBounds);
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
        if (helicopter.isStationary() && isHelicopterWithinHelipad())
            helicopter.toggleIgnition();
    }

    public void handleRKeyPressed() {
        loop.stop();
        init();
    }

    public void handleBKeyPressed() {
        bounds.setVisible(!bounds.isVisible());
    }
}

class BoundsPane extends Pane implements Updatable {
    private List<Bound> bounds;

    public BoundsPane() {
        bounds = new LinkedList<>();
    }

    public void add(GameObject objectToBound, Shape boundShape) {
        Bound bound = new Bound(objectToBound, boundShape);
        bounds.add(bound);
        this.getChildren().add(bound);

    }

    public Bound getFor(GameObject gameObject) {
        for (Bound b : bounds) {
            if (b.getObjectBeingBounded() == gameObject)
                return b;
        }
        return null;
    }

    public void remove(Bound bound) {
        for (Bound b : bounds) {
            if (bound == b)
                bounds.remove(b);
        }
    }

    @Override
    public void update() {
        for (Bound b : bounds)
            b.update();
    }
}

class Bound extends GameObject implements Updatable {
    private Shape boundShape;
    private GameObject objectBeingBounded;

    public Bound(GameObject objectToBound, Shape boundShape) {
        super(objectToBound.getPosition());
        this.boundShape = boundShape;
        boundShape.setFill(Game.BOUND_FILL);
        boundShape.setStroke(Game.BOUND_STROKE);
        boundShape.setStrokeWidth(Game.BOUND_STROKE_WIDTH);

        // TODO: delegate to RectangleBound subclass; inappropriate here
        if (boundShape instanceof Rectangle) {
            this.boundShape.setTranslateX(
                    -((Rectangle) boundShape).getWidth() / 2);
            this.boundShape.setTranslateY(
                    -((Rectangle) boundShape).getHeight() / 2);
        }

        this.getChildren().add(this.boundShape);

        objectBeingBounded = objectToBound;

        this.setTranslateX(objectToBound.getPosition().getX());
        this.setTranslateY(objectToBound.getPosition().getY());
    }

    @Override
    public void update() {
        this.setPosition(new Point2D(objectBeingBounded.getPosition().getX(),
                objectBeingBounded.getPosition().getY()));
        this.setTranslateX(this.getPosition().getX());
        this.setTranslateY(this.getPosition().getY());
    }

    public boolean collidesWith(Bound other) {
        return !Shape.intersect(this.getBoundShape(), other.getBoundShape())
                .getBoundsInLocal().isEmpty();
    }

    public boolean containedIn(Bound container) {
        Shape containerShape = container.boundShape;
        double containerMinX = container.getPosition().getX() -
                containerShape.getBoundsInLocal().getWidth() / 2;
        double containerMaxX = container.getPosition().getX() +
                containerShape.getBoundsInLocal().getWidth() / 2;
        double containerMinY = container.getPosition().getY() -
                containerShape.getBoundsInLocal().getHeight() / 2;
        double containerMaxY = container.getPosition().getY() +
                containerShape.getBoundsInLocal().getHeight() / 2;

        double thisMinX = getPosition().getX() -
                boundShape.getBoundsInLocal().getWidth() / 2;
        double thisMaxX = getPosition().getX() +
                boundShape.getBoundsInLocal().getWidth() / 2;
        double thisMinY = getPosition().getY() -
                boundShape.getBoundsInLocal().getHeight() / 2;
        double thisMaxY = getPosition().getY() +
                boundShape.getBoundsInLocal().getHeight() / 2;

        return containerMinX < thisMinX &&
                containerMaxX > thisMaxX &&
                containerMinY < thisMinY &&
                containerMaxY > thisMaxY;
    }

    public GameObject getObjectBeingBounded() {
        return objectBeingBounded;
    }

    public Shape getBoundShape() {
        return boundShape;
    }
}

class RectangleBound extends Bound {

    public RectangleBound(GameObject objectToBound, Rectangle boundShape) {
        super(objectToBound, boundShape);
//        centerAboutOrigin();
    }

    private void centerAboutOrigin() {
        // TODO: see Bound constructor todo
//        Shape rectangleBound = (Shape) super.getChildren().get(0);
//        rectangleBound.setTranslateX(- rectangleBound.getBoundsInLocal()
//                .getWidth() / 2);
//        rectangleBound.setTranslateY(- rectangleBound.getBoundsInLocal()
//                .getHeight() / 2);
    }
}

/**
 * Used for helicopter whose bound is formed by its spinning blade
 */
class CircleBound extends Bound {

    public CircleBound(GameObject objectToBound, Circle boundShape) {
        super(objectToBound, boundShape);
    }

}

/**
 * Is-a Group to treat game objects as Node objects to be put straight onto
 * scene graph. Position treated as center of object (like Circle; unlike
 * Rectangle, etc.). So GameObjects who aren't Circles will have to translate
 * their x and y by half their width and height respectively. Standardizing
 * position attribute of all GameObjects like this makes it consistent and
 * simplifies distance calculations between GameObjects.
 */
abstract class GameObject extends Group {
    private Point2D position;

    public GameObject(Point2D startPosition) {
        position = startPosition;
    }

    public Point2D getPosition() {
        return position;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }
}

class Ponds extends Pane implements Updatable {
    private List<Pond> ponds;

    public Ponds() {
        ponds = new LinkedList<>();
    }

    public void add(Pond pond) {
        ponds.add(pond);
        this.getChildren().add(pond);
    }

    @Override
    public void update() {
        for (Pond p : ponds)
            p.update();
    }

    public int getTotalCapacity() {
        int totalCapacity = 0;
        for (Pond p : ponds)
            totalCapacity += p.getPercentFull();
        return totalCapacity;
    }

    // TODO
    public void fill() {
        for (Pond p : ponds)
            p.fill();
    }
}

class Pond extends GameObject implements Updatable {
    private Circle pondCircle;
    private double maxRadius, currentRadius;
    private GameText percentFullText;
    private int percentFull;

    public Pond(Point2D startPosition, double maxRadius, double currentRadius,
                final Color fill, final Color textFill) {
        super(startPosition);
        this.maxRadius = maxRadius;
        this.currentRadius = currentRadius;
        pondCircle = new Circle(currentRadius, fill);

        makePercentFullText(maxRadius, currentRadius, textFill);

        this.getChildren().addAll(pondCircle, percentFullText);
    }

    private void makePercentFullText(double maxRadius, double currentRadius,
                                     Color textFill) {
        percentFull = (int) ((currentRadius / maxRadius) * 100);
        percentFullText = new GameText(percentFull + "%", textFill);

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

    public int getPercentFull() {
        return percentFull;
    }
}

class Clouds extends Pane implements Updatable {
    private List<Cloud> clouds;

    public Clouds() {
        clouds = new LinkedList<>();
    }

    public void add(Cloud cloud) {
        clouds.add(cloud);
    }

    @Override
    public void update() {
        for (Cloud c : clouds)
            c.update();
    }
}

class Cloud extends GameObject implements Updatable {
    private Circle cloudCircle;
    private Color fill;
    private GameText percentSaturatedText;
    private int seedPercentage;

    public Cloud(Point2D initialPosition, double radius, Color fill,
                 Color textFill) {
        super(initialPosition);
        this.fill = fill;
        cloudCircle = new Circle(radius, fill);

        seedPercentage = 0;
        makePercentSaturatedText(textFill);

        this.getChildren().addAll(cloudCircle, percentSaturatedText);
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

    @Override
    public void update() {
        if (cloudCircle.getFill() != fill) {
            cloudCircle.setFill(fill);
            percentSaturatedText.setText(seedPercentage + "%");
        }
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
 * The starting/ending location for helicopter. Represented as an image.
 */
class Helipad extends GameObject {

    public Helipad(Point2D initialPosition, Point2D dimensions) {
        super(initialPosition);

        loadAndSetupImage(dimensions);

        this.getTransforms().add(new Translate(Game.HELIPAD_POSITION.getX(),
                Game.HELIPAD_POSITION.getY()));
    }

    private void loadAndSetupImage(Point2D dimensions) {
        ImageView image = new ImageView(new Image("helipad_textured.png"));
        image.setFitHeight(dimensions.getY());
        image.setFitWidth(dimensions.getX());
        centerAboutOrigin(dimensions, image);
        this.getChildren().add(image);
    }

    private static void centerAboutOrigin(
            Point2D dimensions, ImageView image) {
        image.setTranslateX(- dimensions.getX() / 2);
        image.setTranslateY(- dimensions.getY() / 2);
    }
}

class Helicopter extends GameObject implements Updatable {
    private GameText fuelGauge;
    private double heading;
    private double speed;
    private double fuel;

    private HeliBody heliBody;
    private HeliBlade heliBlade;
    private HeliState state;

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
        fuelGauge = new GameText("F:" + fuel, Game.HELICOPTER_COLOR,
                FontWeight.BOLD);
        fuelGauge.setTranslateY(- Game.HELIBODY_SIZE / 2);
        fuelGauge.setTranslateX(-25);
    }

    @Override
    public void update() {
        Point2D newPosition = state.updatePosition(this.getPosition(),
                heading, speed, fuel, heliBlade, this);

        if (newPosition != null) {
            this.setPosition(newPosition);
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

    public boolean hasFuel() {
        return fuel > 0;
    }

    public boolean isEngineOff() {
        return state instanceof OffHeliState;
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

    // TODO: check if in OffHeliState instead of checking speed
    public boolean isStationary() {
        return Math.abs(speed) < 1e-3;
//        return state instanceof OffHeliState;
    }
}

class HeliBody extends Group {
    public HeliBody() {
        ImageView image = new ImageView(
                new Image("helibody_2x_transparent.png"));
        image.setFitHeight(Game.HELIBODY_SIZE);
        image.setFitWidth(Game.HELIBODY_SIZE);
        centerAboutOrigin();
        this.setRotate(180);
        this.getChildren().add(image);
    }

    private void centerAboutOrigin() {
        this.setTranslateX(-Game.HELIBODY_SIZE /2);
        this.setTranslateY(-Game.HELIBODY_SIZE /2);
    }
}

class HeliBlade extends Group {
    private double rotationalSpeed;
    private boolean isSpinning;

    public HeliBlade() {
        loadAndSetImage();
        rotationalSpeed = 0;
        startAnimation();
    }

    private void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (isSpinning && rotationalSpeed < Game.ROTOR_MAX_SPEED)
                    rotationalSpeed += Game.ROTOR_ACCELERATION;
                else if (!isSpinning && rotationalSpeed > Game.ROTOR_MIN_SPEED) {
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

interface HeliState {
    HeliState toggleIgnition(HeliBlade heliBlade);

    Point2D updatePosition(Point2D position, double heading,
       double speed, double fuel, HeliBlade heliBlade, Helicopter helicopter);

    double consumeFuel(double currentFuel, double speed);

    void increaseSpeed(Helicopter helicopter);

    void decreaseSpeed(Helicopter helicopter);

    void turnLeft(Helicopter helicopter);

    void turnRight(Helicopter helicopter);
}

class OffHeliState implements HeliState {

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinUp();
        return new StartingHeliState();
    }

    @Override
    public Point2D updatePosition(Point2D position, double heading,
      double speed, double fuel, HeliBlade heliBlade, Helicopter helicopter) {
        return null;
    }

    @Override
    public double consumeFuel(double currentFuel, double speed) {
        return currentFuel;
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void decreaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnLeft(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnRight(Helicopter helicopter) { /* impossible */ }
}

class StartingHeliState implements HeliState {

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinDown();
        return new StoppingHeliState();
    }

    @Override
    public Point2D updatePosition(Point2D position, double heading,
      double speed, double fuel, HeliBlade heliBlade, Helicopter helicopter) {
        if (heliBlade.isUpToSpeed())
            helicopter.changeState(new ReadyHeliState());
        return null;
    }

    @Override
    public double consumeFuel(double currentFuel, double speed) {
        double remainingFuel = currentFuel - Game.FUEL_CONSUMPTION_RATE;
        return remainingFuel > 0 ? remainingFuel : 0;
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void decreaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnLeft(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnRight(Helicopter helicopter) { /* impossible */ }
}

class StoppingHeliState implements HeliState {

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinUp();
        return new StartingHeliState();
    }

    @Override
    public Point2D updatePosition(Point2D position, double heading,
      double speed, double fuel, HeliBlade heliBlade, Helicopter helicopter) {
        if (!heliBlade.isRotating()) {
            helicopter.changeState(new OffHeliState());
        }
        return null;
    }

    @Override
    public double consumeFuel(double currentFuel, double speed) {
        return currentFuel;
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void decreaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnLeft(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnRight(Helicopter helicopter) { /* impossible */ }
}

class ReadyHeliState implements HeliState {

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinDown();
        return new StoppingHeliState();
    }

    @Override
    public Point2D updatePosition(Point2D position, double heading,
      double speed, double fuel, HeliBlade heliBlade, Helicopter helicopter) {
        return new Point2D(
            position.getX() + (Math.sin(Math.toRadians(heading)) * speed),
            position.getY() + (Math.cos(Math.toRadians(heading)) * speed));
    }

    @Override
    public double consumeFuel(double currentFuel, double speed) {
        double remainingFuel =
                currentFuel - (Math.abs(speed) + Game.FUEL_CONSUMPTION_RATE);
        return remainingFuel > 0 ? remainingFuel : 0;
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
class GameText extends Group {
    private Text text;

    public GameText(final String string, final Color fill,
                    FontWeight fontWeight) {
        text = new Text(string);
        text.setFill(fill);
        text.setFont(Font.font("Futura", fontWeight, 13));

        this.setScaleY(-1);
        this.getChildren().add(text);
    }

    public GameText(final String string, final Color fill) {
        this(string, fill, FontWeight.NORMAL);
    }

    public void setText(String string) {
        text.setText(string);
    }

    public String getText() {
        return text.getText();
    }
}
