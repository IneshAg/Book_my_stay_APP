/**
 * Book My Stay App
 *
 * Use Case 5: Booking Request (First-Come-First-Served)
 *
 * This version integrates:
 * - Room Domain Model
 * - Centralized Inventory
 * - Read-only Search visibility
 * - FIFO Booking Request Queue
 *
 * NOTE:
 * No inventory mutation occurs at this stage.
 * Requests are only collected and preserved in arrival order.
 *
 * Version: 5.1 (Refactored & Integrated)
 */

import java.util.*;


/* ===========================
   Room Domain Model
   =========================== */

abstract class Room {

    private String roomType;
    private int numberOfBeds;
    private double pricePerNight;

    public Room(String roomType, int numberOfBeds, double pricePerNight) {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public abstract void displayRoomDetails();
}

class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 2000.0);
    }

    public void displayRoomDetails() {
        System.out.println("Single Room | ₹" + getPricePerNight());
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 3500.0);
    }

    public void displayRoomDetails() {
        System.out.println("Double Room | ₹" + getPricePerNight());
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 6000.0);
    }

    public void displayRoomDetails() {
        System.out.println("Suite Room | ₹" + getPricePerNight());
    }
}


/* ===========================
   Centralized Inventory (Read-Only in UC5)
   =========================== */

class RoomInventory {

    private Map<String, Integer> inventory = new HashMap<>();

    public void registerRoomType(String roomType, int count) {
        inventory.put(roomType, count);
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public void displayInventory() {
        System.out.println("\n--- Current Inventory ---");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " → Available: " + entry.getValue());
        }
    }
}


/* ===========================
   Reservation Model
   =========================== */

class Reservation {

    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public String toString() {
        return "Guest: " + guestName + " | Requested: " + roomType;
    }
}


/* ===========================
   Booking Request Queue (FIFO)
   =========================== */

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void submitRequest(Reservation reservation) {
        queue.offer(reservation);
        System.out.println("Request queued successfully.");
    }

    public void displayQueue() {
        if (queue.isEmpty()) {
            System.out.println("No pending booking requests.");
            return;
        }

        System.out.println("\n--- Booking Requests (FIFO Order) ---");
        for (Reservation r : queue) {
            System.out.println(r);
        }
    }

    public int size() {
        return queue.size();
    }
}


/* ===========================
   Application Entry Point
   =========================== */

public class UseCase5BookingRequestQueue {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("     Book My Stay - Version 5.1");
        System.out.println("=========================================");

        // Initialize Domain
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Initialize Inventory (State Holder)
        RoomInventory inventory = new RoomInventory();
        inventory.registerRoomType(single.getRoomType(), 2);
        inventory.registerRoomType(doubleRoom.getRoomType(), 1);
        inventory.registerRoomType(suite.getRoomType(), 1);

        inventory.displayInventory();

        // Initialize Queue
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        System.out.print("\nEnter number of booking requests: ");
        int n = scanner.nextInt();
        scanner.nextLine();

        for (int i = 1; i <= n; i++) {

            System.out.println("\nBooking Request #" + i);

            System.out.print("Guest Name: ");
            String guest = scanner.nextLine();

            System.out.print("Room Type (Single Room / Double Room / Suite Room): ");
            String roomType = scanner.nextLine();

            // Defensive validation
            if (inventory.getAvailability(roomType) >= 0) {
                Reservation reservation = new Reservation(guest, roomType);
                bookingQueue.submitRequest(reservation);
            } else {
                System.out.println("Invalid room type. Request rejected.");
            }
        }

        bookingQueue.displayQueue();

        System.out.println("\nTotal Pending Requests: " + bookingQueue.size());

        System.out.println("\nNOTE:");
        System.out.println("No rooms allocated yet.");
        System.out.println("Inventory remains unchanged.");
        System.out.println("Requests are waiting for allocation processing.");

        scanner.close();
    }
}