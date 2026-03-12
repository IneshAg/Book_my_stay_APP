/**
 * Book My Stay App
 *
 * Full Integrated System (Use Case 1 → 7)
 *
 * Version: 7.0 (Add-On Service Selection)
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
    private String reservationId;

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

    public void setReservationId(String id) {
        this.reservationId = id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String toString() {
        return guestName + " → " + roomType + " | ID: " + reservationId;
    }
}


/* ============================================================
   FIFO BOOKING QUEUE
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
    private Map<String, Reservation> confirmedReservations = new HashMap<>();

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public Reservation processReservation(Reservation reservation) {

        String type = reservation.getRoomType();

        if (inventory.getAvailability(type) <= 0) {
            System.out.println("Booking Failed for " + reservation.getGuestName());
            return null;
        }

        String roomId = generateUniqueRoomId(type);

        allocatedRoomIds.add(roomId);
        reservation.setReservationId(roomId);
        confirmedReservations.put(roomId, reservation);

        inventory.decrement(type);

        System.out.println("Booking Confirmed!");
        System.out.println(reservation);

        return reservation;
    }

    private String generateUniqueRoomId(String type) {

        String prefix = type.substring(0, 2).toUpperCase();
        String id;

        do {
            id = prefix + "-" + (100 + new Random().nextInt(900));
        } while (allocatedRoomIds.contains(id));

        return id;
    }

    public Map<String, Reservation> getConfirmedReservations() {
        return confirmedReservations;
    }
}


/* ============================================================
   ADD-ON SERVICE MODEL (Use Case 7)
   ============================================================ */

class AddOnService {

    private String serviceName;
    private double cost;

    public AddOnService(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }

    public String toString() {
        return serviceName + " (₹" + cost + ")";
    }
}


/* ============================================================
   ADD-ON SERVICE MANAGER
   ============================================================ */

class AddOnServiceManager {

    private Map<String, List<AddOnService>> serviceMap = new HashMap<>();

    public void addService(String reservationId, AddOnService service) {

        serviceMap
                .computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);

        System.out.println("Service added to Reservation ID: " + reservationId);
    }

    public double calculateTotalServiceCost(String reservationId) {

        double total = 0;

        List<AddOnService> list = serviceMap.get(reservationId);

        if (list != null) {
            for (AddOnService s : list)
                total += s.getCost();
        }

        return total;
    }

    public void displayServices(String reservationId) {

        System.out.println("\nAdd-On Services for " + reservationId);

        List<AddOnService> list = serviceMap.get(reservationId);

        if (list == null) {
            System.out.println("No services selected.");
            return;
        }

        for (AddOnService s : list)
            System.out.println(s);

        System.out.println("Total Service Cost = ₹" +
                calculateTotalServiceCost(reservationId));
    }
}


/* ============================================================
   MAIN APPLICATION
   ============================================================ */

public class UseCase7AddOnServiceSelection {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        RoomInventory inventory = new RoomInventory();
        inventory.registerRoom("Single Room", 2);
        inventory.registerRoom("Double Room", 1);
        inventory.registerRoom("Suite Room", 1);

        Map<String, Room> catalog = new HashMap<>();
        catalog.put("Single Room", new SingleRoom());
        catalog.put("Double Room", new DoubleRoom());
        catalog.put("Suite Room", new SuiteRoom());

        RoomSearchService search = new RoomSearchService(inventory, catalog);
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingService booking = new BookingService(inventory);
        AddOnServiceManager addOnManager = new AddOnServiceManager();

        search.searchAvailableRooms();

        System.out.print("\nEnter booking requests: ");
        int n = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < n; i++) {

            System.out.print("Guest Name: ");
            String g = sc.nextLine();

            System.out.print("Room Type: ");
            String t = sc.nextLine();

            queue.addRequest(new Reservation(g, t));
        }

        List<Reservation> confirmed = new ArrayList<>();

        while (!queue.isEmpty()) {
            Reservation r = booking.processReservation(queue.nextRequest());
            if (r != null)
                confirmed.add(r);
        }

        /* ===== ADD-ON SERVICE FLOW ===== */

        for (Reservation r : confirmed) {

            System.out.println("\nSelect services for Reservation ID: "
                    + r.getReservationId());

            System.out.print("Number of services: ");
            int s = sc.nextInt();
            sc.nextLine();

            for (int i = 0; i < s; i++) {

                System.out.print("Service Name: ");
                String name = sc.nextLine();

                System.out.print("Service Cost: ");
                double cost = sc.nextDouble();
                sc.nextLine();

                addOnManager.addService(
                        r.getReservationId(),
                        new AddOnService(name, cost)
                );
            }

            addOnManager.displayServices(r.getReservationId());
        }

        inventory.displayInventory();

        System.out.println("\nThank You for using Book My Stay");
    }
}