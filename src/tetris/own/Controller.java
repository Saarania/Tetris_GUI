/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris.own;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author Jan
 */
public class Controller {

    Random r = new Random(); //pro padani nahodnych tvaru
    Block block;
    Shape nextShape;
    public Text scoreText = new Text("Score: " + Settings.score);
    public Text hightScoreText = new Text(); //text, ve kterem je hightScore
    private Field[][] fields = new Field[Settings.WIDTH][Settings.HEIGHT];
    private int rotadeCount = 0;
    private boolean opakvat = true;

    public Controller() {
        TetrisOwn.root.getChildren().add(hightScoreText);
        hightScoreText.setTranslateX(TetrisOwn.scene.getWidth()+10);
        hightScoreText.setTranslateY(200);
        hightScoreText.setFont(Font.font(19));
        try {
            Settings.HIGTH_SCORE = Integer.parseInt(Files.readAllLines(Paths.get(TetrisOwn.hightScoreString)).get(0));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        hightScoreText.setText("Hight score = " + Settings.HIGTH_SCORE);
        TetrisOwn.root.getChildren().add(scoreText);
        scoreText.setTranslateX(TetrisOwn.scene.getWidth() + 50);
        scoreText.setTranslateY(75);
        scoreText.setFont(Font.font(22));
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < fields[i].length; j++) {
                fields[i][j] = new Field();
            }
        }
        block = new Block(6, 0, Settings.vsechnyTvary[r.nextInt(Settings.vsechnyTvary.length)]); //tady je pocatecni
        nextShape = chooseNextShape(); //tvar, ktery se nastavi jako dalsi
    }

    private Shape chooseNextShape() {
        do {
            nextShape = Settings.vsechnyTvary[r.nextInt(Settings.vsechnyTvary.length)];
        } while (block.getShape() == nextShape);
        return nextShape;
    }

    //vlozi se hodnota a porovna se, jestli neni v poli(druhy parametr), vraci true pokud se rovnaji
    private boolean compareteFields(int i, int[] normalCoordinates, int[] testCoordinates) {
        for (int j = 0; j < normalCoordinates.length; j += 2) {
            if (testCoordinates[i] == normalCoordinates[j] && testCoordinates[i + 1] == testCoordinates[j + 1]) {
                return true;
            }
        }
        return false;
    }

    public void rotade() {

        int testCoordinates[] = block.calculateFields(block.increaseRotation());//ulozi se souradnice budouciho otoceni
        Block pomocnyBlock = new Block(block.getX(), block.getY(), block.getShape());
        for (int i = 0; i < testCoordinates.length; i += 2) {
            if (testCoordinates[i] < 0 || testCoordinates[i] > Settings.WIDTH - 1 || testCoordinates[i + 1] > Settings.HEIGHT - 2) {
                System.out.println("Neotocilo");
                return;
            }
        }
        pomocnyBlock.setRotation(block.increaseRotation());
        if (checkCollision(pomocnyBlock)) {
            return;
        }
        repaint(true);

        if (rotadeCount == 0) {
            block.setRotation(Rotation.R_90);
        }
        if (rotadeCount == 1) {
            block.setRotation(Rotation.R_180);
        }
        if (rotadeCount == 2) {
            block.setRotation(Rotation.R_270);
        }
        if (rotadeCount == 3) {
            block.setRotation(Rotation.R_0);
            rotadeCount = -1;
        }
        rotadeCount++;
    }

    public Field[][] getFields() {
        return fields;
    }

    public void moveLeft() {
        if (!checkLeftCollision(block)) {
            repaintLeft(true);
            block.left();
            repaintLeft(false);
        }
    }

    public void moveRight() {
        if (!checkRightCollision(block)) {
            repaintRight(true);
            block.right();
            repaintRight(false);
        }
    }

    private void chooseAll() {
        int[] coordinates = block.calculateFields(block.getRotation());
        for (int i = 0; i < coordinates.length; i += 2) {
            fields[coordinates[i]][coordinates[i + 1]].setAlive(false);
        }
    }

    private void empty_controller(int i, boolean empty) {
        int[] coordinates = block.calculateFields(block.getRotation());
        fields[coordinates[i]][coordinates[i + 1]].setEmpty(empty);
        fields[coordinates[i]][coordinates[i + 1]].setEmpty(empty);

        fields[coordinates[i]][coordinates[i + 1]].setEmpty(empty);
        fields[coordinates[i]][coordinates[i + 1]].setEmpty(empty);
    }

    private void setEmpty(int i, boolean empty) { //nastaveni mazani podle parametru
        int[] coordinates = block.calculateFields(block.getRotation());
        fields[coordinates[i]][coordinates[i + 1]].setEmpty(empty);
        fields[coordinates[i]][coordinates[i + 1]].setAlive(!empty);
    }

    public void timerChanged() {
        int[] coordinates = block.calculateFields(block.getRotation());
        if (block.isPushedDown() || checkCollision(block)) { //kdyz spadne a dopadne
            block.setPushedDown(false);//pro jistotu
            rotadeCount = 0;       //nastavit rotaci pro novy tvar
            chooseAll(); //nechat tvar umrit
            scan();     //nechat smazat vsechny rady
            for (int i = 1; i < coordinates.length; i += 2) { //konec hry
                if (coordinates[i] == 0) {
                    TetrisOwn.empty.setVisible(true);
                    Settings.konecHry = true;
                    TetrisOwn.theme.stop();
                    if (Settings.score > Settings.HIGTH_SCORE) { //ukladani nejvyssiho skore
                        try {
                            PrintWriter writer = new PrintWriter(System.getProperty("user.dir") + "\\src\\txtDocuments\\HightScore.txt");
                            writer.print(Settings.score);
                            writer.close();
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            Settings.score += 400 + r.nextInt(50);
            scoreText.setText("Score: " + Settings.score);
            block = new Block(6, 0, nextShape);
            nextShape = chooseNextShape();
        } else {
            repaint(true);
            block.move();
            repaint(false);
//                       Settings.rightAndLeftDisabled = false; // aby se zas mohlo posouvat doprava a doleva
        }
    }

    public void pushDown() { //zavola se kdyz chces hodit objekt dolu
        // Settings.rightAndLeftDisabled = true;
        block.setPushedDown(true);
        while (!checkCollision(block)) {
            repaint(true);
            block.move();
            repaint(false);
        }
    }

    private void repaintRight(boolean empty) {
        int[] coordinates = block.calculateFields(block.getRotation());
        if (coordinates != null) {
            for (int i = 0; i < coordinates.length; i += 2) {
                empty_controller(i, empty);
            }
        }
    }

    private void repaintLeft(boolean empty) {
        int[] coordinates = block.calculateFields(block.getRotation());
        if (coordinates != null) {
            for (int i = 0; i < coordinates.length; i += 2) {
                empty_controller(i, empty);
            }
        }
    }

    private void repaint(boolean empty) { //true pokud chceme vyprazdnit
        int[] coordinates = block.calculateFields(block.getRotation());
        if (coordinates != null) {
            for (int i = 0; i < coordinates.length; i += 2) {
                setEmpty(i, empty);
            }
        }
    }

    private boolean checkLeftCollision(Block block) {
        int[] coordinates = block.calculateFields(block.getRotation());
        if (block.getX() == 0) {
            return true;
        }
        if (block.getShape() == Shape.DOT) {
            for (int i = 0; i < coordinates.length; i += 4) {
                if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                    return true;
                }
            }
        }
        if (block.getShape() == Shape.I && (block.getRotation() == Rotation.R_0 || block.getRotation() == Rotation.R_180)) {
            for (int i = 0; i < 4; i++) {
                if (!fields[block.getX() - 1][block.getY() + i].isEmpty() && !fields[block.getX() - 1][block.getY() + i].isAlive()) {
                    return true;
                }
            }
        }
        if (block.getShape() == Shape.I && (block.getRotation() == Rotation.R_90 || block.getRotation() == Rotation.R_270)) {
            if (!fields[block.getX() - 1][block.getY()].isEmpty() && !fields[block.getX() - 1][block.getY()].isAlive()) {
                return true;
            }
        }
        if (block.getShape() == Shape.L) {
            if (block.getRotation() == Rotation.R_0) {
                for (int i = 0; i < 3; i++) {
                    if (!fields[block.getX() - 1][block.getY() + i].isEmpty() && !fields[block.getX() - 1][block.getY() + i].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90) {
                for (int i = 0; i < 2; i++) {
                    if (!fields[block.getX() - 1][block.getY() + i].isEmpty() && !fields[block.getX() - 1][block.getY() + i].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_180) {
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 2) {
                        i = 4;
                    }
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_270) {
                for (int i = 0; i < coordinates.length; i += 6) {
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.J) {
            if (block.getRotation() == Rotation.R_0) {
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 4) {
                        i = 6;
                    }
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90) {
                for (int i = 0; i < 4; i += 2) {
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_180) {
                for (int i = 0; i < coordinates.length - 2; i += 2) {
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_270) {
                for (int i = 0; i < coordinates.length; i += 6) {
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.T) {
            if (block.getRotation() == Rotation.R_0) {
                for (int i = 0; i < coordinates.length; i += 6) {
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90) {
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 2) {
                        i = 4;
                    }
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_180) {
                for (int i = 0; i < coordinates.length; i += 6) {
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.S) {
            if (block.getRotation() == Rotation.R_0 || block.getRotation() == Rotation.R_180) {
                for (int i = 0; i < coordinates.length; i += 4) {
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90 || block.getRotation() == Rotation.R_270) {
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 4) {
                        i = 6;
                    }
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.Z) {
            if (block.getRotation() == Rotation.R_0 || block.getRotation() == Rotation.R_180) {
                for (int i = 0; i < coordinates.length; i += 4) {
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90 || block.getRotation() == Rotation.R_270) {
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 2) {
                        i = 4;
                    }
                    if (!fields[coordinates[i] - 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] - 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }

                }
            }
        }
        return false;
    }

    private boolean checkRightCollision(Block block) {
        int[] coordinates = block.calculateFields(block.getRotation());
        if (block.getShape() == Shape.DOT) {
            if (block.getX() + 1 == Settings.WIDTH - 1) {
                return true;
            }
            for (int i = 2; i < coordinates.length; i += 4) {
                if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                    return true;
                }
            }
        }
        if (block.getShape() == Shape.I && (block.getRotation() == Rotation.R_0 || block.getRotation() == Rotation.R_180)) {
            if (block.getX() == Settings.WIDTH - 1) {
                return true;
            }
            for (int i = 0; i < 4; i++) {
                if (!fields[block.getX() + 1][block.getY() + i].isEmpty() && !fields[block.getX() + 1][block.getY() + i].isAlive()) {
                    return true;
                }
            }
        }
        if (block.getShape() == Shape.I && (block.getRotation() == Rotation.R_90 || block.getRotation() == Rotation.R_270)) {
            if (block.getX() + 3 == Settings.WIDTH - 1) {
                return true;
            }
            if (!fields[block.getX() + 4][block.getY()].isEmpty() && !fields[block.getX() + 4][block.getY()].isAlive()) {
                return true;
            }
        }
        if (block.getShape() == Shape.L) {
            if (block.getRotation() == Rotation.R_0) {
                if (block.getX() + 1 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 4) {
                        i += 2;
                    }
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90) {
                if (block.getX() + 2 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_180) {
                if (block.getX() + 1 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 2; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_270) {
                if (block.getX() + 2 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.J) {
            if (block.getRotation() == Rotation.R_0) {
                if (block.getX() + 1 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 0; i < 6; i += 2) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90) {
                if (block.getX() + 2 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length; i += 6) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_180) {
                if (block.getX() + 1 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 2; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_270) {
                if (block.getX() + 2 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.T) {
            if (block.getRotation() == Rotation.R_0) {
                if (block.getX() + 2 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90) {
                if (block.getX() + 1 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length - 2; i += 2) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_180) {
                if (block.getX() + 2 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_270) {
                if (block.getX() + 1 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 2) {
                        i = 4;
                    }
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.S) {
            if (block.getRotation() == Rotation.R_0 || block.getRotation() == Rotation.R_180) {
                if (block.getX() + 2 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 2; i < coordinates.length; i += 4) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90 || block.getRotation() == Rotation.R_270) {
                if (block.getX() + 1 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 2) {
                        i = 4;
                    }
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.Z) {
            if (block.getRotation() == Rotation.R_0 || block.getRotation() == Rotation.R_180) {
                if (block.getX() + 2 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 2; i < coordinates.length; i += 4) {
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90 || block.getRotation() == Rotation.R_270) {
                if (block.getX() + 1 == Settings.WIDTH - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 4) {
                        i = 6;
                    }
                    if (!fields[coordinates[i] + 1][coordinates[i + 1]].isEmpty() && !fields[coordinates[i] + 1][coordinates[i + 1]].isAlive()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkCollision(Block block) {
        int[] coordinates = block.calculateFields(block.getRotation());
        if (block.getShape() == Shape.DOT) {
            if (block.getY() + 1 == Settings.HEIGHT - 1) {
                return true;
            }
            for (int i = 4; i < coordinates.length; i += 2) {
                if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                    return true;
                }
            }
        }
        if ((block.getShape() == Shape.I) && (block.getRotation() == Rotation.R_0 || block.getRotation() == Rotation.R_180)) {
            if (block.getY() + 3 >= Settings.HEIGHT - 1) {
                return true;
            }
            if (!fields[coordinates[6]][coordinates[7] + 1].isEmpty() && !fields[coordinates[6]][coordinates[7] + 1].isAlive()) {
                return true;
            }
        }
        if ((block.getShape() == Shape.I) && (block.getRotation() == Rotation.R_90 || block.getRotation() == Rotation.R_270)) {
            for (int i = 0; i < coordinates.length; i += 2) {
                if (block.getY() == Settings.HEIGHT - 1) {
                    return true;
                }
                if ((!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty()) && (!fields[coordinates[i]][coordinates[i + 1] + 1].isAlive())) {
                    {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.L) {
            if (block.getRotation() == Rotation.R_0) {
                if (block.getY() + 2 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90) {
                if (block.getY() + 1 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 2; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_180) {
                if (block.getY() + 2 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length; i += 6) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_270) {
                if (block.getY() + 1 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 0; i < 6; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.J) {
            if (block.getRotation() == Rotation.R_0) {
                if (block.getY() + 2 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90) {
                if (block.getY() + 1 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 2; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_180) {
                if (block.getY() + 2 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_270) {
                if (block.getY() + 1 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 4) {
                        i = 6;
                    }
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.T) {
            if (block.getRotation() == Rotation.R_0) {
                if (block.getY() + 1 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 2) {
                        i = 4;
                    }
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90) {
                if (block.getY() + 2 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_180) {
                if (block.getY() + 1 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length - 2; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_270) {
                if (block.getY() + 2 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 4; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.S) {
            if (block.getRotation() == Rotation.R_0 || block.getRotation() == Rotation.R_180) {
                if (block.getY() + 1 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 2; i < coordinates.length; i += 2) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90 || block.getRotation() == Rotation.R_270) {
                if (block.getY() + 2 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 2; i < coordinates.length; i += 4) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
        }
        if (block.getShape() == Shape.Z) {
            if (block.getRotation() == Rotation.R_0 || block.getRotation() == Rotation.R_180) {
                if (block.getY() + 1 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 0; i < coordinates.length; i += 2) {
                    if (i == 2) {
                        i = 4;
                    }
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
            if (block.getRotation() == Rotation.R_90 || block.getRotation() == Rotation.R_270) {
                if (block.getY() + 2 == Settings.HEIGHT - 1) {
                    return true;
                }
                for (int i = 2; i < coordinates.length; i += 4) {
                    if (!fields[coordinates[i]][coordinates[i + 1] + 1].isEmpty() && !fields[coordinates[i]][coordinates[i + 1] + 1].isAlive()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void scan() { //j == x, i == y
        int x = 0;
        for (int i = 0; i < Settings.HEIGHT; i++) {      //tady to jednou  0 - 19    20krat
            for (int j = 0; j < Settings.WIDTH; j++) {    // tady to 12   0 - 11    12 krat
                if (!fields[j][i].isEmpty()) {
                    x++;
                }
                if (x == 12) {// smazat line
                    Settings.score+=250; //odmena za znicenou linu
                    for (int k = 0; k < Settings.WIDTH; k++) {
                        fields[k][i].setEmpty(true);
                    }
                    for (int l = i; l >= 0; l--) {
                        for (int m = 0; m < Settings.WIDTH; m++) {
                            if (!fields[m][l].isEmpty()) { //mazani
                                fields[m][l + 1].setEmpty(false, fields[m][l].getColor());
                                fields[m][l].setEmpty(true);
                            }
                        }
                    }
                }
            }
            x = 0;
        }
    }
}
