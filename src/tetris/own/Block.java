/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris.own;

import javafx.scene.paint.Color;


/**
 *
 * @author Jan
 */
public class Block {

    private int x;
    private int y;
    private Rotation rotation;
    private Shape shape;
    private int height;
    private int width;
    private int length;
    private Color color;
    private boolean pushedDown; //na zacatku false nastavi se na true pote, co ho shodime, aby se vedelo ze je dole,

    public boolean isPushedDown() {
        return pushedDown;
    }

    public void setPushedDown(boolean pushedDown) {
        this.pushedDown = pushedDown;
    }
    
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Color getColor() {
        return color;
    }
    
    public Block(int x, int y, Shape shape) {
        this.x = x;
        this.y = y;
        this.shape = shape;
        switch (shape) {
            case DOT:
                color = Color.GREEN;
                break;
            case I:
                color = Color.YELLOW;
                break;
            case J:
                color = Color.ORANGE;
                break;
            case L:
                color = Color.RED;
                break;
            case S:
                color = Color.BLUE;
                break;
            case T:
                color = Color.MAGENTA;
                break;
            case Z:
                color = Color.AQUA;
                break;
        }
        pushedDown = false;
        rotation = Rotation.R_0; // bacha tady!!!
        height = 0;
        width = 0;
    }

    public int getLength() {
        return length;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public void left() {
        x--;
    }

    public void right() {
        x++;
    }

    public void move() {
        y++;
    }

    public Rotation increaseRotation() {//vraci o rotaci vyssi
        switch (getRotation()) {
            case R_0:
                return Rotation.R_90;
            case R_90:
                return Rotation.R_180;
            case R_180:
                return Rotation.R_270;
            case R_270:
                return Rotation.R_0;
        }
        return null;
    }

    public int[] calculateFields(Rotation r) {
        if (shape == Shape.DOT) {
            length = 2;
            //                0  1    2      3    4  5        6      7 
            return new int[]{x, y, x + 1, y, x, y + 1, x + 1, y + 1};
        }
        if (shape == Shape.I) {
            if (r == Rotation.R_0 || r == Rotation.R_180) {
                length = 1;
                //                 0 1   2 3     4 5     6  7
                return new int[]{x, y, x, y + 1, x, y + 2, x, y + 3};
            }
            if (r == Rotation.R_90 || r == Rotation.R_270) {
                length = 4;
                //                 0 1  2   3  4   5  6   7
                return new int[]{x, y, x + 1, y, x + 2, y, x + 3, y};
            }
        }
        if (shape == Shape.L) {
            if (r == Rotation.R_0) {
                length = 2;
                //             0 1   2 3     4 5     6   7
                return new int[]{x, y, x, y + 1, x, y + 2, x + 1, y + 2};
            }
            if (r == Rotation.R_90) {
                length = 3;
                return new int[]{x, y, x + 1, y, x + 2, y, x, y + 1};
            }
            if (r == Rotation.R_180) {
                length = 2;
                return new int[]{x, y, x + 1, y, x + 1, y + 1, x + 1, y + 2};
            }
            if (r == Rotation.R_270) {
                length = 3;
                return new int[]{x, y + 1, x + 1, y + 1, x + 2, y + 1, x + 2, y};
            }
        }
        if (shape == Shape.J) {
            if (r == Rotation.R_0) {
                length = 2;
                //                 0   1   2   3     4   5     6 7
                return new int[]{x + 1, y, x + 1, y + 1, x + 1, y + 2, x, y + 2};
            }
            if (r == Rotation.R_90) {
                length = 3;
                return new int[]{x, y, x, y + 1, x + 1, y + 1, x + 2, y + 1};
            }
            if (r == Rotation.R_180) {
                length = 2;
                return new int[]{x, y, x, y + 1, x, y + 2, x + 1, y};
            }
            if (r == Rotation.R_270) {
                length = 3;
                return new int[]{x, y, x + 1, y, x + 2, y, x + 2, y + 1};
            }

        }
        if (shape == Shape.T) {
            if (r == Rotation.R_0) {
                length = 3;
                return new int[]{x, y, x + 1, y, x + 2, y, x + 1, y + 1};
            }
            if (r == Rotation.R_90) {
                length = 2;
                return new int[]{x + 1, y, x + 1, y + 1, x + 1, y + 2, x, y + 1};
            }
            if (r == Rotation.R_180) {
                length = 3;
                return new int[]{x, y + 1, x + 1, y + 1, x + 2, y + 1, x + 1, y};
            }
            if (r == Rotation.R_270) {
                length = 2;
                return new int[]{x, y, x, y + 1, x, y + 2, x + 1, y + 1};
            }
        }
        if (shape == Shape.S) {
            if (r == Rotation.R_0 || r == Rotation.R_180) {
                length = 3;
                return new int[]{x + 1, y, x + 2, y, x, y + 1, x + 1, y + 1};
            }
            if (r == Rotation.R_90 || r == Rotation.R_270) {
                length = 2;
                return new int[]{x, y, x, y + 1, x + 1, y + 1, x + 1, y + 2};
            }
        }
        if (shape == Shape.Z) {
            if (r == Rotation.R_0 || r == Rotation.R_180) {
                length = 3;
                return new int[]{x, y, x + 1, y, x + 1, y + 1, x + 2, y + 1};
            }
            if (r == Rotation.R_90 || r == Rotation.R_270) {
                length = 2;
                return new int[]{x + 1, y, x + 1, y + 1, x, y + 1, x, y + 2};
            }
        }
        return null;
    }

}
