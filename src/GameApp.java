import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 * Manages high-level stuff and initializes/shows scene. Sets up key event
 * handlers that invoke Game class methods.
 */
public class GameApp extends Application {
    private Game game;
    private Scene scene;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
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
 * Contains game logic and constructs objects. Holds game state and
 * determines win/lose conditions. Not concerned with where/how of user input.
 * Is a Pane to serve as the container for all game objects.
 */
class Game extends Pane {
    final static int GAME_HEIGHT = 800;
    final static int GAME_WIDTH = 400;
    final static Color BACKGROUND_COLOR = Color.BLACK;

    final static Color POND_COLOR = Color.BLUE;
    final static Color POND_TEXT_COLOR = Color.WHITE;
    final static int MAX_POND_RADIUS = 50;
    final static int MAX_STARTING_POND_RADIUS = (int) (MAX_POND_RADIUS * 0.30);
    final static int MIN_POND_RADIUS = 5;

    final static Color CLOUD_COLOR = Color.WHITE;
    final static Color CLOUD_TEXT_COLOR = Color.BLACK;
    final static int MAX_CLOUD_RADIUS = 70;
    final static int MIN_CLOUD_RADIUS = 30;

    final static Color HELIPAD_COLOR = Color.LIGHTGRAY;
    final static Point2D HELIPAD_SIZE = new Point2D(100, 100);
    final static Point2D HELIPAD_POSITION =
            new Point2D((GAME_WIDTH / 2) - (HELIPAD_SIZE.getX() / 2), 20);

    final static Color HELICOPTER_COLOR = Color.YELLOW;
    final static Point2D HELICOPTER_START = new Point2D(200, 70);
    final static int HELICOPTER_MAX_SPEED = 10;
    final static int HELICOPTER_MIN_SPEED = -2;
    final static int HELICOPTER_START_FUEL = 25000;

    final static double NANOS_PER_SEC = 1e9;

    private Pond pond;
    private Cloud cloud;
    private Helipad helipad;
    private Helicopter helicopter;
    private boolean allowSeeding;

    public static double randomInRange(double min, double max) {
        return (Math.random() * (max - min) + min);
    }

    public static Point2D randomPositionInBound(
            Point2D lowerLeft, Point2D upperRight) {
        return new Point2D(randomInRange(lowerLeft.getX(), upperRight.getX()),
                randomInRange(lowerLeft.getY(), upperRight.getY()));
    }

    public Game() {
        this.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR
                , null, null)));
        this.setScaleY(-1);
        init();
        startGameLoop();
    }

    /**
     * Creates all the new state of the world including the positioning of
     * each of the game objects. Don’t forget to clear all children out of the
     * Pane before initializing new objects.
     */
    private void init() {
        this.getChildren().clear();

        this.pond = makePond();
        this.cloud = makeCloud();
        this.helipad = makeHelipad();
        this.helicopter = makeHelicopter();

        this.getChildren().addAll(pond, cloud, helipad, helicopter);
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
                MAX_CLOUD_RADIUS));
        Point2D position =
                randomPositionInBound(new Point2D(0, (GAME_HEIGHT * (0.33))),
                        new Point2D(GAME_WIDTH, GAME_HEIGHT));
        cloud.setTranslateX(position.getX());
        cloud.setTranslateY(position.getY());
        return cloud;
    }

    private static Pond makePond() {
        Pond pond = new Pond(MAX_POND_RADIUS, randomInRange(MIN_POND_RADIUS,
                MAX_STARTING_POND_RADIUS));
        Point2D position =
                randomPositionInBound(new Point2D(0, (GAME_HEIGHT * (0.33))),
                        new Point2D(GAME_WIDTH, GAME_HEIGHT));
        pond.setTranslateX(position.getX());
        pond.setTranslateY(position.getY());
        return pond;
    }

    private void startGameLoop() {
        AnimationTimer loop = new AnimationTimer() {
            double old = -1;
            double secondTimer = 0;

            @Override
            public void handle(long now) {
                double delta = calculateDelta(now);
                secondTimer += delta;

                helicopter.update(delta);
                checkForSeeding();

                if (secondTimer >= 0.7) {
                    checkForRain();
                    secondTimer = 0;
                }
            }

            private void checkForRain() {
                if (cloud.rain()) {
                    pond.fill();
                }
            }

            private void checkForSeeding() {
                if (!Shape.intersect(helicopter.getBoundingBox(),
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
        loop.start();
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
        System.out.println("I");
    }

    public void handleRKeyPressed() {
        System.out.println("R");
    }

    public void handleBKeyPressed() {
        cloud.toggleBoundingBox();
        helipad.toggleBoundingBox();
        helicopter.toggleBoundingBox();
    }
}

/**
 * Shares fields and methods common to all game objects. Is a Group to treat
 * game objects as Node objects to be put straight onto scene graph.
 */
abstract class GameObject extends Group {

}

/**
 * Abstract as blue circle placed at random such that it does not intersect
 * any other ground based object.
 */
class Pond extends GameObject implements Updatable {
    private Circle pondCircle;
    private double maxRadius, currentRadius;
    private GameText percentFullText;
    private int percentFull;

    public Pond(double maxRadius, double currentRadius) {
        this.maxRadius = maxRadius;
        this.currentRadius = currentRadius;
        pondCircle = new Circle(currentRadius, Game.POND_COLOR);

        percentFull = (int) ((currentRadius / maxRadius) * 100);
        percentFullText = new GameText(percentFull + "%",
                Game.POND_TEXT_COLOR);
        Bounds fpBounds = percentFullText.getBoundsInParent();
        percentFullText.setTranslateX(
                percentFullText.getTranslateX() - fpBounds.getWidth() / 2);
        percentFullText.setTranslateY(
                percentFullText.getTranslateY() + fpBounds.getHeight() / 2);

        this.getChildren().addAll(pondCircle, percentFullText);
    }

    @Override
    public void update(double delta) {

    }

    public void fill() {
        if (percentFull < 100) {
            double newRadius = calcNextRadiusForPercent(++percentFull);
            pondCircle.setRadius(newRadius);
            percentFullText.setText(percentFull + "%");
        }
    }

    private double calcNextRadiusForPercent(int percent) {
        return (maxRadius * ((double) percent / 100));
    }
}

/**
 * Abstract as a simple, initially white, circle placed at random
 * anywhere other than fully directly over the helipad.
 */
class Cloud extends GameObject implements Updatable {
    private Circle cloudCircle;
    private GameText percentSaturatedText;
    private int seedPercentage;
    private Rectangle bounds;
    private boolean showBounds;

    public Cloud(double radius) {
        cloudCircle = new Circle(radius, Game.CLOUD_COLOR);

        seedPercentage = 0;
        percentSaturatedText =
                new GameText(seedPercentage + "%", Game.CLOUD_TEXT_COLOR);
        Bounds fpBounds = percentSaturatedText.getBoundsInParent();
        percentSaturatedText.setTranslateX(
                percentSaturatedText.getTranslateX()
                        - fpBounds.getWidth() / 2);
        percentSaturatedText.setTranslateY(percentSaturatedText.getTranslateY()
                + fpBounds.getHeight() / 2);

        this.getChildren().addAll(cloudCircle, percentSaturatedText);

        // Bounding box work
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
    public void update(double delta) {

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
            percentSaturatedText.setText(seedPercentage + "%");
            int red = (int) (255 * ((Color) cloudCircle.getFill()).getRed());
            int green = (int) (255 * ((Color) cloudCircle.getFill()).getGreen());
            int blue = (int) (255 * ((Color) cloudCircle.getFill()).getBlue());
            cloudCircle.setFill(Color.rgb(--red, --green, --blue));
        }
    }

    public boolean rain() {
        if (seedPercentage >= 30) {
            seedPercentage--;
            percentSaturatedText.setText(seedPercentage + "%");
            int red = (int) (255 * ((Color) cloudCircle.getFill()).getRed());
            int green = (int) (255 * ((Color) cloudCircle.getFill()).getGreen());
            int blue = (int) (255 * ((Color) cloudCircle.getFill()).getBlue());
            cloudCircle.setFill(Color.rgb(++red, ++green, ++blue));
            return true;
        }
        return false;
    }
}

/**
 * Represents starting/ending location for helicopter. Helicopter takes off
 * from here and must return after seeding clouds to win game (helicopter
 * contained in bounds of helipad). Represented by smaller gray circle
 * centered within gray square. Centered along screen width with padding from
 * screen bottom.
 */
class Helipad extends GameObject {
    private Rectangle rectangle;
    private Circle circle;
    private Rectangle bounds;
    private boolean showBounds;

    public Helipad(Point2D size) {
        rectangle = new Rectangle(size.getX(), size.getY());
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Game.HELIPAD_COLOR);

        circle = new Circle(rectangle.getWidth() / 2 - 10);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Game.HELIPAD_COLOR);
        circle.setTranslateX(circle.getTranslateX() + size.getX() / 2);
        circle.setTranslateY(circle.getTranslateY() + size.getY() / 2);

        this.getChildren().addAll(rectangle, circle);

        this.getTransforms().add(new Translate(Game.HELIPAD_POSITION.getX(),
                Game.HELIPAD_POSITION.getY()));

        // Bounding box work
        bounds = new Rectangle(rectangle.getWidth(), rectangle.getHeight());
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

/**
 * Represented as yellow circle with line extending from center pointing in
 * direction of heading (in degrees; starts true north == 0 degrees). Derive
 * the size of the helicopter object from the dimensions of the screen. Fuel
 * displayed below body to start and rotates with change in heading.
 */
class Helicopter extends GameObject implements Updatable {
    // TODO: derive helicopter size from screen dimensions
    final static int HEADING_LENGTH = 30;
    private Circle bodyCircle;
    private Line headingLine;
    private GameText fuelGauge;
    private double heading = 0;
    private double speed = 0;
    private Point2D position;
    private int fuel;
    private Rectangle bounds;
    private boolean showBounds;

    public Helicopter(int fuel, Point2D position) {
        makeHelicopterShape();
        makeFuelGauge(fuel);
        this.getChildren().addAll(bodyCircle, headingLine, fuelGauge);

        this.fuel = fuel;
        this.position = position;

        this.getTransforms().add(new Translate(position.getX(),
                position.getY()));

        // Bounding box work
        setupBoundingBox();
    }

    private void setupBoundingBox() {
        bounds = new Rectangle(this.getBoundsInParent().getWidth(),
                this.getBoundsInParent().getHeight());
        bounds.getTransforms().add(
                new Translate((-bounds.getWidth() / 2),
                        (-bounds.getHeight() / 2) ));
        bounds.setFill(Color.TRANSPARENT);
        bounds.setStrokeWidth(1);
        bounds.setStroke(Color.YELLOW);
        bounds.setVisible(false);
        this.getChildren().add(bounds);
    }

    private void makeFuelGauge(int fuel) {
        fuelGauge = new GameText("F:" + fuel, Game.HELICOPTER_COLOR);
        fuelGauge.setTranslateY(-15);
        fuelGauge.setTranslateX(-25);
    }

    private void makeHelicopterShape() {
        bodyCircle = new Circle(10, Game.HELICOPTER_COLOR);
        headingLine = new Line(0, 0, 0, HEADING_LENGTH);
        headingLine.setStrokeWidth(2);
        headingLine.setStroke(Game.HELICOPTER_COLOR);
    }

    @Override
    public void update(double delta) {
        Point2D newPosition =
                new Point2D(position.getX() + (Math.sin(Math.toRadians(heading)) * speed),
                        position.getY() + (Math.cos(Math.toRadians(heading)) * speed));

        position = newPosition;

        this.getTransforms().clear();
        this.getTransforms().addAll(
                new Translate(position.getX(), position.getY()),
                new Rotate(-heading));

        // Bounding box work
        updateBoundingBox();
    }

    private void updateBoundingBox() {
        this.getChildren().remove(bounds);

//        System.out.println("Width: " + this.getBoundsInParent().getWidth() +
//                ", Height: " + this.getBoundsInParent().getHeight());
        bounds.setWidth(this.getBoundsInParent().getWidth());
        bounds.setHeight(this.getBoundsInParent().getHeight());
        bounds.getTransforms().clear();
        bounds.getTransforms().add(
                new Translate((-bounds.getWidth() / 2),
                        (-bounds.getHeight() / 2) ));
        bounds.getTransforms().add(new Rotate(heading,
                this.getBoundsInParent().getWidth() / 2,
                this.getBoundsInParent().getHeight() / 2));

        this.getChildren().add(bounds);
    }

    public void turnLeft() {
        heading -= 15;
    }

    public void turnRight() {
        heading += 15;
    }

    public void increaseSpeed() {
        if (speed < Game.HELICOPTER_MAX_SPEED)
            speed += 0.1;
    }

    public void decreaseSpeed() {
        if (speed > Game.HELICOPTER_MIN_SPEED)
            speed -= 0.1;
    }

    public void toggleBoundingBox() {
        showBounds = !showBounds;
        bounds.setVisible(showBounds);
    }

    public Rectangle getBoundingBox() {
        return bounds;
    }
}

/**
 * Game objects that should update will implement this interface. Objects
 * will be updated from the main game timer.
 */
interface Updatable {
    void update(double delta);

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

}
