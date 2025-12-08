import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

public interface AdminControls {
    void setHours(LocalTime open, LocalTime close);
    void setSeatingArrangement(int rows, int cols, double defaultPrice);
    void setSeatPrice(int seatIndex, double price);
    void lockSeats(Set<Integer> seats);
    void unlockSeats(Set<Integer> seats);
}
