package at.ac.fhcampuswien.fhmdb;

import at.ac.fhcampuswien.fhmdb.api.MovieAPI;
import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.models.SortedState;
import at.ac.fhcampuswien.fhmdb.ui.MovieCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HomeController implements Initializable {
    @FXML
    public JFXButton searchBtn;

    @FXML
    public TextField searchField;

    @FXML
    public JFXListView<Movie> movieListView;

    @FXML
    public JFXComboBox<String> genreComboBox;

    @FXML
    public JFXButton sortBtn;

    @FXML
    public JFXComboBox<Integer> releaseYearComboBox;

    @FXML
    public JFXComboBox<Double> ratingComboBox;

    public List<Movie> allMovies;
    protected ObservableList<Movie> observableMovies = FXCollections.observableArrayList();
    protected SortedState sortedState;
    private MovieAPI movieAPI = new MovieAPI();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeState();
        initializeLayout();
    }

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

    public void initializeLayout() {
        movieListView.setItems(observableMovies);
        movieListView.setCellFactory(movieListView -> new MovieCell());

        genreComboBox.getItems().add("No filter");
        Arrays.stream(Genre.values()).map(Enum::toString).forEach(genreComboBox.getItems()::add);
        genreComboBox.setPromptText("Filter by Genre");

//        releaseYearComboBox.getItems().add("No filter");
//        releaseYearComboBox.getItems().add(null);
//        releaseYearComboBox.getItems().addAll(2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020);
//        releaseYearComboBox.setPromptText("Select Year");
//
//        ratingComboBox.getItems().add("No filter");
//        ratingComboBox.getItems().add(null);
//        ratingComboBox.getItems().addAll(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
//        ratingComboBox.setPromptText("Select Rating");

//        if (!releaseYearComboBox.getItems().contains(null)) {
//            releaseYearComboBox.getItems().add(null);
//        }

        releaseYearComboBox.getItems().addAll(2020, 2019, 2018, 2017, 2016, 2015, 2014, 2013, 2012, 2011, 2010, 2009, 2008, 2007, 2006, 2005, 2004, 2003, 2002, 2001, 2000);
        releaseYearComboBox.setPromptText("Select Year");

//        if (!ratingComboBox.getItems().contains(null)) {
//            ratingComboBox.getItems().add(null);
//        }
        ratingComboBox.getItems().addAll(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        ratingComboBox.setPromptText("Select Rating");
    }

    public void sortMovies() {
        sortMovies(sortedState == SortedState.ASCENDING ? SortedState.DESCENDING : SortedState.ASCENDING);
    }

    public void sortMovies(SortedState sortDirection) {
        observableMovies.sort(sortDirection == SortedState.ASCENDING ?
                Comparator.comparing(Movie::getTitle) :
                Comparator.comparing(Movie::getTitle).reversed());
        sortedState = sortDirection;
    }

    public List<Movie> filterByQuery(List<Movie> movies, String query) {
        if (query == null || query.isEmpty()) return movies;
        return movies.stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        movie.getDescription().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public List<Movie> filterByGenre(List<Movie> movies, String genre) {
        if (genre == null || genre.equals("No filter")) return movies;
        Genre selectedGenre = Genre.valueOf(genre);
        return movies.stream().filter(movie -> movie.getGenres().contains(selectedGenre)).toList();
    }

    public List<Movie> filterByReleaseYear(List<Movie> movies, Integer releaseYear) {
        return (releaseYear == null) ? movies : movies.stream().filter(movie -> movie.getReleaseYear() == releaseYear).toList();
    }

    public List<Movie> filterByRating(List<Movie> movies, Double rating) {
        return (rating == null) ? movies : movies.stream().filter(movie -> movie.getRating() >= rating).toList();
    }

    public void applyAllFilters() {
        String searchQuery = searchField.getText().trim().toLowerCase();
        String selectedGenre = genreComboBox.getValue();
        Integer releaseYear = releaseYearComboBox.getValue();
        Double rating = ratingComboBox.getValue();

        List<Movie> filteredMovies = allMovies;
        if (!searchQuery.isEmpty()) filteredMovies = filterByQuery(filteredMovies, searchQuery);
        filteredMovies = filterByGenre(filteredMovies, selectedGenre);
        filteredMovies = filterByReleaseYear(filteredMovies, releaseYear);
        filteredMovies = filterByRating(filteredMovies, rating);

        observableMovies.clear();
        observableMovies.addAll(filteredMovies);
        sortMovies(sortedState);
    }

    public void searchBtnClicked(ActionEvent actionEvent) {
        applyAllFilters();
    }

    public void sortBtnClicked(ActionEvent actionEvent) {
        sortMovies();
    }

    public String getMostPopularActor(List<Movie> movies) {
        return movies.stream()
                .flatMap(movie -> movie.getMainCast().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    public String getLongestMovieTitle(List<Movie> movies) {
        return movies.stream()
                .map(movie -> movie.getTitle())
                .max(Comparator.comparingInt(String::length))
                .map(title -> title + " (" + title.length() + " characters)")
                .orElse("No movies available");
    }

    public long countMoviesFrom(List<Movie> movies, String director) {
        return movies.stream()
                .filter(movie -> movie.getDirector().equalsIgnoreCase(director))
                .count();
    }

    public List<Movie> getMoviesBetweenYears(List<Movie> movies, int startYear, int endYear) {
        return movies.stream()
                .filter(movie -> movie.getReleaseYear() >= startYear && movie.getReleaseYear() <= endYear)
                .toList();
    }
}

