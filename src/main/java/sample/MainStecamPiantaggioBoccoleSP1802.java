package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainStecamPiantaggioBoccoleSP1802 extends Application {

    ConfigurationManager conf = ConfigurationManager.getInstance();
    @Override
    public void start(Stage primaryStage) throws Exception{
        conf.getConfiguration();
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("STECAM Piantaggio Poccole SP1802");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
