package stecamSP1802.schedulers;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stecamSP1802.ConfigurationManager;
import stecamSP1802.controllers.MainController;

import java.util.Timer;
import java.util.TimerTask;


public class WatchDog {
    private static Logger Logger = LogManager.getLogger(WatchDog.class);
    private final MainController mainController;
    ConfigurationManager conf = ConfigurationManager.getInstance();
    public Timer logoutTimer;

    public WatchDog(final MainController mainController) {
        Preconditions.checkNotNull(mainController); //It has to be instatiated
        this.mainController = mainController;
    }

    public void scheduleTimer() {
        logoutTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Logger.info("TRY TO LOGOUT");
                mainController.resetLoggedUser();
            }
        };

        logoutTimer.schedule(timerTask, conf.getLogoffTimeout() * 1000);
    }


    public void resetSchedule() {
        logoutTimer.cancel();
        scheduleTimer();
    }

    public void stopSchedule() {
        logoutTimer.cancel();
    }
}