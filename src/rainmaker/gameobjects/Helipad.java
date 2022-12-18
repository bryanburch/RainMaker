package rainmaker.gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Translate;
import rainmaker.Game;

/**
 * The starting/ending location for helicopter. Represented as an image.
 */
public class Helipad extends GameObject {

    public Helipad(Point2D initialPosition, Point2D dimensions) {
        super(initialPosition);

        loadAndSetupImage(dimensions);

        this.getTransforms().add(new Translate(Game.HELIPAD_POSITION.getX(),
                Game.HELIPAD_POSITION.getY()));
    }

    private void loadAndSetupImage(Point2D dimensions) {
        ImageView image = new ImageView(new Image("images/helipad_textured.png"));
        image.setFitHeight(dimensions.getY());
        image.setFitWidth(dimensions.getX());
        centerAboutOrigin(dimensions, image);
        this.getChildren().add(image);
    }

    private static void centerAboutOrigin(
            Point2D dimensions, ImageView image) {
        image.setTranslateX(-dimensions.getX() / 2);
        image.setTranslateY(-dimensions.getY() / 2);
    }
}
