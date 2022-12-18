package rainmaker.gameobjects;

import audio.SoundPlayer;
import javafx.geometry.Point2D;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import rainmaker.Game;

public interface HeliState {
    HeliState toggleIgnition(HeliBlade heliBlade);

    Point2D updatePosition(Point2D position, double heading, double speed,
                   double fuel, HeliBlade heliBlade, Helicopter helicopter);

    double consumeFuel(double currentFuel, double speed);

    void increaseSpeed(Helicopter helicopter);

    void decreaseSpeed(Helicopter helicopter);

    void turnLeft(Helicopter helicopter);

    void turnRight(Helicopter helicopter);

    void stopAudio();
}

class OffHeliState implements HeliState {

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinUp();
        return new StartingHeliState();
    }

    @Override
    public Point2D updatePosition(Point2D position, double heading,
                                  double speed, double fuel,
                                  HeliBlade heliBlade, Helicopter helicopter) {
        return null;
    }

    @Override
    public double consumeFuel(double currentFuel, double speed) {
        return currentFuel;
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void decreaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnLeft(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnRight(Helicopter helicopter) { /* impossible */ }

    @Override
    public void stopAudio() { /* impossible */ }
}

class StartingHeliState implements HeliState {
    private AudioClip helicopterAudio;

    public StartingHeliState() {
        configAndPlayAudio();
    }

    private void configAndPlayAudio() {
        helicopterAudio = new AudioClip(
                SoundPlayer.class.getResource(
                                "../audio/helicopter-engine-startup.wav")
                        .toExternalForm());
        helicopterAudio.play();
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinDown();
        return new StoppingHeliState();
    }

    @Override
    public Point2D updatePosition(Point2D position, double heading,
                                  double speed, double fuel,
                                  HeliBlade heliBlade, Helicopter helicopter) {
        if (heliBlade.isUpToSpeed())
            helicopter.changeState(new ReadyHeliState());
        return null;
    }

    @Override
    public double consumeFuel(double currentFuel, double speed) {
        double remainingFuel = currentFuel - Game.BASE_FUEL_CONSUMPTION_RATE;
        return remainingFuel > 0 ? remainingFuel : 0;
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void decreaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnLeft(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnRight(Helicopter helicopter) { /* impossible */ }

    @Override
    public void stopAudio() {
        helicopterAudio.stop();
    }
}

class ReadyHeliState implements HeliState {
    private MediaPlayer helicopterHum;

    public ReadyHeliState() {
        configureAudio();
    }

    private void configureAudio() {
        helicopterHum = new MediaPlayer(Game.HELICOPTER_MEDIA);
        helicopterHum.setCycleCount(AudioClip.INDEFINITE);
        helicopterHum.setVolume(Game.HELICOPTER_VOLUME);
        helicopterHum.setRate(Game.HELICOPTER_PLAYBACK_RATE);
        helicopterHum.play();
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinDown();
        helicopterHum.stop();
        return new StoppingHeliState();
    }

    @Override
    public Point2D updatePosition(Point2D position, double heading,
                                  double speed, double fuel,
                                  HeliBlade heliBlade, Helicopter helicopter) {
        return new Point2D(
                position.getX() + (Math.sin(Math.toRadians(heading)) * speed),
                position.getY() + (Math.cos(Math.toRadians(heading)) * speed));
    }

    @Override
    public double consumeFuel(double currentFuel, double speed) {
        double remainingFuel = currentFuel
                - (Math.abs(speed) + Game.BASE_FUEL_CONSUMPTION_RATE);
        return remainingFuel > 0 ? remainingFuel : 0;
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) {
        if (helicopter.getSpeed() < Game.HELICOPTER_MAX_SPEED)
            helicopter.setSpeed(helicopter.getSpeed() + 0.1);
    }

    @Override
    public void decreaseSpeed(Helicopter helicopter) {
        if (helicopter.getSpeed() > Game.HELICOPTER_MIN_SPEED)
            helicopter.setSpeed(helicopter.getSpeed() - 0.1);
    }

    @Override
    public void turnLeft(Helicopter helicopter) {
        if (Math.abs(helicopter.getSpeed()) > 1e-3)
            helicopter.setHeading(helicopter.getHeading() - 15);
    }

    @Override
    public void turnRight(Helicopter helicopter) {
        if (Math.abs(helicopter.getSpeed()) > 1e-3)
            helicopter.setHeading(helicopter.getHeading() + 15);
    }

    @Override
    public void stopAudio() {
        helicopterHum.stop();
    }
}

class StoppingHeliState implements HeliState {
    private AudioClip helicopterAudio;

    public StoppingHeliState() {
        configAndPlayAudio();
    }

    private void configAndPlayAudio() {
        helicopterAudio = new AudioClip(
                SoundPlayer.class.getResource(
                                "../audio/helicopter-engine-shutdown.wav")
                        .toExternalForm());
        helicopterAudio.play();
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinUp();
        helicopterAudio.stop();
        return new StartingHeliState();
    }

    @Override
    public Point2D updatePosition(Point2D position, double heading,
                                  double speed, double fuel,
                                  HeliBlade heliBlade, Helicopter helicopter) {
        if (!heliBlade.isRotating()) {
            helicopterAudio.stop();
            helicopter.changeState(new OffHeliState());
        }
        return null;
    }

    @Override
    public double consumeFuel(double currentFuel, double speed) {
        return currentFuel;
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void decreaseSpeed(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnLeft(Helicopter helicopter) { /* impossible */ }

    @Override
    public void turnRight(Helicopter helicopter) { /* impossible */ }

    @Override
    public void stopAudio() {
        helicopterAudio.stop();
    }
}

