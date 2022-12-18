package rainmaker.gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.transform.Translate;
import rainmaker.Game;

public interface BlimpState {
    BlimpState update(Blimp blimp, GameText fuelText);

    double extractFuel();

    void stopAudio();
}

class CreatedBlimp implements BlimpState {
    private double fuel;

    public CreatedBlimp(double fuel) {
        this.fuel = fuel;
    }

    @Override
    public BlimpState update(Blimp blimp, GameText fuelText) {
        updatePosition(blimp);

        if (blimp.getPosition().getX()
                + (Game.BLIMP_BODY_SIZE.getX() / 2) > 0)
            return new InViewBlimp(fuel);
        return this;
    }

    private void updatePosition(Blimp blimp) {
        Point2D newPosition = new Point2D(
                blimp.getPosition().getX() + blimp.getSpeed(),
                blimp.getPosition().getY());
        blimp.updatePositionTo(newPosition);

        blimp.getTransforms().clear();
        blimp.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));
    }

    @Override
    public double extractFuel() {
        /* impossible */
        return 0;
    }

    @Override
    public void stopAudio() { /* impossible */ }

}

class InViewBlimp implements BlimpState {
    private double fuel;
    private MediaPlayer blimpAudio;

    public InViewBlimp(double fuel) {
        this.fuel = fuel;
        blimpAudio = new MediaPlayer(Game.BLIMP_MEDIA);
        blimpAudio.setCycleCount(AudioClip.INDEFINITE);
        blimpAudio.setVolume(Game.BLIMP_VOLUME);
        blimpAudio.play();
    }

    @Override
    public BlimpState update(Blimp blimp, GameText fuelText) {
        updatePosition(blimp);
        updateFuelText(fuelText);

        if (blimp.getPosition().getX()
                - (Game.BLIMP_BODY_SIZE.getX() / 2) > Game.GAME_WIDTH) {
            blimpAudio.stop();
            return new DeadBlimp();
        }
        return this;
    }

    private void updatePosition(Blimp blimp) {
        Point2D newPosition = new Point2D(
                blimp.getPosition().getX() + blimp.getSpeed(),
                blimp.getPosition().getY());
        blimp.updatePositionTo(newPosition);

        blimp.getTransforms().clear();
        blimp.getTransforms().add(
                new Translate(newPosition.getX(), newPosition.getY()));
    }

    private void updateFuelText(GameText fuelText) {
        fuelText.setText(String.valueOf((int) fuel));
    }

    @Override
    public double extractFuel() {
        if (fuel >= Game.REFUEL_RATE) {
            fuel -= Game.REFUEL_RATE;
            return Game.REFUEL_RATE;
        } else {
            double remainder = fuel;
            fuel = 0;
            return remainder;
        }
    }

    public void stopAudio() {
        blimpAudio.stop();
    }
}

class DeadBlimp implements BlimpState {

    @Override
    public BlimpState update(Blimp blimp, GameText fuelText) {
        /* impossible */
        return this;
    }

    @Override
    public double extractFuel() {
        /* impossible */
        return 0;
    }

    @Override
    public void stopAudio() { /* impossible */ }
}
