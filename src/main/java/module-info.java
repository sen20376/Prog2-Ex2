module at.ac.fhcampuswien.fhmdb {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.jfoenix;
    requires com.google.gson;
    requires okhttp3;

    opens at.ac.fhcampuswien.fhmdb to javafx.fxml;
    exports at.ac.fhcampuswien.fhmdb.models;
    exports at.ac.fhcampuswien.fhmdb;
    opens at.ac.fhcampuswien.fhmdb.models to com.google.gson;

}