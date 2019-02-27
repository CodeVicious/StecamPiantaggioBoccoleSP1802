package stecamSP1802.controllers;


public class LoggedUser {
    private static LoggedUser ourInstance = new LoggedUser();
    private String nomeoperatore;

    public static LoggedUser getInstance() {
        return ourInstance;
    }

    private LoggedUser() {
    }

    private String Matricola = "";
    private boolean ConduttoreDiLinea = false;
    private boolean loggedIN = false;

    public String getMatricola() {
        return Matricola;
    }

    public boolean isConduttoreDiLinea() {
        return ConduttoreDiLinea;
    }

    public boolean isLoggedIN() {
        return loggedIN;
    }


    public void setMatricola(String matricola) {
        Matricola = matricola;
    }

    public void setConduttoreDiLinea(boolean conduttoreDiLinea) {
        ConduttoreDiLinea = conduttoreDiLinea;
    }

    public void setLoggedIN(boolean loggedIN) {
        this.loggedIN = loggedIN;
    }

    public void setNomeOperatore(String nomeOperatore) {
        this.nomeoperatore = nomeOperatore;
    }

    public String getNomeoperatore() {
        return nomeoperatore;
    }
}
