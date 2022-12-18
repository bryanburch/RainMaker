package rainmaker.gameobjects;

import audio.SoundPlayer;
import javafx.geometry.Point2D;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;
import rainmaker.Game;
import rainmaker.gameobjects.BezierOval;
import rainmaker.gameobjects.GameText;

public interface CloudState {
    void updatePosition(Cloud cloud);

    void updateSaturationText(GameText saturationPercent);

    void seed(BezierOval cloudShape);

    boolean tryToRain(BezierOval cloudShape);

    void stopAudio();

}

class AliveCloud implements CloudState {
    @Override
    public void updatePosition(Cloud cloud) {
        Point2D newPosition = new Point2D(
                cloud.getPosition().getX() + cloud.getSpeed(),
                cloud.getPosition().getY());
        cloud.updatePositionTo(newPosition);

        cloud.getTransforms().clear();
        cloud.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));

        if (cloud.getPosition().getX()
                + (cloud.getWidth() / 2) > 0)
            cloud.changeState(new InPlayCloud());
    }

    @Override
    public void updateSaturationText(GameText saturationPercent) {
        /* impossible */
    }

    @Override
    public void seed(BezierOval cloudShape) {
        /* impossible */
    }

    @Override
    public boolean tryToRain(BezierOval cloudShape) {
        /* impossible */
        return false;
    }

    @Override
    public void stopAudio() { /* impossible */ }
}

class InPlayCloud implements CloudState {
    private double seedPercentage;
    private MediaPlayer rainAudio;
    private AudioClip thunder;

    public InPlayCloud() {
        seedPercentage = 0;
        configureAudio();
    }

    private void configureAudio() {
        rainAudio = new MediaPlayer(Game.RAIN_MEDIA);
        rainAudio.setCycleCount(AudioClip.INDEFINITE);
        rainAudio.setVolume(Game.RAIN_VOLUME);

        thunder = new AudioClip(SoundPlayer.class.getResource(
                "../audio/thunder-explosion.wav").toExternalForm());
        thunder.setVolume(Game.THUNDER_VOLUME);
    }

    @Override
    public void updatePosition(Cloud cloud) {
        Point2D newPosition = new Point2D(
                cloud.getPosition().getX() + cloud.getSpeed(),
                cloud.getPosition().getY());
        cloud.updatePositionTo(newPosition);

        cloud.getTransforms().clear();
        cloud.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));

        if (cloud.getPosition().getX()
                - (cloud.getWidth() / 2) > Game.GAME_WIDTH) {
            rainAudio.stop();
            cloud.changeState(new DeadCloud());
        }
    }

    @Override
    public void updateSaturationText(GameText saturationPercent) {
        saturationPercent.setText(((int) seedPercentage) + "%");
    }

    @Override
    public void seed(BezierOval cloudShape) {
        if (seedPercentage < Game.HUNDRED_PERCENT)
            incrementSeedPercentage(cloudShape);
    }

    private void incrementSeedPercentage(BezierOval cloudShape) {
        seedPercentage++;
        int red = (int) (Game.MAX_RGB_INT * cloudShape.getFill().getRed());
        int green =
                (int) (Game.MAX_RGB_INT * cloudShape.getFill().getGreen());
        int blue = (int) (Game.MAX_RGB_INT * cloudShape.getFill().getBlue());
        cloudShape.setFill(Color.rgb(--red, --green, --blue));
    }

    @Override
    public boolean tryToRain(BezierOval cloudShape) {
        if (seedPercentage >= Game.MIN_CLOUD_SATURATION_TO_RAIN) {
            decrementSeedPercentage(cloudShape);
            playAudio();
            return true;
        }
        rainAudio.stop();
        return false;
    }

    private void playAudio() {
        rainAudio.play();
        if (Game.checkProbability(Game.THUNDER_CHANCE)
                && !thunder.isPlaying())
            thunder.play();
    }

    public void decrementSeedPercentage(BezierOval cloudShape) {
        seedPercentage--;
        int red = (int) (Game.MAX_RGB_INT * cloudShape.getFill().getRed());
        int green =
                (int) (Game.MAX_RGB_INT * cloudShape.getFill().getGreen());
        int blue = (int) (Game.MAX_RGB_INT * cloudShape.getFill().getBlue());
        cloudShape.setFill(Color.rgb(++red, ++green, ++blue));
    }

    @Override
    public void stopAudio() {
        rainAudio.stop();
    }
}

class DeadCloud implements CloudState {
    @Override
    public void updatePosition(Cloud cloud) { /* impossible */ }

    @Override
    public void updateSaturationText(GameText saturationPercent) {
        /* impossible */
    }

    @Override
    public void seed(BezierOval cloudShape) { /* impossible */ }

    @Override
    public boolean tryToRain(BezierOval cloudShape) {
        /* impossible */
        return false;
    }

    @Override
    public void stopAudio() { /* impossible */ }
}