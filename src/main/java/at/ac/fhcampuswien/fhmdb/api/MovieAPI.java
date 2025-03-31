package at.ac.fhcampuswien.fhmdb.api;

import at.ac.fhcampuswien.fhmdb.models.Movie;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class MovieAPI {

    private static final String BASE_URL = "https://prog2.fh-campuswien.ac.at/movies";
    private static final OkHttpClient client = new OkHttpClient();

    // Methode, um Filme abzurufen (ohne Filter)
    public List<Movie> fetchMovies() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .header("User-Agent", "http.agent") // Header hinzufügen
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                return parseMovies(jsonResponse);
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    // Methode, um Filme mit Filtern abzurufen
    public List<Movie> fetchMoviesWithFilters(String query, String genre, String releaseYear, String ratingFrom) throws IOException {
        HttpUrl url = HttpUrl.parse(BASE_URL).newBuilder()
                .addQueryParameter("query", query)
                .addQueryParameter("genre", genre)
                .addQueryParameter("releaseYear", releaseYear)
                .addQueryParameter("ratingFrom", ratingFrom)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "http.agent")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                return parseMovies(jsonResponse);
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    // Methode, um die JSON-Antwort in Movie-Objekte zu parsen
    private List<Movie> parseMovies(String jsonResponse) {
        Gson gson = new Gson();
        Movie[] moviesArray = gson.fromJson(jsonResponse, Movie[].class);
        return List.of(moviesArray); // Umwandlung in eine Liste
    }
}
