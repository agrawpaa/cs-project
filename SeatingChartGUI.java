import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

public class SeatingChartGUI extends JFrame {
    private ReservationHandler handler;
    private JButton[] seatButtons;
    private boolean isAdmin = false;
    private String currentUser;
    private LocalDate selectedDate;
    private LocalTime selectedTime;
    private JPanel seatPanel;
    private JTextArea infoArea;

    private final int TOTAL_SEATS = 50;

    private JComboBox<String> dateSelector;
    private JComboBox<String> timeSelector;

    public SeatingChartGUI(ReservationHandler handler) {
        this.handler = handler;
        this.selectedDate = LocalDate.now();
        this.selectedTime = LocalTime.of(18, 0);

        setTitle("Reservation System");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel: login, create account, admin
        JPanel topPanel = new JPanel();
        JButton loginBtn = new JButton("Login");
        JButton createBtn = new JButton("Create Account");
        JButton adminBtn = new JButton("Admin Login");
        topPanel.add(loginBtn);
        topPanel.add(createBtn);
        topPanel.add(adminBtn);

        // Date selector (next 7 days)
        dateSelector = new JComboBox<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = LocalDate.now().plusDays(i);
            dateSelector.addItem(d.toString());
        }
        topPanel.add(new JLabel("Date:"));
        topPanel.add(dateSelector);

        // Time selector (6pm to 10pm)
        timeSelector = new JComboBox<>();
        for (int h = 18; h <= 22; h++) {
            LocalTime t = LocalTime.of(h, 0);
            timeSelector.addItem(t.toString());
        }
        topPanel.add(new JLabel("Time:"));
        topPanel.add(timeSelector);

        add(topPanel, BorderLayout.NORTH);

        // Seat grid
        seatPanel = new JPanel(new GridLayout(5, 10, 5, 5));
        seatButtons = new JButton[TOTAL_SEATS];
        for (int i = 0; i < TOTAL_SEATS; i++) {
            JButton b = new JButton();
            b.setBackground(Color.GREEN);
            int seatIndex = i;
            b.addActionListener(e -> handleSeatClick(seatIndex));
            seatButtons[i] = b;
            seatPanel.add(b);
        }
        add(seatPanel, BorderLayout.CENTER);

        // Info panel
        infoArea = new JTextArea(5, 30);
        infoArea.setEditable(false);
        add(new JScrollPane(infoArea), BorderLayout.SOUTH);

        // Button actions
        loginBtn.addActionListener(e -> userLogin());
        createBtn.addActionListener(e -> createAccount());
        adminBtn.addActionListener(e -> adminLogin());

        dateSelector.addActionListener(e -> updateDateTime());
        timeSelector.addActionListener(e -> updateDateTime());

        refreshGrid();
        setVisible(true);
    }

    private void updateDateTime() {
        selectedDate = LocalDate.parse((String) dateSelector.getSelectedItem());
        selectedTime = LocalTime.parse((String) timeSelector.getSelectedItem());
        refreshGrid();
    }

    private void userLogin() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] fields = {"Username:", usernameField, "Password:", passwordField};
        int option = JOptionPane.showConfirmDialog(this, fields, "User Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (handler.login(username, password)) {
                currentUser = username;
                isAdmin = false;
                infoArea.setText("Logged in as: " + username);
                refreshGrid();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials");
            }
        }
    }

    private void createAccount() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] fields = {"Username:", usernameField, "Password:", passwordField};
        int option = JOptionPane.showConfirmDialog(this, fields, "Create Account", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (handler.createAccount(username, password)) {
                JOptionPane.showMessageDialog(this, "Account created!");
            } else {
                JOptionPane.showMessageDialog(this, "Account already exists!");
            }
        }
    }

    private void adminLogin() {
        String key = JOptionPane.showInputDialog(this, "Enter Admin Key:");
        if (handler.validateAdmin(key)) {
            isAdmin = true;
            currentUser = "ADMIN";
            infoArea.setText("Admin logged in");
            refreshGrid();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Do you want to cancel ALL reservations for this date and time?",
                    "Admin Action", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                handler.cancelAllReservations(selectedDate, selectedTime);
                refreshGrid();
                JOptionPane.showMessageDialog(this, "All reservations cancelled!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Admin Key");
        }
    }

    private void handleSeatClick(int seatIndex) {
        if (isAdmin) {
            Reservation bookedRes = findReservationBySeat(seatIndex);
            if (bookedRes != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Cancel reservation for " + bookedRes.getUser().getUsername() + "?",
                        "Confirm Cancel", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    handler.cancelReservation(bookedRes.getUser().getUsername(),
                            bookedRes.getDate(),
                            bookedRes.getTime(),
                            bookedRes.getSeatsAsList());
                    refreshGrid();
                    JOptionPane.showMessageDialog(this, "Reservation canceled!");
                }
            }
        } else {
            Reservation bookedRes = findReservationBySeat(seatIndex);
            if (bookedRes != null) {
                JOptionPane.showMessageDialog(this, "Seat already reserved!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reserve seat " + seatIndex + " for $" + handler.getSeatPrice(seatIndex) + "?",
                    "Confirm Seat", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                List<Integer> seats = Collections.singletonList(seatIndex);
                boolean ok = handler.makeReservation(currentUser, selectedDate, selectedTime, seats);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Reservation confirmed!");
                    refreshGrid();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to reserve seat!");
                }
            }
        }
    }

    private Reservation findReservationBySeat(int seatIndex) {
        List<Reservation> reservations = handler.getReservationsForSlot(selectedDate, selectedTime);
        for (Reservation r : reservations) {
            if (r.containsSeat(seatIndex)) return r;
        }
        return null;
    }

    private void refreshGrid() {
        List<Reservation> reservations = handler.getReservationsForSlot(selectedDate, selectedTime);
        Set<Integer> reservedSeats = new HashSet<>();
        Map<Integer, String> seatOwners = new HashMap<>();
        for (Reservation r : reservations) {
            for (int s : r.getSeats()) {
                reservedSeats.add(s);
                seatOwners.put(s, r.getUser().getUsername());
            }
        }

        for (int i = 0; i < TOTAL_SEATS; i++) {
            JButton b = seatButtons[i];
            if (reservedSeats.contains(i)) {
                b.setBackground(Color.RED);
                if (isAdmin) b.setText(seatOwners.get(i) + " ($" + handler.getSeatPrice(i) + ")");
                else b.setText("$" + handler.getSeatPrice(i));
            } else {
                b.setBackground(Color.GREEN);
                b.setText("$" + handler.getSeatPrice(i));
            }
        }
    }

    public static void main(String[] args) {
        ReservationHandler handler = new ReservationHandler();
        new SeatingChartGUI(handler);
    }
}