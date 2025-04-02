package at.ac.fhcampuswien.fhmdb.models;

import java.util.List;

public class Movie {
    public String title;
    public String description;
    public List<Genre> genres;
    public List<String> mainCast;
    public String director;
    public int releaseYear;
    public double rating;

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