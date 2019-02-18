package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainStecamPiantaggioBoccoleSP1802 extends Application {

    ConfigurationManager conf = ConfigurationManager.getInstance();
    final Logger logger = LogManager.getLogger(MainStecamPiantaggioBoccoleSP1802.class);


    @Override
    public void start(Stage primaryStage) throws Exception{
        logger.info("PROVA");
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
