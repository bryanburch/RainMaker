package rainmaker.gameobjects;

import javafx.scene.layout.Pane;
import rainmaker.gameobjects.DeadBlimp;
import rainmaker.gameobjects.Updatable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Blimps extends Pane implements Updatable, Iterable<Blimp> {
    private List<Blimp> blimps;
    private List<Blimp> markedForDeletion;

    public Blimps() {
        blimps = new LinkedList<>();
        markedForDeletion = new LinkedList<>();
    }

    public void add(Blimp b) {
        blimps.add(b);
        getChildren().add(b);
    }

    @Override
    public void update() {
        updateEachOrMarkForDeletion();
        tryDeletingDeadBlimps();
    }

    private void updateEachOrMarkForDeletion() {
        for (Blimp b : blimps) {
            if (b.isDead())
                markedForDeletion.add(b);
            else
                b.update();
        }
    }

    private void tryDeletingDeadBlimps() {
        if (markedForDeletion.size() > 0) {
            markedForDeletion.forEach(blimp -> getChildren().remove(blimp));
            blimps.removeAll(markedForDeletion);
            markedForDeletion.clear();
        }
    }

    @Override
    public Iterator<Blimp> iterator() {
        return blimps.iterator();
    }

    public void stopAudio() {
        for (Blimp b : blimps)
            b.stopAudio();
    }
}
