package rainmaker.gameobjects;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import rainmaker.Game;

public class Pond extends GameObject implements Updatable {
    public static final Color POND_COLOR = Color.BLUE;
    public static final Color POND_TEXT_COLOR = Color.WHITE;
    public static final double MIN_CONTROL_DEGREE_SEPARATION = 60;
    public static final double MAX_CONTROL_DEGREE_SEPARATION = 90;
    public static final double ONE_PERCENT = 0.01;

    private BezierOval pondShape;
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
        double controlStrength = (maxRadius / currentRadius);
        pondShape = new BezierOval(currentRadius, currentRadius, fill,
                Color.TRANSPARENT, controlStrength,
                MIN_CONTROL_DEGREE_SEPARATION,
                MAX_CONTROL_DEGREE_SEPARATION);

        makePercentFullText(textFill);

        getChildren().addAll(pondShape, percentFullText);
    }

    private void makePercentFullText(Color textFill) {
        percentFull = (int) ((currentArea / maxArea) * Game.HUNDRED_PERCENT);
        percentFullText = new GameText(percentFull + "%", textFill);

        Bounds fpBounds = percentFullText.getBoundsInParent();
        percentFullText.setTranslateX(
                percentFullText.getTranslateX() - fpBounds.getWidth() / 2);
        percentFullText.setTranslateY(
                percentFullText.getTranslateY() + fpBounds.getHeight() / 2);
    }

    @Override
    public void update() {
        if (pondShape.getMajorAxisRadius() != currentRadius) {
            pondShape.growBaseOvalTo(currentRadius, currentRadius);
            percentFullText.setText(percentFull + "%");
        }
    }

    public void fillByIncrement(double multiplier) {
        currentArea += (maxArea * ONE_PERCENT) * multiplier;
        if (currentArea > maxArea)
            currentArea = maxArea;

        currentRadius = Math.sqrt((currentArea / Math.PI));
        percentFull = (int) (currentArea / maxArea * Game.HUNDRED_PERCENT);
    }

    public int getPercentFull() {
        return percentFull;
    }

    public double getMaxRadius() {
        return maxRadius;
    }
}
