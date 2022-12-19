package rainmaker.gameobjects;

import javafx.scene.layout.Pane;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Clouds extends Pane implements Updatable, Iterable<Cloud> {
    private List<Cloud> clouds;
    private List<Cloud> markedForDeletion;
    private Wind wind;

    public Clouds(Wind wind) {
        clouds = new LinkedList<>();
        markedForDeletion = new LinkedList<>();
        this.wind = wind;
    }

    public void add(Cloud cloud) {
        clouds.add(cloud);
        this.getChildren().add(cloud);
    }

    @Override
    public void update() {
        updateOrMarkForDeletion();
        tryDeletingDeadClouds();
    }

    private void updateOrMarkForDeletion() {
        for (Cloud c : clouds) {
            if (c.isDead())
                markedForDeletion.add(c);
            else
                c.update();
        }
    }

    private void tryDeletingDeadClouds() {
        if (markedForDeletion.size() > 0) {
            markedForDeletion.forEach(cloud -> {
                getChildren().remove(cloud);
                wind.removeObserver(cloud);
            });
            clouds.removeAll(markedForDeletion);
            markedForDeletion.clear();
        }
    }

    @Override
    public Iterator<Cloud> iterator() {
        return clouds.iterator();
    }

    public int getNumberOf() {
        return clouds.size();
    }

    public void stopAudio() {
        for (Cloud c : clouds)
            c.stopAudio();
    }
}
