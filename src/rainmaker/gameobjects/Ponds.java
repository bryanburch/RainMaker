package rainmaker.gameobjects;

import javafx.scene.layout.Pane;
import rainmaker.gameobjects.Updatable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Ponds extends Pane implements Updatable, Iterable<Pond> {
    private List<Pond> ponds;

    public Ponds() {
        ponds = new LinkedList<>();
    }

    public void add(Pond pond) {
        ponds.add(pond);
        this.getChildren().add(pond);
    }

    @Override
    public void update() {
        for (Pond p : ponds)
            p.update();
    }

    public double getTotalCapacity() {
        int totalCapacity = 0;
        for (Pond p : ponds)
            totalCapacity += p.getPercentFull();
        return ((double) totalCapacity) / 100;
    }

    @Override
    public Iterator<Pond> iterator() {
        return ponds.iterator();
    }
}
