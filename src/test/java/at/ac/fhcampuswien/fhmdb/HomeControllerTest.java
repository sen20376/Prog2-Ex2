package at.ac.fhcampuswien.fhmdb;

import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.models.SortedState;
import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    static {
        try {
            // Versuche das JavaFX-Toolkit zu starten. Falls es bereits gestartet wurde, wird eine IllegalStateException geworfen.
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit wurde bereits initialisiert – nichts zu tun.
        }
    }

    private HomeController homeController;

    // Hilfsmethode, um ein Movie-Objekt zu erzeugen und alle Felder zu setzen
    private Movie createMovie(String title, String description, List<Genre> genres,
                              List<String> mainCast, String director, int releaseYear, double rating) {
        Movie m = new Movie();
        m.title = title;
        m.description = description;
        m.genres = genres;
        m.mainCast = mainCast;
        m.director = director;
        m.releaseYear = releaseYear;
        m.rating = rating;
        return m;
    }

    // Liefert einen festen Testdatensatz
    private List<Movie> getTestMovies() {
        return Arrays.asList(
                createMovie("Avatar",
                        "A paraplegic Marine dispatched to the moon Pandora on a unique mission becomes torn between following his orders and protecting the world he feels is his home.",
                        Arrays.asList(Genre.ANIMATION, Genre.DRAMA, Genre.ACTION),
                        Collections.emptyList(),
                        "James Cameron",
                        2009,
                        7.8),
                createMovie("Life Is Beautiful",
                        "When an open-minded Jewish librarian and his son become victims of the Holocaust, he uses a perfect mixture of will, humor, and imagination to protect his son from the dangers around their camp.",
                        Arrays.asList(Genre.DRAMA, Genre.ROMANCE),
                        Collections.emptyList(),
                        "Roberto Benigni",
                        1997,
                        8.6),
                createMovie("Puss in Boots",
                        "An outlaw cat, his childhood egg-friend, and a seductive thief kitty set out in search for the eggs of the fabled Golden Goose to clear his name, restore his lost honor, and regain the trust of his mother and town.",
                        Arrays.asList(Genre.COMEDY, Genre.FAMILY, Genre.ANIMATION),
                        Collections.emptyList(),
                        "Chris Miller",
                        2011,
                        6.3),
                createMovie("The Usual Suspects",
                        "A sole survivor tells of the twisty events leading up to a horrific gun battle on a boat, which begin when five criminals meet at a seemingly random police lineup.",
                        Arrays.asList(Genre.CRIME, Genre.DRAMA, Genre.MYSTERY),
                        Collections.emptyList(),
                        "Bryan Singer",
                        1995,
                        8.5),
                createMovie("The Wolf of Wall Street",
                        "Based on the true story of Jordan Belfort, from his rise to a wealthy stock-broker living the high life to his fall involving crime, corruption and the federal government.",
                        Arrays.asList(Genre.DRAMA, Genre.ROMANCE, Genre.BIOGRAPHY),
                        Collections.emptyList(),
                        "Martin Scorsese",
                        2013,
                        8.2)
        );
    }

    @BeforeEach
    void init() {
        homeController = new HomeController();
        // Da in Unit-Tests keine FXML-Injektion erfolgt, initialisieren wir die UI-Felder manuell:
        homeController.searchField = new TextField();
        homeController.genreComboBox = new JFXComboBox<>();
        homeController.releaseYearComboBox = new JFXComboBox<>();
        homeController.ratingComboBox = new JFXComboBox<>();

        // Setze den Testdatensatz:
        List<Movie> testMovies = getTestMovies();
        homeController.allMovies = testMovies;
        homeController.observableMovies.clear();
        homeController.observableMovies.addAll(testMovies);
        homeController.sortedState = SortedState.NONE;
    }

    @Test
    void at_initialization_allMovies_and_observableMovies_should_be_filled_and_equal() {
        assertEquals(homeController.allMovies, homeController.observableMovies);
    }

    @Test
    void if_not_yet_sorted_sort_is_applied_in_ascending_order() {
        homeController.sortedState = SortedState.NONE;
        homeController.sortMovies();

        List<String> expectedTitles = List.of(
                "Avatar",
                "Life Is Beautiful",
                "Puss in Boots",
                "The Usual Suspects",
                "The Wolf of Wall Street"
        );

        List<String> actualTitles = homeController.observableMovies.stream()
                .map(Movie::getTitle)
                .toList();

        assertEquals(expectedTitles, actualTitles);
    }

    @Test
    void if_last_sort_ascending_next_sort_should_be_descending() {
        homeController.sortedState = SortedState.ASCENDING;
        homeController.sortMovies();

        List<String> expectedTitles = List.of(
                "The Wolf of Wall Street",
                "The Usual Suspects",
                "Puss in Boots",
                "Life Is Beautiful",
                "Avatar"
        );

        List<String> actualTitles = homeController.observableMovies.stream()
                .map(Movie::getTitle)
                .toList();

        assertEquals(expectedTitles, actualTitles);
    }

    @Test
    void if_last_sort_descending_next_sort_should_be_ascending() {
        homeController.sortedState = SortedState.DESCENDING;
        homeController.sortMovies();

        List<String> expectedTitles = List.of(
                "Avatar",
                "Life Is Beautiful",
                "Puss in Boots",
                "The Usual Suspects",
                "The Wolf of Wall Street"
        );

        List<String> actualTitles = homeController.observableMovies.stream()
                .map(Movie::getTitle)
                .toList();

        assertEquals(expectedTitles, actualTitles);
    }

    @Test
    void query_filter_matches_with_lower_and_uppercase_letters() {
        String query = "IfE";

        List<String> actualTitles = homeController.findByQuery(homeController.observableMovies, query)
                .stream()
                .map(Movie::getTitle)
                .toList();

        List<String> expectedTitles = List.of(
                "Life Is Beautiful",
                "The Wolf of Wall Street"
        );

        assertEquals(expectedTitles, actualTitles);
    }

    @Test
    void query_filter_with_null_movie_list_throws_exception() {
        String query = "IfE";
        assertThrows(IllegalArgumentException.class, () -> homeController.findByQuery(null, query));
    }

    @Test
    void query_filter_with_null_value_returns_unfiltered_list() {
        String query = null;
        List<Movie> actual = homeController.findByQuery(homeController.observableMovies, query);
        assertEquals(homeController.observableMovies, actual);
    }

    @Test
    void genre_filter_with_null_value_returns_unfiltered_list() {
        List<Movie> actual = homeController.findByGenre(homeController.observableMovies, null);
        assertEquals(homeController.observableMovies, actual);
    }

    @Test
    void genre_filter_returns_all_movies_containing_given_genre() {
        List<Movie> actual = homeController.findByGenre(homeController.observableMovies, Genre.DRAMA.toString());
        // Es sollten 4 Filme mit Genre DRAMA enthalten sein
        assertEquals(4, actual.size());
    }

    @Test
    void no_filtering_ui_if_empty_query_or_no_genre_is_set() {
        homeController.searchField.setText("");
        homeController.genreComboBox.getSelectionModel().clearSelection();
        homeController.applyAllFilters();

        List<String> expectedTitles = homeController.allMovies.stream()
                .map(Movie::getTitle)
                .sorted()
                .toList();

        List<String> actualTitles = homeController.observableMovies.stream()
                .map(Movie::getTitle)
                .sorted()
                .toList();

        assertEquals(expectedTitles, actualTitles);
    }
}
