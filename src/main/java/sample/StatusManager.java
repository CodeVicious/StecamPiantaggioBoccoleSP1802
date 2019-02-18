package sample;

public class StatusManager {
    private static StatusManager ourInstance = new StatusManager();

    public static StatusManager getInstance() {
        return ourInstance;
    }

    private StatusManager() {
    }
}
