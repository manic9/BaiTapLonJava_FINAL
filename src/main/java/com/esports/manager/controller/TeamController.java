package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Team;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.ToastManager;
import com.esports.manager.util.ViewNavigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import java.util.Optional;

public class TeamController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbRegionFilter;
    @FXML private Label lblResultCount;
    @FXML private TableView<Team> teamTable;

    @FXML private TableColumn<Team, String> colId;
    @FXML private TableColumn<Team, String> colTag;
    @FXML private TableColumn<Team, String> colName;
    @FXML private TableColumn<Team, String> colRegion;
    @FXML private TableColumn<Team, String> colCoach;
    @FXML private TableColumn<Team, String> colRoster;
    @FXML private TableColumn<Team, String> colFounded;
    @FXML private TableColumn<Team, String> colEmail;
    @FXML private TableColumn<Team, Void> colActions;

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<Team> filteredData;

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        bindData();
    }

    private void setupFilters() {
        cbRegionFilter.setItems(FXCollections.observableArrayList(
                "Tất cả khu vực", "Vietnam", "SEA", "Korea", "China", "International"
        ));
        cbRegionFilter.getSelectionModel().selectFirst();

        txtSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        cbRegionFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));

        colTag.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTag()));
        colTag.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String tag, boolean empty) {
                super.updateItem(tag, empty);
                if (empty || tag == null) {
                    setGraphic(null);
                } else {
                    Team team = getTableView().getItems().get(getIndex());
                    String bg = team.getAccentColor() != null ? team.getAccentColor() : "#7C5CFC";
                    Label pill = new Label(tag);
                    pill.setMinWidth(48);
                    pill.setAlignment(Pos.CENTER);
                    pill.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: #0B0E14; " +
                            "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 6px;");
                    setGraphic(pill);
                }
            }
        });

        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                } else {
                    setText(name);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
                }
            }
        });

        colRegion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRegion()));
        colCoach.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCoach() != null && !d.getValue().getCoach().isBlank() ? d.getValue().getCoach() : "-"
        ));

        colRoster.setCellValueFactory(d -> {
            int count = ctx.getTeamService().getRoster(d.getValue().getId()).size();
            return new SimpleStringProperty(count + " tuyển thủ");
        });
        colRoster.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #00D4FF; -fx-font-weight: 600;");
                }
            }
        });

        colFounded.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getFoundedYear())));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContactEmail()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnRoster = new Button("Roster");
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox box = new HBox(6, btnRoster, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnRoster.getStyleClass().add("btn-secondary");
                btnRoster.setStyle("-fx-padding: 5 10; -fx-font-size: 11.5px;");
                btnRoster.setTooltip(new Tooltip("Xem danh sách tuyển thủ của đội"));

                btnEdit.setText("✎");
                btnEdit.setStyle("-fx-text-fill: #00D4FF; -fx-font-size: 13px;");
                btnEdit.getStyleClass().add("btn-icon");
                btnEdit.setTooltip(new Tooltip("Chỉnh sửa đội tuyển"));

                btnDelete.setText("🗑");
                btnDelete.setStyle("-fx-text-fill: #F87171; -fx-font-size: 13px;");
                btnDelete.getStyleClass().add("btn-icon");
                btnDelete.setTooltip(new Tooltip("Xóa đội tuyển"));

                btnRoster.setOnAction(e -> {
                    Team team = getTableView().getItems().get(getIndex());
                    ViewNavigator.<PlayerController>navigate(
                            "PlayerView.fxml",
                            "Player Management",
                            "Đội hình thi đấu (Roster) — " + team.getName(),
                            ctrl -> ctrl.filterByTeam(team.getId())
                    );
                });

                btnEdit.setOnAction(e -> {
                    Team team = getTableView().getItems().get(getIndex());
                    showTeamFormDialog(team);
                });

                btnDelete.setOnAction(e -> {
                    Team team = getTableView().getItems().get(getIndex());
                    handleDeleteTeam(team);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void bindData() {
        filteredData = new FilteredList<>(ctx.getTeamService().getAll(), p -> true);
        SortedList<Team> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(teamTable.comparatorProperty());
        teamTable.setItems(sortedData);
        updateCountLabel();
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String region = cbRegionFilter.getValue();

        filteredData.setPredicate(t -> {
            boolean matchKeyword = keyword.isEmpty()
                    || t.getName().toLowerCase().contains(keyword)
                    || t.getTag().toLowerCase().contains(keyword)
                    || t.getId().toLowerCase().contains(keyword)
                    || (t.getCoach() != null && t.getCoach().toLowerCase().contains(keyword));

            boolean matchRegion = region == null
                    || "Tất cả khu vực".equals(region)
                    || (t.getRegion() != null && t.getRegion().equalsIgnoreCase(region));

            return matchKeyword && matchRegion;
        });

        updateCountLabel();
    }

    private void updateCountLabel() {
        lblResultCount.setText("Hiển thị " + filteredData.size() + " đội tuyển");
    }

    @FXML
    public void handleResetFilters() {
        txtSearch.clear();
        cbRegionFilter.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleCreateTeam() {
        showTeamFormDialog(null);
    }

    private void handleDeleteTeam(Team team) {
        if (!ctx.getSettings().isConfirmBeforeDelete() || DialogHelper.confirmDelete(
                "Xóa đội tuyển",
                "Bạn có chắc chắn muốn xóa đội tuyển \"" + team.getName() + "\" ([" + team.getTag() + "])?")) {
            if (ctx.getTeamService().delete(team.getId())) {
                teamTable.refresh();
                updateCountLabel();
                ToastManager.showSuccess("Đã xóa đội tuyển: " + team.getName());
            } else {
                ToastManager.showError("Không thể xóa đội tuyển này.");
            }
        }
    }

    private void showTeamFormDialog(Team existing) {
        boolean isEdit = existing != null;
        Dialog<Team> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Chỉnh sửa đội tuyển" : "Thêm đội tuyển mới");
        dialog.setHeaderText(isEdit ? "Cập nhật hồ sơ: " + existing.getName() : "Đăng ký hồ sơ đội tuyển Esports mới");
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType(isEdit ? "Lưu thay đổi" : "Thêm đội tuyển", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));

        TextField txtName = new TextField(isEdit ? existing.getName() : "");
        txtName.setPromptText("VD: T1 Esports");
        txtName.setPrefWidth(290);

        TextField txtTag = new TextField(isEdit ? existing.getTag() : "");
        txtTag.setPromptText("VD: T1 (tối đa 5 ký tự)");

        ComboBox<String> cbRegion = new ComboBox<>(FXCollections.observableArrayList(
                "Vietnam", "SEA", "Korea", "China", "North America", "Europe", "International"
        ));
        cbRegion.setEditable(true);
        cbRegion.setValue(isEdit ? existing.getRegion() : "Vietnam");
        cbRegion.setMaxWidth(Double.MAX_VALUE);

        TextField txtCoach = new TextField(isEdit ? existing.getCoach() : "");
        txtCoach.setPromptText("Tên HLV trưởng");

        TextField txtEmail = new TextField(isEdit ? existing.getContactEmail() : "");
        txtEmail.setPromptText("manager@team.gg");

        Spinner<Integer> spFounded = new Spinner<>(1998, 2026, isEdit ? existing.getFoundedYear() : 2024);
        spFounded.setEditable(true);
        spFounded.setMaxWidth(Double.MAX_VALUE);

        Color initialColor = Color.web("#7C5CFC");
        if (isEdit && existing.getAccentColor() != null) {
            try {
                initialColor = Color.web(existing.getAccentColor());
            } catch (Exception ignored) {}
        }
        ColorPicker colorPicker = new ColorPicker(initialColor);
        colorPicker.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Tên đội tuyển *:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Tag viết tắt *:"), 0, 1);
        grid.add(txtTag, 1, 1);
        grid.add(new Label("Khu vực:"), 0, 2);
        grid.add(cbRegion, 1, 2);
        grid.add(new Label("Huấn luyện viên:"), 0, 3);
        grid.add(txtCoach, 1, 3);
        grid.add(new Label("Email liên hệ:"), 0, 4);
        grid.add(txtEmail, 1, 4);
        grid.add(new Label("Năm thành lập:"), 0, 5);
        grid.add(spFounded, 1, 5);
        grid.add(new Label("Màu nhận diện:"), 0, 6);
        grid.add(colorPicker, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (txtName.getText().trim().isEmpty() || txtTag.getText().trim().isEmpty()) {
                ToastManager.showError("Vui lòng nhập đầy đủ Tên đội tuyển và Tag!");
                ev.consume();
                return;
            }
            if (txtTag.getText().trim().length() > 6) {
                ToastManager.showError("Tag đội tuyển không được vượt quá 6 ký tự!");
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                Team target = isEdit ? existing : new Team();
                target.setName(txtName.getText().trim());
                target.setTag(txtTag.getText().trim().toUpperCase());
                target.setRegion(cbRegion.getValue() != null ? cbRegion.getValue().trim() : "Vietnam");
                target.setCoach(txtCoach.getText().trim());
                target.setContactEmail(txtEmail.getText().trim());
                target.setFoundedYear(spFounded.getValue());
                target.setAccentColor(toHex(colorPicker.getValue()));
                return target;
            }
            return null;
        });

        Optional<Team> res = dialog.showAndWait();
        res.ifPresent(team -> {
            ctx.getTeamService().save(team, !isEdit);
            teamTable.refresh();
            updateCountLabel();
            ToastManager.showSuccess((isEdit ? "Đã cập nhật đội tuyển: " : "Đã thêm đội tuyển: ") + team.getName());
        });
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }
}