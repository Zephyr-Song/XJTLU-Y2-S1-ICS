package cpt111.movie.io;

import cpt111.movie.model.History;
import cpt111.movie.model.Movie;
import cpt111.movie.model.User;
import cpt111.movie.model.Watchlist;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHandler {
    public static final String MOVIE_FILE_PATH = "data/movies.csv";
    public static final String USER_FILE_PATH = "data/users.csv";

    // loadAllMovies method remains unchanged
    public List<Movie> loadAllMovies() {
        List<Movie> movies = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(MOVIE_FILE_PATH))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 5) {
                    System.err.println("Movie file format error: Skipping invalid line -> " + line);
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    String title = parts[1].trim();
                    String genre = parts[2].trim();
                    int year = Integer.parseInt(parts[3].trim());
                    double rating = Double.parseDouble(parts[4].trim());

                    Movie movie = new Movie(id, title, genre, year, rating);
                    movies.add(movie);
                } catch (NumberFormatException e) {
                    System.err.println("Movie data type error: Skipping invalid line -> " + line);
                }
            }
            System.out.println("Successfully loaded movie data: Total " + movies.size() + " movies");
        } catch (FileNotFoundException e) {
            System.err.println("Error: Movie file not found! Path -> " + MOVIE_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Movie file reading failed! Error: " + e.getMessage());
        }
        return movies;
    }

    public Map<String, User> loadAllUsers(List<Movie> allMovies) {
        Map<String, User> users = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE_PATH))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    System.err.println("User file format error: Skipping invalid line -> " + line);
                    continue;
                }

                try {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String watchlistIdsStr = parts[2].trim();
                    String historyIdsStr = parts.length > 3 ? parts[3].trim() : "";

                    List<String> watchlistIds = parseIdString(watchlistIdsStr);
                    Watchlist watchlist = new Watchlist(watchlistIds, allMovies);
                    Map<Movie, Integer> watchedMoviesData = parseHistoryString(historyIdsStr, allMovies);
                    History history = new History(watchedMoviesData);

                    User user = new User(username, password, watchlist, history);
                    users.put(username, user);

                } catch (Exception e) {
                    System.err.println("User data parsing error: Skipping invalid line -> " + line + " | Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Successfully loaded user data: Total " + users.size() + " users");
        } catch (FileNotFoundException e) {
            System.err.println("Error: User file not found! Path -> " + USER_FILE_PATH);
        } catch (IOException e) {
            System.err.println("User file reading failed! Error: " + e.getMessage());
        }
        return users;
    }

    public boolean saveAllUsers(Map<String, User> users) {
        File userFile = new File(USER_FILE_PATH);
        File parentDir = userFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            System.err.println("Error: Unable to create data directory -> " + parentDir.getAbsolutePath());
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE_PATH))) {
            bw.write("Username,Password,Watchlist,History");
            bw.newLine();

            for (User user : users.values()) {
                String username = user.getUsername();
                String password = user.getPassword();
                String watchlistIds = user.getWatchlist().toIdString();
                String historyIds = user.getHistory().toIdString();

                String line = String.join(",", username, password, watchlistIds, historyIds);
                bw.write(line);
                bw.newLine();
            }
            System.out.println("Successfully saved user data: Total " + users.size() + " users");
            return true;
        } catch (IOException e) {
            System.err.println("User file saving failed! Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Parse history string with date format (e.g., "M001@2025-07-12;M011@2025-08-10")
     * @param historyStr History string read from CSV
     * @param allMovies List of all movies in the system to find Movie objects
     * @return Map of Movie and its watch count
     */
    private Map<Movie, Integer> parseHistoryString(String historyStr, List<Movie> allMovies) {
        Map<Movie, Integer> watchedMovies = new HashMap<>();

        if (historyStr == null || historyStr.trim().isEmpty()) {
            return watchedMovies;
        }

        // Split by semicolon to get each watch entry
        String[] historyEntries = historyStr.split(";");

        for (String entry : historyEntries) {
            entry = entry.trim();
            if (entry.isEmpty()) {
                continue;
            }

            // Split by "@" to separate movie ID and date (ignore date)
            String[] parts = entry.split("@");
            if (parts.length > 0) {
                String movieId = parts[0].trim();

                // Find Movie object by ID
                Movie movie = findMovieById(movieId, allMovies);
                if (movie != null) {
                    // Update watch count: +1 if exists, 1 if new
                    watchedMovies.put(movie, watchedMovies.getOrDefault(movie, 0) + 1);
                } else {
                    System.err.println("Warning: Movie with ID '" + movieId + "' not found in history.");
                }
            }
        }

        return watchedMovies;
    }

    /**
     * Helper method to find Movie by ID in the movie list
     * @param movieId Movie ID to search
     * @param allMovies List of all movies
     * @return Movie object if found, null otherwise
     */
    private Movie findMovieById(String movieId, List<Movie> allMovies) {
        for (Movie movie : allMovies) {
            if (movie.getId().equals(movieId)) {
                return movie;
            }
        }
        return null;
    }

    /**
     * Helper method to parse ID string (for watchlist)
     * @param idStr ID string separated by semicolon
     * @return List of parsed IDs
     */
    private List<String> parseIdString(String idStr) {
        List<String> ids = new ArrayList<>();
        if (idStr == null || idStr.trim().isEmpty()) {
            return ids;
        }
        String[] idParts = idStr.split(";");
        for (String part : idParts) {
            part = part.trim();
            if (!part.isEmpty()) {
                ids.add(part);
            }
        }
        return ids;
    }
}