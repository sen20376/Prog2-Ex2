package at.ac.fhcampuswien.fhmdb.api;

import at.ac.fhcampuswien.fhmdb.models.Movie;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MovieAPI {

    private static final String BASE_URL = "https://prog2.fh-campuswien.ac.at/movies";
    private static final OkHttpClient client = new OkHttpClient();
    // Gson als Singleton, um wiederholte Instanziierungen zu vermeiden.
    private static final Gson gson = new Gson();

    // Methode, um Filme abzurufen (ohne Filter)
    public List<Movie> fetchMovies() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .header("User-Agent", "http.agent") // Header hinzufügen
                .build();

        return executeRequest(request);
    }

    // Methode, um Filme mit Filtern abzurufen
    public List<Movie> fetchMoviesWithFilters(String query, String genre, Integer releaseYear, Double ratingFrom) throws IOException {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(BASE_URL)).newBuilder();
        if (query != null && !query.isEmpty()) {
            urlBuilder.addQueryParameter("query", query);
        }
        if (genre != null && !genre.isEmpty()) {
            urlBuilder.addQueryParameter("genre", genre);
        }
        if (releaseYear != null) {
            urlBuilder.addQueryParameter("releaseYear", String.valueOf(releaseYear));
        }
        if (ratingFrom != null) {
            urlBuilder.addQueryParameter("ratingFrom", String.valueOf(ratingFrom));
        }
        HttpUrl url = urlBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "http.agent")
                .build();

        return executeRequest(request);
    }

    private List<Movie> executeRequest(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Response body is null");
            }
            String jsonResponse = responseBody.string();
            return parseMovies(jsonResponse);
        }
    }

    // Parsen der JSON-Antwort in Movie-Objekte
    private List<Movie> parseMovies(String jsonResponse) {
        Movie[] moviesArray = gson.fromJson(jsonResponse, Movie[].class);
        return List.of(moviesArray);
    }
}