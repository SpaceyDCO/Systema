package com.tamv.systema.frontend;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.Utils.General;
import com.tamv.systema.frontend.model.Customer;
import com.tamv.systema.frontend.model.RepairOrder;
import com.tamv.systema.frontend.model.Status;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import lombok.Setter;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class RepairOrderFormController {
    @FXML
    public ComboBox<Status> statusComboBox;
    @FXML
    public ComboBox<Customer> customerComboBox;
    @FXML
    public Label titleLabel;
    @FXML
    public Button deleteButton;
    @FXML
    public TextField equipmentField;
    @FXML
    public TextField serialNumberField;
    @FXML
    public TextArea issueField;
    @FXML
    public TextArea notesField;
    @FXML
    public Label errorLabel;
    @FXML
    public Button generateRepairView;
    private RepairOrder order;
    private final ApiService api;
    @Setter
    private StackPane contentArea;
    private List<Customer> allCustomers;
    public RepairOrderFormController(ApiService api) {
        this.api = api;
    }
    @FXML
    public void initialize() {
        loadStatuses();
        loadCustomers();
    }
    @FXML
    public void onBack() {
        goBack();
    }
    @FXML
    public void onSave() {
        if(!validateFields()) return;
        Customer customer = this.customerComboBox.getValue();
        String equipmentDescription = this.equipmentField.getText();
        String serialNumber = this.serialNumberField.getText();
        String issueDescription = this.issueField.getText();
        Status status = this.statusComboBox.getValue();
        String technicianNotes = this.notesField.getText();
        new Thread(() -> {
            try {
                boolean success;
                if(order == null || order.getId() == null)
                    success = api.createRepairOrder(customer.getId(), equipmentDescription, serialNumber, issueDescription) != null;
                else success = api.updateRepairOrderStatusAndNotes(order.getId(), status.getId(), technicianNotes);
                Platform.runLater(() -> {
                    if(success) goBack();
                    else General.showError(this.errorLabel, "Failed to save repairOrder, please try again or contact an admin.");
                });
            }catch (Exception e) {
                General.showError(this.errorLabel, "There has been an error while trying to save the Repair Order. Please contact an admin.");
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    public void onDelete() {
        if(order == null || order.getId() == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Repair Order");
        alert.setHeaderText("Are you sure you want to delete this repair order?");
        alert.setContentText("Order for: " + order.getCustomerName() + "\nID: " + order.getId() + "\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                boolean success = api.deleteRepairOrder(order.getId());
                Platform.runLater(() -> {
                    if (success) goBack();
                    else General.showError(this.errorLabel, "Failed to delete invoice. Please try again.");
                });
            }).start();
        }
    }
    @FXML
    public void onGenerateRepairPdf() {
        if(this.order == null) {
            General.showError(this.errorLabel, "Invalid repair order ID, it must be saved first.");
            return;
        }
        String html = buildRepairOrderHtml(this.order);
        WebView webView = new WebView();
        webView.getEngine().loadContent(html, "text/html");
        ButtonType exportPdf = new ButtonType("Export to PDF", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Repair order preview");
        dialog.getDialogPane().getButtonTypes().addAll(exportPdf, cancel);
        dialog.getDialogPane().setContent(webView);
        dialog.setResizable(true);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth() * 0.40;
        double height = screenBounds.getHeight() * 0.80;
        dialog.getDialogPane().setPrefSize(width, height);
        dialog.showAndWait().ifPresent(button -> {
            if(button == exportPdf) {
                exportRepairOrderPdf(html);
            }
        });
    }
    private String buildRepairOrderHtml(RepairOrder order) {
        String customerName = order.getCustomerName();
        String customerEmail = order.getCustomer().getEmail() == null ? "N/A" : order.getCustomer().getEmail();
        String customerPhone = order.getCustomer().getPhoneNumber();
        String equipment = order.getEquipmentName();
        String serial = (order.getSerialNumber() != null) ? order.getSerialNumber() : "N/A";
        String issue = order.getReportedIssue();
        String status = order.getStatusName();
        String id = String.valueOf(order.getId());
        String date = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        );
        String logoDataUri = getLogoDataUri();
        String html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8" />
            <style>
                body {\s
                    font-family: Georgia, 'Times New Roman', serif;
                    margin: 20px;\s
                    color: #333;
                    line-height: 1.6;
                }
                .header-table {
                    width: 100%;
                    border-bottom: 2px solid #48c9a8;
                    margin-bottom: 30px;
                    padding-bottom: 10px;
                }
                .logo-cell {
                    width: 120px;
                    vertical-align: middle;
                }
                .logo {
                    width: 120px;
                    height: auto;
                    display: block;
                }
                .title-cell {
                    text-align: center;
                    vertical-align: middle;
                    padding-bottom: 10px;
                }
                .title-text {
                     margin: 0;
                     font-size: 32px;
                     color: #2d3748;
                     text-transform: uppercase;
                     letter-spacing: 2px;
                     line-height: 1.2;
                }
                .order-info {
                    font-size: 14px;
                    color: #666;
                    margin-top: 5px;
                }
                .order-info {
                    margin-top: 10px;
                    font-size: 14px;
                    color: #666;
                }
                .section-title {
                    font-size: 18px;
                    font-weight: bold;
                    color: #48c9a8;
                    border-bottom: 1px solid #eee;
                    margin-top: 30px;
                    margin-bottom: 15px;
                    padding-bottom: 5px;
                }
                table { width: 100%; border-collapse: collapse; }
                td { vertical-align: top; padding: 8px 0; }
                .label {\s
                    font-weight: bold;\s
                    color: #555;
                    width: 150px;
                }
                .footer {
                    margin-top: 50px;
                    text-align: center;
                    font-size: 12px;
                    color: #aaa;
                    border-top: 1px solid #eee;
                    padding-top: 20px;
                }
            </style>
        </head>
        <body>
            <table class="header-table">
                <tr>
                    <td class="logo-cell">
                        <img class="logo" src="${LOGO_DATA_URI}" alt="Logo" />
                    </td>
                        <td class="title-cell">
                            <h1 class="title-text">Repair Order</h1>
                            <div class="order-info">Order #""" + id + " " + """
                            • Date:\s""" + date + """
                            </div>
                        </td>
                    <td style="width: 120px;"></td>\s
                </tr>
            </table>
            <div class="section-title">CUSTOMER INFORMATION</div>
            <table>
                <tr>
                    <td class="label">Customer Name:</td>
                    <td>""" + customerName + """
                </td>
                </tr>
                <tr>
                    <td class="label">Phone:</td>
                    <td>""" + customerPhone + """
                </td>
                </tr>
                <tr>
                    <td class="label">Email:</td>
                    <td>""" + customerEmail + """
                </td>
                </tr>
            </table>
            <div class="section-title">EQUIPMENT DETAILS</div>
            <table>
                <tr>
                    <td class="label">Equipment:</td>
                    <td>""" + equipment + """
                </td>
                </tr>
                <tr>
                    <td class="label">Serial Number:</td>
                    <td>""" + serial + """
                </td>
                </tr>
            </table>
            <div class="section-title">SERVICE INFORMATION</div>
            <table>
                <tr>
                    <td class="label">Reported Issue:</td>
                    <td>""" + issue + """
                </td>
                </tr>
                <tr>
                    <td class="label">Current Status:</td>
                    <td><strong>""" + status + """
                </strong></td>
                </tr>
            </table>
            <div style="margin-top: 60px;">
                <table style="width: 100%;">
                    <tr>
                        <td style="width: 50%; padding-right: 40px;">
                            <div style="border-bottom: 1px solid #000; height: 40px;"></div>
                            <div style="font-size: 12px; margin-top: 5px; text-align: center;">Customer Signature</div>
                        </td>
                        <td style="width: 50%; padding-left: 40px;">
                            <div style="border-bottom: 1px solid #000; height: 40px;"></div>
                            <div style="font-size: 12px; margin-top: 5px; text-align: center;">Technician Signature</div>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <p>Thank you for your business!</p>
                <p>Generated by Systema</p>
            </div>
        </body>
        </html>
        \s""";
        return html.replace("${LOGO_DATA_URI}", logoDataUri);
    }
    private String getLogoDataUri() {
        try(InputStream input = getClass().getResourceAsStream("/com/tamv/systema/frontend/logo.png")) {
            if(input == null) return "";
            byte[] bytes = input.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return "data:image/png;base64," + base64;
        }catch(Exception e) {
            return "";
        }
    }
    private void exportRepairOrderPdf(String html) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Repair Order PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("RepairOrder_" + this.order.getId() + ".pdf");
        File target = fileChooser.showSaveDialog(titleLabel.getScene().getWindow());
        if(target == null) return;
        new Thread(() -> {
            try(OutputStream os = new FileOutputStream(target)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();
                Platform.runLater(() -> {
                    showSuccess("PDF saved successfully:\n" + target.getAbsolutePath());
                });
            }catch(Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> General.showError(this.errorLabel, "Failed to generate PDF"));
            }
        }).start();
    }
    private void showSuccess(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, text);
        alert.showAndWait();
    }
    protected void setRepairOrder(RepairOrder order) {
        this.order = order;
        if(order == null) {
            this.titleLabel.setText("New Repair Order");
            this.deleteButton.setVisible(false);
            this.deleteButton.setManaged(false);
            this.statusComboBox.setDisable(true);
            this.generateRepairView.setDisable(true);
        }else {
            this.titleLabel.setText("Edit Repair Order #" + this.order.getId());
            populateFields();
            this.equipmentField.setDisable(true);
            this.serialNumberField.setDisable(true);
            this.issueField.setDisable(true);
            this.customerComboBox.setDisable(true);
        }
    }
    private boolean validateFields() {
        Customer customer = this.customerComboBox.getValue();
        String equipmentDescription = this.equipmentField.getText();
        String issueDescription = this.issueField.getText();
        if(customer == null) {
            General.showError(this.errorLabel, "Customer is required.");
            return false;
        }
        if(equipmentDescription.trim().isEmpty()) {
            General.showError(this.errorLabel, "Equipment description is required.");
            return false;
        }
        if(issueDescription.trim().isEmpty()) {
            General.showError(this.errorLabel, "Issue description is required.");
            return false;
        }
        this.errorLabel.setText("");
        this.errorLabel.setVisible(false);
        this.errorLabel.setManaged(false);
        return true;
    }
    private void populateFields() {
        this.equipmentField.setText(this.order.getEquipmentName());
        this.serialNumberField.setText(this.order.getSerialNumber());
        this.issueField.setText(this.order.getReportedIssue());
        if(this.order.getTechnicianNotes() != null && !this.order.getTechnicianNotes().isEmpty()) this.notesField.setText(this.order.getTechnicianNotes());
        this.statusComboBox.setValue(this.order.getStatus());
    }
    private void loadCustomers() {
        new Thread(() -> {
            allCustomers = api.getCustomers();
            ObservableList<Customer> observableList = FXCollections.observableArrayList(allCustomers);
            Platform.runLater(() -> {
                this.customerComboBox.setItems(observableList);
                if(order != null && order.getCustomer() != null) this.customerComboBox.setValue(order.getCustomer());
            });
        }).start();
    }
    private void loadStatuses() {
        new Thread(() -> {
            List<Status> statuses = api.getStatuses();
            List<Status> repairStatuses = statuses.stream()
                    .filter(s -> "REPAIR_ORDER".equals(s.getType()))
                    .toList();
            ObservableList<Status> observableList = FXCollections.observableArrayList(repairStatuses);
            Platform.runLater(() -> this.statusComboBox.setItems(observableList));
        }).start();
    }
//    private void loadStatuses() {
//        General.loadEntities(api::getStatuses, entityList -> {
//            ObservableList<Status> observableList = FXCollections.observableArrayList(entityList);
//            this.statusComboBox.setItems(observableList);
//        });
//    }
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/repair-order-view.fxml"));
            loader.setControllerFactory(controllerClass -> new RepairOrderViewController(this.api, this.contentArea));
            Parent repairView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(repairView);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
