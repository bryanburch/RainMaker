package rainmaker.gameobjects;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import rainmaker.Game;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// TODO: remove observers isn't being called...
public class Wind implements Updatable {
    private double speed;
    private Random random;
    private List<TransientGameObject> observers;
    private MediaPlayer windAmbience;

    public Wind() {
        this.speed = Game.MEAN_WIND_SPEED;
        random = new Random();
        observers = new LinkedList<>();
        configureAndPlayAudio();
    }

    private void configureAndPlayAudio() {
        windAmbience = new MediaPlayer(Game.WIND_MEDIA);
        windAmbience.setCycleCount(AudioClip.INDEFINITE);
        windAmbience.setVolume(Game.WIND_VOLUME);
        windAmbience.play();
    }

    @Override
    public void update() {
        /* how to use nextGaussian(): https://stackoverflow.com/a/6012014 */
        speed = random.nextGaussian()
                * Game.STD_DEV_WIND_SPEED + Game.MEAN_WIND_SPEED;
        notifyObservers();
    }

    public void addObserver(TransientGameObject t) {
        observers.add(t);
    }

    public void removeObserver(TransientGameObject t) {
        observers.remove(t);
    }

    private void notifyObservers() {
        for (TransientGameObject t : observers) {
            t.impartSpeed(speed);
        }
    }

    public void stopAudio() {
        windAmbience.stop();
    }
}
