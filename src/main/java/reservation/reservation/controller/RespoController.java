package reservation.reservation.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import reservation.reservation.dao.EtageDAO;
import reservation.reservation.dao.ReservationDAO;
import reservation.reservation.dao.SalleDAO;
import reservation.reservation.model.Etage;
import reservation.reservation.model.Reservation;
import reservation.reservation.model.Respo;
import reservation.reservation.model.Salle;
import reservation.reservation.util.SessionCon;

import java.util.List;

public class RespoController {

    private Respo respo;

    private final SalleDAO salleDAO = new SalleDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final EtageDAO etageDAO = new EtageDAO();

    @FXML
    private TableView<Salle> tableSalles;

    @FXML
    private TableView<Reservation> tableReservations;

    @FXML
    public void initialize() {
        try {
            System.out.println("initialize RespoController called");
            respo = (Respo) SessionCon.getUser();
            System.out.println("Respo ID: " + respo.getId());
            int nbSalles = salleDAO.getSallesByRespo(respo.getId()).size();
            System.out.println("Nb salles: " + nbSalles);

            tableSalles.getItems().setAll(
                    salleDAO.getSallesByRespo(respo.getId())
            );

            tableReservations.getItems().setAll(
                    reservationDAO.getReservationsByRespo(respo.getId())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SALLES =================

    @FXML
    public void ajouterSalle() {
        Dialog<Salle> dialog = buildSalleDialog(null);
        dialog.showAndWait().ifPresent(salle -> {
            salle.setRespo(respo);
            salleDAO.save(salle);
            tableSalles.getItems().setAll(salleDAO.getSallesByRespo(respo.getId()));
            showAlert("Succès", "Salle ajoutée avec succès");
        });
    }

    @FXML
    public void modifierSalle() {
        Salle salle = tableSalles.getSelectionModel().getSelectedItem();
        if (salle == null) {
            showAlert("Erreur", "Veuillez sélectionner une salle à modifier");
            return;
        }
        Dialog<Salle> dialog = buildSalleDialog(salle);
        dialog.showAndWait().ifPresent(updated -> {
            salle.setTypeSalle(updated.getTypeSalle());
            salle.setCapacite(updated.getCapacite());
            salle.setNumEtage(updated.getNumEtage());
            salle.setPrix(updated.getPrix());
            salle.setDispo(updated.isDispo());
            salle.setEtage(updated.getEtage());
            salleDAO.update(salle);
            tableSalles.getItems().setAll(salleDAO.getSallesByRespo(respo.getId()));
            showAlert("Succès", "Salle modifiée avec succès");
        });
    }

    @FXML
    public void supprimerSalle() {
        Salle salle = tableSalles.getSelectionModel().getSelectedItem();
        if (salle == null) {
            showAlert("Erreur", "Veuillez sélectionner une salle à supprimer");
            return;
        }
        salleDAO.delete(salle);
        tableSalles.getItems().setAll(salleDAO.getSallesByRespo(respo.getId()));
        showAlert("Succès", "Salle supprimée avec succès");
    }

    @FXML
    public void changerDisponibilite() {
        Salle salle = tableSalles.getSelectionModel().getSelectedItem();

        if (salle == null) {
            showAlert("Erreur", "Veuillez sélectionner une salle");
            return;
        }

        salle.setDispo(!salle.isDispo());
        salleDAO.update(salle);

        tableSalles.refresh();
    }

    // ================= RESERVATIONS =================

    @FXML
    public void validerReservation() {
        Reservation reservation = tableReservations.getSelectionModel().getSelectedItem();

        if (reservation == null) {
            showAlert("Erreur", "Sélectionnez une réservation");
            return;
        }

        if (!respo.isActif()) {
            showAlert("Accès refusé", "Responsable désactivé");
            return;
        }

        reservation.setEtat("validee");
        reservationDAO.update(reservation);

        tableReservations.refresh();
    }

    @FXML
    public void refuserReservation() {
        Reservation reservation = tableReservations.getSelectionModel().getSelectedItem();

        if (reservation == null) {
            showAlert("Erreur", "Sélectionnez une réservation");
            return;
        }

        reservation.setEtat("refusee");
        reservationDAO.update(reservation);

        tableReservations.refresh();
    }

    // ================= UTIL =================

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private Dialog<Salle> buildSalleDialog(Salle existing) {
        Dialog<Salle> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter une salle" : "Modifier une salle");

        ButtonType saveButtonType = new ButtonType(existing == null ? "Ajouter" : "Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField typeField = new TextField();
        TextField capaciteField = new TextField();
        TextField numEtageField = new TextField();
        TextField prixField = new TextField();
        CheckBox dispoCheck = new CheckBox("Disponible");

        List<Etage> etages = etageDAO.getEtagesByRespo(respo.getId());
        ComboBox<Etage> etageCombo = new ComboBox<>();
        etageCombo.getItems().setAll(etages);
        etageCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Etage item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : ("Etage #" + item.getId()));
            }
        });
        etageCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Etage item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : ("Etage #" + item.getId()));
            }
        });

        if (existing != null) {
            typeField.setText(existing.getTypeSalle());
            capaciteField.setText(String.valueOf(existing.getCapacite()));
            numEtageField.setText(String.valueOf(existing.getNumEtage()));
            prixField.setText(String.valueOf(existing.getPrix()));
            dispoCheck.setSelected(existing.isDispo());
            if (existing.getEtage() != null) {
                for (Etage e : etageCombo.getItems()) {
                    if (e != null && e.getId() == existing.getEtage().getId()) {
                        etageCombo.getSelectionModel().select(e);
                        break;
                    }
                }
            }
        } else {
            dispoCheck.setSelected(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Type"), 0, 0);
        grid.add(typeField, 1, 0);
        grid.add(new Label("Capacité"), 0, 1);
        grid.add(capaciteField, 1, 1);
        grid.add(new Label("Num étage"), 0, 2);
        grid.add(numEtageField, 1, 2);
        grid.add(new Label("Prix"), 0, 3);
        grid.add(prixField, 1, 3);
        grid.add(new Label("Étage (FK)"), 0, 4);
        grid.add(etageCombo, 1, 4);
        grid.add(dispoCheck, 1, 5);

        GridPane.setHgrow(typeField, Priority.ALWAYS);
        GridPane.setHgrow(capaciteField, Priority.ALWAYS);
        GridPane.setHgrow(numEtageField, Priority.ALWAYS);
        GridPane.setHgrow(prixField, Priority.ALWAYS);
        GridPane.setHgrow(etageCombo, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.disableProperty().bind(
                typeField.textProperty().isEmpty()
                        .or(capaciteField.textProperty().isEmpty())
                        .or(numEtageField.textProperty().isEmpty())
                        .or(prixField.textProperty().isEmpty())
                        .or(etageCombo.getSelectionModel().selectedItemProperty().isNull())
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != saveButtonType) {
                return null;
            }
            try {
                int capacite = Integer.parseInt(capaciteField.getText().trim());
                int numEtage = Integer.parseInt(numEtageField.getText().trim());
                double prix = Double.parseDouble(prixField.getText().trim());
                Etage etage = etageCombo.getSelectionModel().getSelectedItem();

                Salle salle = new Salle();
                salle.setTypeSalle(typeField.getText().trim());
                salle.setCapacite(capacite);
                salle.setNumEtage(numEtage);
                salle.setPrix(prix);
                salle.setDispo(dispoCheck.isSelected());
                salle.setEtage(etage);
                return salle;
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Capacité, numéro d'étage et prix doivent être des nombres valides");
                return null;
            }
        });

        return dialog;
    }
}
