package cpt111.movie;

import cpt111.movie.io.FileHandler;
import cpt111.movie.model.Movie;
import cpt111.movie.model.User;
import cpt111.movie.recommendation.RecommendationEngine;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    // Global components (persist throughout program lifecycle after initialization)
    private static final FileHandler fileHandler = new FileHandler();
    private static final RecommendationEngine recommendationEngine = new RecommendationEngine();
    private static List<Movie> allMovies; // All movies in the system
    private static Map<String, User> allUsers; // All users in the system
    private static final Scanner scanner = new Scanner(System.in); // Receive user input

    public static void main(String[] args) {
        // ==================== Path Debug Output ====================
        System.out.println("===== Path Debug Information =====");
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current working directory (user.dir): " + workingDir);
        System.out.println("\n===== Movie Recommendation and Tracking System =====");
        allMovies = fileHandler.loadAllMovies();
        allUsers = fileHandler.loadAllUsers(allMovies);
        System.out.println("Data loaded successfully! Total movies: " + (allMovies != null ? allMovies.size() : 0) + ", Total users: " + (allUsers != null ? allUsers.size() : 0));

        showLoginMenu();
        int choice = getValidIntegerInput("Please enter your choice (1-2): ", 1, 2);
        if (choice == -1) {
            System.out.println("Too many invalid inputs. Exiting program...\n" +"Thank you for using!");
            scanner.close();
            return;
        }
        switch (choice) {
            case 1:
                // Login process
                User loggedInUser = login();
                if (loggedInUser != null) {
                 // Login successful, enter logged-in menu
                    int menuStatus = showLoggedInMenu(loggedInUser);
                    if(menuStatus == -3){
                        return;
                    }
                    else if(menuStatus==-2){
                        System.out.println("\nSaving data...");
                        System.out.println("Save path: " + workingDir + "/data/users.csv");
                        if (fileHandler.saveAllUsers(allUsers)) {
                            System.out.println("Data saved successfully. Thank you for using!");
                        } else {
                            System.out.println("Failed to save data. Please check file permissions or if the path exists!");
                        }
                    }
                }
                break;
            case 2:
                System.out.println("\nSaving data...");
                System.out.println("Save path: " + workingDir + "/data/users.csv");
                if (fileHandler.saveAllUsers(allUsers)) {
                    System.out.println("Data saved successfully. Thank you for using!");
                } else {
                    System.out.println("Failed to save data. Please check file permissions or if the path exists!");
                }
                scanner.close();
                return; // Exit program
            default:
                System.out.println("Invalid choice. Please try again!");
        }
    }

    private static void showLoginMenu() {
        System.out.println("\n===== Unlogged-in Menu =====");
        System.out.println("1. Login");
        System.out.println("2. Exit Program");
    }

    private static int showLoggedInMenu(User user) {
        while (true) {
            System.out.println("\n===== Welcome, " + user.getUsername() + " =====");
            System.out.println("1. Browse All Movies or Add Movie to Watchlist");
            System.out.println("2. Remove Movie from Watchlist");
            System.out.println("3. View My Watchlist");
            System.out.println("4. Mark Movie as Watched");
            System.out.println("5. View My Watch History");
            System.out.println("6. Get Movie Recommendations");
            System.out.println("7. Switch Recommendation Strategy");
            System.out.println("8. Change Password");
            System.out.println("9. Logout");

            int choice = getValidIntegerInput("Please enter your choice (1-9): ", 1, 9);
            if(choice == -1){
                System.out.println("Too many invalid inputs. Exiting program...\n" +"Thank you for using!");
                return -3;
            }
            try {
                switch (choice) {
                    case 1:
                        browseAllMovies(user);
                        break;
                    case 2:
                        removeFromWatchlist(user);
                        break;
                    case 3:
                        viewWatchlist(user);
                        break;
                    case 4:
                        markAsWatched(user);
                        break;
                    case 5:
                        viewHistory(user);
                        break;
                    case 6:
                        getRecommendations(user);
                        break;
                    case 7:
                        switchRecommendationStrategy(user);
                        break;
                    case 8:
                        updatePassword(user);
                        break;
                    case 9:
                        System.out.println("Logged out successfully!");
                        return -2;
                    default:
                        System.out.println("Invalid choice. Please try again!");
                }
            }catch (RuntimeException e) {
                if ("TO_EXIT".equals(e.getMessage())) {
                    return -2;
                }
                throw e;
            }
        }
    }

    private static User login() {
        int maxAttempts = 3;
        int attempts = 0;

        while(attempts < maxAttempts) {
            System.out.println("\n===== Login =====");
            System.out.print("Please enter username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Please enter password: ");
            String plainPassword = scanner.nextLine().trim();

            // 1. Check if user exists
            if (!allUsers.containsKey(username)) {
                System.out.println("Error: Username does not exist!");
            }
            else{
                User user = allUsers.get(username);
                // 2. Verify password (plaintext comparison)
                if (plainPassword.equals(user.getPassword())) {
                    System.out.println("Login successful!");
                    return user;
                }
                else {
                    System.out.println("Error: Incorrect password!");
                }
            }

            attempts++;
            int remaining = maxAttempts - attempts;
            if (remaining > 0) {
                int choice = getValidIntegerInput(
                        "Enter 1 to retry (" + remaining + " attempts left), 2 to exit: ",
                        1, 2);
                if (choice == 2) {
                    System.out.println("Exiting program...\n" + "Thank you for using!");
                    scanner.close();
                    return null;
                }
            }
            else {
                System.out.println("The number of errors exceeds the limit. Exiting program...\n"
                        +"Thank you for using!");
            }
        }
        return null;
    }

    private static void browseAllMovies(User user) {
        System.out.println("\n===== All Movies =====");
        if (allMovies.isEmpty()) {
            System.out.println("No movie data available!");
            return;
        }
        // Pagination display (10 movies per page)
        int pageSize = 10;
        int totalPages = (allMovies.size() + pageSize - 1) / pageSize; // Round up
        int currentPage = 1;
        printMoviePage(currentPage, pageSize, totalPages);

        while (true) {
            // Pagination control
            if (totalPages == 1) {
                break; // Only 1 page, end directly
            }
            System.out.print("Enter 'n' for next page, 'p' for previous page," +
                    " 'q' to return to the main interface, 'r' for adding movie to your own watchlist : ");
            String input = scanner.nextLine().trim().toLowerCase();

            boolean isInputValid = true;
            if(input.equals("r")){
                addToWatchlist(user);
            }
            else if (input.equals("q")) {
                break;
            }
            else if (input.equals("n") && currentPage < totalPages) {
                currentPage++;
                printMoviePage(currentPage, pageSize, totalPages);
            }
            else if(input.equals("n") && currentPage ==totalPages){
                System.out.println("Invalid input, the current page is the last page. Please try again!");
                isInputValid = false;
            }
            else if (input.equals("p") && currentPage > 1) {
                currentPage--;
                printMoviePage(currentPage, pageSize, totalPages);
            }
            else if(input.equals("p") && currentPage == 1){
                System.out.println("Invalid input, the current page is the first page. Please try again!");
                isInputValid = false;
            }
            else {
                System.out.println("Invalid input. Please try again!");
                isInputValid = false;
            }
            if (!isInputValid) {
                continue;
            }
        }
    }

    private static void printMoviePage(int currentPage, int pageSize, int totalPages) {
        // Calculate movie range for current page
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, allMovies.size());
        List<Movie> currentPageMovies = allMovies.subList(start, end);

        // Output current page movies
        System.out.println("Page " + currentPage + "/" + totalPages);
        System.out.println("------------------------------------------------------------------");
        for (Movie movie : currentPageMovies) {
            System.out.println(movie);
        }
        System.out.println("------------------------------------------------------------------");
    }

    private static void addToWatchlist(User user) {
        System.out.println("\n===== Add Movie to Watchlist =====");
        // 1. Get movie ID input from user (changed to String type to match Movie's String ID)
        while(true) {
            System.out.print("Please enter the ID of the movie to add: ");
            String movieId = scanner.nextLine().trim();

            // 2. Find the movie object
            Movie movieToAdd = findMovieById(movieId);
            if (movieToAdd == null) {
                System.out.println("Error: Movie with ID " + movieId + " not found!");
                int choice = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ", 1, 2);
                if (choice == 2) {
                    return;
                }
            } else {
                // 3. Add to watchlist (handle exceptions)
                try {
                    user.getWatchlist().addMovie(movieToAdd);
                    System.out.println("Successfully added \"" + movieToAdd.getTitle() + "\" to your watchlist!");
                    break;
                }
                catch (IllegalStateException e) {
                    System.out.println("Error: " + e.getMessage());
                    int choice = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ", 1, 2);
                    if (choice == 2) {
                        return;
                    }
                }
            }
        }
    }

    private static void removeFromWatchlist(User user) {
        System.out.println("\n===== Remove Movie from Watchlist =====");
        // 1. First, view the user's watchlist
        printWatchlist(user);
        while(true) {
            // 2. Get movie ID input from user (changed to String type)
            System.out.print("Please enter the ID of the movie to remove: ");
            String movieId = scanner.nextLine().trim();

            Movie movieToRemove = findMovieById(movieId);
            if (movieToRemove == null) {
                System.out.println("Error: Movie with ID " + movieId + " not found!");
                int choice = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ", 1, 2);
                if (choice == 2) {
                    return;
                }
            } else {
                try {
                // 3. Remove from watchlist (handle exceptions)
                    user.getWatchlist().removeMovie(movieId);
                    System.out.println("Successfully removed this movie from your watchlist!");
                    int choice = getValidIntegerInput("Enter 1 to view the latest watchlist, 2 to return to the main interface: ", 1, 2);
                    if (choice == 1) {
                        printWatchlist(user);
                        int choice2 = getValidIntegerInput("Enter 3 to return to the main interface, 4 to exit: ", 3, 4);
                        if (choice2 == 4) {
                            throw new RuntimeException("TO_EXIT");
                        }
                    }
                    break;
                } catch (IllegalStateException e) {
                    System.out.println("Error: " + e.getMessage());
                    int choice = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ", 1, 2);
                    if (choice == 2) {
                        return;
                    }
                }
            }
        }
    }

    private static void viewWatchlist(User user) {
        System.out.println("\n===== Your Watchlist =====");
        printWatchlist(user);
        int choice = getValidIntegerInput("Enter 1 to return to the main interface, 2 to exit: ", 1, 2);
        if (choice==2){
            throw new RuntimeException("TO_EXIT");
        }
    }

    private static void printWatchlist(User user) {
        List<Movie> watchlistMovies = user.getWatchlist().getAllMovies();
        int count_movies = watchlistMovies.size();
        if (watchlistMovies.isEmpty()) {
            System.out.println("Your watchlist is empty!");
            return;
        }
        System.out.println("------------------------------------------------------------------");
        System.out.println("Total " + count_movies + " movies in watchlist");
        for (Movie movie : watchlistMovies) {
            System.out.println(movie);
        }
        System.out.println("------------------------------------------------------------------");
    }

    private static void markAsWatched(User user) {
        System.out.println("\n===== Mark Movie as Watched =====");
        while(true) {
            // 1. Get movie ID input from user (changed to String type)
            System.out.print("Please enter the ID of the movie to mark: ");
            String movieId = scanner.nextLine().trim();

            // 2. Find the movie object
            Movie movieToMark = findMovieById(movieId);
            if (movieToMark == null) {
                System.out.println("Error: Movie with ID " + movieId + " not found!");
                int choice = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ",1,2);
                if (choice == 2) {
                    return;
                }
            }
            else {
                try {
                    // 3. Mark as watched (call core business method from User class)
                    user.markAsWatched(movieToMark);
                    System.out.println("Successfully marked \"" + movieToMark.getTitle() + "\" as watched!");
                    int choice = getValidIntegerInput("Enter 1 to return to the main interface, 2 to exit: ", 1, 2);
                    if (choice == 2) {
                        throw new RuntimeException("TO_EXIT");
                    }
                    break;
                } catch (IllegalStateException e) {
                    System.out.println("Error: " + e.getMessage());
                    int choice = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ", 1, 2);
                    if (choice == 2) {
                        return;
                    }
                }
            }
        }
    }

    private static void viewHistory(User user) {
        System.out.println("\n===== My Watch History =====");
        Map<Movie, Integer> watchedMovies = user.getHistory().getAllWatchedMovies();
        if (watchedMovies.isEmpty()) {
            System.out.println("Your watch history is empty, go mark some movies as watched!");
            int choice = getValidIntegerInput("Enter 1 to return to the main interface, 2 to exit: ",1,2);
            if (choice == 2) {
                throw new RuntimeException("TO_EXIT");
            }
            return;
        }
        for (Map.Entry<Movie, Integer> entry : watchedMovies.entrySet()) {
            Movie movie = entry.getKey();
            int watchCount = entry.getValue();
            System.out.println(movie + " | Watch Count: " + watchCount);
        }
        System.out.println("Total " + watchedMovies.size() + " different movies in history");
        int choice = getValidIntegerInput("Enter 1 to return to the main interface, 2 to exit: ", 1, 2);
        if (choice == 2) {
            throw new RuntimeException("TO_EXIT");
        }
    }

    private static void getRecommendations(User user) {
        System.out.println("\n===== Get Movie Recommendations =====");
        RecommendationEngine.StrategyType currentStrategy = recommendationEngine.getCurrentStrategyType();
        String strategyName = switch (currentStrategy) {
            case GENRE -> "Genre";
            case YEAR -> "Year";
            case RATING -> "Rating";
        };
        System.out.println("Current recommendation strategy: " + strategyName);

        int topN = getValidIntegerInput("Please enter the number of recommendations (1-20): ", 1, 20);
        List<Movie> recommendations = recommendationEngine.getRecommendations(user, allMovies, topN);
        if (recommendations.isEmpty()) {
            System.out.println("No recommendations available. Try marking more movies as watched!");
            int choice = getValidIntegerInput("Enter 1 to return to the main interface, 2 to exit: ", 1, 2);
            if (choice == 2) {
                throw new RuntimeException("TO_EXIT");
            }
        }

        System.out.println("\nThere are some recommended movies for you:");
        System.out.println("------------------------------------------------------------------");
        for (int i = 0; i < recommendations.size(); i++) {
            Movie movie = recommendations.get(i);
            System.out.printf("%d. %s (Genre: %s, Year: %d, Rating: %.1f)%n",
                    i + 1, movie.getTitle(), movie.getGenre(), movie.getReleaseYear(), movie.getRating());
        }
        System.out.println("------------------------------------------------------------------");
        int choice = getValidIntegerInput("Enter 1 to return to the main interface, 2 to exit: ",1,2);
        if (choice == 2) {
            throw new RuntimeException("TO_EXIT");
        }
    }

    private static void switchRecommendationStrategy(User user) {
        System.out.println("\n===== Switch Recommendation Strategy =====");
        System.out.println("1. Recommend by Genre (Default)");
        System.out.println("2. Recommend by Release Year");
        System.out.println("3. Recommend by High Rating");
        int choice = getValidIntegerInput("Please enter your choice (1-3): ", 1, 3);

        switch (choice) {
            case 1:
                recommendationEngine.setStrategy(RecommendationEngine.StrategyType.GENRE);
                break;
            case 2:
                recommendationEngine.setStrategy(RecommendationEngine.StrategyType.YEAR);
                System.out.println("Successfully switched to Year-Based Strategy!");
                break;
            case 3:
                recommendationEngine.setStrategy(RecommendationEngine.StrategyType.RATING);
                System.out.println("Successfully switched to Rating-Based Strategy!");
                break;
        }
        while (true) {
            int nextChoice = getValidIntegerInput(
                    "Enter 1 to go to the recommendation interface, 2 to entry, 3 to return to the main interface: ",
                    1, 3);
            if (nextChoice == 1) {
                getRecommendations(user);
                break;
            } else if (nextChoice == 2) {
                switchRecommendationStrategy(user);
                break;
            } else if (nextChoice == 3) {
                break;
            }
        }
    }

    private static void updatePassword(User user) {
        System.out.println("\n===== Change Password =====");
        while(true) {
            // 1. Verify old password (direct plaintext comparison)
            System.out.print("Please enter your old password: ");
            String oldPassword = scanner.nextLine().trim();
            if (!oldPassword.equals(user.getPassword())) {
                System.out.println("Error: Incorrect old password!");
                int choice = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ", 1, 2);
                if (choice == 2) {
                    return;
                }
            }
            else {
                // 2. Enter new password (confirm twice)
                System.out.print("\nPlease enter your new password: ");
                String newPassword = scanner.nextLine().trim();
                System.out.print("Please re-enter your new password: ");
                String confirmPassword = scanner.nextLine().trim();
                if (!newPassword.equals(confirmPassword)) {
                    System.out.println("\nError: New passwords do not match!");
                    int choice1 = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ", 1, 2);
                    if (choice1 == 2) {
                        return;
                    }
                }
                else if (newPassword.length() < 6){
                        System.out.println("\nThe password must be at least 6 characters long!");
                        int choice2 = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ", 1, 2);
                        if (choice2 == 2) {
                            return;
                        }
                }
                // 3. Update password directly in plaintext
                else {
                    user.updatePassword(newPassword);
                    boolean saveSuccess = fileHandler.saveAllUsers(allUsers); // Force save to CSV
                    if (saveSuccess) {
                        System.out.println("Password changed successfully! Please use your new password next time you log in.");
                        int choice3 = getValidIntegerInput("Enter 1 to return to the main interface, 2 to exit: ", 1, 2);
                        if (choice3 == 1) {
                            return;
                        }
                        else if(choice3 == 2){
                            throw new RuntimeException("TO_EXIT");
                        }
                    } else {
                        System.out.println("Password changed successfully, but failed to save data. Please check file permissions!");
                        int choice4 = getValidIntegerInput("Enter 1 to retry, 2 to return to the main interface: ", 1, 2);
                        if (choice4 == 2) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private static Movie findMovieById(String movieId) {
        for (Movie movie : allMovies) {
            if (movie.getId().equals(movieId)) {
                return movie;
            }
        }
        return null;
    }

    private static int getValidIntegerInput(String prompt, int min, int max) {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print(prompt);
            try {
                int input = Integer.parseInt(scanner.nextLine().trim());
                if (input >= min && input <= max) {
                    return input;
                } else {
                    attempts++;
                    if (attempts <3) {
                        System.out.println("Input out of range. Please enter an integer between " + min + " and " + max + "!");
                        System.out.println("If input is invalid for three times, system will automatically exit. " +
                                "\nRemaining attempts: " + (3 - attempts) + "\n");
                    }
                }
            } catch (NumberFormatException e) {
                attempts++;
                if (attempts <3) {
                    System.out.println("Invalid input format. Please enter a valid integer, between " + min + " and " + max + "!");
                    System.out.println("If input is invalid for three times, system will automatically exit. " +
                            "\nRemaining attempts: " + (3 - attempts) + "\n");
                }
            }
        }
        return -1;
    }
}