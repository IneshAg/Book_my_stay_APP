/**
 * Book My Stay App
 *
 * This class represents the entry point of the Hotel Booking Management System.
 * It demonstrates how a Java application begins execution using the main() method
 * and prints a welcome message along with application details.
 *
 * The purpose of this use case is to establish a predictable startup flow
 * and illustrate core Java fundamentals such as:
 * - Class structure
 * - main() method
 * - static keyword
 * - Console output
 * - String literals
 * - Linear execution flow
 *
 * @author 12asascoder
 * @version 1.0
 */
public class UseCase1HotelBookingApp {

    /**
     * Entry point of the application.
     * The JVM invokes this method when the program is executed.
     *
     * @param args Command-line arguments (not used in this application)
     */
    public static void main(String[] args) {

        // Application Name and Version
        String appName = "Book My Stay - Hotel Booking Management System";
        String version = "Version 1.0";

        // Welcome Message
        System.out.println("=========================================");
        System.out.println("        Welcome to " + appName);
        System.out.println("                " + version);
        System.out.println("=========================================");
        System.out.println("Application started successfully.");
        System.out.println("Thank you for using Book My Stay!");

        // Program terminates naturally after execution
    }
}