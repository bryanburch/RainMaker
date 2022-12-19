package rainmaker.gameobjects;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class Cloud extends TransientGameObject implements Updatable {
    public static final Color DEFAULT_CLOUD_COLOR = Color.WHITE;
    public static final Color CLOUD_STROKE_COLOR = Color.GREY;
    public static final Color CLOUD_TEXT_COLOR = Color.BLUE;
    private BezierOval cloudShape;
    private GameText percentSaturatedText;
    private int seedPercentage;
    private CloudState state;

    public Cloud(Point2D initialPosition, double majorAxisRadius,
                 double minorAxisRadius, double speed, double speedOffset) {
        super(initialPosition, speed, speedOffset);
        cloudShape = new BezierOval(majorAxisRadius, minorAxisRadius,
                DEFAULT_CLOUD_COLOR, CLOUD_STROKE_COLOR);

        seedPercentage = 0;
        makePercentSaturatedText(CLOUD_TEXT_COLOR);

        getChildren().addAll(cloudShape, percentSaturatedText);
        state = new CreatedCloud();
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
        super.update();
        state = state.update(this, percentSaturatedText);
    }

    public void seed() {
        state.seed(cloudShape);
    }

    public boolean tryToRain() {
        return state.tryToRain(cloudShape);
    }

    public boolean isDead() {
        return state instanceof DeadCloud;
    }

    public void stopAudio() {
        state.stopAudio();
    }

    public double getWidth() {
        return cloudShape.getWidth();
    }
}
