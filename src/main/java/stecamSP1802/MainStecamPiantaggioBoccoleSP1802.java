package stecamSP1802;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.controllers.LoginController;
import stecamSP1802.controllers.MainController;
import stecamSP1802.controllers.ScreensController;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;


public class MainStecamPiantaggioBoccoleSP1802 extends Application {
    private static Logger Logger = LogManager.getLogger(MainStecamPiantaggioBoccoleSP1802.class);

    ConfigurationManager conf = ConfigurationManager.getInstance();

    public static String mainID = "main";
    public static String mainFile = "/main.fxml";
    public static String loginID = "login";
    public static String loginFile = "/loginpanel.fxml";
    public static String propertiesID = "properties";
    public static String propertiesFILE = "/properties.fxml";

    private Stage primaryStage;
    private Group root;
    private FXMLLoader loader;

    private ScreensController mainContainer;


    @Override
    public void start(Stage primaryStage) throws Exception {


        Logger.info("START STECAMSP1802");
        conf.getConfiguration();

        mainContainer = new ScreensController();
        mainContainer.loadScreen(MainStecamPiantaggioBoccoleSP1802.mainID, MainStecamPiantaggioBoccoleSP1802.mainFile);
        mainContainer.loadScreen(MainStecamPiantaggioBoccoleSP1802.loginID, MainStecamPiantaggioBoccoleSP1802.loginFile);
        mainContainer.loadScreen(MainStecamPiantaggioBoccoleSP1802.propertiesID, MainStecamPiantaggioBoccoleSP1802.propertiesFILE);
        mainContainer.setScreen(MainStecamPiantaggioBoccoleSP1802.mainID);

        LoginController lc = (LoginController) mainContainer.getController(MainStecamPiantaggioBoccoleSP1802.loginID);

        root = new Group();
        root.getChildren().addAll(mainContainer);

        this.primaryStage = primaryStage;
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("STECAM Piantaggio Boccole SP1802");
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });

        primaryStage.setOnShown(event -> {
            MainController m = (MainController) mainContainer.getController(MainStecamPiantaggioBoccoleSP1802.mainID);
            lc.setStatusManager(m.getStatusManager());
            lc.setDbService(m.getDBService());
            m.startMainServices();
            mainContainer.setScreen(MainStecamPiantaggioBoccoleSP1802.loginID);
        });


        primaryStage.show();


    }

    private void closeProgram() {
        System.out.println("CHIUDO");
        mainContainer.closeMain(MainStecamPiantaggioBoccoleSP1802.mainID);
        this.primaryStage.close();
    }

    public static void main(String[] args) {

        if (checkSingleRun()) {
            launch(args);
        }
        System.exit(0);
    }

    private static boolean checkSingleRun() {
        try {
            RandomAccessFile randomFile =
                    new RandomAccessFile("single.class", "rw");

            FileChannel channel = randomFile.getChannel();

            if (channel.tryLock() == null) {
                System.out.println("Already Running...");
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return true;
    }
}

