package com.aaronkersten.timetracker.controller;

import com.aaronkersten.timetracker.EditingCell;
import com.aaronkersten.timetracker.FileUtils;
import com.aaronkersten.timetracker.Task;
import com.aaronkersten.timetracker.TimeTrackerApplication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainLayout implements Initializable {
    @FXML TableView<Task> tableView;
    @FXML TableColumn<Task, Integer> numberColumn;
    @FXML TableColumn<Task, String> startTimeColumn;
    @FXML TableColumn<Task, String> endTimeColumn;
    @FXML TableColumn<Task, String> descriptionColumn;
    @FXML TableColumn<Task, String> chargeCodeColumn;
    @FXML ProgressBar progressBar;
    @FXML Label progressLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableView.setEditable(true);

        // adjust progress bar width
        tableView.widthProperty().addListener((observableValue, oldWidth, newWidth) -> {
            progressBar.setPrefWidth((Double) newWidth);
        });

        // TODO
        tableView.setOnMouseClicked(event -> {
            if (event.getTarget() instanceof TableCell<?,?> clickedCell) {
                tableView.edit(clickedCell.getTableRow().getIndex(), (TableColumn<Task, ?>) clickedCell.getTableColumn());
            }
        });

        // columns
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));

        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeColumn.setCellFactory(getCellFactory());

        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeColumn.setCellFactory(getCellFactory());

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(getCellFactory());

        chargeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("chargeCode"));
        chargeCodeColumn.setCellFactory(getCellFactory());

        setColumnWidths();
        loadFile();
    }

    public void onAbout() {
        Stage aboutStage = new Stage();
        aboutStage.setTitle("About");

        VBox aboutLayout = new VBox(10);
        aboutLayout.setPadding(new Insets(10));
        aboutLayout.setAlignment(Pos.CENTER);

        Label aboutLabel = new Label("Time Tracker App\nDeveloped by Aaron Kersten\naaronkersten21@gmail.com");
        aboutLayout.getChildren().addAll(aboutLabel);

        Scene aboutScene = new Scene(aboutLayout, 300, 200);
        aboutStage.setScene(aboutScene);
        aboutStage.initModality(Modality.APPLICATION_MODAL);
        aboutStage.show();
    }

    public void onCloseWindow() {
        ((Stage) tableView.getScene().getWindow()).close();
    }

    public void onDeleteTask() {
        Task task = tableView.getSelectionModel().getSelectedItem();
        tableView.getItems().remove(task);

        // reset numbers for each task
        for (int i = 0; i < tableView.getItems().size(); i++ ) {
            tableView.getItems().get(i).setNumber(i + 1);
        }
    }

    public void onEditStartTime(TableColumn.CellEditEvent<Task, String> event) {
        Task selectedTask = tableView.getSelectionModel().getSelectedItem();
        selectedTask.setStartTime(event.getNewValue());
        setProgress();
    }

    public void onEditEndTime(TableColumn.CellEditEvent<Task, String> event) {
        Task selectedTask = tableView.getSelectionModel().getSelectedItem();
        selectedTask.setEndTime(event.getNewValue());
        setProgress();
    }

    public void onEditDescription(TableColumn.CellEditEvent<Task, String> event) {
        Task selectedTask = tableView.getSelectionModel().getSelectedItem();
        selectedTask.setDescription(event.getNewValue());
    }

    public void onEditChargeCode(TableColumn.CellEditEvent<Task, String> event) {
        Task selectedTask = tableView.getSelectionModel().getSelectedItem();
        selectedTask.setChargeCode(event.getNewValue());
    }

    public void onExit() {
        System.exit(0);
    }

    public void onGenerateReport() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Charge Code Report");

        VBox reportLayout = new VBox(10);
        reportLayout.setPadding(new Insets(10));
        reportLayout.setAlignment(Pos.CENTER);

        Map<String, Double> report = generateChargeCodeReport();
        for (String key : report.keySet()) {
            String hours = String.format("%.2f", report.get(key) / 60);
            Label label = new Label(key + ": " + hours + " hours");
            reportLayout.getChildren().add(label);
        }

        Scene aboutScene = new Scene(reportLayout, 300, 200);
        reportStage.setScene(aboutScene);
        reportStage.initModality(Modality.APPLICATION_MODAL);
        reportStage.show();
    }

    public void onNewFile() {
        try {
            new TimeTrackerApplication().start(new Stage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onNewTask() {
        String startTime = "";

        if (!tableView.getItems().isEmpty()) {
            Task lastTask = tableView.getItems().get(tableView.getItems().size() - 1);

            if (!lastTask.getEndTime().isBlank()) {
                startTime = lastTask.getEndTime();
            }
        }

        addTask(startTime, "", "", "");
    }

    public void onOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Time Tracker File");
        fileChooser.setInitialDirectory(new File("Time Sheets"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt")
        );

        File selectedFile = fileChooser.showOpenDialog(getStage());
        if (selectedFile != null) {
            tableView.getItems().clear();
            tableView.getItems().addAll(FileUtils.readFile(selectedFile));
        }
    }

    public void onSave() {
        File directory = new File("Time Sheets");
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                System.err.println("Failed to create 'Time Sheets' directory");
            }
        }

        List<String> headers = tableView.getColumns().stream().map(TableColumnBase::getText).toList();
        FileUtils.writeFile(new File("Time Sheets/" + getFilename()), headers, tableView.getItems());
    }

    public void onSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.setInitialDirectory(new File("Time Sheets"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt")
        );
        fileChooser.setInitialFileName(getFilename());

        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            List<String> headers = tableView.getColumns().stream().map(TableColumnBase::getText).toList();
            FileUtils.writeFile(file, headers, tableView.getItems());
        }
    }

    private void addTask(String startTime, String endTime, String description, String chargeCode) {
        int size = tableView.getItems().size();
        Task task = new Task(size + 1, startTime, endTime, description, chargeCode);
        tableView.getItems().add(task);
    }

    private Map<String, Double> generateChargeCodeReport() {
        Map<String, Double> hours = new HashMap<>();
        hours.put("Uncategorized", 0.0);

        for (Task task : tableView.getItems()) {
            if (task.getChargeCode().isBlank()) {
                hours.compute("Uncategorized", (k, uncategorized) -> uncategorized + task.getDuration());
            } else if (hours.containsKey(task.getChargeCode())) {
                hours.compute(task.getChargeCode(), (k, num) -> num + task.getDuration());
            } else {
                hours.put(task.getChargeCode(), task.getDuration());
            }
        }

        return hours;
    }

    public Callback<TableColumn<Task, String>, TableCell<Task, String>> getCellFactory() {
        return p -> new EditingCell();
    }

    private static double getColumnMaxWidth(TableColumn<Task, String> column) {
        double maxWidth = column.getText().length() * 7;

        for (Task task : column.getTableView().getItems()) {
            double width = column.getCellData(task).length() * 7;
            maxWidth = Math.max(maxWidth, width);
        }

        return maxWidth;
    }

    private String getFilename() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        return "timesheet-" + currentDate.format(formatter) + ".csv";
    }

    private Stage getStage() {
        return (Stage) tableView.getScene().getWindow();
    }

    private static void resizeColumnToFitContent(TableColumn<Task, String> column) {
        double maxWidth = getColumnMaxWidth(column);
        column.setPrefWidth(maxWidth + 20);
    }

    private void loadFile() {
        File directory = new File("Time Sheets");
        if (directory.exists()) {
            File file = new File("Time Sheets/" + getFilename());
            if (file.exists()) {
                tableView.getItems().clear();
                tableView.getItems().addAll(FileUtils.readFile(file));
            } else {
                addTask("9:00", "", "", "");
                onSave();
            }
        }
    }

    private void setColumnWidths() {
        // have to set number column width separately
        double maxWidth = numberColumn.getText().length() * 7;
        for (Task task : tableView.getItems()) {
            double width = String.valueOf(task.getNumber()).length() * 7;
            maxWidth = Math.max(maxWidth, width);
        }
        numberColumn.setPrefWidth(maxWidth + 20);

        resizeColumnToFitContent(startTimeColumn);
        resizeColumnToFitContent(endTimeColumn);
        resizeColumnToFitContent(descriptionColumn);
        resizeColumnToFitContent(chargeCodeColumn);

        double otherColWidths = numberColumn.getWidth()
                + startTimeColumn.getWidth()
                + endTimeColumn.getWidth()
                + chargeCodeColumn.getWidth();

        descriptionColumn.prefWidthProperty()
                .bind(tableView.widthProperty().subtract(otherColWidths));
    }

    private void setProgress() {
        double totalDuration = 0;
        for (Task task : tableView.getItems()) {
            totalDuration += task.getDuration();
        }

        progressBar.setProgress(totalDuration / 480);
        progressLabel.setText(String.format("%.2f", totalDuration) + " / " + 480
                + " (" + String.format("%.2f", 100 * totalDuration / 480) + "%)");
    }
}
