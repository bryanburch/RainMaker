package rainmaker.gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import rainmaker.Game;

public interface HeliState {
    HeliState toggleIgnition(HeliBlade heliBlade);

    HeliState update(Helicopter helicopter, HeliBlade heliBlade,
                 GameText fuelGauge);

    void increaseSpeed(Helicopter helicopter);

    void decreaseSpeed(Helicopter helicopter);

    void turnLeft(Helicopter helicopter);

    void turnRight(Helicopter helicopter);

    void refuelBy(double fuel);

    void stopAudio();

    double getFuel();

    double getSpeed();
}

class OffHeliState implements HeliState {
    private double fuel;
    private double heading;

    public OffHeliState(double fuel, double heading) {
        this.fuel = fuel;
        this.heading = heading;
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinUp();
        return new StartingHeliState(fuel, heading);
    }

    @Override
    public HeliState update(Helicopter helicopter, HeliBlade heliBlade,
                            GameText fuelGauge) {
        return this;
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
    public void refuelBy(double fuel) { /* impossible */ }

    @Override
    public void stopAudio() { /* impossible */ }

    @Override
    public double getFuel() {
        return fuel;
    }

    @Override
    public double getSpeed() {
        return 0;
    }
}

class StartingHeliState implements HeliState {
    private double fuel;
    private double heading;
    private MediaPlayer helicopterStartup;

    public StartingHeliState(double fuel, double heading) {
        this.fuel = fuel;
        this.heading = heading;
        configAndPlayAudio();
    }

    private void configAndPlayAudio() {
        helicopterStartup = new MediaPlayer(Game.HELICOPTER_STARTING_MEDIA);
        helicopterStartup.setVolume(Game.HELICOPTER_VOLUME);
        helicopterStartup.play();
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinDown();
        return new StoppingHeliState(fuel, heading);
    }

    @Override
    public HeliState update(Helicopter helicopter, HeliBlade heliBlade,
                            GameText fuelGauge) {
        updateFuelGaugeText(fuelGauge);
        if (heliBlade.isUpToSpeed()) {
            helicopterStartup.stop();
            return new ReadyHeliState(fuel, heading);
        }
        return this;
    }

    private void updateFuelGaugeText(GameText fuelGauge) {
        double remainingFuel = fuel - Game.BASE_FUEL_CONSUMPTION_RATE;
        fuel = remainingFuel > 0 ? remainingFuel : 0;
        fuelGauge.setText("F:" + (int) fuel);
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
    public void refuelBy(double fuel) { /* impossible */ }

    @Override
    public void stopAudio() {
        helicopterStartup.stop();
    }

    @Override
    public double getFuel() {
        return fuel;
    }

    @Override
    public double getSpeed() {
        return 0;
    }
}

class ReadyHeliState implements HeliState {
    private double fuel;
    private double heading;
    private double speed;
    private MediaPlayer helicopterHum;

    public ReadyHeliState(double fuel, double heading) {
        this.fuel = fuel;
        speed = 0;
        this.heading = heading;
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
        return new StoppingHeliState(fuel, heading);
    }

    @Override
    public HeliState update(Helicopter helicopter, HeliBlade heliBlade,
                            GameText fuelGauge) {
        updatePosition(helicopter);
        updateFuelGaugeText(fuelGauge);
        return this;
    }

    private void updatePosition(Helicopter helicopter) {
        Point2D newPosition = new Point2D(
                helicopter.getPosition().getX()
                        + (Math.sin(Math.toRadians(heading)) * speed),
                helicopter.getPosition().getY()
                        + (Math.cos(Math.toRadians(heading)) * speed));
        helicopter.updatePositionTo(newPosition);

        helicopter.getTransforms().clear();
        helicopter.getTransforms().addAll(
                new Translate(helicopter.getPosition().getX(),
                        helicopter.getPosition().getY()),
                new Rotate(-heading));
    }

    private void updateFuelGaugeText(GameText fuelGauge) {
        double remainingFuel = fuel
                - (Math.abs(speed) + Game.BASE_FUEL_CONSUMPTION_RATE);
        fuel = remainingFuel > 0 ? remainingFuel : 0;
        fuelGauge.setText("F:" + (int) fuel);
    }

    @Override
    public void increaseSpeed(Helicopter helicopter) {
        if (speed < Game.HELICOPTER_MAX_SPEED)
            speed += Helicopter.SPEED_ADJUSTMENT;
    }

    @Override
    public void decreaseSpeed(Helicopter helicopter) {
        if (speed > Game.HELICOPTER_MIN_SPEED)
            speed -= Helicopter.SPEED_ADJUSTMENT;
    }

    @Override
    public void turnLeft(Helicopter helicopter) {
        if (Math.abs(speed) > Game.EFFECTIVELY_ZERO)
            heading -= Helicopter.HEADING_ADJUSTMENT;
    }

    @Override
    public void turnRight(Helicopter helicopter) {
        if (Math.abs(speed) > Game.EFFECTIVELY_ZERO)
            heading += Helicopter.HEADING_ADJUSTMENT;
    }

    @Override
    public void refuelBy(double fuel) {
        this.fuel += fuel;
    }

    @Override
    public void stopAudio() {
        helicopterHum.stop();
    }

    @Override
    public double getFuel() {
        return fuel;
    }

    @Override
    public double getSpeed() {
        return speed;
    }
}

class StoppingHeliState implements HeliState {
    private double fuel;
    private double heading;
    private MediaPlayer helicopterShutdown;

    public StoppingHeliState(double fuel, double heading) {
        this.fuel = fuel;
        this.heading = heading;
        configAndPlayAudio();
    }

    private void configAndPlayAudio() {
        helicopterShutdown = new MediaPlayer(Game.HELICOPTER_STOPPING_MEDIA);
        helicopterShutdown.setVolume(Game.HELICOPTER_VOLUME);
        helicopterShutdown.play();
    }

    @Override
    public HeliState toggleIgnition(HeliBlade heliBlade) {
        heliBlade.spinUp();
        helicopterShutdown.stop();
        return new StartingHeliState(fuel, heading);
    }

    @Override
    public HeliState update(Helicopter helicopter, HeliBlade heliBlade,
                            GameText fuelGauge) {
        if (!heliBlade.isRotating()) {
            helicopterShutdown.stop();
            return new OffHeliState(fuel, heading);
        }
        return this;
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
    public void refuelBy(double fuel) { /* impossible */ }

    @Override
    public void stopAudio() {
        helicopterShutdown.stop();
    }

    @Override
    public double getFuel() {
        return fuel;
    }

    @Override
    public double getSpeed() {
        return 0;
    }
}
