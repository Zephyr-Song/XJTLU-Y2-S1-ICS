package cpt111.movie.model;

public class User {
    // Private attributes (encapsulation)
    private final String username;
    private String password; // Store plaintext password directly (remove encryption-related comments)
    private final Watchlist watchlist;
    private final History history;

    public User(String username, String password, Watchlist watchlist, History history) {
        // Validate username legality
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        this.username = username.trim();
        this.password = password.trim(); // Assign plaintext password directly
        this.watchlist = watchlist;
        this.history = history;
    }

    // Getter methods
    public String getUsername() {
        return username;
    }

    // Modification 2: Remove "Encrypted" from Getter method name, return plaintext password directly
    public String getPassword() {
        return password;
    }

    public Watchlist getWatchlist() {
        return watchlist;
    }

    public History getHistory() {
        return history;
    }

    public void updatePassword(String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        this.password = newPassword.trim(); // Assign new plaintext password directly
    }

    public void markAsWatched(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }
        history.addWatchedMovie(movie);
        if (watchlist.containsMovie(movie.getId())) {
            watchlist.removeMovie(movie.getId());
        }
    }

    @Override
    public String toString() {
        return "Username: " + username +
                " | Number of movies in watchlist: " + watchlist.getAllMovies().size() +
                " | Number of movies in watching history: " + history.getAllWatchedMovies().size();
    }
}