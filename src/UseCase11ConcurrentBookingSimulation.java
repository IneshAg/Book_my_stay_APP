/**
 * BOOK MY STAY SYSTEM
 * Version 11.0 – Concurrent Booking Simulation
 * Covers Use Case 1 → 11
 */

import java.util.*;


/* ============================================================
   CUSTOM EXCEPTION
   ============================================================ */

class BookingException extends Exception {
    public BookingException(String msg) { super(msg); }
}


/* ============================================================
   INVENTORY (THREAD SAFE)
   ============================================================ */

class RoomInventory {

    private Map<String,Integer> stock = new HashMap<>();

    public synchronized void addType(String type,int count) {
        stock.put(type,count);
    }

    public synchronized boolean validType(String type) {
        return stock.containsKey(type);
    }

    public synchronized void reduce(String type)
            throws BookingException {

        int a = stock.getOrDefault(type,-1);

        if(a<=0)
            throw new BookingException(
                    "No rooms available for "+type);

        stock.put(type,a-1);
    }

    public synchronized void increase(String type) {
        stock.put(type,stock.get(type)+1);
    }

    public synchronized void show() {

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

    public Reservation(String guest,String type){
        this.guest=guest;
        this.type=type;
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
   SHARED THREAD SAFE BOOKING QUEUE
   ============================================================ */

class BookingQueue {

    private Queue<Reservation> queue =
            new LinkedList<>();

    public synchronized void add(Reservation r){
        queue.offer(r);
    }

    public synchronized Reservation next(){
        return queue.poll();
    }

    public synchronized boolean isEmpty(){
        return queue.isEmpty();
    }
}


/* ============================================================
   VALIDATOR
   ============================================================ */

class BookingValidator {

    private RoomInventory inventory;

    public BookingValidator(RoomInventory inv){
        this.inventory=inv;
    }

    public void validate(Reservation r)
            throws BookingException {

        if(r.getGuest()==null ||
                r.getGuest().trim().isEmpty())
            throw new BookingException(
                    "Invalid Guest Name");

        if(!inventory.validType(r.getType()))
            throw new BookingException(
                    "Invalid Room Type");
    }
}


/* ============================================================
   BOOKING SERVICE (CRITICAL SECTION)
   ============================================================ */

class BookingService {

    private RoomInventory inventory;
    private Set<String> allocated =
            Collections.synchronizedSet(
                    new HashSet<>());

    public BookingService(RoomInventory inv){
        this.inventory=inv;
    }

    public synchronized Reservation confirm(Reservation r)
            throws BookingException {

        inventory.reduce(r.getType());

        String id = generateId(r.getType());
        r.setId(id);

        allocated.add(id);

        System.out.println(Thread.currentThread().getName()
                +" → Booking Confirmed "+r);

        return r;
    }

    private String generateId(String type){

        String prefix = type.substring(0,2)
                .toUpperCase();

        String id;

        do{
            id = prefix+"-"+(100+
                    new Random().nextInt(900));
        }while(allocated.contains(id));

        return id;
    }
}


/* ============================================================
   BOOKING PROCESSOR THREAD
   ============================================================ */

class BookingProcessor extends Thread {

    private BookingQueue queue;
    private BookingValidator validator;
    private BookingService booking;

    public BookingProcessor(String name,
                            BookingQueue queue,
                            BookingValidator validator,
                            BookingService booking){
        super(name);
        this.queue=queue;
        this.validator=validator;
        this.booking=booking;
    }

    public void run(){

        while(true){

            Reservation r;

            synchronized(queue){

                if(queue.isEmpty())
                    break;

                r = queue.next();
            }

            try{
                validator.validate(r);
                booking.confirm(r);
            }
            catch(Exception e){
                System.out.println(getName()
                        +" → Failed: "+e.getMessage());
            }
        }
    }
}


/* ============================================================
   MAIN APPLICATION
   ============================================================ */

public class UseCase11ConcurrentBookingSimulation {

    public static void main(String[] args)
            throws Exception {

        Scanner sc = new Scanner(System.in);

        RoomInventory inventory =
                new RoomInventory();

        inventory.addType("Single Room",2);
        inventory.addType("Double Room",1);
        inventory.addType("Suite Room",1);

        BookingQueue queue = new BookingQueue();

        System.out.print("Enter total booking requests: ");
        int n = sc.nextInt();
        sc.nextLine();

        for(int i=0;i<n;i++){

            System.out.print("Guest Name: ");
            String g = sc.nextLine();

            System.out.print("Room Type: ");
            String t = sc.nextLine();

            queue.add(new Reservation(g,t));
        }

        BookingValidator validator =
                new BookingValidator(inventory);

        BookingService booking =
                new BookingService(inventory);

        /* ===== MULTI THREAD SIMULATION ===== */

        BookingProcessor t1 =
                new BookingProcessor(
                        "Processor-1",
                        queue,validator,booking);

        BookingProcessor t2 =
                new BookingProcessor(
                        "Processor-2",
                        queue,validator,booking);

        BookingProcessor t3 =
                new BookingProcessor(
                        "Processor-3",
                        queue,validator,booking);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        inventory.show();

        System.out.println("\nConcurrent Booking Simulation Completed");
    }
}