/**
 * Book My Stay App
 *
 * Use Case 2: Basic Room Types & Static Availability (Interactive Version)
 *
 * Version: 2.1 (Refactored with User Input)
 *
 * @author 12asascoder
 * @version 2.1
 */

import java.util.Scanner;

// Abstract Class
abstract class Room {

    private String roomType;
    private int numberOfBeds;
    private double pricePerNight;
    private double roomSize;

    public Room(String roomType, int numberOfBeds, double pricePerNight, double roomSize) {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.pricePerNight = pricePerNight;
        this.roomSize = roomSize;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getNumberOfBeds() {
        return numberOfBeds;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public double getRoomSize() {
        return roomSize;
    }

    public abstract void displayRoomDetails();
}


// Single Room
class SingleRoom extends Room {

    public SingleRoom() {
        super("Single Room", 1, 2000.0, 180.0);
    }

    @Override
    public void displayRoomDetails() {
        System.out.println("\nRoom Type: " + getRoomType());
        System.out.println("Beds: " + getNumberOfBeds());
        System.out.println("Size: " + getRoomSize() + " sq.ft");
        System.out.println("Price per Night: ₹" + getPricePerNight());
    }
}


// Double Room
class DoubleRoom extends Room {

    public DoubleRoom() {
        super("Double Room", 2, 3500.0, 250.0);
    }

    @Override
    public void displayRoomDetails() {
        System.out.println("\nRoom Type: " + getRoomType());
        System.out.println("Beds: " + getNumberOfBeds());
        System.out.println("Size: " + getRoomSize() + " sq.ft");
        System.out.println("Price per Night: ₹" + getPricePerNight());
    }
}


// Suite Room
class SuiteRoom extends Room {

    public SuiteRoom() {
        super("Suite Room", 3, 6000.0, 400.0);
    }

    @Override
    public void displayRoomDetails() {
        System.out.println("\nRoom Type: " + getRoomType());
        System.out.println("Beds: " + getNumberOfBeds());
        System.out.println("Size: " + getRoomSize() + " sq.ft");
        System.out.println("Price per Night: ₹" + getPricePerNight());
    }
}


// Main Application
public class UseCase2RoomInitialization {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("     Book My Stay - Version 2.1");
        System.out.println("=========================================");

        // Create Room Objects
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        // Static availability variables
        int singleRoomAvailability = 10;
        int doubleRoomAvailability = 5;
        int suiteRoomAvailability = 2;

        // Display Menu
        System.out.println("\nSelect a Room Type:");
        System.out.println("1. Single Room");
        System.out.println("2. Double Room");
        System.out.println("3. Suite Room");
        System.out.print("Enter your choice (1-3): ");

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                singleRoom.displayRoomDetails();
                System.out.println("Available Units: " + singleRoomAvailability);
                break;

            case 2:
                doubleRoom.displayRoomDetails();
                System.out.println("Available Units: " + doubleRoomAvailability);
                break;

            case 3:
                suiteRoom.displayRoomDetails();
                System.out.println("Available Units: " + suiteRoomAvailability);
                break;

            default:
                System.out.println("Invalid selection. Please restart the application.");
        }

        System.out.println("\nApplication terminated successfully.");
        scanner.close();
    }
}