/**
 * Book My Stay App
 *
 * Use Case 3: Centralized Room Inventory Management
 *
 * This version replaces scattered availability variables with a centralized
 * inventory component using HashMap.
 *
 * Version: 3.1 (Refactored)
 *
 * @author 12asascoder
 * @version 3.1
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/* ===========================
   Room Domain Model
   =========================== */

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


/* ===========================
   Centralized Inventory Class
   =========================== */

class RoomInventory {

    // Single Source of Truth
    private Map<String, Integer> inventory;

    // Constructor initializes inventory
    public RoomInventory() {
        inventory = new HashMap<>();
    }

    // Register room type with availability
    public void registerRoomType(String roomType, int count) {
        inventory.put(roomType, count);
    }

    // Get current availability (O(1))
    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    // Controlled update (reduce count safely)
    public boolean bookRoom(String roomType) {
        int available = getAvailability(roomType);

        if (available > 0) {
            inventory.put(roomType, available - 1);
            return true;
        }
        return false;
    }

    // Display full inventory state
    public void displayInventory() {
        System.out.println("\n--- Current Room Inventory ---");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " → Available: " + entry.getValue());
        }
    }
}


/* ===========================
   Application Entry Point
   =========================== */

public class UseCase3InventorySetup {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("     Book My Stay - Version 3.1");
        System.out.println("=========================================");

        // Initialize Room Objects (Domain)
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        // Initialize Inventory (Centralized State)
        RoomInventory inventory = new RoomInventory();

        inventory.registerRoomType(singleRoom.getRoomType(), 10);
        inventory.registerRoomType(doubleRoom.getRoomType(), 5);
        inventory.registerRoomType(suiteRoom.getRoomType(), 2);

        inventory.displayInventory();

        System.out.println("\nSelect a room type to book:");
        System.out.println("1. Single Room");
        System.out.println("2. Double Room");
        System.out.println("3. Suite Room");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        String selectedRoomType = null;
        Room selectedRoom = null;

        switch (choice) {
            case 1:
                selectedRoomType = singleRoom.getRoomType();
                selectedRoom = singleRoom;
                break;
            case 2:
                selectedRoomType = doubleRoom.getRoomType();
                selectedRoom = doubleRoom;
                break;
            case 3:
                selectedRoomType = suiteRoom.getRoomType();
                selectedRoom = suiteRoom;
                break;
            default:
                System.out.println("Invalid selection.");
                scanner.close();
                return;
        }

        selectedRoom.displayRoomDetails();

        boolean booked = inventory.bookRoom(selectedRoomType);

        if (booked) {
            System.out.println("\nBooking successful!");
        } else {
            System.out.println("\nBooking failed. No rooms available.");
        }

        inventory.displayInventory();

        System.out.println("\nApplication terminated successfully.");
        scanner.close();
    }
}