package com.example.heiznekuehlen;

import MainProgram.Heizen_Kuelen;
import essentail.control;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;

public class HeizKuehlController {
    @FXML
    private Label temp_label;
    @FXML
    private Label lampe_label;
    @FXML
    private Label ventilator_label;
    @FXML
    private Label og_label;
    @FXML
    private Label ug_label;
    @FXML
    private Label fehler_label;
    @FXML
    private Circle lampe_kreis;
    @FXML
    private Circle ventilator_kreis;

    @FXML
    protected void initialize() {
        og_label.setText("Obergrenze :" + Heizen_Kuelen.OG);
        ug_label.setText("Untergrenze : " + Heizen_Kuelen.UG);

        // der Regler läuft in einem eigenen Thread, sonst friert das Fenster ein
        Thread regler = new Thread(this::regeln, "Regler");
        regler.setDaemon(true); // endet, wenn das Fenster geschlossen wird
        regler.start();
    }

    private void regeln() {
        boolean gestartet = false;
        while (true) {
            try {
                if (!gestartet) {
                    Heizen_Kuelen.init();
                    gestartet = true;
                }
                int temp = Heizen_Kuelen.regel_schritt(Heizen_Kuelen.OG, Heizen_Kuelen.UG);
                boolean lampe = control.heater_is_on();
                boolean ventilator = control.fan_is_on();
                // Oberfläche darf nur im JavaFX-Thread geändert werden
                Platform.runLater(() -> anzeigen(temp, lampe, ventilator));
            } catch (IOException e) {
                Platform.runLater(() -> fehler_label.setText(e.getMessage()));
            }
            try {
                Thread.sleep(1000); // abfrage jede sekunde
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void anzeigen(int temp, boolean lampe, boolean ventilator) {
        temp_label.setText(String.valueOf(temp));
        lampe_label.setText(lampe ? "an" : "aus");
        ventilator_label.setText(ventilator ? "an" : "aus");
        // rot = aktiv, weiß = inaktiv
        lampe_kreis.setFill(lampe ? Color.RED : Color.WHITE);
        ventilator_kreis.setFill(ventilator ? Color.RED : Color.WHITE);
        fehler_label.setText("");
    }
}
