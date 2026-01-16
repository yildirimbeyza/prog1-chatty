module at.ac.hcw.chatty {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    exports at.ac.hcw.chatty;

    opens at.ac.hcw.chatty to javafx.fxml;
    opens at.ac.hcw.chatty.controller to javafx.fxml;
}
