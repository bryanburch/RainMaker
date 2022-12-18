package rainmaker.gameobjects;

import javafx.scene.shape.Circle;

/**
 * Used for helicopter whose bound is formed by its spinning blade
 */
public class CircleBound extends Bound {

    public CircleBound(GameObject objectToBound, Circle boundShape) {
        super(objectToBound, boundShape);
    }
}
