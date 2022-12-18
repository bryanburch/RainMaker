package rainmaker.gameobjects;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import rainmaker.*;

// TODO: "When the saturation reaches 30% the rainfall will start to fill the
//  pond at a rate *proportional to the cloud’s saturation*."
// TODO: "The cloud will *automatically lose saturation* when it’s not being
//  seeded at a rate that allows the percentage to drop about 1%/second."
// TODO: "So let's choose the Y coordinate randomly, but, such that it is
//  within functional distance of at least one pond. I suggest rotating through
//  the ponds such that the first new cloud is within Y-delta of the first pond,
//  then the next new cloud is within Y-delta of the second pond, and so on.
//  This will guarantee that you can eventually fill all of the ponds."
public class Cloud extends TransientGameObject implements Updatable {
    private BezierOval cloudShape;
    private GameText percentSaturatedText;
    private int seedPercentage;
    private CloudState state;

    public Cloud(Point2D initialPosition, double majorAxisRadius,
                 double minorAxisRadius, double speed, double speedOffset) {
        super(initialPosition, speed, speedOffset);
        cloudShape = new BezierOval(majorAxisRadius, minorAxisRadius,
                Game.DEFAULT_CLOUD_COLOR, Game.CLOUD_STROKE_COLOR);

        seedPercentage = 0;
        makePercentSaturatedText(Game.CLOUD_TEXT_COLOR);

        this.getChildren().addAll(cloudShape, percentSaturatedText);
        state = new AliveCloud();
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
        state.updatePosition(this);
        state.updateSaturationText(percentSaturatedText);
    }

    public void seed() {
        state.seed(cloudShape);
    }

    public boolean tryToRain() {
        return state.tryToRain(cloudShape);
    }

    public void changeState(CloudState state) {
        this.state = state;
    }

    public double getWidth() {
        return cloudShape.getWidth();
    }

    public boolean isDead() {
        return state instanceof DeadCloud;
    }

    public void stopAudio() {
        state.stopAudio();
    }
}
