import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationManager {
    boolean makeReservation(String username, LocalDate date, LocalTime time, List<Integer> seats);
    boolean cancelReservation(String username, LocalDate date, LocalTime time, List<Integer> seats);
    boolean adminCancelReservation(Reservation reservation);
}
