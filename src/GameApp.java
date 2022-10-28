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
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 * Manages high-level stuff and initializes/shows scene. Sets up key event
 * handlers that invoke Game class methods.
 */
public class GameApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Game game = new Game();
        Scene scene = new Scene(game, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("RainMaker");
        primaryStage.show();
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

    private Pond pond;
    private Cloud cloud;
    private Helipad helipad;
    private Helicopter helicopter;

    public static double randomInRange(double min, double max) {
        return (Math.random() * (max - min) + min);
    }

    public static Point2D randomPositionInBound(
            Point2D lowerLeft, Point2D upperRight) {
        // min = (0, 0)
        // max = (GAME_WIDTH, GAME_HEIGHT)
        return new Point2D(randomInRange(lowerLeft.getX(), upperRight.getX()),
                randomInRange(lowerLeft.getY(), upperRight.getY()));
    }

    public Game() {
        this.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR
                , null, null)));
        this.setScaleY(-1);
        init();
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
        Helicopter helicopter = new Helicopter();
        helicopter.setTranslateX(HELICOPTER_START.getX());
        helicopter.setTranslateY(HELICOPTER_START.getY());
        return helicopter;
    }

    private static Helipad makeHelipad() {
        Helipad helipad = new Helipad(HELIPAD_SIZE);
        helipad.setTranslateX(HELIPAD_POSITION.getX());
        helipad.setTranslateY(HELIPAD_POSITION.getY());
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

    public Pond(double maxRadius, double currentRadius) {
        this.maxRadius = maxRadius;
        this.currentRadius = currentRadius;
        pondCircle = new Circle(currentRadius, Game.POND_COLOR);

        percentFullText = new GameText(
                (int) (( currentRadius / maxRadius) * 100) + "%",
                Game.POND_TEXT_COLOR);
        Bounds fpBounds = percentFullText.getBoundsInParent();
        percentFullText.setTranslateX(
                percentFullText.getTranslateX() - fpBounds.getWidth() / 2);
        percentFullText.setTranslateY(
                percentFullText.getTranslateY() + fpBounds.getHeight() / 2);

        this.getChildren().addAll(pondCircle, percentFullText);
    }

    @Override
    public void update() {

    }
}

/**
 * Abstract as a simple, initially white, circle placed at random
 * anywhere other than fully directly over the helipad.
 */
class Cloud extends GameObject implements Updatable {
    private Circle cloudCircle;
    private GameText percentSaturatedText;

    public Cloud(double radius) {
        cloudCircle = new Circle(radius, Game.CLOUD_COLOR);

        percentSaturatedText =
                new GameText("0%", Game.CLOUD_TEXT_COLOR);
        Bounds fpBounds = percentSaturatedText.getBoundsInParent();
        percentSaturatedText.setTranslateX(
                percentSaturatedText.getTranslateX()
                        - fpBounds.getWidth() / 2);
        percentSaturatedText.setTranslateY(percentSaturatedText.getTranslateY()
                + fpBounds.getHeight() / 2);

        this.getChildren().addAll(cloudCircle, percentSaturatedText);
    }

    @Override
    public void update() {

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
    }

}

/**
 * Represented as yellow circle with line extending from center pointing in
 * direction of heading (in degrees; starts true north == 0 degrees). Derive
 * the size of the helicopter object from the dimensions of the screen. Fuel
 * displayed below body to start and rotates with change in heading.
 */
class Helicopter extends GameObject implements Updatable {
    Circle bodyCircle;
    Line headingLine;
    GameText fuelGauge;
    double heading = 0;
    double speed = 0;
    int fuel = 25000; // should be grabbed from Game class
    final static int maxSpeed = 10;
    final static int minSpeed = -2;
    final static int HEADING_LENGTH = 30;

    public Helicopter() {
        bodyCircle = new Circle(10, Game.HELICOPTER_COLOR);
        headingLine = new Line(0, 0, 0, HEADING_LENGTH);
        headingLine.setStrokeWidth(2);
        headingLine.setStroke(Game.HELICOPTER_COLOR);

        fuelGauge = new GameText("F:" + fuel, Game.HELICOPTER_COLOR);
        fuelGauge.setTranslateY(-15);
        fuelGauge.setTranslateX(-25);

        this.getChildren().addAll(bodyCircle, headingLine, fuelGauge);
    }

    @Override
    public void update() {

    }
}

/**
 * Game objects that should update will implement this interface. Objects
 * will be updated from the main game timer.
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

}
