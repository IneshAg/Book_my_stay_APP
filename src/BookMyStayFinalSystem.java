/**
 * BOOK MY STAY – FINAL CONSOLIDATED SYSTEM
 *
 * Covers Use Case 1 → 8
 * Version 8.5 – Industry Structured Model
 */

import java.util.*;


/* ============================================================
   ROOM DOMAIN MODEL
   ============================================================ */

abstract class Room {

    private String type;
    private int beds;
    private double price;

    public Room(String type, int beds, double price) {
        this.type = type;
        this.beds = beds;
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public abstract void display();
}

class SingleRoom extends Room {
    public SingleRoom() { super("Single Room",1,2000); }
    public void display() { System.out.println("Single Room | 1 Bed | ₹2000"); }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super("Double Room",2,3500); }
    public void display() { System.out.println("Double Room | 2 Beds | ₹3500"); }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super("Suite Room",3,6000); }
    public void display() { System.out.println("Suite Room | 3 Beds | ₹6000"); }
}


/* ============================================================
   INVENTORY
   ============================================================ */

class RoomInventory {

    private Map<String,Integer> stock = new HashMap<>();

    public void addRoomType(String type,int count) {
        stock.put(type,count);
    }

    public int available(String type) {
        return stock.getOrDefault(type,0);
    }

    public void reduce(String type) {
        stock.put(type,stock.get(type)-1);
    }

    public void showInventory() {
        System.out.println("\n--- Inventory Status ---");
        for(String k:stock.keySet())
            System.out.println(k+" : "+stock.get(k));
    }
}


/* ============================================================
   SEARCH SERVICE
   ============================================================ */

class SearchService {

    private RoomInventory inventory;
    private Map<String,Room> catalog;

    public SearchService(RoomInventory inventory,
                         Map<String,Room> catalog) {
        this.inventory = inventory;
        this.catalog = catalog;
    }

    public void showAvailable() {

        System.out.println("\n=== AVAILABLE ROOMS ===");

        for(String type:catalog.keySet()) {

            int a = inventory.available(type);

            if(a>0) {
                catalog.get(type).display();
                System.out.println("Available: "+a);
            }
        }
    }
}


/* ============================================================
   RESERVATION MODEL
   ============================================================ */

class Reservation {

    private String guest;
    private String roomType;
    private String reservationId;

    public Reservation(String guest,String roomType) {
        this.guest = guest;
        this.roomType = roomType;
    }

    public String getGuest() { return guest; }
    public String getRoomType() { return roomType; }

    public void setReservationId(String id) {
        reservationId = id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String toString() {
        return reservationId+" | "+guest+" | "+roomType;
    }
}


/* ============================================================
   FIFO REQUEST QUEUE
   ============================================================ */

class BookingQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void add(Reservation r) {
        queue.offer(r);
        System.out.println("Request Added");
    }

    public Reservation next() { return queue.poll(); }
    public boolean empty() { return queue.isEmpty(); }
}


/* ============================================================
   BOOKING SERVICE (ALLOCATION + DOUBLE BOOKING PREVENTION)
   ============================================================ */

class BookingService {

    private RoomInventory inventory;
    private Set<String> allocatedIds = new HashSet<>();

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public Reservation confirm(Reservation r) {

        if(inventory.available(r.getRoomType())<=0) {
            System.out.println("Booking Failed for "+r.getGuest());
            return null;
        }

        String id = generateId(r.getRoomType());

        r.setReservationId(id);
        allocatedIds.add(id);

        inventory.reduce(r.getRoomType());

        System.out.println("Booking Confirmed → "+r);

        return r;
    }

    private String generateId(String type) {

        String prefix = type.substring(0,2).toUpperCase();
        String id;

        do {
            id = prefix+"-"+(100+new Random().nextInt(900));
        } while(allocatedIds.contains(id));

        return id;
    }
}


/* ============================================================
   ADD-ON SERVICE MODULE (Use Case 7)
   ============================================================ */

class AddOnService {

    private String name;
    private double cost;

    public AddOnService(String name,double cost) {
        this.name = name;
        this.cost = cost;
    }

    public double getCost() { return cost; }

    public String toString() {
        return name+" ₹"+cost;
    }
}

class AddOnServiceManager {

    private Map<String,List<AddOnService>> map = new HashMap<>();

    public void attach(String reservationId,AddOnService s) {

        map.computeIfAbsent(reservationId,
                k->new ArrayList<>()).add(s);
    }

    public double totalCost(String reservationId) {

        double total=0;

        List<AddOnService> list = map.get(reservationId);

        if(list!=null)
            for(AddOnService s:list)
                total+=s.getCost();

        return total;
    }
}


/* ============================================================
   BOOKING HISTORY MODULE (Use Case 8)
   ============================================================ */

class BookingHistory {

    private List<Reservation> history = new ArrayList<>();

    public void store(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getHistory() {
        return history;
    }
}


/* ============================================================
   REPORTING SERVICE
   ============================================================ */

class ReportService {

    public void showAll(List<Reservation> list) {

        System.out.println("\n=== BOOKING HISTORY ===");

        for(Reservation r:list)
            System.out.println(r);
    }

    public void showSummary(List<Reservation> list) {

        System.out.println("\n=== SUMMARY REPORT ===");

        Map<String,Integer> count = new HashMap<>();

        for(Reservation r:list)
            count.put(r.getRoomType(),
                    count.getOrDefault(r.getRoomType(),0)+1);

        for(String type:count.keySet())
            System.out.println(type+" → "+count.get(type));
    }
}


/* ============================================================
   MAIN APPLICATION
   ============================================================ */

public class BookMyStayFinalSystem {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("Single Room",2);
        inventory.addRoomType("Double Room",1);
        inventory.addRoomType("Suite Room",1);

        Map<String,Room> catalog = new HashMap<>();
        catalog.put("Single Room",new SingleRoom());
        catalog.put("Double Room",new DoubleRoom());
        catalog.put("Suite Room",new SuiteRoom());

        SearchService search = new SearchService(inventory,catalog);
        BookingQueue queue = new BookingQueue();
        BookingService booking = new BookingService(inventory);
        AddOnServiceManager addOn = new AddOnServiceManager();
        BookingHistory history = new BookingHistory();
        ReportService report = new ReportService();

        search.showAvailable();

        System.out.print("\nEnter number of requests: ");
        int n = sc.nextInt();
        sc.nextLine();

        for(int i=0;i<n;i++) {

            System.out.print("Guest Name: ");
            String g = sc.nextLine();

            System.out.print("Room Type: ");
            String t = sc.nextLine();

            queue.add(new Reservation(g,t));
        }

        while(!queue.empty()) {

            Reservation r = booking.confirm(queue.next());

            if(r!=null) {

                history.store(r);

                System.out.print("Add-on service count: ");
                int s = sc.nextInt();
                sc.nextLine();

                for(int i=0;i<s;i++) {

                    System.out.print("Service Name: ");
                    String name = sc.nextLine();

                    System.out.print("Cost: ");
                    double cost = sc.nextDouble();
                    sc.nextLine();

                    addOn.attach(r.getReservationId(),
                            new AddOnService(name,cost));
                }

                System.out.println("Total Add-on Cost ₹"+
                        addOn.totalCost(r.getReservationId()));
            }
        }

        inventory.showInventory();

        report.showAll(history.getHistory());
        report.showSummary(history.getHistory());

        System.out.println("\nSystem Finished Successfully");
    }
}