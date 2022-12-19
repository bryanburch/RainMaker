package rainmaker.gameobjects;

import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import rainmaker.Game;

/**
 * Since the game coordinate space is being scaled by -1 (to convert to
 * quadrant I from IV), any text will have to be scaled accordingly.
 */
public class GameText extends Group {
    public static final String FONT_FAMILY = "Futura";
    private Text text;

    public GameText(final String string, final Paint fill,
                    FontWeight fontWeight) {
        text = new Text(string);
        text.setFill(fill);
        text.setFont(Font.font(FONT_FAMILY, fontWeight, 13));

        this.setScaleY(Game.INVERT_AXIS);
        this.getChildren().add(text);
    }

    public GameText(final String string, final Paint fill) {
        this(string, fill, FontWeight.NORMAL);
    }

    public void setSize(double size) {
        Font font = text.getFont();
        text.setFont(Font.font(font.getFamily(),
                FontWeight.NORMAL, size));
    }

    public void setText(String string) {
        text.setText(string);
    }
}
