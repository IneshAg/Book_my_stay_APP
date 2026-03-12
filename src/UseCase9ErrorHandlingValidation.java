/**
 * BOOK MY STAY SYSTEM
 * FINAL VERSION – USE CASE 1 → 9
 * Version 9.0 (Error Handling & Validation)
 */

import java.util.*;


/* ============================================================
   CUSTOM EXCEPTION (Use Case 9 Core Concept)
   ============================================================ */

class InvalidBookingException extends Exception {

    public InvalidBookingException(String msg) {
        super(msg);
    }
}


/* ============================================================
   ROOM MODEL
   ============================================================ */

abstract class Room {

    private String type;
    private double price;

    public Room(String type,double price) {
        this.type = type;
        this.price = price;
    }

    public String getType() { return type; }

    public abstract void display();
}

class SingleRoom extends Room {
    public SingleRoom(){ super("Single Room",2000); }
    public void display(){ System.out.println("Single Room ₹2000"); }
}

class DoubleRoom extends Room {
    public DoubleRoom(){ super("Double Room",3500); }
    public void display(){ System.out.println("Double Room ₹3500"); }
}

class SuiteRoom extends Room {
    public SuiteRoom(){ super("Suite Room",6000); }
    public void display(){ System.out.println("Suite Room ₹6000"); }
}


/* ============================================================
   INVENTORY WITH VALIDATION GUARD
   ============================================================ */

class RoomInventory {

    private Map<String,Integer> stock = new HashMap<>();

    public void addType(String type,int count) {
        stock.put(type,count);
    }

    public int available(String type) {
        return stock.getOrDefault(type,-1);
    }

    public void reduce(String type) throws InvalidBookingException {

        int avail = stock.getOrDefault(type,-1);

        if(avail <= 0)
            throw new InvalidBookingException(
                    "No inventory available for "+type);

        stock.put(type,avail-1);
    }

    public boolean isValidRoomType(String type) {
        return stock.containsKey(type);
    }

    public void show() {
        System.out.println("\nInventory Status");
        for(String k:stock.keySet())
            System.out.println(k+" → "+stock.get(k));
    }
}


/* ============================================================
   RESERVATION
   ============================================================ */

class Reservation {

    private String guest;
    private String type;
    private String id;

    public Reservation(String guest,String type) {
        this.guest = guest;
        this.type = type;
    }

    public String getGuest(){ return guest; }
    public String getType(){ return type; }

    public void setId(String id){ this.id=id; }
    public String getId(){ return id; }

    public String toString(){
        return id+" | "+guest+" | "+type;
    }
}


/* ============================================================
   FIFO QUEUE
   ============================================================ */

class BookingQueue {

    private Queue<Reservation> q = new LinkedList<>();

    public void add(Reservation r){
        q.offer(r);
    }

    public Reservation next(){
        return q.poll();
    }

    public boolean empty(){
        return q.isEmpty();
    }
}


/* ============================================================
   VALIDATOR SERVICE (Fail-Fast Design)
   ============================================================ */

class BookingValidator {

    private RoomInventory inventory;

    public BookingValidator(RoomInventory inventory){
        this.inventory = inventory;
    }

    public void validate(Reservation r)
            throws InvalidBookingException {

        if(r.getGuest()==null || r.getGuest().trim().isEmpty())
            throw new InvalidBookingException("Guest name invalid");

        if(!inventory.isValidRoomType(r.getType()))
            throw new InvalidBookingException(
                    "Invalid room type selected: "+r.getType());
    }
}


/* ============================================================
   BOOKING SERVICE WITH ERROR HANDLING
   ============================================================ */

class BookingService {

    private RoomInventory inventory;
    private Set<String> allocated = new HashSet<>();

    public BookingService(RoomInventory inventory){
        this.inventory = inventory;
    }

    public Reservation confirm(Reservation r)
            throws InvalidBookingException {

        inventory.reduce(r.getType());

        String id = generateId(r.getType());

        r.setId(id);
        allocated.add(id);

        System.out.println("Booking Confirmed → "+r);

        return r;
    }

    private String generateId(String type){

        String prefix = type.substring(0,2).toUpperCase();
        String id;

        do{
            id = prefix+"-"+(100+new Random().nextInt(900));
        }while(allocated.contains(id));

        return id;
    }
}


/* ============================================================
   ADD-ON SERVICE (Use Case 7)
   ============================================================ */

class AddOnService {

    private String name;
    private double cost;

    public AddOnService(String name,double cost){
        this.name=name;
        this.cost=cost;
    }

    public double getCost(){ return cost; }
}

class AddOnManager {

    private Map<String,List<AddOnService>> map =
            new HashMap<>();

    public void add(String id,AddOnService s){

        map.computeIfAbsent(id,k->new ArrayList<>())
                .add(s);
    }

    public double total(String id){

        double t=0;

        List<AddOnService> list = map.get(id);

        if(list!=null)
            for(AddOnService s:list)
                t+=s.getCost();

        return t;
    }
}


/* ============================================================
   BOOKING HISTORY (Use Case 8)
   ============================================================ */

class BookingHistory {

    private List<Reservation> list = new ArrayList<>();

    public void store(Reservation r){
        list.add(r);
    }

    public List<Reservation> all(){
        return list;
    }
}


/* ============================================================
   REPORT SERVICE
   ============================================================ */

class ReportService {

    public void show(List<Reservation> list){

        System.out.println("\nBooking History");

        for(Reservation r:list)
            System.out.println(r);
    }
}


/* ============================================================
   MAIN APPLICATION
   ============================================================ */

public class UseCase9ErrorHandlingValidation {

    public static void main(String[] args){

        Scanner sc = new Scanner(System.in);

        RoomInventory inventory = new RoomInventory();
        inventory.addType("Single Room",2);
        inventory.addType("Double Room",1);
        inventory.addType("Suite Room",1);

        BookingQueue queue = new BookingQueue();
        BookingValidator validator =
                new BookingValidator(inventory);
        BookingService booking =
                new BookingService(inventory);

        AddOnManager addOn = new AddOnManager();
        BookingHistory history = new BookingHistory();
        ReportService report = new ReportService();

        System.out.print("Enter booking count: ");
        int n = sc.nextInt();
        sc.nextLine();

        for(int i=0;i<n;i++){

            System.out.print("Guest Name: ");
            String g = sc.nextLine();

            System.out.print("Room Type: ");
            String t = sc.nextLine();

            queue.add(new Reservation(g,t));
        }

        while(!queue.empty()){

            Reservation r = queue.next();

            try {

                validator.validate(r);

                Reservation confirmed =
                        booking.confirm(r);

                history.store(confirmed);

                System.out.print("Add-on count: ");
                int s = sc.nextInt();
                sc.nextLine();

                for(int i=0;i<s;i++){

                    System.out.print("Service Name: ");
                    String name = sc.nextLine();

                    System.out.print("Cost: ");
                    double cost = sc.nextDouble();
                    sc.nextLine();

                    addOn.add(confirmed.getId(),
                            new AddOnService(name,cost));
                }

                System.out.println("Add-on Total ₹"+
                        addOn.total(confirmed.getId()));

            }
            catch(InvalidBookingException ex){

                System.out.println("Booking Error → "
                        +ex.getMessage());
            }
        }

        inventory.show();
        report.show(history.all());

        System.out.println("\nSystem completed safely");
    }
}