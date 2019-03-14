package stecamSP1802.controllers;

import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;

public abstract class AbstractController {

    protected MainStecamPiantaggioBoccoleSP1802 main;

    public void setMainApp(MainStecamPiantaggioBoccoleSP1802 main) {
        this.main = main;
    }
}