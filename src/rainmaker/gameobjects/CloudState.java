package rainmaker.gameobjects;

import audio.SoundPlayer;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import rainmaker.Game;

public interface CloudState {
    CloudState update(Cloud cloud, GameText saturationPercent);

    void seed(BezierOval cloudShape);

    boolean tryToRain(BezierOval cloudShape);

    void stopAudio();

}

class CreatedCloud implements CloudState {
    @Override
    public CloudState update(Cloud cloud, GameText saturationPercent) {
        if (cloud.getPosition().getX()
                + (cloud.getWidth() / 2) > 0)
            return new InViewCloud();
        return this;
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

class InViewCloud implements CloudState {
    private double seedPercentage;
    private MediaPlayer rainAudio;
    private AudioClip thunder;

    public InViewCloud() {
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
    public CloudState update(Cloud cloud, GameText saturationPercent) {
        updateSaturationText(saturationPercent);

        if (cloud.getPosition().getX()
                - (cloud.getWidth() / 2) > Game.GAME_WIDTH) {
            rainAudio.stop();
            return new DeadCloud();
        }
        return this;
    }

    private void updateSaturationText(GameText saturationPercent) {
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
    public CloudState update(Cloud cloud, GameText saturationPercent) {
        return this;
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
