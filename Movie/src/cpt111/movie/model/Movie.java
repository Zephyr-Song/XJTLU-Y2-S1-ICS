package cpt111.movie.model;

import java.util.Objects;

public class Movie {
    private final String id;
    private final String title;
    private final String genre;
    private final int releaseYear;
    private final double rating;

    public Movie(String id, String title, String genre, int releaseYear, double rating) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.rating = rating;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getReleaseYear() { return releaseYear; }
    public double getRating() { return rating; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return Objects.equals(id, movie.id);
    }

    @Override
    public int hashCode() {return Objects.hash(id);}

    @Override
    public String toString() {
        return String.format("ID: %s,  Title: %s,  Genre: %s,  Year: %d,  Rating: %.1f",
                id, title, genre, releaseYear, rating);
    }
}