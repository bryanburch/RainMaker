package rainmaker.gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

/**
 * Is a GameObject like Bound is. Postion defined as stationary endpoint of the
 * DistanceLine (e.g. Pond).
 */
public class DistanceLine extends GameObject implements Updatable {
    public static final double DISTANCE_LINE_WIDTH = 1;
    public static final Paint DISTANCE_LINE_COLOR = Color.WHITE;
    public static final Paint DISTANCE_LINE_TEXT_COLOR = Color.BLACK;
    private GameObject staticEndpoint, dynamicEndpoint;
    private Line line;
    private GameText distanceText;
    private StackPane textPane;

    public DistanceLine(GameObject staticEndpoint,
                        GameObject dynamicEndpoint) {
        super(staticEndpoint.getPosition());
        this.staticEndpoint = staticEndpoint;
        this.dynamicEndpoint = dynamicEndpoint;

        setupLineShape();
        setupDistanceText();
    }

    private void setupDistanceText() {
        distanceText = new GameText(String.valueOf((int) getDistance()),
                DISTANCE_LINE_TEXT_COLOR);
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
        line.setStrokeWidth(DISTANCE_LINE_WIDTH);
        line.setStroke(DISTANCE_LINE_COLOR);
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
