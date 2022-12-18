package rainmaker.gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import rainmaker.Game;
import rainmaker.gameobjects.GameText;

/**
 * Is a rainmaker.gameobjects.GameObject like rainmaker.gameobjects.Bound is. Postion defined as stationary endpoint of the
 * rainmaker.DistanceLine (e.g. rainmaker.gameobjects.Pond).
 */
public class DistanceLine extends GameObject implements Updatable {
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
