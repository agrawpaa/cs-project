import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Initialize the reservation handler
        ReservationHandler handler = new ReservationHandler();

        // Create a set of all seat numbers
        Set<Integer> allSeats = new HashSet<>();
        for (int i = 1; i <= 25; i++) {
            allSeats.add(i);
        }
        handler.setSeating(allSeats);

        // hours
        LocalTime openTime = LocalTime.of(9, 0);  // 9 AM
        LocalTime closeTime = LocalTime.of(21, 0); // 9 PM
        handler.setHours(openTime, closeTime);

        // demo users
        handler.createAccount("alice", "password123");
        handler.createAccount("bob", "securepass");

        // seat prices
        handler.setSeatPrice(1, 50.0); // seat 1 costs $50
        handler.setSeatPrice(2, 50.0); // seat 2 costs $50
        handler.setSeatPrice(3, 40.0); // etc.
        handler.setSeatPrice(4, 40.0);

        // Launch the GUI
        SwingUtilities.invokeLater(() -> {
            SeatingChartGUI gui = new SeatingChartGUI(handler);
            gui.setVisible(true);
        });

        System.out.println("Reservation system initialized. GUI should be visible now.");
    }
}
