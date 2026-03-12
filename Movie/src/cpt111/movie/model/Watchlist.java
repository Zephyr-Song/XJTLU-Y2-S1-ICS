package cpt111.movie.model;

import java.util.ArrayList;
import java.util.List;

public class Watchlist {
    private final List<Movie> movies;

    public Watchlist() {
        this.movies = new ArrayList<>();
    }
    public Watchlist(List<String> movieIds, List<Movie> allMovies) {
        this.movies = new ArrayList<>();
        if (movieIds == null || allMovies == null) {
            return;
        }

        for (String id : movieIds) {
            Movie foundMovie = null;
            for (Movie movie : allMovies) {
                if (movie.getId().trim().equalsIgnoreCase(id.trim())) {
                    foundMovie = movie;
                    break;
                }
            }
            if (foundMovie != null) {
                this.movies.add(foundMovie);
            }
        }
    }

    public void addMovie(Movie movie) {
        if (!movies.contains(movie)) {
            movies.add(movie);
        }
    }

    public void removeMovie(String movieId) {
        movies.removeIf(movie -> movie.getId().trim().equalsIgnoreCase(movieId.trim()));
    }

    public boolean containsMovie(String movieId) {
        return movies.stream().anyMatch(movie -> movie.getId().trim().equalsIgnoreCase(movieId.trim()));
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies);
    }

    public String toIdString() {
        if (movies.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Movie movie : movies) {
            sb.append(movie.getId()).append(";");
        }
        return sb.substring(0, sb.length() - 1);
    }
}