/**
 * Book My Stay App
 *
 * Use Case 4: Room Search & Availability Check
 *
 * This version introduces read-only search functionality that retrieves
 * available rooms without modifying inventory state.
 *
 * Version: 4.0
 *
 * @author 12asascoder
 * @version 4.0
 */

import java.util.HashMap;
import java.util.Map;


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
   Centralized Inventory (State Holder)
   =========================== */

class RoomInventory {

    private Map<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
    }

    public void registerRoomType(String roomType, int count) {
        inventory.put(roomType, count);
    }

    // Read-only access
    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    // Expose entire inventory safely (read-only usage expected)
    public Map<String, Integer> getAllAvailability() {
        return inventory;
    }
}


/* ===========================
   Search Service (Read-Only Layer)
   =========================== */

class RoomSearchService {

    private RoomInventory inventory;
    private Map<String, Room> roomCatalog;

    public RoomSearchService(RoomInventory inventory, Map<String, Room> roomCatalog) {
        this.inventory = inventory;
        this.roomCatalog = roomCatalog;
    }

    public void searchAvailableRooms() {

        System.out.println("\n--- Available Rooms ---");

        boolean found = false;

        for (String roomType : roomCatalog.keySet()) {

            int available = inventory.getAvailability(roomType);

            // Defensive programming: Only show valid & available rooms
            if (available > 0) {

                Room room = roomCatalog.get(roomType);

                if (room != null) {
                    room.displayRoomDetails();
                    System.out.println("Available Units: " + available);
                    System.out.println("---------------------------------");
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("No rooms currently available.");
        }
    }
}


/* ===========================
   Application Entry Point
   =========================== */

public class UseCase4RoomSearch {

    public static void main(String[] args) {

        System.out.println("=========================================");
        System.out.println("     Book My Stay - Version 4.0");
        System.out.println("=========================================");

        // Domain Objects
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        // Centralized Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.registerRoomType(singleRoom.getRoomType(), 10);
        inventory.registerRoomType(doubleRoom.getRoomType(), 5);
        inventory.registerRoomType(suiteRoom.getRoomType(), 0); // intentionally unavailable

        // Room Catalog (Domain Mapping)
        Map<String, Room> roomCatalog = new HashMap<>();
        roomCatalog.put(singleRoom.getRoomType(), singleRoom);
        roomCatalog.put(doubleRoom.getRoomType(), doubleRoom);
        roomCatalog.put(suiteRoom.getRoomType(), suiteRoom);

        // Search Service (Read-Only Access)
        RoomSearchService searchService = new RoomSearchService(inventory, roomCatalog);

        // Perform Search (NO inventory mutation)
        searchService.searchAvailableRooms();

        System.out.println("\nSearch completed. Inventory state unchanged.");
        System.out.println("Application terminated successfully.");
    }
}