package rainmaker.gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.Group;

/**
 * Is-a Group to treat game objects as Node objects to be put straight onto
 * scene graph. Position treated as center of object (like Circle but unlike
 * Rectangle, etc.). In other words,  GameObjects who aren't Circles will have
 * to translate their x- and y-position by half their width and height
 * respectively. Standardizing the position attribute of all GameObjects like
 * this makes it consistent and simplifies distance calculations between
 * GameObjects.
 * Additionally, rainmaker.GameObject does not implement rainmaker.Updatable because it cannot be
 * assumed that all inheritors will have an updatable quality (e.g. rainmaker.Helipad).
 */
public abstract class GameObject extends Group {
    private Point2D position;

    public GameObject(Point2D position) {
        this.position = position;
    }

    public Point2D getPosition() {
        return position;
    }

    public void updatePositionTo(Point2D position) {
        this.position = position;
    }
}
