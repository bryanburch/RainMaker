package rainmaker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import rainmaker.gameobjects.*;

/**
 * Sets up key event handlers that invoke Game class methods.
 */
public class GameApp extends Application {
    private Game game;
    private Scene scene;
    
    @Override
    public void start(Stage primaryStage) {
        game = Game.getInstance();
        scene = new Scene(game, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        setupEventHandlers();
        configAndShow(primaryStage);
    }

    private void configAndShow(Stage primaryStage) {
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("RainMaker");
        primaryStage.show();
    }

    private void setupEventHandlers() {
        scene.setOnKeyPressed(e -> {
            switch(e.getCode()) {
                case LEFT -> game.handleLeftKeyPressed();
                case RIGHT -> game.handleRightKeyPressed();
                case UP -> game.handleUpKeyPressed();
                case DOWN -> game.handleDownKeyPressed();
                case SPACE -> game.handleSpaceKeyPressed();
                case I -> game.handleIKeyPressed();
                case R -> game.handleRKeyPressed();
                case B -> game.handleBKeyPressed();
                case D -> game.handleDKeyPressed();
            }
        });
    }
}
