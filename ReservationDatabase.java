import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ReservationDatabase {
    private Map<String, User> users; // username -> User
    private List<Reservation> reservations;
    private final String USER_FILE = "users.dat";
    private final String RESERVATION_FILE = "reservations.dat";

    public ReservationDatabase() {
        users = loadUsers();
        reservations = loadReservations();
    }

    // ------------------ User Management ------------------
    public boolean addUser(User user) {
        if (users.containsKey(user.getUsername())) return false;
        users.put(user.getUsername(), user);
        saveUsers();
        return true;
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public boolean removeUser(String username) {
        if (!users.containsKey(username)) return false;
        users.remove(username);
        saveUsers();
        // Also remove reservations for this user
        reservations.removeIf(r -> r.getUser().getUsername().equals(username));
        saveReservations();
        return true;
    }

    public boolean validateLogin(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }

    private void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            out.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, User> loadUsers() {
        File f = new File(USER_FILE);
        if (!f.exists()) return new HashMap<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            return (Map<String, User>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    // ------------------ Reservations ------------------
    public boolean addReservation(Reservation r) {
        reservations.add(r);
        saveReservations();
        return true;
    }

    public boolean removeReservation(Reservation r) {
        boolean removed = reservations.remove(r);
        if (removed) saveReservations();
        return removed;
    }

    public List<Reservation> getReservationsForSlot(LocalDate date, LocalTime time) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getDate().equals(date) && r.getTime().equals(time)) result.add(r);
        }
        return result;
    }

    private void saveReservations() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(RESERVATION_FILE))) {
            out.writeObject(reservations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Reservation> loadReservations() {
        File f = new File(RESERVATION_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(RESERVATION_FILE))) {
            return (List<Reservation>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
