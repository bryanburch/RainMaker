package rainmaker.gameobjects;

import javafx.scene.layout.Pane;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BoundsPane extends Pane implements Updatable, Iterable<Bound> {
    private List<Bound> bounds;
    private List<Bound> markedForDeletion;

    public BoundsPane() {
        bounds = new LinkedList<>();
        markedForDeletion = new LinkedList<>();
        setVisible(false);
    }

    public void add(CircleBound circleBound) {
        bounds.add(circleBound);
        getChildren().add(circleBound);
    }

    public void add(RectangleBound rectangleBound) {
        bounds.add(rectangleBound);
        getChildren().add(rectangleBound);
    }

    public Bound getBoundFor(GameObject gameObject) {
        for (Bound b : bounds) {
            if (b.getBoundedObject() == gameObject)
                return b;
        }
        return null;
    }

    public void markForDeletion(Bound bound) {
        boolean validDeletion = bounds.contains(bound);
        if (validDeletion)
            markedForDeletion.add(bound);
    }

    @Override
    public void update() {
        for (Bound b : bounds)
            b.update();
        tryDeletingBoundsMarkedForDeletion();
    }

    private void tryDeletingBoundsMarkedForDeletion() {
        if (markedForDeletion.size() > 0) {
            markedForDeletion.forEach(bound -> getChildren().remove(bound));
            bounds.removeAll(markedForDeletion);
            markedForDeletion.clear();
        }
    }

    public void toggleVisibility() {
        this.setVisible(!this.isVisible());
    }

    @Override
    public Iterator<Bound> iterator() {
        return bounds.iterator();
    }
}
