import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ReservationHandler handler;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket, ReservationHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof Request req)) continue;

                Response res = handleRequest(req);
                out.writeObject(res);
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        }
    }

    private Response handleRequest(Request req) {
        try {
            String action = req.getAction();
            Object payload = req.getPayload();

            switch (action) {
                case "createAccount" -> {
                    String[] data = (String[]) payload;
                    boolean ok = handler.createAccount(data[0], data[1]);
                    return new Response(ok, ok ? "Account created" : "Account exists", null);
                }
                case "login" -> {
                    String[] data = (String[]) payload;
                    boolean ok = handler.login(data[0], data[1]);
                    return new Response(ok, ok ? "Login successful" : "Invalid credentials", null);
                }
                case "makeReservation" -> {
                    Object[] data = (Object[]) payload;
                    String username = (String) data[0];
                    LocalDate date = (LocalDate) data[1];
                    LocalTime time = (LocalTime) data[2];
                    List<Integer> seats = (List<Integer>) data[3];

                    boolean ok = handler.makeReservation(username, date, time, seats);
                    double total = handler.calculateTotalPrice(seats);
                    return new Response(ok, ok ? "Reservation confirmed" : "Seats unavailable", total);
                }
                case "getReservations" -> {
                    Object[] data = (Object[]) payload;
                    LocalDate date = (LocalDate) data[0];
                    LocalTime time = (LocalTime) data[1];
                    List<Reservation> reservations = handler.getReservationsForSlot(date, time);
                    return new Response(true, "Success", reservations);
                }
                case "cancelReservation" -> {
                    Object[] data = (Object[]) payload;
                    String username = (String) data[0];
                    LocalDate date = (LocalDate) data[1];
                    LocalTime time = (LocalTime) data[2];
                    List<Integer> seats = (List<Integer>) data[3];

                    boolean ok = handler.cancelReservation(username, date, time, seats);
                    return new Response(ok, ok ? "Reservation cancelled" : "Could not cancel", null);
                }
                case "validateAdmin" -> {
                    String key = (String) payload;
                    boolean ok = handler.validateAdmin(key);
                    return new Response(ok, ok ? "Admin access granted" : "Invalid admin key", null);
                }
                default -> {
                    return new Response(false, "Unknown action: " + action, null);
                }
            }
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }
}