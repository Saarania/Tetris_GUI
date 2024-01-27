package tetris.own;

import javafx.scene.paint.Color;

public class Field {

    private boolean empty;
    private boolean alive;
    private FieldView fieldView;
    private Color color; //barva na danem miste

    public Field() {
        this.empty = true;
        this.alive = false;
    }
    
    public Color getColor() {
        return color;
    }
    
    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
        fieldView.update(empty);
        if (empty) {
            color = null;
        }else {
             color = Settings.controller.block.getColor();
        }
    }
    
    public void setEmpty(boolean empty, Color color) { //pouziva se nakonci pri padu patra
        this.empty = empty;
        fieldView.update(empty, color);
        this.color = color;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    public void setFieldView(FieldView fieldView) {
        this.fieldView = fieldView;
        fieldView.update(empty);
    }
}
