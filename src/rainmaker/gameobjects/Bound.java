package rainmaker.gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * Holds a reference to the object it's bounding so that it can be garbage
 * collected along with its object.
 */
public class Bound extends GameObject implements Updatable {
    public static final Color BOUND_FILL = Color.TRANSPARENT;
    public static final Color BOUND_STROKE = Color.YELLOW;
    public static final int BOUND_STROKE_WIDTH = 1;
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
        boundShape.setFill(BOUND_FILL);
        boundShape.setStroke(BOUND_STROKE);
        boundShape.setStrokeWidth(BOUND_STROKE_WIDTH);
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
