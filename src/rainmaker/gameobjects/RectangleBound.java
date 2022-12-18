package rainmaker.gameobjects;

import javafx.scene.shape.Rectangle;

public class RectangleBound extends Bound {

    public RectangleBound(GameObject objectToBound, Rectangle boundShape) {
        super(objectToBound, boundShape);
        centerAboutOrigin();
    }

    private void centerAboutOrigin() {
        getBoundShape().setTranslateX(
                -((Rectangle) getBoundShape()).getWidth() / 2);
        getBoundShape().setTranslateY(
                -((Rectangle) getBoundShape()).getHeight() / 2);
    }
}
