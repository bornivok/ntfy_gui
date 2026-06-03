module borni.top.ntfy_gui {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires static lombok;

    opens borni.top.ntfy_gui to javafx.fxml;
    exports borni.top.ntfy_gui;

    opens borni.top.ntfy_gui.Controller to javafx.fxml;
    exports borni.top.ntfy_gui.Controller;

    opens borni.top.ntfy_gui.Model to com.fasterxml.jackson.databind;
    exports borni.top.ntfy_gui.Model;
}