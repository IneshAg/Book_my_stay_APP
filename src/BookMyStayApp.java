/**
 * Book My Stay App
 *
 * Full Integrated System (Use Case 1 → 6)
 *
 * Version: 6.5 (Complete Application)
 */

import java.util.*;


/* ============================================================
   ROOM DOMAIN MODEL (Use Case 2)
   ============================================================ */

abstract class Room {

    private String roomType;
    private int beds;
    private double price;

    public Room(String roomType, int beds, double price) {
        this.roomType = roomType;
        this.beds = beds;
        this.price = price;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getPrice() {
        return price;
    }

    public abstract void displayDetails();
}

class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 2000);
    }

    public void displayDetails() {
        System.out.println("Single Room | 1 Bed | ₹2000");
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 3500);
    }

    public void displayDetails() {
        System.out.println("Double Room | 2 Beds | ₹3500");
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 6000);
    }

    public void displayDetails() {
        System.out.println("Suite Room | 3 Beds | ₹6000");
    }
}


/* ============================================================
   INVENTORY SERVICE (Use Case 3)
   ============================================================ */

class RoomInventory {

    private Map<String, Integer> inventory = new HashMap<>();

    public void registerRoom(String type, int count) {
        inventory.put(type, count);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    public void decrement(String type) {
        inventory.put(type, inventory.get(type) - 1);
    }

    public void displayInventory() {
        System.out.println("\n--- Current Inventory ---");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " → Available: " + entry.getValue());
        }
    }
}


/* ============================================================
   SEARCH SERVICE (Use Case 4)
   ============================================================ */

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

        for (String type : roomCatalog.keySet()) {

            int available = inventory.getAvailability(type);

            if (available > 0) {
                roomCatalog.get(type).displayDetails();
                System.out.println("Available: " + available);
                System.out.println("--------------------------");
                found = true;
            }
        }

        if (!found) {
            System.out.println("No rooms currently available.");
        }
    }
}


/* ============================================================
   RESERVATION MODEL (Use Case 5)
   ============================================================ */

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
        return guestName + " → " + roomType;
    }
}


/* ============================================================
   FIFO BOOKING QUEUE (Use Case 5)
   ============================================================ */

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
        System.out.println("Booking request added to queue.");
    }

    public Reservation nextRequest() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}


/* ============================================================
   BOOKING SERVICE (Use Case 6)
   ============================================================ */

class BookingService {

    private RoomInventory inventory;

    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, Set<String>> allocations = new HashMap<>();

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void processReservation(Reservation reservation) {

        String type = reservation.getRoomType();

        if (inventory.getAvailability(type) <= 0) {
            System.out.println("Booking Failed for " + reservation.getGuestName() +
                    " | No availability.");
            return;
        }

        String roomId = generateUniqueRoomId(type);

        allocatedRoomIds.add(roomId);

        allocations
                .computeIfAbsent(type, k -> new HashSet<>())
                .add(roomId);

        inventory.decrement(type);

        System.out.println("Booking Confirmed!");
        System.out.println("Guest: " + reservation.getGuestName());
        System.out.println("Room ID: " + roomId);
        System.out.println("----------------------------");
    }

    private String generateUniqueRoomId(String type) {

        String prefix = type.substring(0, 2).toUpperCase();
        String id;

        do {
            id = prefix + "-" + (100 + new Random().nextInt(900));
        } while (allocatedRoomIds.contains(id));

        return id;
    }

    public void displayAllocations() {

        System.out.println("\n--- Confirmed Allocations ---");

        for (Map.Entry<String, Set<String>> entry : allocations.entrySet()) {
            System.out.println(entry.getKey() + " → " + entry.getValue());
        }
    }
}


/* ============================================================
   MAIN APPLICATION (Use Case 1)
   ============================================================ */

public class BookMyStayApp {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("     Welcome to Book My Stay");
        System.out.println("             Version 6.5");
        System.out.println("=========================================");

        // Initialize Rooms
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Catalog
        Map<String, Room> catalog = new HashMap<>();
        catalog.put(single.getRoomType(), single);
        catalog.put(doubleRoom.getRoomType(), doubleRoom);
        catalog.put(suite.getRoomType(), suite);

        // Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.registerRoom("Single Room", 2);
        inventory.registerRoom("Double Room", 1);
        inventory.registerRoom("Suite Room", 1);

        // Services
        RoomSearchService searchService = new RoomSearchService(inventory, catalog);
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingService bookingService = new BookingService(inventory);

        // SEARCH
        searchService.searchAvailableRooms();

        // REQUEST INTAKE
        System.out.print("\nEnter number of booking requests: ");
        int n = scanner.nextInt();
        scanner.nextLine();

        for (int i = 1; i <= n; i++) {

            System.out.println("\nBooking Request #" + i);

            System.out.print("Guest Name: ");
            String guest = scanner.nextLine();

            System.out.print("Room Type (Single Room / Double Room / Suite Room): ");
            String type = scanner.nextLine();

            queue.addRequest(new Reservation(guest, type));
        }

        // PROCESS BOOKINGS (FIFO)
        System.out.println("\nProcessing Booking Requests...");

        while (!queue.isEmpty()) {
            bookingService.processReservation(queue.nextRequest());
        }

        bookingService.displayAllocations();
        inventory.displayInventory();

        System.out.println("\nThank you for using Book My Stay!");
        scanner.close();
    }
}