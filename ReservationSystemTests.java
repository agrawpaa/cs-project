import org.junit.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ReservationSystemTests {

    private ReservationHandler handler;
    private LocalDate date;
    private LocalTime time;

    @Before
    public void setup() {
        handler = new ReservationHandler();
        date = LocalDate.now();
        time = LocalTime.of(18, 0);
    }

    @Test(timeout = 1000)
    public void testCreateAccount() {
        boolean ok = handler.createAccount("bob", "123");
        assertTrue("Account should be created", ok);

        boolean dup = handler.createAccount("bob", "123");
        assertFalse("Duplicate username must fail", dup);
    }

    @Test(timeout = 1000)
    public void testLogin() {
        handler.createAccount("alice", "pw");

        assertTrue(handler.login("alice", "pw"));
        assertFalse(handler.login("alice", "wrongpw"));
        assertFalse(handler.login("ghost", "pw"));
    }


    @Test(timeout = 1000)
    public void testMakeReservation() {
        handler.createAccount("tim", "pw");

        List<Integer> seats = Arrays.asList(1, 2, 3);

        boolean first = handler.makeReservation("tim", date, time, seats);
        assertTrue("First reservation should succeed", first);

        boolean second = handler.makeReservation("tim", date, time, seats);
        assertFalse("Cannot double-book seats", second);
    }

    @Test(timeout = 1000)
    public void testCancelReservation() {
        handler.createAccount("sam", "pw");

        List<Integer> seats = Arrays.asList(4, 5);

        handler.makeReservation("sam", date, time, seats);
        assertTrue(handler.cancelReservation("sam", date, time, seats));

        assertFalse("Cancelling again should fail",
                handler.cancelReservation("sam", date, time, seats));
    }

    @Test(timeout = 1000)
    public void testCancelAllReservations() {
        handler.createAccount("a", "a");
        handler.createAccount("b", "b");

        handler.makeReservation("a", date, time, Arrays.asList(1));
        handler.makeReservation("b", date, time, Arrays.asList(2));

        handler.cancelAllReservations(date, time);

        assertTrue(handler.getReservationsForSlot(date, time).isEmpty());
    }


    @Test(timeout = 1000)
    public void testValidateAdmin() {
        assertTrue(handler.validateAdmin("admin123"));
        assertFalse(handler.validateAdmin("nope"));
    }

    @Test(timeout = 1000)
    public void testSetHours() {
        LocalTime open = LocalTime.of(10, 0);
        LocalTime close = LocalTime.of(23, 0);

        handler.setHours(open, close);

        assertEquals(open, handler.getOpenTime());
        assertEquals(close, handler.getCloseTime());
    }

    @Test(timeout = 1000)
    public void testSetSeatPrice() {
        handler.setSeatPrice(5, 20.0);
        assertEquals(20.0, handler.getSeatPrice(5), 0.001);
    }

    @Test(timeout = 1000)
    public void testSetSeatingArrangement() {
        handler.setSeatingArrangement(4, 4, 15.0); // 16 seats

        assertEquals("Total seats should match rows*cols", 16, handler.getTotalSeats());
        assertEquals(15.0, handler.getSeatPrice(15), 0.001);
    }

    @Test(timeout = 1000)
    public void testLockAndUnlockSeats() {
        Set<Integer> locked = new HashSet<>(Arrays.asList(1, 2, 3));
        handler.lockSeats(locked);

        assertFalse(handler.isSeatAvailable(date, time, 1));  // locked
        assertFalse(handler.isSeatAvailable(date, time, 2));
        assertFalse(handler.isSeatAvailable(date, time, 3));

        handler.unlockSeats(Set.of(2));

        assertTrue("Seat 2 should now be unlocked",
                handler.isSeatAvailable(date, time, 2));

        assertFalse("Seat 1 remains locked",
                handler.isSeatAvailable(date, time, 1));
    }


    @Test(timeout = 1000)
    public void testCalculateTotalPrice() {
        handler.setSeatPrice(0, 10.0);
        handler.setSeatPrice(1, 15.0);
        handler.setSeatPrice(2, 20.0);

        double total = handler.calculateTotalPrice(Arrays.asList(0, 1, 2));
        assertEquals(45.0, total, 0.001);
    }


    @Test(timeout = 1000)
    public void testInvalidSeatIndex() {
        assertFalse(handler.isSeatAvailable(date, time, -1));
        assertFalse(handler.isSeatAvailable(date, time, handler.getTotalSeats() + 5));
    }

    @Test(timeout = 1000)
    public void testReservationForNonexistentUser() {
        boolean ok = handler.makeReservation("ghost", date, time, Arrays.asList(1, 2));
        assertFalse(ok);
    }
}
