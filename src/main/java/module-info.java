module source.eticaret {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires javafx.swing;

    opens source.eticaret to javafx.fxml;
    opens source.eticaret.controller to javafx.fxml;
    opens source.eticaret.model to javafx.base;
    exports source.eticaret;
    exports source.eticaret.controller;
}