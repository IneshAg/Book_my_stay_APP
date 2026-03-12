/**
 * BOOK MY STAY SYSTEM
 * Version 10.0 – Full Lifecycle Management
 * Covers Use Case 1 → 10
 */

import java.util.*;


/* ============================================================
   CUSTOM EXCEPTION
   ============================================================ */

class BookingException extends Exception {
    public BookingException(String msg) {
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
}

class SingleRoom extends Room {
    public SingleRoom(){ super("Single Room",2000); }
}

class DoubleRoom extends Room {
    public DoubleRoom(){ super("Double Room",3500); }
}

class SuiteRoom extends Room {
    public SuiteRoom(){ super("Suite Room",6000); }
}


/* ============================================================
   INVENTORY (SAFE STATE MANAGEMENT)
   ============================================================ */

class RoomInventory {

    private Map<String,Integer> stock = new HashMap<>();

    public void addType(String type,int count) {
        stock.put(type,count);
    }

    public boolean validType(String type) {
        return stock.containsKey(type);
    }

    public int available(String type) {
        return stock.getOrDefault(type,0);
    }

    public void reduce(String type) throws BookingException {

        int a = stock.getOrDefault(type,-1);

        if(a<=0)
            throw new BookingException("No rooms available for "+type);

        stock.put(type,a-1);
    }

    public void increase(String type) {
        stock.put(type, stock.get(type)+1);
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
    private boolean cancelled=false;

    public Reservation(String guest,String type) {
        this.guest=guest;
        this.type=type;
    }

    public String getGuest(){ return guest; }
    public String getType(){ return type; }

    public void setId(String id){ this.id=id; }
    public String getId(){ return id; }

    public boolean isCancelled(){ return cancelled; }
    public void cancel(){ cancelled=true; }

    public String toString(){
        return id+" | "+guest+" | "+type+
                (cancelled?" | CANCELLED":"");
    }
}


/* ============================================================
   VALIDATOR (FAIL FAST)
   ============================================================ */

class BookingValidator {

    private RoomInventory inventory;

    public BookingValidator(RoomInventory inventory){
        this.inventory=inventory;
    }

    public void validate(Reservation r)
            throws BookingException {

        if(r.getGuest()==null || r.getGuest().trim().isEmpty())
            throw new BookingException("Invalid guest name");

        if(!inventory.validType(r.getType()))
            throw new BookingException("Invalid room type");
    }
}


/* ============================================================
   FIFO QUEUE
   ============================================================ */

class BookingQueue {

    private Queue<Reservation> q = new LinkedList<>();

    public void add(Reservation r){ q.offer(r); }
    public Reservation next(){ return q.poll(); }
    public boolean empty(){ return q.isEmpty(); }
}


/* ============================================================
   BOOKING SERVICE
   ============================================================ */

class BookingService {

    private RoomInventory inventory;
    private Set<String> allocated = new HashSet<>();
    private Map<String,Reservation> confirmed =
            new HashMap<>();

    public BookingService(RoomInventory inventory){
        this.inventory=inventory;
    }

    public Reservation confirm(Reservation r)
            throws BookingException {

        inventory.reduce(r.getType());

        String id = generateId(r.getType());
        r.setId(id);

        allocated.add(id);
        confirmed.put(id,r);

        System.out.println("Confirmed → "+r);

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

    public Reservation getReservation(String id){
        return confirmed.get(id);
    }
}


/* ============================================================
   ADD-ON SERVICES
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
        map.computeIfAbsent(id,k->new ArrayList<>()).add(s);
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
   BOOKING HISTORY
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
   CANCELLATION SERVICE (Use Case 10 CORE)
   ============================================================ */

class CancellationService {

    private RoomInventory inventory;
    private Stack<String> rollbackStack = new Stack<>();

    public CancellationService(RoomInventory inventory){
        this.inventory=inventory;
    }

    public void cancel(Reservation r)
            throws BookingException {

        if(r==null)
            throw new BookingException("Reservation not found");

        if(r.isCancelled())
            throw new BookingException("Already cancelled");

        rollbackStack.push(r.getId());

        inventory.increase(r.getType());

        r.cancel();

        System.out.println("Cancellation Successful → "+r.getId());
    }

    public void showRollbackStack(){
        System.out.println("Rollback Stack → "+rollbackStack);
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

public class UseCase10BookingCancellation {

    public static void main(String[] args){

        Scanner sc = new Scanner(System.in);

        RoomInventory inventory = new RoomInventory();
        inventory.addType("Single Room",2);
        inventory.addType("Double Room",1);
        inventory.addType("Suite Room",1);

        BookingValidator validator =
                new BookingValidator(inventory);

        BookingQueue queue = new BookingQueue();
        BookingService booking =
                new BookingService(inventory);

        AddOnManager addOn = new AddOnManager();
        BookingHistory history = new BookingHistory();
        CancellationService cancelService =
                new CancellationService(inventory);

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
                Reservation c = booking.confirm(r);
                history.store(c);

                System.out.print("Add-on count: ");
                int s = sc.nextInt();
                sc.nextLine();

                for(int i=0;i<s;i++){

                    System.out.print("Service Name: ");
                    String name = sc.nextLine();

                    System.out.print("Cost: ");
                    double cost = sc.nextDouble();
                    sc.nextLine();

                    addOn.add(c.getId(),
                            new AddOnService(name,cost));
                }

                System.out.println("Add-on Total ₹"+
                        addOn.total(c.getId()));

            }
            catch(BookingException ex){
                System.out.println("Booking Error → "
                        +ex.getMessage());
            }
        }

        /* ===== CANCELLATION FLOW ===== */

        System.out.print("\nEnter Reservation ID to cancel: ");
        String cid = sc.nextLine();

        try {
            cancelService.cancel(
                    booking.getReservation(cid));
        }
        catch(BookingException ex){
            System.out.println("Cancellation Error → "
                    +ex.getMessage());
        }

        cancelService.showRollbackStack();

        inventory.show();
        report.show(history.all());

        System.out.println("\nSystem finished safely");
    }
}