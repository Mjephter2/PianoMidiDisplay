package sample.views;

import javafx.application.Application;
import javafx.css.Style;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Sample GUI of a partial Grand Staff
 * starting from Middle C to 2nd F Note.
 */
public class GrandStaffSample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        double initialStartX = 100.0;
        double initialEndX = 800.0;
        double initialY = 10.0;
        //Creating line objects, circle objects and Text objects
        Line[] lines = new Line[11];
        Circle[] circles = new Circle[11];
        Text[] texts = new Text[11];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new Line(initialStartX, initialY + 40 * i, initialEndX, initialY + 40 * i);
            lines[i].setStrokeWidth(6);

            circles[i] = new Circle(lines[i].getStartX(), lines[i].getStartY(), 19);
            circles[i].setFill(Paint.valueOf("gray"));
            circles[i].setVisible(true);
            circles[i].setTranslateX(100);
            texts[i] = new Text("" + i);
            texts[i].setX(lines[i].getStartX() + 50);
            texts[i].setY(lines[i].getStartY());
            texts[i].setFont(new Font(30));
            texts[i].setBoundsType(TextBoundsType.VISUAL);
            texts[i].setVisible(true);
        }

        for (int i : new int[]{1, 3, 5, 7, 9, 10}) {
            lines[i].setStrokeWidth(0.1);
            circles[i].setTranslateX(50);
        }
        circles[9].setFill(Color.TRANSPARENT);
        circles[10].setFill(Color.TRANSPARENT);

        Line verticalLine = new Line(initialStartX, initialY, initialStartX, initialY + 40 * 8);
        verticalLine.setStrokeWidth(6);
        verticalLine.setTranslateX((initialStartX - initialEndX) / 2.0);
        verticalLine.setTranslateY(-38.5);


        InputStream stream = null;
        try {
            stream = new FileInputStream("src/main/java/sample/views/treble_clef.png");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Image image = new Image(stream);
        ImageView treble_clef = new ImageView();
        treble_clef.setImage(image);
        treble_clef.setX(10);
        treble_clef.setY(10);
        treble_clef.setFitWidth(200);
        treble_clef.setFitHeight(525);
        treble_clef.setTranslateX(-240);
        treble_clef.setTranslateY(-15);

        //Create a Group object for lines
        Group lineGroup = new Group(lines);
        //Create a Group object for circle
        Group circleGroup = new Group(circles);
        circleGroup.setTranslateX(-90);
        //Create a Group for texts objects
        Group textsGroup = new Group(texts);
        textsGroup.setTranslateX(100);

        StackPane root = new StackPane();
        root.getChildren().add(0, lineGroup);
        root.getChildren().add(1, circleGroup);
        root.getChildren().add(2, textsGroup);
        root.getChildren().add(3, verticalLine);
        root.getChildren().add(4, treble_clef);

        Scene scene = new Scene(root ,900, 600);
        primaryStage.setTitle("Grand Staff Sample");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
