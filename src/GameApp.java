import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.*;

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
                case D -> game.handleDKeyPressed();
            }
        });
    }
}

/**
 * Is a Pane to serve as the container for all game objects.
 */
class Game extends Pane {
    public static final int GAME_WIDTH = 800;
    public static final int GAME_HEIGHT = 800;

    public static final int NUM_PONDS = 3;
    public static final Color POND_COLOR = Color.BLUE;
    public static final Color POND_TEXT_COLOR = Color.WHITE;
    public static final int MIN_POND_RADIUS = 5;
    public static final int MAX_POND_RADIUS = 50;
    public static final int MAX_STARTING_POND_RADIUS =
            (int) (MAX_POND_RADIUS * 0.30);
    public static final double TOTAL_POND_CAPACITY_TO_WIN = 0.8;

    public static final int MIN_CLOUDS = 3;
    public static final int MAX_CLOUDS = 5;
    public static final Color CLOUD_COLOR = Color.WHITE;
    public static final Color CLOUD_TEXT_COLOR = Color.BLUE;
    public static final int MIN_CLOUD_RADIUS = 30;
    public static final int MAX_CLOUD_RADIUS = 70;
    public static final double RAIN_FREQUENCY = 0.6;

    public static final double MEAN_WIND_SPEED = 0.4;
    public static final double STD_DEV_WIND_SPEED = 0.2;
    public static final int WIND_UPDATE_FREQ_SEC = 5;
    public static final double DISTANCE_LINE_WIDTH = 1;
    public static final Paint DISTANCE_LINE_COLOR = Color.WHITE;

    public static final Point2D HELIPAD_DIMENSIONS = new Point2D(100, 100);
    public static final Point2D HELIPAD_POSITION =
            new Point2D((GAME_WIDTH / 2),
                    (GAME_HEIGHT / 25) + (HELIPAD_DIMENSIONS.getY() / 2));

    public static final Point2D BLIMP_BODY_SIZE = new Point2D(200, 68);
    public static final int BLIMP_ROTOR_SIZE = 70;
    public static final int BLIMP_BLADE_XOFFSET = -90;
    public static final double BLIMP_ROTOR_XSCALE_FACTOR = 0.25;
    public static final double BLIMP_ROTOR_SPEED = 7.5;
    public static final Color BLIMP_FUEL_TEXT_COLOR = Color.rgb(44, 235, 242);
    public static final Point2D BLIMP_TEXT_PANE_SIZE =
            new Point2D(BLIMP_BODY_SIZE.getX() / 2,
                    BLIMP_BODY_SIZE.getY() / 2);
    public static final int BLIMP_TEXT_FONT_SIZE = 16;
    public static final int REFUEL_RATE = 30;
    public static final double REFUELING_SPEED_DIFF_MARGIN = 0.1;

    public static final Color FUEL_GAUGE_COLOR = Color.MAROON;
    public static final int HELIBODY_SIZE = 75;
    public static final int ROTOR_LENGTH = 80;
    public static final int HELICOPTER_MIN_SPEED = -2;
    public static final int HELICOPTER_MAX_SPEED = 10;
    public static final int ROTOR_MIN_SPEED = 0;
    public static final int ROTOR_MAX_SPEED = 15;
    public static final double ROTOR_ACCELERATION = 0.075;
    public static final int HELICOPTER_START_FUEL = 25000;
    public static final int FUEL_CONSUMPTION_RATE = 5;

    public static final Color BOUND_FILL = Color.TRANSPARENT;
    public static final Color BOUND_STROKE = Color.YELLOW;
    public static final int BOUND_STROKE_WIDTH = 1;

    public static final double NANOS_PER_SEC = 1e9;

    private Ponds ponds;
    private Clouds clouds;
    private Blimp blimp; // TODO: collection of blimps
    private Wind wind;
    private Helipad helipad;
    private Helicopter helicopter;
    private BoundsPane bounds;
    private DistanceLines distanceLines;

    private boolean isHelicopterTryingToSeed;
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
        wind = new Wind();

        this.getChildren().clear();
        initPonds();
        initClouds();
        initBlimps();
        this.helipad = makeHelipad();
        this.helicopter = makeHelicopter();
        initBounds();
        initDistanceLines();
        // TODO: blimp -> blimps
        this.getChildren().addAll(ponds, clouds, blimp, helipad, helicopter,
                bounds,
                distanceLines);

        initWind();
        makeGameLoop();
        loop.start();
    }

    // TODO: instantiate blimps
    private void initBlimps() {
        blimp = new Blimp(new Point2D(0, 400), 0.5, 0, 10000);

    }

    private void initWind() {
        for (Cloud c : clouds) {
            wind.addObserver(c);
        }
    }

    private void initDistanceLines() {
        distanceLines = new DistanceLines();
        for (Pond p : ponds)
            for (Cloud c : clouds)
                distanceLines.add(new DistanceLine(p, c));
    }

    private void initBounds() {
        bounds = new BoundsPane();
        for (Cloud c : clouds)
            bounds.add(new CircleBound(c,
                    new Circle(c.getBoundsInParent().getWidth() / 2)));
        bounds.add(new RectangleBound(helipad, new Rectangle(
                helipad.getBoundsInParent().getWidth(),
                helipad.getBoundsInParent().getHeight())));
        bounds.add(new CircleBound(helicopter,
                new Circle(ROTOR_LENGTH / 2)));
        // TODO: blimp -> blimps
        bounds.add(new RectangleBound(blimp, new Rectangle(
                blimp.getBoundsInParent().getWidth(),
                blimp.getBoundsInParent().getHeight())));
    }

    private void initClouds() {
        clouds = new Clouds(wind);
        for (int i = 0; i < randomInRange(MIN_CLOUDS - 1, MAX_CLOUDS); i++)
            clouds.add(makeCloud());
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
        Point2D position = randomPositionInBound(
                    new Point2D(0, (GAME_HEIGHT * (0.33))),
                    new Point2D(GAME_WIDTH, GAME_HEIGHT));
        Cloud cloud = new Cloud(position,
                randomInRange(MIN_CLOUD_RADIUS, MAX_CLOUD_RADIUS),
                MEAN_WIND_SPEED, randomInRange(0, STD_DEV_WIND_SPEED));
        cloud.setTranslateX(position.getX());
        cloud.setTranslateY(position.getY());
        return cloud;
    }

    private void respawnCloud() {
        Point2D position = randomPositionInBound(
                new Point2D(-MAX_CLOUD_RADIUS, (GAME_HEIGHT * (0.33))),
                new Point2D(-MAX_CLOUD_RADIUS, GAME_HEIGHT));
        Cloud cloud = new Cloud(position, randomInRange(MIN_CLOUD_RADIUS,
            MAX_CLOUD_RADIUS), MEAN_WIND_SPEED, randomInRange(0, 0.25));
        cloud.setTranslateX(position.getX());
        cloud.setTranslateY(position.getY());
        clouds.add(cloud);
        bounds.add(new CircleBound(cloud,
                new Circle(cloud.getBoundsInParent().getWidth() / 2)));
        for (Pond p : ponds)
            distanceLines.add(new DistanceLine(p, cloud));
        wind.addObserver(cloud);
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

    private void makeGameLoop() {
        AnimationTimer loop = new AnimationTimer() {
            private double old = -1;
            private double timer = 0;
            private double windTimer = 0;

            @Override
            public void handle(long now) {
                double delta = calculateDelta(now);
                timer += delta;
                windTimer += delta;

                updateGameObjects();
                updateWind();
                refuelIfNearBlimp();
                seedIfNearCloud();
                fillPondsWithRain();
                tryRespawningClouds();

                showLoseDialogIfConditionsMet();
                showWinDialogIfConditionsMet();
            }

            private void refuelIfNearBlimp() {
                if (isRefuelingPossible()) {
                    double extractedFuel = blimp.extractFuel();
                    helicopter.refuelBy(extractedFuel);
                }
            }

            private boolean isRefuelingPossible() {
                var helicopterBounds = bounds.getFor(helicopter);
                var blimpBounds = bounds.getFor(blimp);
                boolean isColliding =
                        helicopterBounds.collidesWith(blimpBounds);
                boolean isSpeedMatching = Math.abs(helicopter.getSpeed()
                        - blimp.getSpeed()) < REFUELING_SPEED_DIFF_MARGIN;
                return isColliding
                        && isSpeedMatching;
            }

            private void updateWind() {
                if (windTimer >= WIND_UPDATE_FREQ_SEC) {
                    wind.update();
                    windTimer = 0;
                }
            }

            private void tryRespawningClouds() {
                if (clouds.getNumberOf() < 3)
                    respawnCloud();
                else if (clouds.getNumberOf() < 5) {
                    int randomNumIn100 = (int) (Math.random() * 100);
                    if (randomNumIn100 % 2 == 0) {
                        respawnCloud();
                    }
                }
            }

            private void updateGameObjects() {
                blimp.update(); // TODO: blimp -> blimps
                helicopter.update();
                clouds.update();
                ponds.update();
                bounds.update();
                distanceLines.update();
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
                double score = helicopter.getRemainingFuel()
                        * ponds.getTotalCapacity();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "You scored " + decimalFormat.format(score)
                            + " points. Give it another go, pilot?");
                alert.setTitle("Mission Success");
                alert.setHeaderText(
                        "You managed to restore the Central Valley!");

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
                        "Try again?");
                alert.setTitle("Mission Failure");
                alert.setHeaderText(
                        "Despite your best efforts the drought still has a " +
                                "hold on the Central Valley.");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                alert.getButtonTypes().setAll(yesButton, noButton);
                return alert;
            }

            private void fillPondsWithRain() {
                if (timer >= RAIN_FREQUENCY) {
                    for (DistanceLine distanceLine : distanceLines) {
                        Pond p = distanceLine.getPond();
                        Cloud c = distanceLine.getCloud();
                        fillPondInProportionToCloudDistance(p, c, distanceLine);
                    }
                    timer = 0;
                }
            }

            private void fillPondInProportionToCloudDistance(Pond p, Cloud c,
                                                 DistanceLine distanceLine) {
                double pondDiameter = 2 * p.getMaxRadius();
                double pondCloudDistance = distanceLine.getDistance();
                if (pondCloudDistance <= (4 * pondDiameter)) {
                    boolean hasRained = c.rain();
                    if (hasRained)
                        p.fillByIncrement(1 -
                                (pondCloudDistance / (4 * pondDiameter)));
                }
            }

            private void seedIfNearCloud() {
                var helicopterBounds = bounds.getFor(helicopter);
                for (Cloud c : clouds) {
                    var cBound = bounds.getFor(c);
                    if (helicopterBounds.collidesWith(cBound)
                            && isHelicopterTryingToSeed) {
                        c.seed();
                        break;
                    }
                }
                isHelicopterTryingToSeed = false;
            }

            private double calculateDelta(long now) {
                if (old < 0)
                    old = now;
                double delta = (now - old) / NANOS_PER_SEC;
                old = now;
                return delta;
            }
        };
        this.loop = loop;
    }

    private boolean isHelicopterWithinHelipad() {
        return (bounds.getFor(helicopter)).containedIn(bounds.getFor(helipad));
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
        isHelicopterTryingToSeed = true;
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
        bounds.toggleVisibility();
    }

    public void handleDKeyPressed() {
        distanceLines.toggleVisibility();
    }
}

class DistanceLines extends Pane implements Updatable, Iterable<DistanceLine> {
    private List<DistanceLine> distanceLines;

    public DistanceLines() {
        distanceLines = new LinkedList<>();
        this.setVisible(false);
    }

    public void add(DistanceLine dLine) {
        distanceLines.add(dLine);
        this.getChildren().add(dLine);
    }

    @Override
    public void update() {
        Iterator<DistanceLine> iterator = distanceLines.iterator();
        for (int i = 0; i < distanceLines.size(); i++) {
            DistanceLine d = iterator.next();
            if (d.getCloud().isOutOfPlay()) {
                this.getChildren().remove(d);
                iterator.remove();
            }
            else
                d.update();
        }
    }

    public void toggleVisibility() {
        this.setVisible(!this.isVisible());
    }

    @Override
    public Iterator<DistanceLine> iterator() {
        return distanceLines.iterator();
    }
}

/**
 * Is a GameObject like Bound is. Postion defined as one end of the
 * DistanceLine, preferably stationary (i.e. Pond)
 */
class DistanceLine extends GameObject implements Updatable {
    private Cloud cloud;
    private Pond pond;
    private Line line;
    private GameText distance;

    public DistanceLine(Pond pond, Cloud cloud) {
        super(pond.getPosition());
        this.cloud = cloud;
        this.pond = pond;

        setupLineShape(pond, cloud);
        setupDistanceText();
    }

    private void setupDistanceText() {
        distance = new GameText(String.valueOf((int) getDistance()),
                Game.DISTANCE_LINE_COLOR);
        setDistanceTextToMidpoint();
        this.getChildren().add(distance);
    }

    private void setDistanceTextToMidpoint() {
        Point2D midpoint = getMidpoint();
        distance.setTranslateX(midpoint.getX());
        distance.setTranslateY(midpoint.getY());
    }

    private void setupLineShape(Pond pond, Cloud cloud) {
        line = new Line(pond.getPosition().getX(), pond.getPosition().getY(),
                cloud.getPosition().getX(), cloud.getPosition().getY());
        line.setStrokeWidth(Game.DISTANCE_LINE_WIDTH);
        line.setStroke(Game.DISTANCE_LINE_COLOR);
        this.getChildren().add(line);
    }

    @Override
    public void update() {
        if (cloud == null)
            return;
        line.setEndX(cloud.getPosition().getX());
        line.setEndY(cloud.getPosition().getY());
        updateDistanceText();
    }

    private void updateDistanceText() {
        distance.setText(String.valueOf((int) getDistance()));
        setDistanceTextToMidpoint();
    }

    private Point2D getMidpoint() {
        return new Point2D((line.getStartX() + line.getEndX()) / 2,
                (line.getStartY() + line.getEndY()) / 2);
    }

    public double getDistance() {
        double squaredSumOfX = Math.pow(line.getEndX() - line.getStartX(), 2);
        double squaredSumOfY = Math.pow(line.getEndY() - line.getStartY(), 2);
        return Math.sqrt(squaredSumOfX + squaredSumOfY);
    }

    public Pond getPond() {
        return pond;
    }

    public Cloud getCloud() {
        return cloud;
    }
}

class BoundsPane extends Pane implements Updatable {
    private List<Bound> bounds;

    public BoundsPane() {
        bounds = new LinkedList<>();
        this.setVisible(false);
    }

    public void add(CircleBound circleBound) {
        bounds.add(circleBound);
        this.getChildren().add(circleBound);
    }

    public void add(RectangleBound rectangleBound) {
        bounds.add(rectangleBound);
        this.getChildren().add(rectangleBound);
    }

    public Bound getFor(GameObject gameObject) {
        for (Bound b : bounds) {
            if (b.getBoundedObject() == gameObject)
                return b;
        }
        return null;
    }

    @Override
    public void update() {
        for (Bound b : bounds)
            b.update();
    }

    public void toggleVisibility() {
        this.setVisible(!this.isVisible());
    }
}

/**
 * Holds a reference to the object it's bounding so that it can be garbage
 * collected along with its object.
 */
class Bound extends GameObject implements Updatable {
    private GameObject boundedObject;
    private Shape boundShape;

    public Bound(GameObject objectToBound, Shape boundShape) {
        super(objectToBound.getPosition());
        setBoundShapeDefaultProperties(boundShape);

        boundedObject = objectToBound;
        this.setTranslateX(objectToBound.getPosition().getX());
        this.setTranslateY(objectToBound.getPosition().getY());
    }

    private void setBoundShapeDefaultProperties(Shape boundShape) {
        this.boundShape = boundShape;
        boundShape.setFill(Game.BOUND_FILL);
        boundShape.setStroke(Game.BOUND_STROKE);
        boundShape.setStrokeWidth(Game.BOUND_STROKE_WIDTH);
        this.getChildren().add(this.boundShape);
    }

    @Override
    public void update() {
        this.setPosition(new Point2D(boundedObject.getPosition().getX(),
                boundedObject.getPosition().getY()));
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

    public GameObject getBoundedObject() {
        return boundedObject;
    }

    public Shape getBoundShape() {
        return boundShape;
    }
}

class RectangleBound extends Bound {

    public RectangleBound(GameObject objectToBound, Rectangle boundShape) {
        super(objectToBound, boundShape);
        centerAboutOrigin();
    }

    private void centerAboutOrigin() {
        getBoundShape().setTranslateX(
                -((Rectangle) getBoundShape()).getWidth() / 2);
        getBoundShape().setTranslateY(
                -((Rectangle) getBoundShape()).getHeight() / 2);
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

class Ponds extends Pane implements Updatable, Iterable<Pond> {
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

    public double getTotalCapacity() {
        int totalCapacity = 0;
        for (Pond p : ponds)
            totalCapacity += p.getPercentFull();
        return ((double) totalCapacity) / 100;
    }

    @Override
    public Iterator<Pond> iterator() {
        return ponds.iterator();
    }
}

class TransientGameObject extends GameObject {
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

class BlimpBody extends Group {

    public BlimpBody() {
        configureAndAddImage();
    }

    private void configureAndAddImage() {
        ImageView image = new ImageView(
                new Image("blimp_transparent_trimmed.png"));
        image.setFitHeight(Game.BLIMP_BODY_SIZE.getY());
        image.setFitWidth(Game.BLIMP_BODY_SIZE.getX());
        centerAboutOrigin(image);
        this.getChildren().add(image);
    }

    private void centerAboutOrigin(ImageView image) {
        image.getTransforms().add(
                new Translate(- Game.BLIMP_BODY_SIZE.getX() / 2,
                        - Game.BLIMP_BODY_SIZE.getY() / 2)
        );
    }
}

class BlimpBlade extends Group {
    private double angle = 0;

    public BlimpBlade() {
        ImageView image = new ImageView(
                new Image("blimp_rotor_transparent.png"));
        image.setFitHeight(Game.BLIMP_ROTOR_SIZE);
        image.setFitWidth(Game.BLIMP_ROTOR_SIZE);
        centerAboutOrigin(image);
        this.getChildren().add(image);
        startAnimation();
    }

    private void centerAboutOrigin(ImageView image) {
        image.getTransforms().add(
                new Translate(- Game.BLIMP_ROTOR_SIZE / 2,
                        - Game.BLIMP_ROTOR_SIZE / 2));
    }

    private void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {
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
        loop.start();
    }
}

class Blimp extends TransientGameObject implements Updatable {
    private BlimpBody body;
    private BlimpBlade blade;
    private GameText fuelText;
    private double fuel;

    public Blimp(Point2D initialPosition, double speed, double speedOffset,
                 double fuel) {
        super(initialPosition, speed, speedOffset);
        buildShape();
        addFuelGauge(fuel);
        this.getTransforms().add(new Translate(initialPosition.getX(),
                initialPosition.getY()));
    }

    private void addFuelGauge(double fuel) {
        this.fuel = fuel;
        fuelText = new GameText(String.valueOf((int) fuel),
                Game.BLIMP_FUEL_TEXT_COLOR);
        fuelText.setSize(Game.BLIMP_TEXT_FONT_SIZE);
        StackPane fuelPane = new StackPane(fuelText);
        fuelPane.setAlignment(Pos.CENTER);
        fuelPane.setPrefSize(Game.BLIMP_TEXT_PANE_SIZE.getX(),
                Game.BLIMP_TEXT_PANE_SIZE.getY());
        fuelPane.getTransforms().add(
                new Translate(- Game.BLIMP_TEXT_PANE_SIZE.getX() / 2,
                        - Game.BLIMP_TEXT_PANE_SIZE.getY() / 2));
        this.getChildren().add(fuelPane);
    }

    private void buildShape() {
        body = new BlimpBody();
        blade = new BlimpBlade();
        blade.getTransforms().add(new Translate(Game.BLIMP_BLADE_XOFFSET, 0));
        this.getChildren().addAll(body, blade);
    }

    @Override
    public void update() {
        Point2D newPosition = new Point2D(
                this.getPosition().getX() + this.getSpeed(),
                this.getPosition().getY());
        this.setPosition(newPosition);

        this.getTransforms().clear();
        this.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));

        fuelText.setText(String.valueOf((int) fuel));
    }

    public double extractFuel() {
        if (fuel >= Game.REFUEL_RATE) {
            fuel -= Game.REFUEL_RATE;
            return Game.REFUEL_RATE;
        } else {
            double remainder = fuel;
            fuel = 0;
            return remainder;
        }
    }
}

abstract class BlimpState implements Updatable {
    double fuel;
    /**
     * State dependent behaviors for Blimp:
     * 1. extractFuel() — only possible when InView
     * 2.
     */
    abstract double extractFuel();

}

class CreatedBlimp extends BlimpState {

    @Override
    public double extractFuel() {
        return 0;
    }

    @Override
    public void update() {

    }
}

class InViewBlimp extends BlimpState {
    @Override
    public double extractFuel() {
        if (fuel >= Game.REFUEL_RATE) {
            fuel -= Game.REFUEL_RATE;
            return Game.REFUEL_RATE;
        } else {
            double remainder = fuel;
            fuel = 0;
            return remainder;
        }

    }

    @Override
    public void update() {

    }
}

class DeadBlimp extends BlimpState {

    @Override
    double extractFuel() {
        return 0;
    }

    @Override
    public void update() {

    }
}




class Pond extends GameObject implements Updatable {
    private Circle pondCircle;
    private double maxRadius, currentRadius;
    private double maxArea, currentArea;
    private GameText percentFullText;
    private int percentFull;

    public Pond(Point2D position, double maxRadius, double currentRadius,
                final Color fill, final Color textFill) {
        super(position);
        this.maxRadius = maxRadius;
        maxArea = Math.PI * Math.pow(maxRadius, 2);
        this.currentRadius = currentRadius;
        currentArea = Math.PI * Math.pow(currentRadius, 2);
        pondCircle = new Circle(currentRadius, fill);

        makePercentFullText(textFill);

        this.getChildren().addAll(pondCircle, percentFullText);
    }

    private void makePercentFullText(Color textFill) {
        percentFull = (int) ((currentArea / maxArea) * 100);
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

    public void fillByIncrement(double multiplier) {
        currentArea += (maxArea * 0.01) * multiplier;
        if (currentArea > maxArea)
            currentArea = maxArea;

        currentRadius = Math.sqrt((currentArea / Math.PI));
        percentFull = (int) (currentArea / maxArea * 100);
    }

    public int getPercentFull() {
        return percentFull;
    }

    public double getMaxRadius() {
        return maxRadius;
    }
}

class Wind implements Updatable {
    private double speed;
    private Random random;
    private List<TransientGameObject> observers;

    public Wind() {
        this.speed = Game.MEAN_WIND_SPEED;
        random = new Random();
        observers = new LinkedList<>();
    }

    @Override
    public void update() {
        /* nextGaussian use: https://stackoverflow.com/a/6012014 */
        speed = random.nextGaussian()
                * Game.STD_DEV_WIND_SPEED + Game.MEAN_WIND_SPEED;
        notifyObservers();
    }

    public void addObserver(TransientGameObject t) {
        observers.add(t);
    }

    public void removeObserver(TransientGameObject t) {
        observers.remove(t);
    }

    private void notifyObservers() {
        for (TransientGameObject t : observers) {
            t.impartSpeed(speed);
        }
    }
}

class Clouds extends Pane implements Updatable, Iterable<Cloud> {
    private List<Cloud> clouds;
    private Wind wind;

    public Clouds(Wind wind) {
        clouds = new LinkedList<>();
        this.wind = wind;
    }

    public void add(Cloud cloud) {
        clouds.add(cloud);
        this.getChildren().add(cloud);
    }

    @Override
    public void update() {
        Iterator<Cloud> iterator = clouds.iterator();
        for (int i = 0; i < clouds.size(); i++) {
            Cloud c = iterator.next();
            if (c.isOutOfPlay()) {
                this.getChildren().remove(c);
                wind.removeObserver(c);
                iterator.remove();
            }
            else
                c.update();
        }
    }

    @Override
    public Iterator<Cloud> iterator() {
        return clouds.iterator();
    }

    public int getNumberOf() {
        return clouds.size();
    }
}

class Cloud extends TransientGameObject implements Updatable {
    private Circle cloudCircle;
    private Color fill;
    private GameText percentSaturatedText;
    private int seedPercentage;
    private CloudState state;

    public Cloud(Point2D initialPosition, double radius, double speed,
                 double speedOffset) {
        super(initialPosition, speed, speedOffset);
        this.fill = Game.CLOUD_COLOR;
        cloudCircle = new Circle(radius, fill);

        seedPercentage = 0;
        makePercentSaturatedText(Game.CLOUD_TEXT_COLOR);

        this.getChildren().addAll(cloudCircle, percentSaturatedText);
        state = new AliveCloud();
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
        state.update(this, cloudCircle, fill, percentSaturatedText,
                seedPercentage);
    }

    public void seed() {
        state.seed(this, cloudCircle);
    }

    public boolean rain() {
        return state.rain(this, cloudCircle);
    }

    public int getSeedPercentage() {
        return seedPercentage;
    }

    public void incrementSeedPercentage() {
        seedPercentage++;
        int red = (int) (255 * ((Color) cloudCircle.getFill()).getRed());
        int green =
                (int) (255 * ((Color) cloudCircle.getFill()).getGreen());
        int blue = (int) (255 * ((Color) cloudCircle.getFill()).getBlue());
        fill = Color.rgb(--red, --green, --blue);
    }

    public void decrementSeedPercentage() {
        seedPercentage--;
        int red = (int) (255 * ((Color) cloudCircle.getFill()).getRed());
        int green =
                (int) (255 * ((Color) cloudCircle.getFill()).getGreen());
        int blue = (int) (255 * ((Color) cloudCircle.getFill()).getBlue());
        fill = Color.rgb(++red, ++green, ++blue);
    }

    public void changeState(CloudState cloudState) {
        this.state = cloudState;
    }

    public double getRadius() {
        return cloudCircle.getRadius();
    }

    public boolean isOutOfPlay() {
        return state instanceof DeadCloud;
    }

}

interface CloudState {

    void update(Cloud cloud, Circle cloudCircle, Color fill,
                GameText percentSaturatedText, int seedPercentage);

    void seed(Cloud cloud, Circle cloudCircle);

    boolean rain(Cloud cloud, Circle cloudCircle);

}

class AliveCloud implements CloudState {

    @Override
    public void update(Cloud cloud, Circle cloudCircle, Color fill,
                       GameText percentSaturatedText, int seedPercentage) {
        Point2D currentPosition = cloud.getPosition();
        Point2D newPosition = new Point2D(currentPosition.getX()
                + cloud.getSpeed(), currentPosition.getY());
        cloud.setPosition(newPosition);
        cloud.setTranslateX(cloud.getTranslateX() + cloud.getSpeed());

        if (cloud.getPosition().getX() + cloud.getRadius() >= 0)
            cloud.changeState(new InPlayCloud());
    }

    @Override
    public void seed(Cloud cloud, Circle cloudCircle) {

    }

    @Override
    public boolean rain(Cloud cloud, Circle cloudCircle) {
        return false;
    }
}

class InPlayCloud implements CloudState {

    @Override
    public void update(Cloud cloud, Circle cloudCircle, Color fill,
                       GameText percentSaturatedText, int seedPercentage) {
        if (cloudCircle.getFill() != fill) {
            cloudCircle.setFill(fill);
            percentSaturatedText.setText(seedPercentage + "%");
        }
        Point2D currentPosition = cloud.getPosition();
        Point2D newPosition = new Point2D(currentPosition.getX()
                + cloud.getSpeed(), currentPosition.getY());
        cloud.setPosition(newPosition);
        cloud.setTranslateX(cloud.getTranslateX() + cloud.getSpeed());

        if (cloud.getPosition().getX() - cloud.getRadius() > 800)
            cloud.changeState(new DeadCloud());
    }

    @Override
    public void seed(Cloud cloud, Circle cloudCircle) {
        if (cloud.getSeedPercentage() < 100)
            cloud.incrementSeedPercentage();
    }

    @Override
    public boolean rain(Cloud cloud, Circle cloudCircle) {
        if (cloud.getSeedPercentage() >= 30) {
            cloud.decrementSeedPercentage();
            return true;
        }
        return false;
    }
}

class DeadCloud implements CloudState {

    @Override
    public void update(Cloud cloud, Circle cloudCircle, Color fill,
                       GameText percentSaturatedText, int seedPercentage) {
        /* should be deleted by now */
    }

    @Override
    public void seed(Cloud cloud, Circle cloudCircle) {

    }

    @Override
    public boolean rain(Cloud cloud, Circle cloudCircle) {
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
        fuelGauge = new GameText("F:" + fuel, Game.FUEL_GAUGE_COLOR,
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

    // TODO: check if in OffHeliState instead of checking speed
    public boolean isStationary() {
        return Math.abs(speed) < 1e-3;
//        return state instanceof OffHeliState;
    }

    public void refuelBy(double fuel) {
        this.fuel += fuel;
    }
}

class HeliBody extends Group {
    public HeliBody() {
        loadAndSetImage();
    }

    private void loadAndSetImage() {
        ImageView image = new ImageView(
                new Image("helibody_2x_transparent.png"));
        image.setFitHeight(Game.HELIBODY_SIZE);
        image.setFitWidth(Game.HELIBODY_SIZE);
        centerAboutOriginAndFlip();
        this.getChildren().add(image);
    }

    private void centerAboutOriginAndFlip() {
        this.setTranslateX(- Game.HELIBODY_SIZE / 2);
        this.setTranslateY(- Game.HELIBODY_SIZE / 2);
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
                new Image("heliblade_2wing_transparent.png"));
        image.setFitHeight(Game.ROTOR_LENGTH);
        image.setFitWidth(Game.ROTOR_LENGTH);
        centerAboutOrigin();
        this.getChildren().add(image);
    }

    private void centerAboutOrigin() {
        this.setTranslateX(- Game.ROTOR_LENGTH / 2);
        this.setTranslateY(- Game.ROTOR_LENGTH / 2);
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
 * Not all GameObjects need to update, but those that require so will be
 * updated from Game loop.
 */
interface Updatable {
    void update();

}

/**
 * Since the game coordinate space is being scaled by -1 (to convert to
 * quadrant I from IV), any text will have to be scaled accordingly.
 */
class GameText extends Group {
    private Text text;

    public GameText(final String string, final Paint fill,
                    FontWeight fontWeight) {
        text = new Text(string);
        text.setFill(fill);
        text.setFont(Font.font("Futura", fontWeight, 13));

        this.setScaleY(-1);
        this.getChildren().add(text);
    }

    public GameText(final String string, final Paint fill) {
        this(string, fill, FontWeight.NORMAL);
    }

    public void setStroke(Color color, double width) {
        text.setStroke(color);
        text.setStrokeWidth(width);
    }

    public void setSize(double size) {
        Font font = text.getFont();
        text.setFont(Font.font(font.getFamily(),
                FontWeight.NORMAL, size));
    }

    public void setText(String string) {
        text.setText(string);
    }
}
