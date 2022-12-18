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
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Pair;

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
    public static final Color DEFAULT_CLOUD_COLOR = Color.WHITE;
    public static final Color CLOUD_STROKE_COLOR = Color.GREY;
    public static final Color CLOUD_TEXT_COLOR = Color.BLUE;
    public static final int MIN_CLOUD_MINOR_RADIUS = 40;
    public static final int MAX_CLOUD_MINOR_RADIUS = 60;
    public static final int MIN_CLOUD_MAJOR_RADIUS = 60;
    public static final int MAX_CLOUD_MAJOR_RADIUS = 100;
    public static final double RAIN_FREQUENCY = 0.6;
    public static final int MIN_CLOUD_SATURATION_TO_RAIN = 30;
    public static final int MAX_RANGE_RAIN_MULTIPLIER = 4;

    public static final double MEAN_WIND_SPEED = 0.4;
    public static final double STD_DEV_WIND_SPEED = 0.15;
    public static final int WIND_UPDATE_FREQ_IN_SEC = 5;
    public static final double DISTANCE_LINE_WIDTH = 1;
    public static final Paint DISTANCE_LINE_COLOR = Color.WHITE;
    public static final Paint DISTANCE_LINE_TEXT_COLOR = Color.BLACK;

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
    public static final double BLIMP_MIN_SPEED = 0.7;
    public static final double BLIMP_MAX_SPEED = 1;
    public static final double BLIMP_MIN_SPEED_OFFSET = 0.2;
    public static final double BLIMP_MAX_SPEED_OFFSET = 0.4;
    public static final double BLIMP_MIN_FUEL = 5000;
    public static final double BLIMP_MAX_FUEL = 10000;
    public static final int BLIMP_RESPAWN_ATTEMPT_FREQ_SEC = 5;
    public static final int BLIMP_RESPAWN_CHANCE_PERCENT = 18;

    public static final Color FUEL_GAUGE_COLOR = Color.MAROON;
    public static final int HELIBODY_SIZE = 75;
    public static final int ROTOR_LENGTH = 80;
    public static final int HELICOPTER_MIN_SPEED = -2;
    public static final int HELICOPTER_MAX_SPEED = 10;
    public static final int ROTOR_MIN_SPEED = 0;
    public static final int ROTOR_MAX_SPEED = 15;
    public static final double ROTOR_ACCELERATION = 0.075;
    public static final int STARTING_FUEL = 25000;
    public static final int BASE_FUEL_CONSUMPTION_RATE = 5;

    public static final Color BOUND_FILL = Color.TRANSPARENT;
    public static final Color BOUND_STROKE = Color.YELLOW;
    public static final int BOUND_STROKE_WIDTH = 1;

    public static final Media HELICOPTER_MEDIA = new Media(
            Game.class.getResource(
            "audio/helicopter-engine-loop-long.wav")
                    .toExternalForm());
    public static final Media BLIMP_MEDIA = new Media(Game.class.getResource(
            "audio/drone-engine.wav").toExternalForm());
    public static final Media WIND_MEDIA = new Media(Game.class.getResource(
            "audio/wind-howl.mp3").toExternalForm());
    public static final Media RAIN_MEDIA = new Media(Game.class.getResource(
            "audio/rain-loop-long.wav").toExternalForm());
    public static final double HELICOPTER_VOLUME = 1;
    public static final double HELICOPTER_PLAYBACK_RATE = 1.15;
    public static final double BLIMP_VOLUME = 0.1;
    public static final double WIND_VOLUME = 0.1;
    public static final double RAIN_VOLUME = 0.2;
    public static final double SEEDING_VOLUME = 0.05;
    public static final double REFUELING_VOLUME = 0.3;
    public static final double THUNDER_VOLUME = 0.8;
    public static final double THUNDER_CHANCE = 0.01;

    public static final double NANOS_PER_SEC = 1e9;
    public static final int HUNDRED_PERCENT = 100;
    public static final int MAX_RGB_INT = 255;

    private AudioClip seedingAudio;
    private AudioClip refuelingAudio;
    private Ponds ponds;
    private Clouds clouds;
    private Blimps blimps;
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

    public static boolean checkProbability(double probability) {
        return Math.random() < probability;
    }

    public Game() {
        /* image credit: https://earthobservatory.nasa.gov/images/51341/
        two-views-of-the-painted-desert */
        BackgroundImage background = new BackgroundImage(
                new Image("images/desert_background_large.png"),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        setBackground(new Background(background));
        setScaleY(-1);
        init();
    }

    private void init() {
        wind = new Wind();

        getChildren().clear();
        initPonds();
        initClouds();
        blimps = new Blimps();
        helipad = makeHelipad();
        configureSeedingAndRefuelingAudio();
        helicopter = makeHelicopter();
        initBounds();
        initDistanceLines();
        getChildren().addAll(helipad, ponds, clouds, blimps, helicopter,
                bounds, distanceLines);

        initWind();
        makeGameLoop();
        loop.start();
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
            bounds.add(new RectangleBound(c,
                    new Rectangle(c.getBoundsInLocal().getWidth(),
                            c.getBoundsInLocal().getHeight())));
        bounds.add(new RectangleBound(helipad, new Rectangle(
                helipad.getBoundsInParent().getWidth(),
                helipad.getBoundsInParent().getHeight())));
        bounds.add(new CircleBound(helicopter,
                new Circle(ROTOR_LENGTH / 2)));
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

    // TODO: factory method for helicopter, helipad, cloud, etc. defined in
    //  their own class (like the way blimp does it, see below)
    private static Helicopter makeHelicopter() {
        Helicopter helicopter = new Helicopter(HELIPAD_POSITION,
                STARTING_FUEL);
        return helicopter;
    }

    private void configureSeedingAndRefuelingAudio() {
        seedingAudio = new AudioClip(getClass().getResource(
                "audio/rainmaker-seeding.wav").toExternalForm());
        seedingAudio.setVolume(SEEDING_VOLUME);

        refuelingAudio = new AudioClip(getClass().getResource(
                "audio/helicopter-refueling.wav").toExternalForm());
        refuelingAudio.setVolume(REFUELING_VOLUME);
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
                randomInRange(MIN_CLOUD_MAJOR_RADIUS, MAX_CLOUD_MAJOR_RADIUS),
                randomInRange(MIN_CLOUD_MINOR_RADIUS, MAX_CLOUD_MINOR_RADIUS),
                MEAN_WIND_SPEED, randomInRange(0, STD_DEV_WIND_SPEED));
        cloud.getTransforms().add(
                new Translate(position.getX(), position.getY()));
        return cloud;
    }

    private void respawnCloud() {
        Point2D position = randomPositionInBound(
            new Point2D(-MAX_CLOUD_MAJOR_RADIUS * 2, (GAME_HEIGHT * (0.33))),
            new Point2D(-MAX_CLOUD_MAJOR_RADIUS * 2, GAME_HEIGHT));
        Cloud cloud = new Cloud(position,
                randomInRange(MIN_CLOUD_MAJOR_RADIUS, MAX_CLOUD_MAJOR_RADIUS),
                randomInRange(MIN_CLOUD_MINOR_RADIUS, MAX_CLOUD_MINOR_RADIUS),
                MEAN_WIND_SPEED, randomInRange(0, STD_DEV_WIND_SPEED));
        cloud.getTransforms().add(
                new Translate(position.getX(), position.getY()));
        clouds.add(cloud);
        bounds.add(new RectangleBound(cloud,
                new Rectangle(cloud.getBoundsInLocal().getWidth(),
                        cloud.getBoundsInLocal().getHeight())));
        for (Pond p : ponds)
            distanceLines.add(new DistanceLine(p, cloud));
        wind.addObserver(cloud);
    }

    private void spawnBlimp() {
        Blimp b = Blimp.makeBlimp();
        blimps.add(b);
        bounds.add(new RectangleBound(b,
                new Rectangle(b.getBoundsInLocal().getWidth(),
                        b.getBoundsInLocal().getHeight())));
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
            private double timeSinceLastRain = 0;
            private double timeSinceWindChange = 0;
            private double timeSinceBlimpRespawnTry = 0;

            @Override
            public void handle(long now) {
                double delta = calculateDelta(now);
                incrementTimers(delta);

                markForDeletionBoundsOfDeadObjects();
                markForDeletionDistanceLinesOfDeadClouds();
                updateGameObjects();
                updateWind();
                trySpawningBlimp();
                refuelIfNearBlimp();
                seedIfNearCloud();
                fillPondsWithRain();
                tryRespawningClouds();

                showLoseDialogIfConditionsMet();
                showWinDialogIfConditionsMet();
            }

            private void incrementTimers(double delta) {
                timeSinceLastRain += delta;
                timeSinceWindChange += delta;
                timeSinceBlimpRespawnTry += delta;
            }

            private void updateGameObjects() {
                blimps.update();
                helicopter.update();
                clouds.update();
                ponds.update();
                bounds.update();
                distanceLines.update();
            }

            private void markForDeletionDistanceLinesOfDeadClouds() {
                for (DistanceLine d : distanceLines)
                    if (d.getDynamicEndpoint() instanceof Cloud cloud
                            && cloud.getState() instanceof DeadCloud)
                        distanceLines.markForDeletion(d);
            }

            private void markForDeletionBoundsOfDeadObjects() {
                for (Bound b : bounds) {
                    GameObject gameObject = b.getBoundedObject();
                    if (gameObject instanceof Blimp blimp
                            && blimp.getState() instanceof DeadBlimp)
                        bounds.markForDeletion(b);
                    else if (gameObject instanceof Cloud cloud
                            && cloud.getState() instanceof DeadCloud)
                        bounds.markForDeletion(b);
                }
            }

            private void trySpawningBlimp() {
                boolean isTimeToTryBlimpSpawn =
                    timeSinceBlimpRespawnTry >= BLIMP_RESPAWN_ATTEMPT_FREQ_SEC;
                if (isTimeToTryBlimpSpawn) {
                    int random = (int) randomInRange(0, 100);
                    if (random <= BLIMP_RESPAWN_CHANCE_PERCENT)
                        spawnBlimp();
                    timeSinceBlimpRespawnTry = 0;
                }
            }

            private void refuelIfNearBlimp() {
                for (Blimp b : blimps)
                    if (isRefuelingPossible(b)) {
                        double extractedFuel = b.extractFuel();
                        helicopter.refuelBy(extractedFuel);
                        if (extractedFuel > 0 && !refuelingAudio.isPlaying())
                            refuelingAudio.play();
                    }
            }

            private boolean isRefuelingPossible(Blimp blimp) {
                var helicopterBounds = bounds.getBoundFor(helicopter);
                var blimpBounds = bounds.getBoundFor(blimp);
                boolean isColliding =
                        helicopterBounds.collidesWith(blimpBounds);
                boolean isSpeedMatching = Math.abs(helicopter.getSpeed()
                        - blimp.getSpeed()) < REFUELING_SPEED_DIFF_MARGIN;
                return isColliding
                        && isSpeedMatching;
            }

            private void updateWind() {
                if (timeSinceWindChange >= WIND_UPDATE_FREQ_IN_SEC) {
                    wind.update();
                    timeSinceWindChange = 0;
                }
            }

            private void tryRespawningClouds() {
                if (clouds.getNumberOf() < MIN_CLOUDS)
                    respawnCloud();
                else if (clouds.getNumberOf() < MAX_CLOUDS) {
                    int randomNumIn100 = (int) (Math.random() * 100);
                    if (randomNumIn100 % 2 == 0)
                        respawnCloud();
                }
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
                stopAllAudio();
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
                        "Congratulations! You single-handedly ended the " +
                                "central valley's drought!");

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
                        "Gather your wits and try again, pilot?");
                alert.setTitle("Mission Failure");
                alert.setHeaderText(
                        "Despite your best efforts the drought still has a " +
                                "hold on the central valley.");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                alert.getButtonTypes().setAll(yesButton, noButton);
                return alert;
            }

            private void fillPondsWithRain() {
                if (timeSinceLastRain >= RAIN_FREQUENCY) {
                    for (DistanceLine distanceLine : distanceLines) {
                        Pond p =
                            distanceLine.getStaticEndpoint() instanceof Pond ?
                                    (Pond) distanceLine.getStaticEndpoint()
                                    : null;
                        Cloud c =
                            distanceLine.getDynamicEndpoint() instanceof Cloud ?
                                    (Cloud) distanceLine.getDynamicEndpoint()
                                    : null;
                        if (p == null || c == null)
                            throw new IllegalStateException("Instance of " +
                                    "distance line not a pond, cloud pair");
                        fillPondRelativeToCloudDistance(p, c, distanceLine);
                    }
                    timeSinceLastRain = 0;
                }
            }

            private void fillPondRelativeToCloudDistance(
                    Pond pond, Cloud cloud, DistanceLine distanceLine) {
                double pondDiameter = 2 * pond.getMaxRadius();
                double pondCloudDistance = distanceLine.getDistance();
                if (pondCloudDistance <=
                        (MAX_RANGE_RAIN_MULTIPLIER * pondDiameter)) {
                    boolean hasRained = cloud.tryToRain();
                    if (hasRained) {
                        pond.fillByIncrement(1 - (pondCloudDistance
                                / (MAX_RANGE_RAIN_MULTIPLIER * pondDiameter)));
                    }
                }
            }

            private void seedIfNearCloud() {
                var helicopterBounds = bounds.getBoundFor(helicopter);
                for (Cloud c : clouds) {
                    var cBound = bounds.getBoundFor(c);
                    if (helicopterBounds.collidesWith(cBound)
                            && isHelicopterTryingToSeed) {
                        c.seed();
                        seedingAudio.play();
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
        return (bounds.getBoundFor(helicopter)).containedIn(
                bounds.getBoundFor(helipad));
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
        stopAllAudio();
        init();
    }

    private void stopAllAudio() {
        wind.stopAudio();
        helicopter.stopAudio();
        blimps.stopAudio();
        clouds.stopAudio();
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
    private List<DistanceLine> markedForDeletion;

    public DistanceLines() {
        distanceLines = new LinkedList<>();
        markedForDeletion = new LinkedList<>();
        setVisible(false);
    }

    public void add(DistanceLine dLine) {
        distanceLines.add(dLine);
        getChildren().add(dLine);
    }

    public void markForDeletion(DistanceLine dLine) {
        boolean validDeletion = distanceLines.contains(dLine);
        if (validDeletion)
            markedForDeletion.add(dLine);
    }

    @Override
    public void update() {
        for (DistanceLine d : distanceLines)
            d.update();
        tryDeletingDistanceLinesMarkedForDeletion();
    }

    private void tryDeletingDistanceLinesMarkedForDeletion() {
        if (markedForDeletion.size() > 0) {
            markedForDeletion.forEach(dLine -> getChildren().remove(dLine));
            distanceLines.removeAll(markedForDeletion);
            markedForDeletion.clear();
        }
    }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }

    @Override
    public Iterator<DistanceLine> iterator() {
        return distanceLines.iterator();
    }
}

/**
 * Is a GameObject like Bound is. Postion defined as stationary endpoint of the
 * DistanceLine (e.g. Pond).
 */
class DistanceLine extends GameObject implements Updatable {
    private GameObject staticEndpoint, dynamicEndpoint;
    private Line line;
    private GameText distanceText;
    private StackPane textPane;

    public DistanceLine(GameObject staticEndpoint, GameObject dynamicEndpoint) {
        super(staticEndpoint.getPosition());
        this.staticEndpoint = staticEndpoint;
        this.dynamicEndpoint = dynamicEndpoint;

        setupLineShape();
        setupDistanceText();
    }

    private void setupDistanceText() {
        distanceText = new GameText(String.valueOf((int) getDistance()),
                Game.DISTANCE_LINE_TEXT_COLOR);
        textPane = new StackPane(distanceText);
        alignTextToMidpoint();
        getChildren().add(textPane);
    }

    private void alignTextToMidpoint() {
        Point2D midpoint = getMidpoint();
        textPane.setTranslateX(midpoint.getX() - textPane.getWidth() / 2);
        textPane.setTranslateY(midpoint.getY() - textPane.getHeight() / 2);
    }

    private void setupLineShape() {
        line = new Line(staticEndpoint.getPosition().getX(),
                staticEndpoint.getPosition().getY(),
                dynamicEndpoint.getPosition().getX(),
                dynamicEndpoint.getPosition().getY());
        line.setStrokeWidth(Game.DISTANCE_LINE_WIDTH);
        line.setStroke(Game.DISTANCE_LINE_COLOR);
        getChildren().add(line);
    }

    @Override
    public void update() {
        if (dynamicEndpoint == null)
            return;
        line.setEndX(dynamicEndpoint.getPosition().getX());
        line.setEndY(dynamicEndpoint.getPosition().getY());
        updateDistanceText();
    }

    private void updateDistanceText() {
        distanceText.setText(String.valueOf((int) getDistance()));
        alignTextToMidpoint();
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

    public GameObject getStaticEndpoint() {
        return staticEndpoint;
    }

    public GameObject getDynamicEndpoint() {
        return dynamicEndpoint;
    }
}

class BoundsPane extends Pane implements Updatable, Iterable<Bound> {
    private List<Bound> bounds;
    private List<Bound> markedForDeletion;

    public BoundsPane() {
        bounds = new LinkedList<>();
        markedForDeletion = new LinkedList<>();
        setVisible(false);
    }

    public void add(CircleBound circleBound) {
        bounds.add(circleBound);
        getChildren().add(circleBound);
    }

    public void add(RectangleBound rectangleBound) {
        bounds.add(rectangleBound);
        getChildren().add(rectangleBound);
    }

    public Bound getBoundFor(GameObject gameObject) {
        for (Bound b : bounds) {
            if (b.getBoundedObject() == gameObject)
                return b;
        }
        return null;
    }

    public void markForDeletion(Bound bound) {
        boolean validDeletion = bounds.contains(bound);
        if (validDeletion)
            markedForDeletion.add(bound);
    }

    @Override
    public void update() {
        for (Bound b : bounds)
            b.update();
        tryDeletingBoundsMarkedForDeletion();
    }

    private void tryDeletingBoundsMarkedForDeletion() {
        if (markedForDeletion.size() > 0) {
            markedForDeletion.forEach(bound -> getChildren().remove(bound));
            bounds.removeAll(markedForDeletion);
            markedForDeletion.clear();
        }
    }

    public void toggleVisibility() {
        this.setVisible(!this.isVisible());
    }

    @Override
    public Iterator<Bound> iterator() {
        return bounds.iterator();
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
        this.updatePositionTo(new Point2D(boundedObject.getPosition().getX(),
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
 * scene graph. Position treated as center of object (like Circle but unlike
 * Rectangle, etc.). In other words,  GameObjects who aren't Circles will have
 * to translate their x- and y-position by half their width and height
 * respectively. Standardizing the position attribute of all GameObjects like
 * this makes it consistent and simplifies distance calculations between
 * GameObjects.
 * Additionally, GameObject does not implement Updatable because it cannot be
 * assumed that all inheritors will have an updatable quality (e.g. Helipad).
 */
abstract class GameObject extends Group {
    private Point2D position;

    public GameObject(Point2D position) {
        this.position = position;
    }

    public Point2D getPosition() {
        return position;
    }

    public void updatePositionTo(Point2D position) {
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

// TODO: figure out how to generalize Cloud and Blimp to TransientGameObject.
//  Will also involve figuring out how state pattern will work
//  (idea: generalized state pattern for TGO, Cloud and Blimp maintain their
//  current state classes but just inherit and extend with their necessary
//  behavior?)
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
                new Image("images/blimp_transparent_trimmed.png"));
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
                new Image("images/blimp_rotor_transparent.png"));
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
        this.getTransforms().add(new Translate(initialPosition.getX(),
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
        state.updatePosition(this);
        state.updateFuelText(fuelText);
    }

    public double extractFuel() {
        return state.extractFuel();
    }

    public void changeState(BlimpState state) {
        this.state = state;
    }

    public BlimpState getState() {
        return state;
    }

    public void stopAudio() {
        state.stopAudio();
    }
}

interface BlimpState {
    void updatePosition(Blimp blimp);

    void updateFuelText(GameText fuelText);

    double extractFuel();

    void stopAudio();
}

class CreatedBlimp implements BlimpState {
    private double fuel;

    public CreatedBlimp(double fuel) {
        this.fuel = fuel;
    }

    @Override
    public void updatePosition(Blimp blimp) {
        Point2D newPosition = new Point2D(
                blimp.getPosition().getX() + blimp.getSpeed(),
                blimp.getPosition().getY());
        blimp.updatePositionTo(newPosition);

        blimp.getTransforms().clear();
        blimp.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));

        if (blimp.getPosition().getX()
                + (Game.BLIMP_BODY_SIZE.getX() / 2) > 0)
            blimp.changeState(new InViewBlimp(fuel));
    }

    @Override
    public void updateFuelText(GameText fuelText) { /* impossible */ }

    @Override
    public double extractFuel() {
        /* impossible */
        return 0;
    }

    @Override
    public void stopAudio() { /* impossible */ }

}

class InViewBlimp implements BlimpState {
    private double fuel;
    private MediaPlayer blimpAudio;

    public InViewBlimp(double fuel) {
        this.fuel = fuel;
        blimpAudio = new MediaPlayer(Game.BLIMP_MEDIA);
        blimpAudio.setCycleCount(AudioClip.INDEFINITE);
        blimpAudio.setVolume(Game.BLIMP_VOLUME);
        blimpAudio.play();
    }

    @Override
    public void updatePosition(Blimp blimp) {
        Point2D newPosition = new Point2D(
                blimp.getPosition().getX() + blimp.getSpeed(),
                blimp.getPosition().getY());
        blimp.updatePositionTo(newPosition);

        blimp.getTransforms().clear();
        blimp.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));

        if (blimp.getPosition().getX()
                - (Game.BLIMP_BODY_SIZE.getX() / 2) > Game.GAME_WIDTH) {
            blimpAudio.stop();
            blimp.changeState(new DeadBlimp());
        }
    }

    @Override
    public void updateFuelText(GameText fuelText) {
        fuelText.setText(String.valueOf((int) fuel));
    }

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

    public void stopAudio() {
        blimpAudio.stop();
    }
}

class DeadBlimp implements BlimpState {
    @Override
    public void updatePosition(Blimp blimp) { /* impossible */ }

    @Override
    public void updateFuelText(GameText fuelText) { /* impossible */ }

    @Override
    public double extractFuel() {
        /* impossible */
        return 0;
    }

    @Override
    public void stopAudio() { /* impossible */ }
}

class Blimps extends Pane implements Updatable, Iterable<Blimp> {
    private List<Blimp> blimps;
    private List<Blimp> markedForDeletion;

    public Blimps() {
        blimps = new LinkedList<>();
        markedForDeletion = new LinkedList<>();
    }

    public void add(Blimp b) {
        blimps.add(b);
        getChildren().add(b);
    }

    @Override
    public void update() {
        updateEachOrMarkForDeletion();
        tryDeletingDeadBlimps();
    }

    private void updateEachOrMarkForDeletion() {
        for (Blimp b : blimps) {
            if (b.getState() instanceof DeadBlimp)
                markedForDeletion.add(b);
            else
                b.update();
        }
    }

    private void tryDeletingDeadBlimps() {
        if (markedForDeletion.size() > 0) {
            markedForDeletion.forEach(blimp -> getChildren().remove(blimp));
            blimps.removeAll(markedForDeletion);
            markedForDeletion.clear();
        }
    }

    @Override
    public Iterator<Blimp> iterator() {
        return blimps.iterator();
    }

    public void stopAudio() {
        for (Blimp b : blimps)
            b.stopAudio();
    }
}

class BezierOval extends Group {
    public static final double CONTROL_POINT_STRENGTH = 1.25;

    private double majorAxisRadius, minorAxisRadius;
    private List<Pair<Point2D, Double>> endpoints;
    private Ellipse baseOval, controlPointsOval;
    private List<Point2D> controlPoints;
    private Color fill, stroke;

    public BezierOval(double majorAxisRadius, double minorAxisRadius,
                      Color fill, Color stroke) {
        this.majorAxisRadius = majorAxisRadius;
        this.minorAxisRadius = minorAxisRadius;
        baseOval = new Ellipse(majorAxisRadius, minorAxisRadius);
        this.fill = fill;
        this.stroke = stroke;
        baseOval.setFill(fill);
        getChildren().add(baseOval);
        augmentOvalWithBezierSegments();
    }

    private void augmentOvalWithBezierSegments() {
        makeControlPointOval();
        setEndPoints();
        setControlPoints();
        makeBezierCurves();
    }

    private void makeControlPointOval() {
        controlPointsOval = new Ellipse(
                majorAxisRadius * CONTROL_POINT_STRENGTH,
                minorAxisRadius * CONTROL_POINT_STRENGTH);
    }

    private void setEndPoints() {
        endpoints = new LinkedList<>();
        double theta = Game.randomInRange(35, 55);
        while (theta <= Math.toDegrees(2 * Math.PI)) {
            endpoints.add(new Pair<>(new Point2D(
                majorAxisRadius * Math.cos(Math.toRadians(theta)),
                minorAxisRadius * Math.sin(Math.toRadians(theta))), theta));
            theta += Game.randomInRange(35, 55);
        }
    }

    private void setControlPoints() {
        controlPoints = new LinkedList<>();
        for (int i = 0; i < endpoints.size() - 1; i++) {
            makeControlPointForEndpoints(i, i + 1);
        }
        makeControlPointForEndpoints(endpoints.size() - 1, 0);

    }

    private void makeControlPointForEndpoints(
            int startPointIndex, int endPointIndex) {
        double startTheta = endpoints.get(startPointIndex).getValue();
        double endTheta = endpoints.get(endPointIndex).getValue();
        double controlTheta = startTheta + ((endTheta - startTheta) / 2);
        if (endTheta < startTheta)
            controlTheta += Math.toDegrees(Math.PI);

        controlPoints.add(new Point2D(
                controlPointsOval.getRadiusX()
                        * Math.cos(Math.toRadians(controlTheta)),
                controlPointsOval.getRadiusY()
                        * Math.sin(Math.toRadians(controlTheta))));
    }

    private void makeBezierCurves() {
        for (int i = 0; i < controlPoints.size(); i++) {
            Point2D control = controlPoints.get(i);
            Point2D start = endpoints.get(i).getKey();
            Point2D end = endpoints.get((i + 1) % endpoints.size()).getKey();
            QuadCurve bezier = new QuadCurve(
                    start.getX(), start.getY(),
                    control.getX(), control.getY(),
                    end.getX(), end.getY()
            );
            bezier.setFill(fill);
            bezier.setStroke(stroke);
            getChildren().add(bezier);
        }
    }

    public Color getFill() {
        return fill;
    }

    public void setFill(Color fill) {
        this.fill = fill;
        getChildren().forEach(shape -> ((Shape) shape).setFill(fill));
    }

    public double getWidth() {
        return 2 * controlPointsOval.getRadiusX();
    }

    public double getHeight() {
        return 2 * controlPointsOval.getRadiusY();
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

// TODO: remove observers isn't being called...
class Wind implements Updatable {
    private double speed;
    private Random random;
    private List<TransientGameObject> observers;
    private MediaPlayer windAmbience;

    public Wind() {
        this.speed = Game.MEAN_WIND_SPEED;
        random = new Random();
        observers = new LinkedList<>();
        configureAndPlayAudio();
    }

    private void configureAndPlayAudio() {
        windAmbience = new MediaPlayer(Game.WIND_MEDIA);
        windAmbience.setCycleCount(AudioClip.INDEFINITE);
        windAmbience.setVolume(Game.WIND_VOLUME);
        windAmbience.play();
    }

    @Override
    public void update() {
        /* how to use nextGaussian(): https://stackoverflow.com/a/6012014 */
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

    public void stopAudio() {
        windAmbience.stop();
    }
}

class Clouds extends Pane implements Updatable, Iterable<Cloud> {
    private List<Cloud> clouds;
    private List<Cloud> markedForDeletion;
    private Wind wind;

    public Clouds(Wind wind) {
        clouds = new LinkedList<>();
        markedForDeletion = new LinkedList<>();
        this.wind = wind;
    }

    public void add(Cloud cloud) {
        clouds.add(cloud);
        this.getChildren().add(cloud);
    }

    @Override
    public void update() {
        updateOrMarkForDeletion();
        tryDeletingDeadClouds();
    }

    private void updateOrMarkForDeletion() {
        for (Cloud c : clouds) {
            if (c.getState() instanceof DeadCloud)
                markedForDeletion.add(c);
            else
                c.update();
        }
    }

    private void tryDeletingDeadClouds() {
        if (markedForDeletion.size() > 0) {
            markedForDeletion.forEach(cloud -> getChildren().remove(cloud));
            clouds.removeAll(markedForDeletion);
            markedForDeletion.clear();
        }
    }

    @Override
    public Iterator<Cloud> iterator() {
        return clouds.iterator();
    }

    public int getNumberOf() {
        return clouds.size();
    }

    public void stopAudio() {
        for (Cloud c : clouds)
            c.stopAudio();
    }
}

// TODO: "When the saturation reaches 30% the rainfall will start to fill the
//  pond at a rate *proportional to the cloud’s saturation*."
// TODO: "The cloud will *automatically lose saturation* when it’s not being
//  seeded at a rate that allows the percentage to drop about 1%/second."
// TODO: "So let's choose the Y coordinate randomly, but, such that it is
//  within functional distance of at least one pond. I suggest rotating through
//  the ponds such that the first new cloud is within Y-delta of the first pond,
//  then the next new cloud is within Y-delta of the second pond, and so on.
//  This will guarantee that you can eventually fill all of the ponds."
class Cloud extends TransientGameObject implements Updatable {
    private BezierOval cloudShape;
    private GameText percentSaturatedText;
    private int seedPercentage;
    private CloudState state;

    public Cloud(Point2D initialPosition, double majorAxisRadius,
                 double minorAxisRadius, double speed, double speedOffset) {
        super(initialPosition, speed, speedOffset);
        cloudShape = new BezierOval(majorAxisRadius, minorAxisRadius,
                Game.DEFAULT_CLOUD_COLOR, Game.CLOUD_STROKE_COLOR);

        seedPercentage = 0;
        makePercentSaturatedText(Game.CLOUD_TEXT_COLOR);

        this.getChildren().addAll(cloudShape, percentSaturatedText);
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
        state.updatePosition(this);
        state.updateSaturationText(percentSaturatedText);
    }

    public void seed() {
        state.seed(cloudShape);
    }

    public boolean tryToRain() {
        return state.tryToRain(cloudShape);
    }

    public void changeState(CloudState state) {
        this.state = state;
    }

    public double getWidth() {
        return cloudShape.getWidth();
    }

    public CloudState getState() {
        return state;
    }

    public void stopAudio() {
        state.stopAudio();
    }
}

interface CloudState {
    void updatePosition(Cloud cloud);

    void updateSaturationText(GameText saturationPercent);

    void seed(BezierOval cloudShape);

    boolean tryToRain(BezierOval cloudShape);

    void stopAudio();

}

class AliveCloud implements CloudState {
    @Override
    public void updatePosition(Cloud cloud) {
        Point2D newPosition = new Point2D(
                cloud.getPosition().getX() + cloud.getSpeed(),
                cloud.getPosition().getY());
        cloud.updatePositionTo(newPosition);

        cloud.getTransforms().clear();
        cloud.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));

        if (cloud.getPosition().getX()
                + (cloud.getWidth() / 2) > 0)
            cloud.changeState(new InPlayCloud());
    }

    @Override
    public void updateSaturationText(GameText saturationPercent) {
        /* impossible */
    }

    @Override
    public void seed(BezierOval cloudShape) {
        /* impossible */
    }

    @Override
    public boolean tryToRain(BezierOval cloudShape) {
        /* impossible */
        return false;
    }

    @Override
    public void stopAudio() { /* impossible */ }
}

class InPlayCloud implements CloudState {
    private double seedPercentage;
    private MediaPlayer rainAudio;
    private AudioClip thunder;

    public InPlayCloud() {
        seedPercentage = 0;
        configureAudio();
    }

    private void configureAudio() {
        rainAudio = new MediaPlayer(Game.RAIN_MEDIA);
        rainAudio.setCycleCount(AudioClip.INDEFINITE);
        rainAudio.setVolume(Game.RAIN_VOLUME);

        thunder = new AudioClip(getClass().getResource(
                "audio/thunder-explosion.wav").toExternalForm());
        thunder.setVolume(Game.THUNDER_VOLUME);
    }

    @Override
    public void updatePosition(Cloud cloud) {
        Point2D newPosition = new Point2D(
                cloud.getPosition().getX() + cloud.getSpeed(),
                cloud.getPosition().getY());
        cloud.updatePositionTo(newPosition);

        cloud.getTransforms().clear();
        cloud.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));

        if (cloud.getPosition().getX()
                - (cloud.getWidth() / 2) > Game.GAME_WIDTH) {
            rainAudio.stop();
            cloud.changeState(new DeadCloud());
        }
    }

    @Override
    public void updateSaturationText(GameText saturationPercent) {
        saturationPercent.setText( ((int) seedPercentage) + "%");
    }

    @Override
    public void seed(BezierOval cloudShape) {
        if (seedPercentage < Game.HUNDRED_PERCENT)
            incrementSeedPercentage(cloudShape);
    }

    private void incrementSeedPercentage(BezierOval cloudShape) {
        seedPercentage++;
        int red = (int) (Game.MAX_RGB_INT * cloudShape.getFill().getRed());
        int green =
                (int) (Game.MAX_RGB_INT * cloudShape.getFill().getGreen());
        int blue = (int) (Game.MAX_RGB_INT * cloudShape.getFill().getBlue());
        cloudShape.setFill(Color.rgb(--red, --green, --blue));
    }

    @Override
    public boolean tryToRain(BezierOval cloudShape) {
        if (seedPercentage >= Game.MIN_CLOUD_SATURATION_TO_RAIN) {
            decrementSeedPercentage(cloudShape);
            playAudio();
            return true;
        }
        rainAudio.stop();
        return false;
    }

    private void playAudio() {
        rainAudio.play();
        if (Game.checkProbability(Game.THUNDER_CHANCE)
                && !thunder.isPlaying())
            thunder.play();
    }

    public void decrementSeedPercentage(BezierOval cloudShape) {
        seedPercentage--;
        int red = (int) (Game.MAX_RGB_INT * cloudShape.getFill().getRed());
        int green =
                (int) (Game.MAX_RGB_INT * cloudShape.getFill().getGreen());
        int blue = (int) (Game.MAX_RGB_INT * cloudShape.getFill().getBlue());
        cloudShape.setFill(Color.rgb(++red, ++green, ++blue));
    }

    @Override
    public void stopAudio() {
        rainAudio.stop();
    }
}

class DeadCloud implements CloudState {
    @Override
    public void updatePosition(Cloud cloud) { /* impossible */ }

    @Override
    public void updateSaturationText(GameText saturationPercent) {
        /* impossible */
    }

    @Override
    public void seed(BezierOval cloudShape) { /* impossible */ }

    @Override
    public boolean tryToRain(BezierOval cloudShape) {
        /* impossible */
        return false;
    }

    @Override
    public void stopAudio() { /* impossible */ }
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
        ImageView image = new ImageView(new Image("images/helipad_textured.png"));
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
        fuelGauge.setTranslateY(- Game.HELIBODY_SIZE / 2);
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

    // TODO: check if in OffHeliState instead of checking speed
    public boolean isStationary() {
        return Math.abs(speed) < 1e-3;
//        return state instanceof OffHeliState;
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
                new Image("images/heliblade_2wing_transparent.png"));
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

    void stopAudio();
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

    @Override
    public void stopAudio() { /* impossible */ }
}

class StartingHeliState implements HeliState {
    private AudioClip helicopterAudio;

    public StartingHeliState() {
        configAndPlayAudio();
    }

    private void configAndPlayAudio() {
        helicopterAudio = new AudioClip(
                getClass().getResource(
                        "audio/helicopter-engine-startup.wav")
                        .toExternalForm());
        helicopterAudio.play();
    }

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
        double remainingFuel = currentFuel - Game.BASE_FUEL_CONSUMPTION_RATE;
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

    @Override
    public void stopAudio() {
        helicopterAudio.stop();
    }
}

class StoppingHeliState implements HeliState {
    private AudioClip helicopterAudio;

    public StoppingHeliState() {
        configAndPlayAudio();
    }

    private void configAndPlayAudio() {
        helicopterAudio = new AudioClip(
                getClass().getResource(
                                "audio/helicopter-engine-shutdown.wav")
                        .toExternalForm());
        helicopterAudio.play();
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinUp();
        helicopterAudio.stop();
        return new StartingHeliState();
    }

    @Override
    public Point2D updatePosition(Point2D position, double heading,
      double speed, double fuel, HeliBlade heliBlade, Helicopter helicopter) {
        if (!heliBlade.isRotating()) {
            helicopterAudio.stop();
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

    @Override
    public void stopAudio() {
        helicopterAudio.stop();
    }
}

class ReadyHeliState implements HeliState {
    private MediaPlayer helicopterHum;

    public ReadyHeliState() {
        configureAudio();
    }

    private void configureAudio() {
        helicopterHum = new MediaPlayer(Game.HELICOPTER_MEDIA);
        helicopterHum.setCycleCount(AudioClip.INDEFINITE);
        helicopterHum.setVolume(Game.HELICOPTER_VOLUME);
        helicopterHum.setRate(Game.HELICOPTER_PLAYBACK_RATE);
        helicopterHum.play();
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinDown();
        helicopterHum.stop();
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
                currentFuel - (Math.abs(speed) + Game.BASE_FUEL_CONSUMPTION_RATE);
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

    @Override
    public void stopAudio() {
        helicopterHum.stop();
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
