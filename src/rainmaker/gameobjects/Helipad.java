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
    public static final Point2D HELIPAD_DIMENSIONS = new Point2D(100, 100);
    public static final Point2D HELIPAD_POSITION =
            new Point2D((Game.GAME_WIDTH / 2),
                    (Game.GAME_HEIGHT / 25) + (HELIPAD_DIMENSIONS.getY() / 2));

    public Helipad(Point2D initialPosition, Point2D dimensions) {
        super(initialPosition);

        loadAndSetupImage(dimensions);

        this.getTransforms().add(new Translate(HELIPAD_POSITION.getX(),
                HELIPAD_POSITION.getY()));
    }

    private void loadAndSetupImage(Point2D dimensions) {
        ImageView image = new ImageView(
                new Image("images/helipad_textured.png"));
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
