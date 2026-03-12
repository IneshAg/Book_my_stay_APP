/**
 * BOOK MY STAY – FINAL ENTERPRISE SIMULATION
 * Version 12.0
 * Covers Use Case 1 → 12
 */

import java.io.*;
import java.util.*;


/* ============================================================
   CUSTOM EXCEPTION
   ============================================================ */

class BookingException extends Exception {
    public BookingException(String msg) { super(msg); }
}


/* ============================================================
   INVENTORY (THREAD SAFE + SERIALIZABLE)
   ============================================================ */

class RoomInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String,Integer> stock = new HashMap<>();

    public synchronized void addType(String type,int count){
        stock.put(type,count);
    }

    public synchronized boolean validType(String type){
        return stock.containsKey(type);
    }

    public synchronized void reduce(String type)
            throws BookingException{

        int a = stock.getOrDefault(type,-1);

        if(a<=0)
            throw new BookingException(
                    "No rooms available for "+type);

        stock.put(type,a-1);
    }

    public synchronized void increase(String type){
        stock.put(type,stock.get(type)+1);
    }

    public synchronized Map<String,Integer> snapshot(){
        return new HashMap<>(stock);
    }

    public synchronized void restore(
            Map<String,Integer> data){
        stock.clear();
        stock.putAll(data);
    }

    public synchronized void show(){
        System.out.println("\nInventory Status");
        for(String k:stock.keySet())
            System.out.println(k+" → "+stock.get(k));
    }
}


/* ============================================================
   RESERVATION (SERIALIZABLE)
   ============================================================ */

class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String guest;
    private String type;
    private String id;
    private boolean cancelled=false;

    public Reservation(String guest,String type){
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
   BOOKING HISTORY (SERIALIZABLE)
   ============================================================ */

class BookingHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Reservation> list =
            new ArrayList<>();

    public synchronized void store(Reservation r){
        list.add(r);
    }

    public synchronized List<Reservation> all(){
        return list;
    }
}


/* ============================================================
   PERSISTENCE SERVICE (Use Case 12 CORE)
   ============================================================ */

class PersistenceService {

    private static final String FILE =
            "bookmyStayData.ser";

    public static void save(RoomInventory inv,
                            BookingHistory history){

        try(ObjectOutputStream out =
                    new ObjectOutputStream(
                            new FileOutputStream(FILE))){

            out.writeObject(inv.snapshot());
            out.writeObject(history.all());

            System.out.println(
                    "System State Persisted Successfully");

        }catch(Exception e){
            System.out.println(
                    "Persistence Failed → "+e.getMessage());
        }
    }

    public static void load(RoomInventory inv,
                            BookingHistory history){

        try(ObjectInputStream in =
                    new ObjectInputStream(
                            new FileInputStream(FILE))){

            Map<String,Integer> stock =
                    (Map<String,Integer>) in.readObject();

            List<Reservation> list =
                    (List<Reservation>) in.readObject();

            inv.restore(stock);
            history.all().clear();
            history.all().addAll(list);

            System.out.println(
                    "System State Restored Successfully");

        }catch(Exception e){
            System.out.println(
                    "No previous data found. Starting fresh.");
        }
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
            throws BookingException{

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
   THREAD SAFE QUEUE
   ============================================================ */

class BookingQueue {

    private Queue<Reservation> q =
            new LinkedList<>();

    public synchronized void add(Reservation r){
        q.offer(r);
    }

    public synchronized Reservation next(){
        return q.poll();
    }

    public synchronized boolean empty(){
        return q.isEmpty();
    }
}


/* ============================================================
   BOOKING SERVICE
   ============================================================ */

class BookingService {

    private RoomInventory inventory;
    private Set<String> allocated =
            Collections.synchronizedSet(
                    new HashSet<>());

    private Map<String,Reservation> map =
            new HashMap<>();

    public BookingService(RoomInventory inv){
        this.inventory=inv;
    }

    public synchronized Reservation confirm(
            Reservation r)
            throws BookingException{

        inventory.reduce(r.getType());

        String id = generateId(r.getType());
        r.setId(id);

        allocated.add(id);
        map.put(id,r);

        System.out.println(Thread.currentThread()
                .getName()+" Confirmed "+r);

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

    public Reservation get(String id){
        return map.get(id);
    }
}


/* ============================================================
   CANCELLATION SERVICE
   ============================================================ */

class CancellationService {

    private RoomInventory inventory;
    private Stack<String> stack =
            new Stack<>();

    public CancellationService(RoomInventory inv){
        this.inventory=inv;
    }

    public synchronized void cancel(Reservation r)
            throws BookingException{

        if(r==null)
            throw new BookingException(
                    "Reservation Not Found");

        if(r.isCancelled())
            throw new BookingException(
                    "Already Cancelled");

        stack.push(r.getId());

        inventory.increase(r.getType());
        r.cancel();

        System.out.println(
                "Cancelled "+r.getId());
    }

    public void showStack(){
        System.out.println(
                "Rollback Stack "+stack);
    }
}


/* ============================================================
   BOOKING PROCESSOR THREAD
   ============================================================ */

class BookingProcessor extends Thread {

    private BookingQueue queue;
    private BookingValidator validator;
    private BookingService booking;
    private BookingHistory history;

    public BookingProcessor(String name,
                            BookingQueue queue,
                            BookingValidator validator,
                            BookingService booking,
                            BookingHistory history){

        super(name);
        this.queue=queue;
        this.validator=validator;
        this.booking=booking;
        this.history=history;
    }

    public void run(){

        while(true){

            Reservation r;

            synchronized(queue){

                if(queue.empty())
                    break;

                r = queue.next();
            }

            try{
                validator.validate(r);
                Reservation c =
                        booking.confirm(r);
                history.store(c);
            }
            catch(Exception e){
                System.out.println(getName()
                        +" Failed "+e.getMessage());
            }
        }
    }
}


/* ============================================================
   MAIN APPLICATION
   ============================================================ */

public class UseCase12DataPersistenceRecovery {

    public static void main(String[] args)
            throws Exception{

        Scanner sc = new Scanner(System.in);

        RoomInventory inventory =
                new RoomInventory();

        BookingHistory history =
                new BookingHistory();

        /* ===== LOAD PREVIOUS STATE ===== */
        PersistenceService.load(inventory,history);

        if(!inventory.validType("Single Room")){
            inventory.addType("Single Room",2);
            inventory.addType("Double Room",1);
            inventory.addType("Suite Room",1);
        }

        BookingQueue queue = new BookingQueue();

        System.out.print(
                "Enter booking requests: ");
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

        BookingProcessor t1 =
                new BookingProcessor(
                        "Thread-1",
                        queue,validator,
                        booking,history);

        BookingProcessor t2 =
                new BookingProcessor(
                        "Thread-2",
                        queue,validator,
                        booking,history);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        /* ===== CANCELLATION FLOW ===== */

        CancellationService cancel =
                new CancellationService(inventory);

        System.out.print(
                "Enter reservation ID to cancel: ");
        String id = sc.nextLine();

        try{
            cancel.cancel(booking.get(id));
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

        cancel.showStack();

        inventory.show();

        System.out.println("\nBooking History");
        for(Reservation r:history.all())
            System.out.println(r);

        /* ===== SAVE STATE BEFORE EXIT ===== */
        PersistenceService.save(inventory,history);

        System.out.println(
                "\nSystem shutdown with persistence");
    }
}