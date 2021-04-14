package com.sanvalero.apiRestCountries;

import com.sanvalero.apiRestCountries.domain.Country;
import com.sanvalero.apiRestCountries.service.CountryService;
import com.sanvalero.apiRestCountries.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.controlsfx.control.WorldMapView;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creado por @author: Javier
 * el 11/04/2021
 */
public class AppController implements Initializable {

    public ComboBox<String> cbSelection, cbOrder;
    public Button btCountries, btFilter, btOrder;
    public ProgressIndicator piIndicator;
    public ProgressBar pbProgress;
    public WebView wvFlag;
    public Label lbCountry, lbCode, lbCapital, lbContinent, lbArea, lbPopulation, lbDemonym, lbTitle;
    public TableView tvData;
    public TextField tfMaxPopulation, tfMinPopulation;

    public ObservableList<Country> allCountries = FXCollections.observableArrayList();
    public ObservableList<Country> countriesContinent = FXCollections.observableArrayList();
    public ObservableList<Country> countriesFiltered = FXCollections.observableArrayList();
    public ObservableList<Country> countriesByPopulation = FXCollections.observableArrayList();

    private CountryService countryService;
    private WebEngine webEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        countryService = new CountryService();

        ObservableList<String> itemsCountries = FXCollections.observableArrayList(
                "Select Option", "Countries", "Africa", "Americas", "Asia", "Europe", "Oceania");
        cbSelection.setItems(itemsCountries);
        cbSelection.setValue("Select Option");

        ObservableList<String> itemsOrder = FXCollections.observableArrayList(
                "Order By", "Population");
        cbOrder.setItems(itemsOrder);
        cbOrder.setValue("Order By");

        loadColumsTable();
        lbTitle.setText("All Countries");
        CompletableFuture.runAsync(() -> {
            loadAllCountries();
            timeProgressIndicator();
        }).whenComplete((string, throwable) -> piIndicator.setVisible(false));
    }

    @FXML
    public void getSelection(Event event){
        String selection = cbSelection.getValue().toLowerCase();
        switch (selection){
            case "countries":
                lbTitle.setText("Countries");
                CompletableFuture.runAsync(() -> {
                    loadAllCountries();
                    timeProgressIndicator();
                }).whenComplete((string, throwable) -> piIndicator.setVisible(false));
                break;
            case "africa":
            case "americas":
            case "asia":
            case "europe":
            case "oceania":
                lbTitle.setText(selection);
                CompletableFuture.runAsync(() -> {
                    loadCountriesContinent(selection);
                    timeProgressIndicator();
                }).whenComplete((string, throwable) -> piIndicator.setVisible(false));
                break;
        }
    }

    @FXML
    public void getFilter(Event event){

        if (tfMaxPopulation.getText().isEmpty() || tfMinPopulation.getText().isEmpty()){
            AlertUtils.showWarning("You must fill all fields");
        } else {
            int max, min;
            max = Integer.parseInt(tfMaxPopulation.getText());
            min = Integer.parseInt(tfMinPopulation.getText());
            CompletableFuture.runAsync(() -> {
                loadFilter(max, min);
                timeProgressIndicator();
            }).whenComplete((string, throwable) -> piIndicator.setVisible(false));
        }
    }

    @FXML
    public void exportCSV(Event event){
        List<Country> list = tvData.getItems();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(null);
        export(file, list);

    }

    @FXML
    public void exportZip(Event event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(null);
        CompletableFuture.supplyAsync(() -> export(file, allCountries))
                .thenAccept(this::zipFile)
                .whenComplete((string, throwable) -> System.out.println(throwable.getMessage()));
    }

    @FXML
    public void loadOrder(Event event){
        String selection = cbOrder.getValue();
        switch (selection){
            case "Population":
                lbTitle.setText(selection);
                CompletableFuture.runAsync(() -> {
                    orderByPopulation();
                    timeProgressIndicator();
                }).whenComplete((string, throwable) -> piIndicator.setVisible(false));
                break;
        }
    }

    @FXML
    public void getCountry(Event event){
        Country country = (Country) tvData.getSelectionModel().getSelectedItem();
        webEngine = wvFlag.getEngine();
        webEngine.load(country.getFlag());
        wvFlag.setZoom(0.3);
        String code = country.getCode();
        String capital = country.getCapital();
        String continent = country.getContinent();
        String area = String.valueOf(country.getArea());
        String population = String.valueOf(country.getPopulation());
        String demonym = country.getDemonym();

        if (country.getCode().equals("")) code = "NO DATA";
        if (country.getCapital().equals("")) capital = "NO DATA";
        if (country.getContinent().equals("")) continent ="NO DATA";
        if (country.getArea() == 0) area = "NO DATA";
        if (country.getPopulation() == 0) population = "NO DATA";
        if (country.getDemonym().equals("")) demonym = "NO DATA";

        lbCountry.setText(country.getCountry());
        lbCode.setText(code);
        lbCapital.setText(capital);
        lbContinent.setText(continent);
        lbArea.setText(area);
        lbPopulation.setText(population);
        lbDemonym.setText(demonym);
    }


    public void loadAllCountries(){
        tvData.getItems().clear();
        countryService.getAllCountries()
                .flatMap(Observable::from)
                .doOnCompleted(() -> System.out.println("Loading all countries"))
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> allCountries.add(country));
        tvData.setItems(allCountries);

    }

    public void loadCountriesContinent(String continent){
        tvData.getItems().clear();
        countryService.getCountriesContinent(continent)
                .flatMap(Observable::from)
                .doOnCompleted(() -> System.out.println("Loading countries: " + continent))
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> countriesContinent.add(country));
        tvData.setItems(countriesContinent);

    }

    public void loadFilter(int max, int min){
        tvData.getItems().clear();
        countryService.getAllCountries()
                .flatMap(Observable::from)
                .filter(country -> country.getPopulation() < max && country.getPopulation() > min)
                .doOnCompleted(() -> System.out.println("Loading Data Filtered"))
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> countriesFiltered.add(country));
        tvData.setItems(countriesFiltered);
    }

    public void orderByPopulation(){
        tvData.getItems().clear();
        countryService.getAllCountries()
                .flatMap(Observable::from)
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> countriesByPopulation.add(country));

        tvData.setItems(countriesByPopulation.stream()
                .sorted(Comparator.comparingInt(Country::getPopulation))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    public void loadColumsTable(){
        Field[] fields = Country.class.getDeclaredFields();
        for (Field field : fields){
            if (field.getName().equals("flag")) continue;

            TableColumn<Country, String> column = new TableColumn<>(field.getName());
            column.setCellValueFactory(new PropertyValueFactory<>(field.getName()));
            tvData.getColumns().add(column);
        }
        tvData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public String export(File file, List<Country> list){
        try {
            FileWriter fileWriter = new FileWriter(file + ".csv");
            CSVPrinter printer = CSVFormat.TDF.withHeader("Country;", "Code;", "Capital;", "Continent;", "Area;", "Population;", "Demonym;").print(fileWriter);

            for (Country country : list) {
                printer.printRecord(country.getCountry(), ";", country.getCode(), ";", country.getCapital(),
                        ";", country.getContinent(), ";", country.getArea(), ";", country.getPopulation(), ";", country.getDemonym());
            }
            printer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public void zipFile(String file) {
        try {
            FileOutputStream fos = new FileOutputStream(file + ".zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(file + ".csv");
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            byte [] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0){
                zipOut.write(bytes, 0, length);
            }
            zipOut.close();
            fis.close();
            fos.close();
            Files.delete(fileToZip.toPath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void timeProgressIndicator(){
        piIndicator.setVisible(true);
        piIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        try {
            Thread.sleep(3000);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
