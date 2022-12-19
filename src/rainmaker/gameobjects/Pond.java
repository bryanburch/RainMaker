package rainmaker.gameobjects;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Pond extends GameObject implements Updatable {
    public static final Color POND_COLOR = Color.BLUE;
    public static final Color POND_TEXT_COLOR = Color.WHITE;
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
