package cpt111.movie.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class History {
    private final Map<Movie, Integer> watchedMovies;
    public History() {
        this.watchedMovies = new HashMap<>();
    }
    public History(List<String> movieIds, List<Movie> allMovies) {
        this.watchedMovies = new HashMap<>();
        if (movieIds == null) return;
        for (String id : movieIds) {
            for (Movie movie : allMovies) {
                if (movie.getId().equals(id)) {
                    watchedMovies.put(movie, watchedMovies.getOrDefault(movie, 0) + 1);
                    break;
                }
            }
        }
    }

    public History(Map<Movie, Integer> watchedMovies) {
        this.watchedMovies = new HashMap<>(watchedMovies);
    }

    public void addWatchedMovie(Movie movie) {
        watchedMovies.put(movie, watchedMovies.getOrDefault(movie, 0) + 1);
    }

    public Map<Movie, Integer> getAllWatchedMovies() {
        return new HashMap<>(watchedMovies);
    }

    public Map<String, Integer> getMostWatchedGenres(int topN) {
        Map<String, Integer> genreCount = new HashMap<>();
        for (Map.Entry<Movie, Integer> entry : watchedMovies.entrySet()) {
            Movie movie = entry.getKey();
            int watchCount = entry.getValue();
            String[] genres = movie.getGenre().split(",");
            for (String genre : genres) {
                genre = genre.trim();
                genreCount.put(genre, genreCount.getOrDefault(genre, 0) + watchCount);
            }
        }

        List<Map.Entry<String, Integer>> sortedGenres = new ArrayList<>(genreCount.entrySet());
        sortedGenres.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

        Map<String, Integer> result = new HashMap<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedGenres) {
            if (count >= topN) {
                break;
            }
            result.put(entry.getKey(), entry.getValue());
            count++;
        }

        return result;
    }

    public boolean hasWatched(String movieId) {
        for (Movie movie : watchedMovies.keySet()) {
            if (movie.getId().equals(movieId)) {
                return true;
            }
        }
        return false;
    }

    public String toIdString() {
        if (watchedMovies.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Movie, Integer> entry : watchedMovies.entrySet()) {
            Movie movie = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                sb.append(movie.getId()).append(";");
            }
        }

        return sb.substring(0, sb.length() - 1);
    }
}