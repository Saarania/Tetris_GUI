package tetris.own;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 
 * @author Sara Praks
 * 
 * 1
 * 
 * prvni opravdova graficka aplikace, zaklady byly od ucitele, zbytek trida
 * contstructor byl dodelan
 * asi o rok pozdeji oprava nekterych chyb pri otaceni, pridani barev ruznych tvaru,
 * rychlejsi padani a pridano nejvyssi skore
 */

public class TetrisOwn extends Application {

    private boolean recharge = true; //promenna pouzivana, aby se nedalo spamovat posuny do stran a dolu
    private boolean infoStarted = false;
    private boolean predStartem = false;
    static Stage primaryStage;

    public static MediaPlayer theme = null;
    ImageView startingImage = new ImageView("images/background2.jpg");
    ImageView start = new ImageView("images/start.jpg");
    ImageView end = new ImageView("images/end.jpg");
    ImageView info = new ImageView("images/info.jpg");
    ImageView text = new ImageView("images/infoText.jpg");
    public final Controller controller = new Controller();
    public static ImageView empty = new ImageView("images/empty.png");
    public static String hightScoreString = System.getProperty("user.dir") + "\\src\\txtDocuments\\HightScore.txt";;

    public static Group root = new Group();
    static Scene scene = new Scene(root, (Settings.SIZE + 1) * Settings.WIDTH + 3, (Settings.SIZE + 1) * Settings.HEIGHT + 3);

    public Thread blockThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);  // sem promennou
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (!Settings.konecHry) {
                                controller.timerChanged();
                            }
                        }
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    });

    @Override
    public void start(Stage primaryStage) {
        Settings.controller = controller;
        this.primaryStage = primaryStage;
        try {
            Path pathToHigthScore = Paths.get(hightScoreString);
            Settings.HIGTH_SCORE = Integer.parseInt(Files.readAllLines(pathToHigthScore).get(0));
            //hightScoreText.setText("Nejvyssi score = " + hightScoreInt);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        blockThread.setDaemon(true);
        //blockThread.start();

        Field[][] fields = controller.getFields();
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < fields[i].length; j++) {
                FieldView fieldView = new FieldView(fields[i][j]);
                fieldView.setTranslateX(i * (Settings.SIZE + 1));
                fieldView.setTranslateY(j * (Settings.SIZE + 1));
                fields[i][j].setFieldView(fieldView);
                fields[i][j].setEmpty(true);
                root.getChildren().add(fieldView);
            }
        }

        inicializace(primaryStage, blockThread);

        root.getChildren().addAll(startingImage, start, end, info, text, empty);

        primaryStage.setTitle("Tetris!");
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    primaryStage.close();
                }
                if (predStartem) {
                    if (recharge /*&& !Settings.rightAndLeftDisabled*/) {
                        if (event.getCode() == KeyCode.UP) { //stane se ze objekt spadne hned dolu
                            controller.pushDown();
                            recharge = false;
                        }
                        if (event.getCode() == KeyCode.LEFT) {
                            controller.moveLeft();
                            recharge = false;
                        }
                        if (event.getCode() == KeyCode.RIGHT) {
                            controller.moveRight();
                            recharge = false;
                        }
                        if (event.getCode() == KeyCode.SPACE) {
                            controller.rotade();
                            recharge = false;
                        }
                    }
                    if (Settings.konecHry) {
                        if (event.getCode() == KeyCode.SPACE) {
                            primaryStage.close();
                        }
                    }
                    if (infoStarted) {
                        if (event.getCode() == KeyCode.SPACE) {
                            text.setVisible(false);
                            infoStarted = false;
                        }
                    }
                }
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                recharge = true;
            }
        });

    }

    private void inicializace(Stage primaryStage, Thread blockThread) {
        empty.setVisible(false);

        text.setVisible(false);

        start.setTranslateX(375 / 2 - 148 / 2);
        start.setTranslateY(175);
        start.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                primaryStage.setWidth(scene.getWidth() + 220);
                start.setVisible(false);
                end.setVisible(false);
                startingImage.setVisible(false);
                info.setVisible(false);
                predStartem = true;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
                blockThread.start();

                //start play the theme
                URL urlTheme = TetrisOwn.class.getClassLoader().getResource("sounds/theme.mp3");
                Media media = null;
                try {
                    media = new Media(urlTheme.toURI().toString());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                theme = new MediaPlayer(media);
                // Set up looping
                theme.setOnEndOfMedia(() -> {
                    theme.seek(Duration.ZERO);
                });
                theme.play();
            }
        });

        info.setTranslateX(375 / 2 - 136 / 2);
        info.setTranslateY(start.getTranslateY() + 100);
        info.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                infoStarted = true;
                text.setVisible(true);
            }
        });

        end.setTranslateX(375 / 2 - 154 / 2);
        end.setTranslateY(info.getTranslateY() + 100);
        end.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                primaryStage.close();
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
