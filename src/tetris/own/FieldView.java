package tetris.own;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FieldView extends Rectangle{
    private Field field;

    public FieldView(Field field) {
        this.field = field;
        setWidth(Settings.SIZE);
        setHeight(Settings.SIZE);
    }
    
    public void update(boolean empty) {
        if (empty) {
            setFill(Color.BLACK);
        }else {
            setFill(Settings.controller.block.getColor());
        }
    }
    
    public void update(boolean empty, Color color) {
        if (empty) {
            setFill(Color.BLACK);
        }else {
            setFill(color);
        }
    }
    
    
}
