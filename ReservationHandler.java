import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ReservationHandler {
    private final ReservationDatabase db;
    private int TOTAL_SEATS = 50; // seats per time slot (modifiable)
    private final Map<Integer, Double> seatPrices; // seatIndex -> price
    private final Set<Integer> lockedSeats; // seats locked by admin
    private LocalTime openingTime = LocalTime.of(18, 0);
    private LocalTime closingTime = LocalTime.of(22, 0);

    private final String ADMIN_KEY = "admin123";

    public ReservationHandler() {
        db = new ReservationDatabase();
        seatPrices = new HashMap<>();
        lockedSeats = new HashSet<>();
        for (int i = 0; i < TOTAL_SEATS; i++) seatPrices.put(i, 10.0); // default $10
    }

    // ------------------ User Management ------------------
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

    // ------------------ Admin ------------------
    public boolean validateAdmin(String key) {
        return ADMIN_KEY.equals(key);
    }

    public void setHours(LocalTime open, LocalTime close) {
        openingTime = open;
        closingTime = close;
    }

    public LocalTime getOpenTime() { return openingTime; }
    public LocalTime getCloseTime() { return closingTime; }

    public void lockSeats(Set<Integer> seats) { lockedSeats.addAll(seats); }
    public void unlockSeats(Set<Integer> seats) { lockedSeats.removeAll(seats); }

    public void setSeatPrice(int seatIndex, double price) {
        if (seatIndex < 0) return;
        seatPrices.put(seatIndex, price);
    }

    public double getSeatPrice(int seatIndex) {
        return seatPrices.getOrDefault(seatIndex, 10.0);
    }

    // Admin: change seating arrangement
    public void setSeatingArrangement(int rows, int cols, double defaultPrice) {
        int newTotal = rows * cols;
        Map<Integer, Double> newPrices = new HashMap<>();
        for (int i = 0; i < newTotal; i++) {
            newPrices.put(i, seatPrices.getOrDefault(i, defaultPrice));
        }
        seatPrices.clear();
        seatPrices.putAll(newPrices);
        lockedSeats.removeIf(idx -> idx >= newTotal);
        TOTAL_SEATS = newTotal;
    }

    public int getTotalSeats() { return TOTAL_SEATS; }

    // ------------------ Reservations ------------------
    public boolean makeReservation(String username, LocalDate date, LocalTime time, List<Integer> seatList) {
        User user = db.getUser(username);
        if (user == null) return false;

        for (int s : seatList) {
            if (!isSeatAvailable(date, time, s)) return false;
        }

        int[] seats = seatList.stream().mapToInt(Integer::intValue).toArray();
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
        if (seatIndex < 0 || seatIndex >= TOTAL_SEATS) return false;
        if (lockedSeats.contains(seatIndex)) return false;

        List<Reservation> reservations = db.getReservationsForSlot(date, time);
        for (Reservation r : reservations) if (r.containsSeat(seatIndex)) return false;
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
        for (Reservation r : reservations) db.removeReservation(r);
    }

    // Admin cancel individual reservation
    public boolean adminCancelReservation(Reservation r) {
        return db.removeReservation(r);
    }

    // ----------- GUI helpers -----------
    public Map<Integer, Double> getSeatPrices() {
        return new HashMap<>(seatPrices);
    }

    public void setSeatPrices(Map<Integer, Double> prices) {
        seatPrices.clear();
        seatPrices.putAll(prices);
    }

    public void setSeating(Set<Integer> locked) {
        lockedSeats.clear();
        lockedSeats.addAll(locked);
    }
}
