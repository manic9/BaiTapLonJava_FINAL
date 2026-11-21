package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.PrizeAllocation;
import com.esports.manager.model.Team;
import com.esports.manager.model.Tournament;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.ToastManager;
import javafx.beans.property.SimpleObjectProperty;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PrizeController {

    @FXML private Label lblTotalAllocated;
    @FXML private Label lblTotalPaid;
    @FXML private Label lblTotalPending;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<Object> cbTournamentFilter;
    @FXML private ComboBox<String> cbPaidFilter;
    @FXML private TableView<PrizeAllocation> prizeTable;

    @FXML private TableColumn<PrizeAllocation, String> colId;
    @FXML private TableColumn<PrizeAllocation, String> colTournament;
    @FXML private TableColumn<PrizeAllocation, String> colPlacement;
    @FXML private TableColumn<PrizeAllocation, String> colPercent;
    @FXML private TableColumn<PrizeAllocation, String> colAmount;
    @FXML private TableColumn<PrizeAllocation, String> colTeam;
    @FXML private TableColumn<PrizeAllocation, Boolean> colPaid;
    @FXML private TableColumn<PrizeAllocation, Void> colActions;

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<PrizeAllocation> filteredData;
    private final NumberFormat currencyFmt = NumberFormat.getNumberInstance(Locale.US);

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        bindData();
    }

    private void setupFilters() {
        List<Object> trnOptions = new ArrayList<>();
        trnOptions.add("Tất cả giải đấu");
        trnOptions.addAll(ctx.getTournamentService().getAll());
        cbTournamentFilter.setItems(FXCollections.observableArrayList(trnOptions));
        cbTournamentFilter.getSelectionModel().selectFirst();

        cbPaidFilter.setItems(FXCollections.observableArrayList(
                "Tất cả trạng thái", "Đã giải ngân", "Chờ trao giải"
        ));
        cbPaidFilter.getSelectionModel().selectFirst();

        txtSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        cbTournamentFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbPaidFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));

        colTournament.setCellValueFactory(d -> new SimpleStringProperty(
                ctx.getTournamentService().findById(d.getValue().getTournamentId())
                        .map(Tournament::getName).orElse(d.getValue().getTournamentId())
        ));

        colPlacement.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPlacement()));
        colPlacement.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
                }
            }
        });

        colPercent.setCellValueFactory(d -> new SimpleStringProperty(String.format(Locale.US, "%.1f%%", d.getValue().getPercentage())));

        colAmount.setCellValueFactory(d -> {
            String symbol = ctx.getSettings().getCurrencySymbol();
            return new SimpleStringProperty(symbol + currencyFmt.format(d.getValue().getAmount()));
        });
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #00D4FF;");
                }
            }
        });

        colTeam.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTeamId() != null && !d.getValue().getTeamId().isBlank()
                        ? ctx.getTeamService().getTeamNameOrTbd(d.getValue().getTeamId())
                        : "Chưa xác định"
        ));

        colPaid.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().isPaid()));
        colPaid.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean paid, boolean empty) {
                super.updateItem(paid, empty);
                if (empty || paid == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(paid ? "Đã giải ngân" : "Chờ trao giải");
                    badge.getStyleClass().addAll("badge", paid ? "badge-completed" : "badge-upcoming");
                    setGraphic(badge);
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnTogglePaid = new Button();
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox box = new HBox(6, btnTogglePaid, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnTogglePaid.getStyleClass().add("btn-secondary");
                btnTogglePaid.setStyle("-fx-padding: 4 9; -fx-font-size: 11px;");

                btnEdit.setText("✎");
                btnEdit.setStyle("-fx-text-fill: #00D4FF; -fx-font-size: 12px;");
                btnEdit.getStyleClass().add("btn-icon");
                btnEdit.setTooltip(new Tooltip("Chỉnh sửa giải thưởng"));

                btnDelete.setText("🗑");
                btnDelete.setStyle("-fx-text-fill: #F87171; -fx-font-size: 12px;");
                btnDelete.getStyleClass().add("btn-icon");
                btnDelete.setTooltip(new Tooltip("Xóa hạng mục thưởng"));

                btnTogglePaid.setOnAction(e -> {
                    PrizeAllocation p = getTableView().getItems().get(getIndex());
                    p.setPaid(!p.isPaid());
                    ctx.getPrizeService().save(p);
                    prizeTable.refresh();
                    updateSummaryKpis();
                    ToastManager.showSuccess(p.isPaid()
                            ? "Đã xác nhận giải ngân: " + p.getPlacement()
                            : "Đã chuyển về trạng thái chờ trao giải.");
                });

                btnEdit.setOnAction(e -> {
                    PrizeAllocation p = getTableView().getItems().get(getIndex());
                    showPrizeFormDialog(p);
                });

                btnDelete.setOnAction(e -> {
                    PrizeAllocation p = getTableView().getItems().get(getIndex());
                    handleDeletePrize(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PrizeAllocation p = getTableView().getItems().get(getIndex());
                    btnTogglePaid.setText(p.isPaid() ? "Hoàn tác" : "Giải ngân");
                    setGraphic(box);
                }
            }
        });
    }

    private void bindData() {
        filteredData = new FilteredList<>(ctx.getPrizeService().getAll(), p -> true);
        SortedList<PrizeAllocation> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(prizeTable.comparatorProperty());
        prizeTable.setItems(sortedData);
        updateSummaryKpis();
    }

    private void applyFilters() {
        String kw = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        Object selectedTrn = cbTournamentFilter.getValue();
        String paidFilter = cbPaidFilter.getValue();

        filteredData.setPredicate(p -> {
            String teamName = p.getTeamId() != null ? ctx.getTeamService().getTeamNameOrTbd(p.getTeamId()).toLowerCase() : "";
            boolean matchKw = kw.isEmpty()
                    || p.getPlacement().toLowerCase().contains(kw)
                    || p.getId().toLowerCase().contains(kw)
                    || teamName.contains(kw);

            boolean matchTrn = !(selectedTrn instanceof Tournament t)
                    || p.getTournamentId().equals(t.getId());

            boolean matchPaid = paidFilter == null
                    || "Tất cả trạng thái".equals(paidFilter)
                    || ("Đã giải ngân".equals(paidFilter) && p.isPaid())
                    || ("Chờ trao giải".equals(paidFilter) && !p.isPaid());

            return matchKw && matchTrn && matchPaid;
        });

        updateSummaryKpis();
    }

    private void updateSummaryKpis() {
        String symbol = ctx.getSettings().getCurrencySymbol();
        double total = filteredData.stream().mapToDouble(PrizeAllocation::getAmount).sum();
        double paid = filteredData.stream().filter(PrizeAllocation::isPaid).mapToDouble(PrizeAllocation::getAmount).sum();
        double pending = total - paid;

        lblTotalAllocated.setText(symbol + currencyFmt.format(total));
        lblTotalPaid.setText(symbol + currencyFmt.format(paid));
        lblTotalPending.setText(symbol + currencyFmt.format(pending));
    }

    @FXML
    public void handleResetFilters() {
        txtSearch.clear();
        cbTournamentFilter.getSelectionModel().selectFirst();
        cbPaidFilter.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleCreatePrize() {
        showPrizeFormDialog(null);
    }

    private void handleDeletePrize(PrizeAllocation p) {
        if (!ctx.getSettings().isConfirmBeforeDelete() || DialogHelper.confirmDelete(
                "Xóa hạng mục giải thưởng",
                "Bạn có chắc chắn muốn xóa hạng mục \"" + p.getPlacement() + "\" (" + p.getId() + ")?")) {
            if (ctx.getPrizeService().delete(p.getId())) {
                prizeTable.refresh();
                updateSummaryKpis();
                ToastManager.showSuccess("Đã xóa hạng mục giải thưởng!");
            }
        }
    }

    private void showPrizeFormDialog(PrizeAllocation existing) {
        boolean isEdit = existing != null;
        Dialog<PrizeAllocation> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Chỉnh sửa giải thưởng" : "Thêm hạng mục giải thưởng");
        dialog.setHeaderText(isEdit ? "Cập nhật: " + existing.getPlacement() : "Thiết lập mức thưởng mới cho giải đấu");
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType("Lưu giải thưởng", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));

        ComboBox<Tournament> cbTrn = new ComboBox<>(ctx.getTournamentService().getAll());
        cbTrn.setPrefWidth(290);
        if (isEdit) {
            ctx.getTournamentService().findById(existing.getTournamentId()).ifPresent(cbTrn::setValue);
        } else if (!cbTrn.getItems().isEmpty()) {
            cbTrn.getSelectionModel().selectFirst();
        }

        TextField txtPlacement = new TextField(isEdit ? existing.getPlacement() : "Hạng 1 (Vô địch)");
        TextField txtPercent = new TextField(isEdit ? String.valueOf(existing.getPercentage()) : "50");
        TextField txtAmount = new TextField(isEdit ? String.valueOf((int) existing.getAmount()) : "5000");

        // Tự động tính số tiền khi thay đổi tỷ lệ %
        txtPercent.textProperty().addListener((obs, o, n) -> {
            try {
                double pct = Double.parseDouble(n.trim());
                if (cbTrn.getValue() != null && pct >= 0) {
                    double calc = cbTrn.getValue().getPrizePool() * (pct / 100.0);
                    txtAmount.setText(String.valueOf((int) Math.round(calc)));
                }
            } catch (NumberFormatException ignored) {}
        });

        List<Object> teamChoices = new ArrayList<>();
        teamChoices.add("Chưa xác định");
        teamChoices.addAll(ctx.getTeamService().getAll());
        ComboBox<Object> cbTeam = new ComboBox<>(FXCollections.observableArrayList(teamChoices));
        cbTeam.setPrefWidth(290);
        if (isEdit && existing.getTeamId() != null) {
            ctx.getTeamService().findById(existing.getTeamId()).ifPresentOrElse(cbTeam::setValue, () -> cbTeam.getSelectionModel().selectFirst());
        } else {
            cbTeam.getSelectionModel().selectFirst();
        }

        CheckBox chkPaid = new CheckBox("Đã hoàn tất giải ngân tiền thưởng");
        chkPaid.setSelected(isEdit && existing.isPaid());

        grid.add(new Label("Giải đấu *:"), 0, 0);
        grid.add(cbTrn, 1, 0);
        grid.add(new Label("Thứ hạng *:"), 0, 1);
        grid.add(txtPlacement, 1, 1);
        grid.add(new Label("Tỷ lệ (%):"), 0, 2);
        grid.add(txtPercent, 1, 2);
        grid.add(new Label("Số tiền thưởng:"), 0, 3);
        grid.add(txtAmount, 1, 3);
        grid.add(new Label("Đội nhận giải:"), 0, 4);
        grid.add(cbTeam, 1, 4);
        grid.add(chkPaid, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (cbTrn.getValue() == null || txtPlacement.getText().trim().isEmpty()) {
                ToastManager.showError("Vui lòng chọn Giải đấu và nhập tên Thứ hạng!");
                ev.consume();
                return;
            }
            try {
                double pct = Double.parseDouble(txtPercent.getText().trim());
                double amt = Double.parseDouble(txtAmount.getText().trim());
                if (pct < 0 || amt < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                ToastManager.showError("Tỷ lệ phần trăm và giá trị thưởng phải là số hợp lệ (>= 0)!");
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                PrizeAllocation target = isEdit ? existing : new PrizeAllocation();
                target.setTournamentId(cbTrn.getValue().getId());
                target.setPlacement(txtPlacement.getText().trim());
                target.setPercentage(Double.parseDouble(txtPercent.getText().trim()));
                target.setAmount(Double.parseDouble(txtAmount.getText().trim()));
                target.setTeamId(cbTeam.getValue() instanceof Team t ? t.getId() : null);
                target.setPaid(chkPaid.isSelected());
                return target;
            }
            return null;
        });

        Optional<PrizeAllocation> res = dialog.showAndWait();
        res.ifPresent(p -> {
            ctx.getPrizeService().save(p);
            prizeTable.refresh();
            updateSummaryKpis();
            ToastManager.showSuccess(isEdit ? "Đã cập nhật hạng mục thưởng!" : "Đã thêm hạng mục thưởng mới!");
        });
    }
}