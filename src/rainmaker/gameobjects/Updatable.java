package rainmaker.gameobjects;

/**
 * Not all GameObjects need to update, but those that require so will be
 * updated from Game loop.
 */
public interface Updatable {
    void update();

}
