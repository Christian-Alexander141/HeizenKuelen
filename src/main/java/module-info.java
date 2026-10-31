module com.example.heiznekuehlen {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.heiznekuehlen to javafx.fxml;
    exports com.example.heiznekuehlen;
}