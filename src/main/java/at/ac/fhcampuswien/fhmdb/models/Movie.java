package at.ac.fhcampuswien.fhmdb.models;

import java.util.List;

public class Movie {
    public final String title;
    public final String description;
    public final List<Genre> genres;
    public final List<String> mainCast;
    public final String director;
    public final int releaseYear;
    public final double rating;

    public Movie(String title, String description, List<Genre> genres, List<String> mainCast, String director, int releaseYear, double rating) {
        this.title = title;
        this.description = description;
        this.genres = genres;
        this.mainCast = mainCast;
        this.director = director;
        this.releaseYear = releaseYear;
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public List<String> getMainCast() {
        return mainCast;
    }

    public String getDirector() {
        return director;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public double getRating() {
        return rating;
    }

}