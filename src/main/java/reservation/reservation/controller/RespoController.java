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
    private Label salleFormTitle;

    @FXML
    private TextField salleTypeField;

    @FXML
    private TextField salleCapaciteField;

    @FXML
    private TextField salleNumEtageField;

    @FXML
    private TextField sallePrixField;

    @FXML
    private ComboBox<Etage> salleEtageCombo;

    @FXML
    private CheckBox salleDispoCheck;

    private Salle salleEnEdition;

    @FXML
    public void initialize() {
        respo = (Respo) SessionCon.getUser();
        if (respo == null) {
            showAlert("Erreur", "Aucun utilisateur connecté");
            return;
        }

        initSalleForm();
        refreshSalles();
        refreshReservations();
    }

    // ================= SALLES =================

    @FXML
    public void ajouterSalle() {
        salleEnEdition = null;
        salleFormTitle.setText("Ajouter une salle");
        clearSalleForm();
        salleDispoCheck.setSelected(true);
    }

    @FXML
    public void modifierSalle() {
        Salle salle = getSelectedSalle("Veuillez sélectionner une salle à modifier");
        if (salle == null) return;

        salleEnEdition = salle;
        salleFormTitle.setText("Modifier la salle #" + salle.getId());
        fillSalleFormFromSalle(salle);
    }

    @FXML
    public void enregistrerSalle() {
        Salle salleFromForm = buildSalleFromFormFields();
        if (salleFromForm == null) {
            return;
        }

        if (salleEnEdition == null) {
            salleFromForm.setRespo(respo);
            runSalleAction("Salle ajoutée avec succès", () -> salleDAO.save(salleFromForm));
        } else {
            salleEnEdition.setTypeSalle(salleFromForm.getTypeSalle());
            salleEnEdition.setCapacite(salleFromForm.getCapacite());
            salleEnEdition.setNumEtage(salleFromForm.getNumEtage());
            salleEnEdition.setPrix(salleFromForm.getPrix());
            salleEnEdition.setDispo(salleFromForm.isDispo());
            salleEnEdition.setEtage(salleFromForm.getEtage());
            runSalleAction("Salle modifiée avec succès", () -> salleDAO.update(salleEnEdition));
        }

        annulerEditionSalle();
    }

    @FXML
    public void annulerEditionSalle() {
        salleEnEdition = null;
        salleFormTitle.setText("Formulaire Salle");
        clearSalleForm();
        salleDispoCheck.setSelected(true);
    }

    @FXML
    public void supprimerSalle() {
        Salle salle = getSelectedSalle("Veuillez sélectionner une salle à supprimer");
        if (salle == null) return;

        runSalleAction("Salle supprimée avec succès", () -> salleDAO.delete(salle));
    }

    @FXML
    public void changerDisponibilite() {
        Salle salle = getSelectedSalle("Veuillez sélectionner une salle");
        if (salle == null) return;

        salle.setDispo(!salle.isDispo());
        salleDAO.update(salle);

        tableSalles.refresh();
    }

    // ================= RESERVATIONS =================

    @FXML
    public void validerReservation() {
        updateReservationEtat("validee", true);
    }

    @FXML
    public void refuserReservation() {
        updateReservationEtat("refusee", false);
    }

    // ================= UTIL =================

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void showError(String msg) {
        showAlert("Erreur", msg);
    }

    private void runSalleAction(String successMessage, Runnable action) {
        action.run();
        refreshSalles();
        showAlert("Succès", successMessage);
    }

    private void updateReservationEtat(String etat, boolean checkActifRespo) {
        Reservation reservation = getSelectedReservation("Sélectionnez une réservation");
        if (reservation == null) return;

        if (checkActifRespo && !respo.isActif()) {
            showAlert("Accès refusé", "Responsable désactivé");
            return;
        }

        reservation.setEtat(etat);
        reservationDAO.update(reservation);
        tableReservations.refresh();
    }

    private void refreshSalles() {
        tableSalles.getItems().setAll(salleDAO.getSallesByRespo(respo.getId()));
    }

    private void refreshReservations() {
        tableReservations.getItems().setAll(reservationDAO.getReservationsByRespo(respo.getId()));
    }

    private void initSalleForm() {
        if (salleEtageCombo == null) return;
        salleEtageCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Etage item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : ("Etage #" + item.getId()));
            }
        });
        salleEtageCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Etage item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : ("Etage #" + item.getId()));
            }
        });

        refreshEtagesCombo();
        annulerEditionSalle();
    }

    private void refreshEtagesCombo() {
        if (salleEtageCombo == null) return;
        salleEtageCombo.getItems().setAll(etageDAO.getEtagesByRespo(respo.getId()));
    }

    private void clearSalleForm() {
        if (salleTypeField != null) salleTypeField.clear();
        if (salleCapaciteField != null) salleCapaciteField.clear();
        if (salleNumEtageField != null) salleNumEtageField.clear();
        if (sallePrixField != null) sallePrixField.clear();
        if (salleEtageCombo != null) salleEtageCombo.getSelectionModel().clearSelection();
    }

    private void fillSalleFormFromSalle(Salle salle) {
        salleTypeField.setText(salle.getTypeSalle());
        salleCapaciteField.setText(String.valueOf(salle.getCapacite()));
        salleNumEtageField.setText(String.valueOf(salle.getNumEtage()));
        sallePrixField.setText(String.valueOf(salle.getPrix()));
        salleDispoCheck.setSelected(salle.isDispo());
        refreshEtagesCombo();
        selectEtage(salleEtageCombo, salle.getEtage());
    }

    private Salle buildSalleFromFormFields() {
        String type = salleTypeField.getText();
        Integer capacite = parseInt(salleCapaciteField.getText());
        Integer numEtage = parseInt(salleNumEtageField.getText());
        Double prix = parseDouble(sallePrixField.getText());
        Etage etage = salleEtageCombo.getSelectionModel().getSelectedItem();

        if (type == null || type.trim().isEmpty() || capacite == null || numEtage == null || prix == null || etage == null) {
            showError("Veuillez remplir tous les champs correctement");
            return null;
        }

        return buildSalleFromForm(type, capacite, numEtage, prix, salleDispoCheck.isSelected(), etage);
    }

    private Salle getSelectedSalle(String errorMessage) {
        Salle salle = tableSalles.getSelectionModel().getSelectedItem();
        if (salle == null) {
            showError(errorMessage);
        }
        return salle;
    }

    private Reservation getSelectedReservation(String errorMessage) {
        Reservation reservation = tableReservations.getSelectionModel().getSelectedItem();
        if (reservation == null) {
            showError(errorMessage);
        }
        return reservation;
    }

    private void selectEtage(ComboBox<Etage> combo, Etage selected) {
        if (selected == null) return;
        for (Etage e : combo.getItems()) {
            if (e != null && e.getId() == selected.getId()) {
                combo.getSelectionModel().select(e);
                return;
            }
        }
    }

    private Salle buildSalleFromForm(String type, int capacite, int numEtage, double prix, boolean dispo, Etage etage) {
        Salle salle = new Salle();
        salle.setTypeSalle(type == null ? "" : type.trim());
        salle.setCapacite(capacite);
        salle.setNumEtage(numEtage);
        salle.setPrix(prix);
        salle.setDispo(dispo);
        salle.setEtage(etage);
        return salle;
    }

    private Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private ComboBox<Etage> buildEtageCombo() {
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

        return etageCombo;
    }
}
