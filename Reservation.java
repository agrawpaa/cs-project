import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private User user;
    private LocalDate date;
    private LocalTime time;
    private int[] seats; // seat indices
    private double totalPrice;

    public Reservation(User user, LocalDate date, LocalTime time, int[] seats, double totalPrice) {
        this.user = user;
        this.date = date;
        this.time = time;
        this.seats = seats;
        this.totalPrice = totalPrice;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public int[] getSeats() {
        return seats;
    }

    // ✅ Add this method for GUI/Admin convenience
    public List<Integer> getSeatsAsList() {
        List<Integer> list = new ArrayList<>();
        for (int s : seats) list.add(s);
        return list;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public boolean containsSeat(int seatIndex) {
        for (int s : seats) {
            if (s == seatIndex) return true;
        }
        return false;
    }
}