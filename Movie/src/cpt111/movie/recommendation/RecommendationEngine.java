package cpt111.movie.recommendation;

import cpt111.movie.model.Movie;
import cpt111.movie.model.User;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


interface RecommendationStrategy {
    List<Movie> getRecommendations(User user, List<Movie> allMovies, int topN);
}

public class RecommendationEngine {
    private RecommendationStrategy strategy;
    public enum StrategyType {
        GENRE, YEAR, RATING
    }
    private StrategyType currentStrategyType;

    public RecommendationEngine() {
        this.currentStrategyType = StrategyType.GENRE;
        this.strategy = new GenreBasedStrategy();

    }


    public void setStrategy(StrategyType strategyType) {
        this.currentStrategyType = strategyType;
        switch (strategyType) {
            case GENRE:
                this.strategy = new GenreBasedStrategy();
                break;
            case YEAR:
                this.strategy = new YearBasedStrategy();
                break;
            case RATING:
                this.strategy = new RatingBasedStrategy();
                break;
            default:
                throw new IllegalArgumentException("Invalid strategy type");
        }
    }

    public StrategyType getCurrentStrategyType() {
        return currentStrategyType;
    }

    public List<Movie> getRecommendations(User user, List<Movie> allMovies, int topN) {
        if (topN < 1) topN = 5;
        if (topN > 20) topN = 20;
        return strategy.getRecommendations(user, allMovies, topN);
    }





    private static class GenreBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Movie> getRecommendations(User user, List<Movie> allMovies, int topN) {
            Map<Movie, Integer> watched = user.getHistory().getAllWatchedMovies();
            if (watched.isEmpty()) return List.of();

            String favoriteGenre = watched.keySet().stream()
                    .flatMap(m -> List.of(m.getGenre().split(",")).stream())
                    .map(String::trim)
                    .collect(Collectors.groupingBy(g -> g, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");

            return allMovies.stream()
                    .filter(m -> !watched.containsKey(m))
                    .filter(m -> m.getGenre().contains(favoriteGenre))
                    .sorted((m1, m2) -> Double.compare(m2.getRating(), m1.getRating()))
                    .limit(topN)
                    .collect(Collectors.toList());
        }
    }


    private static class YearBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Movie> getRecommendations(User user, List<Movie> allMovies, int topN) {
            Map<Movie, Integer> watched = user.getHistory().getAllWatchedMovies();
            if (watched.isEmpty()) return List.of();

            int favoriteYear = watched.keySet().stream()
                    .collect(Collectors.groupingBy(Movie::getReleaseYear, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(2020);

            return allMovies.stream()
                    .filter(m -> !watched.containsKey(m))
                    .filter(m -> Math.abs(m.getReleaseYear() - favoriteYear) <= 3)
                    .sorted((m1, m2) -> Double.compare(m2.getRating(), m1.getRating()))
                    .limit(topN)
                    .collect(Collectors.toList());
        }
    }


    private static class RatingBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Movie> getRecommendations(User user, List<Movie> allMovies, int topN) {
            Map<Movie, Integer> watched = user.getHistory().getAllWatchedMovies();

            return allMovies.stream()
                    .filter(m -> !watched.containsKey(m))
                    .filter(m -> m.getRating() >= 4.0)
                    .sorted((m1, m2) -> Double.compare(m2.getRating(), m1.getRating()))
                    .limit(topN)
                    .collect(Collectors.toList());
        }
    }
}