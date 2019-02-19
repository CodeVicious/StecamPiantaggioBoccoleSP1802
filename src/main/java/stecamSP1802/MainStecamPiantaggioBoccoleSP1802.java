package stecamSP1802;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainStecamPiantaggioBoccoleSP1802 extends Application {
    final Logger Logger = LogManager.getLogger(MainStecamPiantaggioBoccoleSP1802.class);

    ConfigurationManager conf = ConfigurationManager.getInstance();




    @Override
    public void start(Stage primaryStage) throws Exception{
        Logger.info("START STECAMSP1802");
        conf.getConfiguration();
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("STECAM Piantaggio Boccole SP1802");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {


        launch(args);
    }
}
