package stecamSP1802.controllers;


import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class TextFieldValidation {
    public static boolean isTextFieldNotEmpy(TextField tf) {
        boolean b = false;
        if (tf.getText().length() != 0 || tf.getText().isEmpty()) {
            b=true;
        }
        return b;
    }

    public static boolean isTextFieldNotEmpty(TextField tf, Label lb, String errorMsg){
        boolean b = true;
        String msg = null;
        if(!isTextFieldNotEmpy(tf)){
            b = false;
            msg = errorMsg;
        }
        lb.setText(errorMsg);
        return b;
    }

    public static boolean isValidCodeProduct(TextField tf){
        boolean b = false;
        if(tf.getText().matches("\\d{8}[A-Z]?")){
            b = true;
        }
        return b;
    }

    public static boolean isValidCodeProduct(TextField tf, Label lb, String errorMsg){
        boolean b = true;
        String msg = null;
        if(!isValidCodeProduct(tf)){
            b = false;
            msg = errorMsg;
        }
        lb.setText(msg);
        return b;
    }
}
