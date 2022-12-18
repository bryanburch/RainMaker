package rainmaker;

import audio.SoundPlayer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import rainmaker.gameobjects.*;

import java.text.DecimalFormat;
import java.util.Optional;

/**
 * Is a Pane to serve as the container for all game objects.
 */
public class Game extends Pane {
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

    public static final Media HELICOPTER_STARTING_MEDIA = new Media(
            SoundPlayer.class.getResource(
                            "../audio/helicopter-engine-startup.wav")
                    .toExternalForm());
    public static final Media HELICOPTER_MEDIA = new Media(
            SoundPlayer.class.getResource(
                            "../audio/helicopter-engine-loop-long.wav")
                    .toExternalForm());
    public static final Media HELICOPTER_STOPPING_MEDIA = new Media(
            SoundPlayer.class.getResource(
                            "../audio/helicopter-engine-shutdown.wav")
                    .toExternalForm());
    public static final Media BLIMP_MEDIA = new Media(SoundPlayer.class
            .getResource("../audio/drone-engine.wav").toExternalForm());
    public static final Media WIND_MEDIA = new Media(SoundPlayer.class
            .getResource("../audio/wind-howl.mp3").toExternalForm());
    public static final Media RAIN_MEDIA = new Media(SoundPlayer.class
            .getResource("../audio/rain-loop-long.wav").toExternalForm());
    public static final double HELICOPTER_VOLUME = 1;
    public static final double HELICOPTER_PLAYBACK_RATE = 1.15;
    public static final double BLIMP_VOLUME = 0.1;
    public static final double WIND_VOLUME = 0.1;
    public static final double RAIN_VOLUME = 0.2;
    public static final double SEEDING_VOLUME = 0.05;
    public static final double REFUELING_VOLUME = 0.3;
    public static final double THUNDER_VOLUME = 0.8;
    public static final double THUNDER_CHANCE = 0.01;

    public static final int INVERT_AXIS = -1;
    public static final double NANOS_PER_SEC = 1e9;
    public static final int HUNDRED_PERCENT = 100;
    public static final int MAX_RGB_INT = 255;
    public static final double EFFECTIVELY_ZERO = 1e-3;

    private static final Game instance = new Game();

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

    private Game() {
        /* image credit: https://earthobservatory.nasa.gov/images/51341/
        two-views-of-the-painted-desert */
        BackgroundImage background = new BackgroundImage(
                new Image("images/desert_background_large.png"),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        setBackground(new Background(background));
        setScaleY(INVERT_AXIS);
        init();
    }

    public static Game getInstance() {
        return instance;
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
        seedingAudio = new AudioClip(SoundPlayer.class.getResource(
                "../audio/rainmaker-seeding.wav").toExternalForm());
        seedingAudio.setVolume(SEEDING_VOLUME);

        refuelingAudio = new AudioClip(SoundPlayer.class.getResource(
                "../audio/helicopter-refueling.wav").toExternalForm());
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
                new Point2D(-MAX_CLOUD_MAJOR_RADIUS * 2,
                        (GAME_HEIGHT * (0.33))),
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
                randomInRange(MIN_POND_RADIUS, MAX_STARTING_POND_RADIUS),
                POND_COLOR, POND_TEXT_COLOR);
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
                            && cloud.isDead())
                        distanceLines.markForDeletion(d);
            }

            private void markForDeletionBoundsOfDeadObjects() {
                for (Bound b : bounds) {
                    GameObject gameObject = b.getBoundedObject();
                    if (gameObject instanceof Blimp blimp
                            && blimp.isDead())
                        bounds.markForDeletion(b);
                    else if (gameObject instanceof Cloud cloud
                            && cloud.isDead())
                        bounds.markForDeletion(b);
                }
            }

            private void trySpawningBlimp() {
                boolean isTimeToTryBlimpSpawn = timeSinceBlimpRespawnTry
                        >= BLIMP_RESPAWN_ATTEMPT_FREQ_SEC;
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
                stopAllAnimations();
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
        stopAllAnimations();
        stopAllAudio();
        init();
    }

    private void stopAllAnimations() {
        helicopter.stopAnimation();
        blimps.stopAnimation();
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
