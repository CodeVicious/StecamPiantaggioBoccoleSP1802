package stecamSP1802;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainStecamPiantaggioBoccoleSP1802 extends Application {
    final Logger Logger = LogManager.getLogger(MainStecamPiantaggioBoccoleSP1802.class);

    ConfigurationManager conf = ConfigurationManager.getInstance();
    private Stage primaryStage;
    private Parent root;
    private FXMLLoader loader;


    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        Logger.info("START STECAMSP1802");
        conf.getConfiguration();
        loader= new FXMLLoader(getClass().getResource("/sample.fxml"));
        root = (Parent)loader.load();
        primaryStage.setTitle("STECAM Piantaggio Boccole SP1802");
        primaryStage.setOnCloseRequest(e->{
            e.consume();
            closeProgram();
        });
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void closeProgram() {
        System.out.println("CHIUDO");
        MainController main = (MainController)loader.getController();
        main.CloseApp(new ActionEvent());

        this.primaryStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
