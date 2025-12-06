import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        // defensive copy
        this.seats = (seats == null) ? new int[0] : Arrays.copyOf(seats, seats.length);
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
        return Arrays.copyOf(seats, seats.length);
    }

    // admin helped-seats as List<Integer>
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

    // Two reservations are considered equal if they belong to the same username, date, time and exact seat set.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        // Compare username (null-safe), date, time, seats contents
        String u1 = (this.user == null) ? null : this.user.getUsername();
        String u2 = (that.user == null) ? null : that.user.getUsername();
        return Objects.equals(u1, u2)
                && Objects.equals(this.date, that.date)
                && Objects.equals(this.time, that.time)
                && Arrays.equals(this.seats, that.seats);
    }

    @Override
    public int hashCode() {
        String u = (user == null) ? null : user.getUsername();
        int result = Objects.hash(u, date, time, totalPrice);
        result = 31 * result + Arrays.hashCode(seats);
        return result;
    }

    @Override
    public String toString() {
        String u = (user == null) ? "null" : user.getUsername();
        return "Reservation{user=" + u +
                ", date=" + date +
                ", time=" + time +
                ", seats=" + Arrays.toString(seats) +
                ", totalPrice=" + totalPrice + "}";
    }
}
