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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HomeController implements Initializable {
    @FXML
    public JFXButton searchBtn;

    @FXML
    public TextField searchField;

    @FXML
    public JFXListView movieListView;

    @FXML
    public JFXComboBox<Genre> genreComboBox;

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
            List<Movie> moviesFromApi = movieAPI.fetchMovies();
            observableMovies.clear();
            observableMovies.addAll(moviesFromApi);
            sortedState = SortedState.NONE;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializeLayout() {
        movieListView.setItems(observableMovies);
        movieListView.setCellFactory(movieListView -> new MovieCell());

        genreComboBox.getItems().add(null);
        genreComboBox.getItems().addAll(Genre.values());

        releaseYearComboBox.getItems().addAll(2020, 2019, 2018, 2017, 2016, 2015, 2014, 2013, 2012, 2011, 2010, 2009, 2008, 2007, 2006, 2005, 2004, 2003, 2002, 2001, 2000);
        releaseYearComboBox.setPromptText("Select Year");

        ratingComboBox.getItems().addAll(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 8.2, 8.8, 9.0, 10.0);
        ratingComboBox.setPromptText("Select Rating");
    }

    public void sortMovies() {
        if (sortedState == SortedState.NONE || sortedState == SortedState.DESCENDING) {
            sortMovies(SortedState.ASCENDING);
        } else if (sortedState == SortedState.ASCENDING) {
            sortMovies(SortedState.DESCENDING);
        }
    }

    public void sortMovies(SortedState sortDirection) {
        if (sortDirection == SortedState.ASCENDING) {
            observableMovies.sort(Comparator.comparing(Movie::getTitle));
            sortedState = SortedState.ASCENDING;
        } else {
            observableMovies.sort(Comparator.comparing(Movie::getTitle).reversed());
            sortedState = SortedState.DESCENDING;
        }
    }

    public List<Movie> filterByQuery(List<Movie> movies, String query) {
        if (query == null || query.isEmpty()) return movies;
        return movies.stream()
                .filter(Objects::nonNull)
                .filter(movie ->
                        movie.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                movie.getDescription().toLowerCase().contains(query.toLowerCase())
                )
                .collect(Collectors.toList());
    }

    public List<Movie> filterByGenre(List<Movie> movies, Genre genre) {
        if (genre == null) return movies;
        return movies.stream()
                .filter(Objects::nonNull)
                .filter(movie -> movie.getGenres().contains(genre))
                .collect(Collectors.toList());
    }

    public List<Movie> filterByReleaseYear(List<Movie> movies, Integer releaseYear) {
        return movies.stream()
                .filter(movie -> movie.getReleaseYear() == releaseYear)
                .collect(Collectors.toList());
    }

    public List<Movie> filterByRating(List<Movie> movies, Double rating) {
        return movies.stream()
                .filter(movie -> movie.getRating() >= rating)
                .collect(Collectors.toList());
    }

    public void applyAllFilters(String searchQuery, Object genre) {
        List<Movie> filteredMovies = allMovies;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            filteredMovies = filterByQuery(filteredMovies, searchQuery);
        }

        if (genre != null) {
            filteredMovies = filterByGenre(filteredMovies, (Genre) genre);
        }

        observableMovies.clear();
        observableMovies.addAll(filteredMovies);
    }

    public void searchBtnClicked(ActionEvent actionEvent) {
        String searchQuery = searchField.getText().trim().toLowerCase();
        Genre selectedGenre = genreComboBox.getValue();
        Integer releaseYear = releaseYearComboBox.getValue();
        Double rating = ratingComboBox.getValue();

        try {
            List<Movie> filteredMovies;
            if (selectedGenre != null && releaseYear != null && rating != null) {
                filteredMovies = movieAPI.fetchMoviesWithFilters(
                        searchQuery,
                        selectedGenre.toString(),
                        releaseYear.toString(),
                        rating.toString());
            } else {
                filteredMovies = movieAPI.fetchMovies();
                filteredMovies = filterByQuery(filteredMovies, searchQuery);
                if (selectedGenre != null) {
                    filteredMovies = filterByGenre(filteredMovies, selectedGenre);
                }
                if (releaseYear != null) {
                    filteredMovies = filterByReleaseYear(filteredMovies, releaseYear);
                }
                if (rating != null) {
                    filteredMovies = filterByRating(filteredMovies, rating);
                }
            }
            observableMovies.clear();
            observableMovies.addAll(filteredMovies);
            sortMovies(sortedState);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sortBtnClicked(ActionEvent actionEvent) {
        sortMovies();
    }

    public String getMostPopularActor(List<Movie> movies) {
        return movies.stream()
                .flatMap(movie -> movie.getMainCast().stream())
                .collect(Collectors.groupingBy(actor -> actor, Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparingLong(entry -> entry.getValue()))
                .map(entry -> entry.getKey())
                .orElse("Unknown Actor");
    }

    public int getLongestMovieTitle(List<Movie> movies) {
        return movies.stream()
                .mapToInt(movie -> movie.getTitle().length())
                .max()
                .orElse(0);
    }

    public long countMoviesFrom(List<Movie> movies, String director) {
        return movies.stream()
                .filter(movie -> movie.getDirector().equalsIgnoreCase(director))
                .count();
    }

    public List<Movie> getMoviesBetweenYears(List<Movie> movies, int startYear, int endYear) {
        return movies.stream()
                .filter(movie -> movie.getReleaseYear() >= startYear && movie.getReleaseYear() <= endYear)
                .collect(Collectors.toList());
    }
}
