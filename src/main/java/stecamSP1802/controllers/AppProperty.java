package stecamSP1802.controllers;

import javafx.beans.property.SimpleStringProperty;

public class AppProperty {
    private SimpleStringProperty keyword;
    private SimpleStringProperty value;

    public AppProperty(String keyword, String value) {
        this.keyword = new SimpleStringProperty(keyword);
        this.value = new SimpleStringProperty(value);
    }

    public String getKeyword() {
        return keyword.get();
    }

    public void setKeyword(String keyword) {
        this.keyword.set(keyword);
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public SimpleStringProperty keywordProperty() {
        return keyword;
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }
}
