package at.ac.fhcampuswien.fhmdb;

import at.ac.fhcampuswien.fhmdb.api.MovieAPI;
import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.models.SortedState;
import at.ac.fhcampuswien.fhmdb.ui.MovieCell;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HomeController implements Initializable {

    @FXML
    JFXButton searchBtn;

    @FXML
    TextField searchField;

    @FXML
    JFXButton clearFiltersBtn;

    @FXML
    JFXListView<Movie> movieListView;

    @FXML
    JFXComboBox<String> genreComboBox;

    @FXML
    JFXButton sortBtn;

    @FXML
    JFXComboBox<Integer> releaseYearComboBox;

    @FXML
    JFXComboBox<Double> ratingComboBox;

    public List<Movie> allMovies;
    protected ObservableList<Movie> observableMovies = FXCollections.observableArrayList();
    protected SortedState sortedState;
    private MovieAPI movieAPI = new MovieAPI();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeState();
        initializeLayout();
    }

    // Lädt den initialen Zustand (Filmdaten)
    public void initializeState() {
        try {
            allMovies = movieAPI.fetchMovies();
            observableMovies.clear();
            observableMovies.addAll(allMovies);
            sortedState = SortedState.NONE;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Initialisiert das Layout und füllt die Filter-ComboBoxen dynamisch
    public void initializeLayout() {
        movieListView.setItems(observableMovies);
        movieListView.setCellFactory(listView -> new MovieCell());

        // Genre-ComboBox enthält nur die tatsächlichen Genres (kein "No filter"-Eintrag)
        for (Genre genre : Genre.values()) {
            genreComboBox.getItems().add(genre.toString());
        }
        genreComboBox.setPromptText("Select Genre");

        // Dynamisch generierte Liste der Erscheinungsjahre (von aktuellem Jahr bis 2000)
        int currentYear = Year.now().getValue();
        List<Integer> years = new ArrayList<>();
        for (int year = currentYear; year >= 2000; year--) {
            years.add(year);
        }
        releaseYearComboBox.getItems().addAll(years);
        releaseYearComboBox.setPromptText("Select Year");

        // Dynamisch generierte Liste der Bewertungen (0.0 bis 10.0)
        List<Double> ratings = new ArrayList<>();
        for (double rating = 0.0; rating <= 10.0; rating++) {
            ratings.add(rating);
        }
        ratingComboBox.getItems().addAll(ratings);
        ratingComboBox.setPromptText("Select Rating");
    }

    // Wechselt zwischen auf- und absteigender Sortierung
    public void sortMovies() {
        sortMovies(sortedState == SortedState.ASCENDING ? SortedState.DESCENDING : SortedState.ASCENDING);
    }

    // Sortiert die observableMovies-Liste anhand des Filmtitels
    public void sortMovies(SortedState sortDirection) {
        observableMovies.sort(sortDirection == SortedState.ASCENDING ?
                Comparator.comparing(Movie::getTitle) :
                Comparator.comparing(Movie::getTitle).reversed());
        sortedState = sortDirection;
    }

    // Filtert anhand des Suchstrings (Titel oder Beschreibung)
    public List<Movie> findByQuery(List<Movie> movies, String query) {
        if (movies == null) {
            throw new IllegalArgumentException("Movie list must not be null");
        }

        if (query == null || query.isEmpty()) {
            return movies;
        }

        String lowerQuery = query.toLowerCase();

        return movies.stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(lowerQuery)
                        || movie.getDescription().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    // Filtert anhand des Genres
    public List<Movie> findByGenre(List<Movie> movies, String selectedGenre) {
        if (selectedGenre == null) {
            return movies;
        }
        return movies.stream()
                .filter(movie -> movie.getGenres().contains(Genre.valueOf(selectedGenre)))
                .collect(Collectors.toList());
    }

    // Filtert anhand des Erscheinungsjahres
    public List<Movie> findByYear(List<Movie> movies, Integer year) {
        if (year == null) {
            return movies;
        }
        return movies.stream()
                .filter(movie -> movie.getReleaseYear() == year)
                .collect(Collectors.toList());
    }

    // Filtert anhand der Mindestbewertung
    public List<Movie> findByRating(List<Movie> movies, Double rating) {
        if (rating == null) {
            return movies;
        }
        return movies.stream()
                .filter(movie -> movie.getRating() >= rating)
                .collect(Collectors.toList());
    }

    // In dieser Methode werden alle Filter nacheinander angewendet.
    public void applyAllFilters() {
        String searchQuery = searchField.getText().trim().toLowerCase();
        String selectedGenre = genreComboBox.getValue();
        Integer selectedYear = releaseYearComboBox.getValue();
        Double selectedRating = ratingComboBox.getValue();

        List<Movie> filteredMovies = allMovies;
        filteredMovies = findByQuery(filteredMovies, searchQuery);
        filteredMovies = findByGenre(filteredMovies, selectedGenre);
        filteredMovies = findByYear(filteredMovies, selectedYear);
        filteredMovies = findByRating(filteredMovies, selectedRating);

        observableMovies.clear();
        observableMovies.addAll(filteredMovies);
        sortMovies(sortedState);
    }


    // Setzt alle Filter zurück und stellt die ursprüngliche Filmliste wieder her
    public void clearAllFilters() {
        searchField.clear();
        genreComboBox.getSelectionModel().clearSelection();
        releaseYearComboBox.getSelectionModel().clearSelection();
        ratingComboBox.getSelectionModel().clearSelection();
        observableMovies.clear();
        observableMovies.addAll(allMovies);
        sortedState = SortedState.NONE;
    }

    // Event-Handler für den Filter-Button
    public void searchBtnClicked(ActionEvent actionEvent) {
        applyAllFilters();
    }

    // Event-Handler für den Sortier-Button
    public void sortBtnClicked(ActionEvent actionEvent) {
        sortMovies();
    }

    // Event-Handler für den "Clear Filters"-Button
    public void clearFiltersBtnClicked(ActionEvent actionEvent) {
        clearAllFilters();
    }
}
