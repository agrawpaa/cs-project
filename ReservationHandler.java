import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ReservationHandler {
    private final ReservationDatabase db;
    private final int TOTAL_SEATS = 50; // seats per time slot
    private final Map<Integer, Double> seatPrices; // seatIndex -> price
    private final Set<Integer> lockedSeats; // seats locked by admin
    private LocalTime openingTime = LocalTime.of(18, 0);
    private LocalTime closingTime = LocalTime.of(22, 0);

    private final String ADMIN_KEY = "admin123";

    public ReservationHandler() {
        db = new ReservationDatabase();
        seatPrices = new HashMap<>();
        lockedSeats = new HashSet<>();
        for (int i = 0; i < TOTAL_SEATS; i++) seatPrices.put(i, 10.0); // default price $10
    }

    //User Management
    public boolean createAccount(String username, String password) {
        return db.addUser(new User(username, password));
    }

    public boolean login(String username, String password) {
        return db.validateLogin(username, password);
    }

    public boolean deleteAccount(String username, String password) {
        if (login(username, password)) return db.removeUser(username);
        return false;
    }

    //Admin
    public boolean validateAdmin(String key) {
        return ADMIN_KEY.equals(key);
    }

    public void setHours(LocalTime open, LocalTime close) {
        openingTime = open;
        closingTime = close;
    }

    public void lockSeats(Set<Integer> seats) {
        lockedSeats.addAll(seats);
    }

    public void unlockSeats(Set<Integer> seats) {
        lockedSeats.removeAll(seats);
    }

    public void setSeatPrice(int seatIndex, double price) {
        seatPrices.put(seatIndex, price);
    }

    public double getSeatPrice(int seatIndex) {
        return seatPrices.getOrDefault(seatIndex, 10.0);
    }

    //Reservations
    public boolean makeReservation(String username, LocalDate date, LocalTime time, List<Integer> seatList) {
        User user = db.getUser(username);
        if (user == null) return false;

        // Check availability
        for (int s : seatList) {
            if (!isSeatAvailable(date, time, s)) return false;
        }

        // Convert list to array
        int[] seats = seatList.stream().mapToInt(Integer::intValue).toArray();

        // Calculate total price
        double totalPrice = calculateTotalPrice(seatList);

        Reservation res = new Reservation(user, date, time, seats, totalPrice);
        return db.addReservation(res);
    }

    public boolean cancelReservation(String username, LocalDate date, LocalTime time, List<Integer> seatList) {
        User user = db.getUser(username);
        if (user == null) return false;

        int[] seats = seatList.stream().mapToInt(Integer::intValue).toArray();
        Reservation res = new Reservation(user, date, time, seats, calculateTotalPrice(seatList));
        return db.removeReservation(res);
    }

    public boolean isSeatAvailable(LocalDate date, LocalTime time, int seatIndex) {
        if (lockedSeats.contains(seatIndex)) return false;

        List<Reservation> reservations = db.getReservationsForSlot(date, time);
        for (Reservation r : reservations) {
            if (r.containsSeat(seatIndex)) return false;
        }
        return true;
    }

    public List<Reservation> getReservationsForSlot(LocalDate date, LocalTime time) {
        return db.getReservationsForSlot(date, time);
    }

    public double calculateTotalPrice(List<Integer> seats) {
        double total = 0;
        for (int s : seats) total += getSeatPrice(s);
        return total;
    }

    public void cancelAllReservations(LocalDate date, LocalTime time) {
        List<Reservation> reservations = new ArrayList<>(db.getReservationsForSlot(date, time));
        for (Reservation r : reservations) {
            db.removeReservation(r);
        }
    }
}
