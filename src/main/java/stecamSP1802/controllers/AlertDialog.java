package stecamSP1802.controllers;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class AlertDialog {
    public static void display(String title, String msg) {
        Stage win = new Stage();
        win.setTitle(title);
        win.setMinWidth(250);
        win.setMinHeight(100);
        Label lbl = new Label();
        lbl.setText(msg);

        Button buttonok = new Button("OK");
        buttonok.setOnAction(a ->
        {
            win.close();
        });
        VBox layout = new VBox(5);
        layout.getChildren().addAll(lbl, buttonok);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout);
        win.setScene(scene);
    }
}
