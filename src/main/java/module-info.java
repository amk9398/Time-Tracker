module com.aaronkersten.timetracker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.aaronkersten.timetracker to javafx.fxml;
    opens com.aaronkersten.timetracker.controller to javafx.fxml;
    exports com.aaronkersten.timetracker;
    exports com.aaronkersten.timetracker.controller to javafx.fxml;
}