package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.PrizeAllocation;
import com.esports.manager.model.Team;
import com.esports.manager.model.Tournament;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.I18n;
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

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<PrizeAllocation> filteredData;
    private final NumberFormat currencyFmt = NumberFormat.getNumberInstance(Locale.US);

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        setupRowContextMenu();
        bindData();
    }

    private void setupFilters() {
        List<Object> trnOptions = new ArrayList<>();
        trnOptions.add(I18n.get("filter.all_tournaments"));
        trnOptions.addAll(ctx.getTournamentService().getAll());
        cbTournamentFilter.setItems(FXCollections.observableArrayList(trnOptions));
        cbTournamentFilter.getSelectionModel().selectFirst();

        cbPaidFilter.setItems(FXCollections.observableArrayList(
                I18n.get("prize.filter.all_statuses"), I18n.get("prize.filter.paid"), I18n.get("prize.filter.pending")
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
                        : I18n.get("prize.unassigned")
        ));

        colPaid.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().isPaid()));
        colPaid.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean paid, boolean empty) {
                super.updateItem(paid, empty);
                if (empty || paid == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(paid ? I18n.get("prize.paid") : I18n.get("prize.pending"));
                    badge.getStyleClass().addAll("badge", paid ? "badge-completed" : "badge-upcoming");
                    setGraphic(badge);
                }
            }
        });

    }

    private void setupRowContextMenu() {
        MenuItem miTogglePaid = new MenuItem();
        MenuItem miEdit = new MenuItem(I18n.get("prize.edit_tooltip"));
        MenuItem miDelete = new MenuItem(I18n.get("prize.delete_tooltip"));

        ContextMenu rowMenu = new ContextMenu(miTogglePaid, new SeparatorMenuItem(), miEdit, miDelete);
        rowMenu.getStyleClass().add("context-menu-dark");

        miTogglePaid.setOnAction(e -> {
            PrizeAllocation p = prizeTable.getSelectionModel().getSelectedItem();
            if (p != null) {
                p.setPaid(!p.isPaid());
                ctx.getPrizeService().save(p);
                prizeTable.refresh();
                updateSummaryKpis();
                ToastManager.showSuccess(p.isPaid()
                        ? String.format(I18n.get("prize.toast.disbursed"), p.getPlacement())
                        : I18n.get("prize.toast.reverted"));
            }
        });

        miEdit.setOnAction(e -> {
            PrizeAllocation p = prizeTable.getSelectionModel().getSelectedItem();
            if (p != null) showPrizeFormDialog(p);
        });

        miDelete.setOnAction(e -> {
            PrizeAllocation p = prizeTable.getSelectionModel().getSelectedItem();
            if (p != null) handleDeletePrize(p);
        });

        prizeTable.setRowFactory(tv -> {
            TableRow<PrizeAllocation> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    PrizeAllocation p = row.getItem();
                    miTogglePaid.setText(p.isPaid() ? I18n.get("prize.revoke") : I18n.get("prize.disburse"));
                    rowMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            row.setOnMouseClicked(event -> {
                if (event.isPrimaryButtonDown() && !event.isShiftDown() && !event.isControlDown()) {
                    rowMenu.hide();
                }
            });
            return row;
        });

        prizeTable.setOnMouseClicked(event -> {
            if (event.isPrimaryButtonDown()) {
                rowMenu.hide();
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
                    || I18n.get("prize.filter.all_statuses").equals(paidFilter)
                    || (I18n.get("prize.filter.paid").equals(paidFilter) && p.isPaid())
                    || (I18n.get("prize.filter.pending").equals(paidFilter) && !p.isPaid());

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
                I18n.get("prize.delete.title"),
                String.format(I18n.get("prize.delete.message"), p.getPlacement(), p.getId()))) {
            if (ctx.getPrizeService().delete(p.getId())) {
                prizeTable.refresh();
                updateSummaryKpis();
                ToastManager.showSuccess(I18n.get("prize.toast.deleted"));
            }
        }
    }

    private void showPrizeFormDialog(PrizeAllocation existing) {
        boolean isEdit = existing != null;
        Dialog<PrizeAllocation> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? I18n.get("prize.dialog.edit_title") : I18n.get("prize.dialog.create_title"));
        dialog.setHeaderText(isEdit ? I18n.get("prize.dialog.edit_header") + ": " + existing.getPlacement() : I18n.get("prize.dialog.create_header"));
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType(I18n.get("prize.dialog.save_btn"), ButtonBar.ButtonData.OK_DONE);
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

        TextField txtPlacement = new TextField(isEdit ? existing.getPlacement() : I18n.get("tour.placeone.title"));
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
        teamChoices.add(I18n.get("prize.unassigned"));
        teamChoices.addAll(ctx.getTeamService().getAll());
        ComboBox<Object> cbTeam = new ComboBox<>(FXCollections.observableArrayList(teamChoices));
        cbTeam.setPrefWidth(290);
        if (isEdit && existing.getTeamId() != null) {
            ctx.getTeamService().findById(existing.getTeamId()).ifPresentOrElse(cbTeam::setValue, () -> cbTeam.getSelectionModel().selectFirst());
        } else {
            cbTeam.getSelectionModel().selectFirst();
        }

        CheckBox chkPaid = new CheckBox(I18n.get("prize.dialog.paid_checkbox"));
        chkPaid.setSelected(isEdit && existing.isPaid());

        grid.add(new Label(I18n.get("prize.dialog.tournament_label")), 0, 0);
        grid.add(cbTrn, 1, 0);
        grid.add(new Label(I18n.get("prize.dialog.placement_label")), 0, 1);
        grid.add(txtPlacement, 1, 1);
        grid.add(new Label(I18n.get("prize.dialog.percent_label")), 0, 2);
        grid.add(txtPercent, 1, 2);
        grid.add(new Label(I18n.get("prize.dialog.amount_label")), 0, 3);
        grid.add(txtAmount, 1, 3);
        grid.add(new Label(I18n.get("prize.dialog.team_label")), 0, 4);
        grid.add(cbTeam, 1, 4);
        grid.add(chkPaid, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (cbTrn.getValue() == null || txtPlacement.getText().trim().isEmpty()) {
                ToastManager.showError(I18n.get("prize.dialog.validation_error"));
                ev.consume();
                return;
            }
            try {
                double pct = Double.parseDouble(txtPercent.getText().trim());
                double amt = Double.parseDouble(txtAmount.getText().trim());
                if (pct < 0 || amt < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                ToastManager.showError(I18n.get("prize.dialog.number_error"));
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
            ToastManager.showSuccess(isEdit ? I18n.get("prize.dialog.updated_msg") : I18n.get("prize.dialog.created_msg"));
        });
    }
}