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

    // default seating layout
    private int rows = 5;
    private int cols = 10;
    private int TOTAL_SEATS = rows * cols;

    private JComboBox<String> dateSelector;
    private JComboBox<String> timeSelector;

    public SeatingChartGUI(ReservationHandler handler) {
        this.handler = handler;
        this.selectedDate = LocalDate.now();
        this.selectedTime = LocalTime.of(18, 0);

        setTitle("Reservation System");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel: login, create account, admin, admin-actions
        JPanel topPanel = new JPanel();
        JButton loginBtn = new JButton("Login");
        JButton createBtn = new JButton("Create Account");
        JButton adminBtn = new JButton("Admin Login");
        topPanel.add(loginBtn);
        topPanel.add(createBtn);
        topPanel.add(adminBtn);

        // Admin action buttons (only visible after admin login)
        JButton setHoursBtn = new JButton("Set Hours");
        JButton configureSeatingBtn = new JButton("Configure Seating");
        JButton setPriceBtn = new JButton("Set Seat Price");
        JButton cancelAllBtn = new JButton("Cancel ALL (selected slot)");

        // start hidden
        setHoursBtn.setVisible(false);
        configureSeatingBtn.setVisible(false);
        setPriceBtn.setVisible(false);
        cancelAllBtn.setVisible(false);

        topPanel.add(setHoursBtn);
        topPanel.add(configureSeatingBtn);
        topPanel.add(setPriceBtn);
        topPanel.add(cancelAllBtn);

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
        seatPanel = new JPanel(new GridLayout(rows, cols, 5, 5));
        initializeSeatButtons(); // builds button array and adds to seatPanel
        add(new JScrollPane(seatPanel), BorderLayout.CENTER);

        // Info panel
        infoArea = new JTextArea(6, 40);
        infoArea.setEditable(false);
        add(new JScrollPane(infoArea), BorderLayout.SOUTH);

        // Button actions
        loginBtn.addActionListener(e -> userLogin());
        createBtn.addActionListener(e -> createAccount());
        adminBtn.addActionListener(e -> {
            adminLogin();
            // toggle admin buttons visibility
            boolean visible = isAdmin;
            setHoursBtn.setVisible(visible);
            configureSeatingBtn.setVisible(visible);
            setPriceBtn.setVisible(visible);
            cancelAllBtn.setVisible(visible);
        });

        // Admin action handlers
        setHoursBtn.addActionListener(e -> {
            JTextField openField = new JTextField("18:00");
            JTextField closeField = new JTextField("22:00");
            Object[] fields = {"Open (HH:mm):", openField, "Close (HH:mm):", closeField};
            int opt = JOptionPane.showConfirmDialog(this, fields, "Set Hours", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                try {
                    LocalTime open = LocalTime.parse(openField.getText());
                    LocalTime close = LocalTime.parse(closeField.getText());
                    handler.setHours(open, close);
                    JOptionPane.showMessageDialog(this, "Hours updated.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid time format.");
                }
            }
        });

        configureSeatingBtn.addActionListener(e -> {
            JTextField rowsField = new JTextField(String.valueOf(rows));
            JTextField colsField = new JTextField(String.valueOf(cols));
            Object[] fields = {"Rows:", rowsField, "Cols:", colsField};
            int opt = JOptionPane.showConfirmDialog(this, fields, "Configure Seating", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                try {
                    int newRows = Integer.parseInt(rowsField.getText());
                    int newCols = Integer.parseInt(colsField.getText());
                    if (newRows <= 0 || newCols <= 0) throw new NumberFormatException();
                    rows = newRows;
                    cols = newCols;
                    TOTAL_SEATS = rows * cols;

                    double defaultPrice = 10.0; // or whatever default you want
                    handler.setSeatingArrangement(rows, cols, defaultPrice);

                    rebuildSeatGrid();
                    refreshGrid();
                    JOptionPane.showMessageDialog(this, "Seating updated.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid rows/cols.");
                }
            }
        });

        setPriceBtn.addActionListener(e -> {
            String seatStr = JOptionPane.showInputDialog(this, "Seat index (0 - " + (TOTAL_SEATS-1) + "):");
            String priceStr = JOptionPane.showInputDialog(this, "Price for seat:");
            try {
                int idx = Integer.parseInt(seatStr);
                double p = Double.parseDouble(priceStr);
                handler.setSeatPrice(idx, p);
                refreshGrid();
                JOptionPane.showMessageDialog(this, "Price updated.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        });

        cancelAllBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Cancel ALL reservations for this date/time?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                handler.cancelAllReservations(selectedDate, selectedTime);
                refreshGrid();
                JOptionPane.showMessageDialog(this, "All reservations cancelled for that slot.");
            }
        });

        dateSelector.addActionListener(e -> updateDateTime());
        timeSelector.addActionListener(e -> updateDateTime());

        refreshGrid();
        setVisible(true);
    }

    private void initializeSeatButtons() {
        seatPanel.removeAll();
        seatButtons = new JButton[TOTAL_SEATS];
        for (int i = 0; i < TOTAL_SEATS; i++) {
            JButton b = new JButton();
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setBackground(Color.GREEN);
            int seatIndex = i;
            b.addActionListener(e -> handleSeatClick(seatIndex));
            seatButtons[i] = b;
            seatPanel.add(b);
        }
        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private void rebuildSeatGrid() {
        seatPanel.setLayout(new GridLayout(rows, cols, 5, 5));
        initializeSeatButtons();
        pack(); // adjust window size if needed
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
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Admin Key");
        }
    }

    private void handleSeatClick(int seatIndex) {
        if (isAdmin) {
            // admin view: show owner (if any) and allow cancel
            Reservation bookedRes = findReservationBySeat(seatIndex);
            if (bookedRes != null) {
                String owner = bookedRes.getUser().getUsername();
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Cancel reservation for " + owner + " on seat " + seatIndex + "?",
                        "Confirm Cancel", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = handler.cancelReservation(owner, bookedRes.getDate(), bookedRes.getTime(), bookedRes.getSeatsAsList());
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Reservation canceled!");
                        refreshGrid();
                    } else {
                        JOptionPane.showMessageDialog(this, "Unable to cancel (server error).");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seat is not reserved.");
            }
        } else {
            // normal user flow: must be logged in
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "Please log in first.");
                return;
            }

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
