package rainmaker.gameobjects;

import javafx.scene.layout.Pane;
import rainmaker.gameobjects.DistanceLine;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DistanceLines extends Pane implements Updatable, Iterable<DistanceLine> {
    private List<DistanceLine> distanceLines;
    private List<DistanceLine> markedForDeletion;

    public DistanceLines() {
        distanceLines = new LinkedList<>();
        markedForDeletion = new LinkedList<>();
        setVisible(false);
    }

    public void add(DistanceLine dLine) {
        distanceLines.add(dLine);
        getChildren().add(dLine);
    }

    public void markForDeletion(DistanceLine dLine) {
        boolean validDeletion = distanceLines.contains(dLine);
        if (validDeletion)
            markedForDeletion.add(dLine);
    }

    @Override
    public void update() {
        for (DistanceLine d : distanceLines)
            d.update();
        tryDeletingDistanceLinesMarkedForDeletion();
    }

    private void tryDeletingDistanceLinesMarkedForDeletion() {
        if (markedForDeletion.size() > 0) {
            markedForDeletion.forEach(dLine -> getChildren().remove(dLine));
            distanceLines.removeAll(markedForDeletion);
            markedForDeletion.clear();
        }
    }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }

    @Override
    public Iterator<DistanceLine> iterator() {
        return distanceLines.iterator();
    }
}
