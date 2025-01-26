package com.aaronkersten.timetracker.controller;

import com.aaronkersten.timetracker.Task;
import com.aaronkersten.timetracker.TimeTrackerApplication;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.FormatStringConverter;

import java.io.*;
import java.net.URL;
import java.text.Format;
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

    private TableCell<Task, String> editingCell;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableView.setEditable(true);
        tableView.widthProperty().addListener((observableValue, oldWidth, newWidth) -> {
            progressBar.setPrefWidth((Double) newWidth);
        });
        tableView.getItems().addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> change) {
                setProgress();
            }
        });
//        tableView.setOnMouseClicked(event -> {
//            if (editingCell != null) {
//                if (event.getTarget() instanceof TableCell<?, ?> clickedCell) {
//                    if (clickedCell != editingCell) {
//                        tableView.edit(-1, null);
//                        editingCell = null;
//                    }
//                } else {
//                    tableView.edit(-1, null);
//                    editingCell = null;
//                }
//            }
//        });

        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));

        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        enableTextWrapping(descriptionColumn);

        chargeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("chargeCode"));
        chargeCodeColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        progressBar.setPrefWidth(tableView.getWidth());

        addTask("9:00", "", "", "");

        setColumnWidths();
    }

    public void onNewFile() {
        try {
            new TimeTrackerApplication().start(new Stage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onOpenFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Time Tracker File");
        fileChooser.setInitialDirectory(new File("Time Sheets"));

        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().addAll(csvFilter, txtFilter);

        File selectedFile = fileChooser.showOpenDialog(tableView.getScene().getWindow());
        if (selectedFile != null) {
            readFile(selectedFile);
        }
    }

    public void onSave(ActionEvent event) {
        try {
            File directory = new File("Time Sheets");
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    System.out.println("Directory 'Time Sheets' created.");
                } else {
                    System.err.println("Failed to create 'Time Sheets' directory.");
                    return;
                }
            }

            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
            String fileName = "Time Sheets/timesheet-" + currentDate.format(formatter) + ".csv";

            try (FileWriter writer = new FileWriter(fileName)) {
                for (TableColumn<Task, ?> column : tableView.getColumns()) {
                    writer.write(column.getText() + ",");
                }
                writer.write("\n");

                for (Task task : tableView.getItems()) {
                    writer.write(escapeForCSV(task.getStartTime()) + ",");
                    writer.write(escapeForCSV(task.getEndTime()) + ",");
                    writer.write(escapeForCSV(task.getDescription()) + ",");
                    writer.write(escapeForCSV(task.getChargeCode()) + "\n");
                }

                System.out.println("Data exported to " + fileName);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void onSaveAs(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.setInitialDirectory(new File("Time Sheets"));

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String fileName = "Time Sheets/timesheet-" + currentDate.format(formatter) + ".csv";
        fileChooser.setInitialFileName(fileName);

        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().addAll(csvFilter, txtFilter);

        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                for (TableColumn<Task, ?> column : tableView.getColumns()) {
                    writer.write(column.getText() + ",");
                }
                writer.write("\n");

                for (Task task : tableView.getItems()) {
                    writer.write(escapeForCSV(task.getStartTime()) + ",");
                    writer.write(escapeForCSV(task.getEndTime()) + ",");
                    writer.write(escapeForCSV(task.getDescription()) + ",");
                    writer.write(escapeForCSV(task.getChargeCode()) + "\n");
                }

                System.out.println("Data exported to " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onCloseWindow() {
        ((Stage) tableView.getScene().getWindow()).close();
    }

    public void onExit() {
        System.exit(0);
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

    public void onNewTask() {
        String startTime = "";

        if (!tableView.getItems().isEmpty()) {
            Task lastTask = tableView.getItems().get(tableView.getItems().size() - 1);

            if (!lastTask.getEndTime().isEmpty()) {
                startTime = lastTask.getEndTime();
            }
        }

        addTask(startTime, "", "", "");
    }

    public void onDeleteTask() {
        Task task = tableView.getSelectionModel().getSelectedItem();
        tableView.getItems().remove(task);

        for (int i = 0; i < tableView.getItems().size(); i++ ) {
            tableView.getItems().get(i).setNumber(i + 1);
        }
    }

    private String escapeForCSV(String field) {
        if (field == null) {
            return "";
        }

        String escaped = field.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }

    private void readFile(File file) {
        tableView.getItems().clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            boolean skipHeader = true;
            String line;

            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                List<String> fields = parseLine(line);

                System.out.println(fields);
                addTask(fields.get(0), fields.get(1), fields.get(2), fields.get(3));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(field.toString());
                    field.setLength(0);
                } else {
                    field.append(c);
                }
            }
        }

        fields.add(field.toString());
        return fields;
    }

    private void addTask(String startTime, String endTime, String description, String chargeCode) {
        int size = tableView.getItems().size();
        Task task = new Task(size + 1, startTime, endTime, description, chargeCode);
        tableView.getItems().add(task);
    }

    private void resizeColumnToFitContent(TableColumn<Task, String> column) {
        double maxWidth = getMaxWidth(column);
        column.setPrefWidth(maxWidth + 20);
    }

    private double getMaxWidth(TableColumn<Task, String> column) {
        double maxWidth = column.getText().length() * 7; // Estimate column header width (adjust as needed)

        for (Task task : column.getTableView().getItems()) {
            double width = column.getCellData(task).length() * 7;
            maxWidth = Math.max(maxWidth, width);
        }

        return maxWidth;
    }

    private void setColumnWidths() {
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

        descriptionColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(
                numberColumn.getWidth() + startTimeColumn.getWidth() + endTimeColumn.getWidth() + chargeCodeColumn.getWidth()
        ));
    }

    private void setProgress() {
        double totalDuration = 0;
        for (Task task : tableView.getItems()) {
            totalDuration += task.getDuration();
        }

        progressBar.setProgress(totalDuration / 480);
        progressLabel.setText(String.format("%.2f", totalDuration) + " / " + 480);
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

    public void onGenerateReport() {
        Stage aboutStage = new Stage();
        aboutStage.setTitle("Charge Code Report");

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
        aboutStage.setScene(aboutScene);
        aboutStage.initModality(Modality.APPLICATION_MODAL);
        aboutStage.show();
    }

    private void enableTextWrapping(TableColumn<Task, String> column) {
        column.setCellFactory(param -> new TableCell<>() {
            private final Text text;

            {
                text = new Text();
                text.wrappingWidthProperty().bind(param.widthProperty()); // Bind text wrapping to column width
                text.setStyle("-fx-padding: 5;"); // Optional: Add padding around text
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setText(null);
                }
            }
        });
    }
}
