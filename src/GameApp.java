import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Manages high-level stuff and initializes/shows scene. Sets up key event
 * handlers that invoke Game class methods.
 */
public class GameApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

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
    Circle circle;

    @Override
    public void update() {

    }
}

/**
 * Abstract as a simple, initially white, circle placed at random
 * anywhere other than fully directly over the helipad.
 */
class Cloud extends GameObject implements Updatable {
    Circle circle;

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
    Rectangle rectangle;
    Circle circle;

}

/**
 * Represented as yellow circle with line extending from center pointing in
 * direction of heading (in degrees; starts true north == 0 degrees). Derive
 * the size of the helicopter object from the dimensions of the screen. Fuel
 * displayed below body to start and rotates with change in heading.
 */
class Helicopter extends GameObject implements Updatable {
    Circle circle;
    Line line;
    double heading = 0;
    double speed = 0;
    GameText fuelGauge;
    int fuel = 25000; // should be grabbed from Game class
    final static int maxSpeed = 10;
    final static int minSpeed = -2;

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
class GameText extends Label {
    String text;

}
