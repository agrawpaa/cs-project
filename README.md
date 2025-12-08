# cs-project
Instructions
To use the reservation system:
	1.	Compile the project using any Java IDE (such as IntelliJ IDEA or VSC) or Java compiler.
	2.	Compile all files using javac *.java
	3.	Start server using java Server
  4.  Start GUI using java SeatingChartGUI
<------------------------------------------------------------------------------------------>
Submissions
Ryan submitted all parts of the project.
  <---------------------------------------------------------------------------------------->
AdminControls.java
Overview:
Defines an interface for admin controls in the reservation system. Provides methods to manage restaurant operating hours, seating arrangements, seat pricing, and seat locks. Ensures any implementing class can perform key admin operations consistently.

Methods:
	1.	void setHours(LocalTime open, LocalTime close)
	•	Sets uniform opening and closing times for the restaurant.
	2.	void setSeatingArrangement(int rows, int cols, double defaultPrice)
	•	Initializes or updates the seating layout with the specified number of rows and columns. Assigns a default price to all seats.
	3.	void setSeatPrice(int seatIndex, double price)
	•	Updates the price of a specific seat identified by its index.
	4.	void lockSeats(Set<Integer> seats)
	•	Locks a set of seats, preventing them from being reserved.
	5.	void unlockSeats(Set<Integer> seats)
	•	Unlocks a previously locked set of seats, making them available for reservation.

Test Cases:
	•	The interface itself is not directly tested. Functionality is verified through implementing classes and their respective unit tests.

Relationship:
	•	Implemented by admin classes in the reservation system.
	
  <---------------------------------------------------------------------------------------->
ClientHandler.java
Overview:
Handles communication between a single client and the reservation system server. Implements Runnable to allow multi-threaded handling of multiple clients simultaneously. Processes requests such as account creation, login, seat reservations, cancellations, and admin commands.
Methods:
	1.	ClientHandler(Socket socket, ReservationHandler handler)
	•	Constructor that initializes the handler with a client socket and a ReservationHandler to process requests.
	2.	void run()
	•	Main execution loop. Continuously listens for incoming objects from the client, deserializes them as Request objects, processes them, and sends back Response objects.
	3.	Response handleRequest(Request req)
	•	Processes a single request from a client. Handles actions including:
	•	"createAccount": creates a new user account.
	•	"login": attempts user login.
	•	"makeReservation": books seats for a user at a specific date/time.
	•	"getReservations": retrieves reservations for a given slot.
	•	"cancelReservation": cancels specific seats for a user.
	•	"cancelAll": cancels all reservations for a given slot.
	•	"validateAdmin": checks admin access.
	•	"setHours": sets operating hours.
	•	"setSeating": sets the seating arrangement.
	•	"setSeatPrice": sets the price of a specific seat.

Test Cases:
	•	Functionality is indirectly tested via multiple client tests and unit tests for ReservationHandler.
	•	Includes scenarios for:
	•	Creating and logging in users.
	•	Booking and cancelling reservations.
	•	Validating admin access.
	•	Setting hours, seating, and seat prices.
	•	Ensures correct handling of invalid or unknown actions.

Relationship:
	•	Works directly with ReservationHandler to process all client actions.
	•	Uses Request and Response objects to communicate with the client.
	•	Supports multi threading for handling multiple client connections concurrently.
<------------------------------------------------------------------------------------------>
Request.java

Overview:
Request is a serializable data-transfer object used to send actions and data between the client and server. It bundles together the action and a payload so the server can interpret what operation the client wants performed.
Fields:
	1.	String action
Represents the command or operation the client wants the server to perform.
	2.	Object payload
Contains any data needed to perform that action (e.g., login info, reservation details)

Constructor:
	•	Request(String action, Object payload)
Creates a new Request storing both the action and the data associated with that action.

Methods:
	1.	getAction()
Returns the action string, allowing the server to determine which operation to execute.
	2.	getPayload()
Returns the payload object, providing the server with the full data required for the action.

Test Cases:
	•	Tested indirectly through client server tests, since it is a data container.
	•	Serialization behavior is validated when sending Request objects over an ObjectOutputStream.

Relationship:
	•	Sent from the client to the server
	•	Received and interpreted by the server handler class
	•	Paired with the Response class to form a complete request response protocol.

<---------------------------------------------------------------------------------------->
Reservation.java

Overview:
Represents a reservation made by a user for a specific date, time, and set of seats. This class stores all relevant reservation details and is fully serializable so it can be shared between the client and server. It also provides helper methods for checking seats, comparing reservations, and retrieving seats in multiple formats.

Fields:
	1.	User user
The user who created the reservation.
	2.	LocalDate date
The date of the reservation.
	3.	LocalTime time
The reservation time.
	4.	int[] seats
An array of seats selected by the user.
	5.	double totalPrice
The final cost of the reservation.

Constructor:
	•	Reservation(User user, LocalDate date, LocalTime time, int[] seats, double totalPrice)
Creates a new reservation while copying the seat array to ensure no changes

Methods:
	1.	getUser()
Returns the User who made the reservation.
	2.	getDate()
Returns the reservation date.
	3.	getTime()
Returns the reservation time.
	4.	getSeats()
Returns a copy of the seat array.
	5.	getSeatsAsList()
Returns the seat indices as a List<Integer> for easier formatting.
	6.	getTotalPrice()
Returns the calculated total price of the reservation.
	7.	containsSeat(int seatIndex)
Checks if a particular seat index is part of the reservation.
	8.	equals(Object o)
Two reservations are considered equal if they match in username, date, time, and exact seat list.
	9.	hashCode()
Generates a matching hash code consistent with the equals implementation.
	10.	toString()
Returns a formatted string representation of the reservation.

Test Cases:
	•	Evaluated within ReservationSystemTests where duplicate checking, seat conflict detection, etc. are validated.
	•	equals and hashCode behavior is tested when ensuring duplicate reservations are not added.

Relationship:
	•	Used by the server to track and validate seat availability for a given date and time.
	•	Returned to the client when viewing past or upcoming reservations.
	•	Used inside Response object after reservation creation.
	•	Interacts with User, seat gui classes, and ReservationDatabase

 <---------------------------------------------------------------------------------------->
ReservationDatabase.java
Overview:
Handles all storage and retrieval of users and reservations. This class acts as the local database for the system, loading data at startup and saving updates automatically when users or reservations change. Data is serialized into .dat files, ensuring persistence between program runs.
It provides methods for user management, login validation, and storing/retrieving reservations for specific time slots.
Fields:
	1.	Map<String, User> users
Maps usernames to their corresponding User objects. Used for login and profile management.
	2.	List<Reservation> reservations
Stores all reservation records made in the system. Each entry corresponds to a specific user, date, time, and seat selection.
	3.	USER_FILE (“users.dat”)
File path used for serialized user storage.
	4.	RESERVATION_FILE (“reservations.dat”)
File path used for serialized reservation storage.
Constructor:
	•	ReservationDatabase()
Loads users and reservations. If no stored data exists, initializes empty collections. Ensures the system always starts in a valid state.
Methods:
User Management
	1.	addUser(User user)
Adds a new user only if the username does not already exist. Automatically saves the updated user map.
	2.	getUser(String username)
Retrieves a User object by its username.
	3.	removeUser(String username)
Deletes the user and all reservations belonging to that user.
	4.	validateLogin(String username, String password)
Returns true if the provided credentials match an existing user.
	5.	saveUsers() / loadUsers()
Handles serialization and deserialization of the user database. Ensures storage even after program ends.

Reservation Management
	1.	addReservation(Reservation r)
Adds the reservation and immediately saves the updated list to disk.
	2.	removeReservation(Reservation r)
Removes the reservation if it exists, and updates the saved file.
	3.	getReservationsForSlot(LocalDate date, LocalTime time)
Returns all reservations matching a specific date and time. Used to determine seat availability.
	4.	saveReservations() / loadReservations()
Manages serialization of reservation data.

Test Cases:
	•	Verified through reservation system tests.
	•	validateLogin() tested via both valid and invalid usernames/passwords.
	•	addReservation() and getReservationsForSlot() tested by creating multiple reservations for overlapping and distinct time slots.
	•	Storage tested by ensuring data remains same after restarting the program.

Relationship:
	•	Used by the server to store all account and reservation information.
	•	Interacts directly with User, Reservation, and any class that requests database reads/writes.
	•	Acts as the store for request/response operations between the client GUI and server logic.
<---------------------------------------------------------------------------------------->
ReservationHandler.java
Overview:
ReservationHandler acts as the central control for the reservation system. It implements three interfaces, ReservationManager, AdminControls, and UserManager, allowing it to coordinate user accounts, seat reservations, admin actions, and seating configuration.
It communicates directly with ReservationDatabase to store and retrieve data, manages seat availability, and calculates pricing for reservations.

Fields:
	1.	ReservationDatabase db
Handles persistent storage of users and reservations. All changes flow through this database.
	2.	int TOTAL_SEATS
Represents the total number of available seats for each time slot. Adjustable by admin.
	3.	Map<Integer, Double> seatPrices
Maps each seat index to its price.
	4.	Set<Integer> lockedSeats
Seats that are unavailable due to admin restrictions. These cannot be reserved.
	5.	LocalTime openingTime, closingTime
Defines when users are allowed to make reservations.
	6.	String ADMIN_KEY
Hardcoded admin credential used to verify administrative access.

Constructor:
	•	ReservationHandler()
Initializes the reservation system with default seat prices, loads user and reservation data from the database, and sets default business hours.

Methods:

User Management
	1.	createAccount(String username, String password)
Adds a new user to the database. Returns false if the username already exists.
	2.	login(String username, String password)
Validates login credentials via the database.
	3.	deleteAccount(String username, String password)
Removes a user and all associated reservations if credentials are valid.

Admin Controls
	1.	validateAdmin(String key)
Checks whether the provided admin key matches the system’s admin password.
	2.	setHours(LocalTime open, LocalTime close)
Updates the restaurant’s operating hours.
	3.	lockSeats(Set seats) / unlockSeats(Set seats)
Allows admin to block or unblock specific seats.
	4.	setSeatPrice(int seatIndex, double price)
Updates the price of a single seat.
	5.	setSeatingArrangement(int rows, int cols, double defaultPrice)
Rebuilds the seating layout, recalculates total seats, and adjusts price mapping.

Reservation Management
	1.	makeReservation(String username, LocalDate date, LocalTime time, List seats)
Ensures all seats are available, calculates total price, and creates a reservation.
	2.	cancelReservation(String username, LocalDate date, LocalTime time, List seats)
Removes an existing reservation that matches the user, date, time, and seat.
	3.	isSeatAvailable(LocalDate date, LocalTime time, int seatIndex)
Checks if a seat is unreserved, unlocked, and valid.
	4.	getReservationsForSlot(LocalDate date, LocalTime time)
Returns all reservations already made for the specified time slot.
	5.	calculateTotalPrice(List seats)
Adds up seat-specific prices to compute the final reservation cost.
	6.	cancelAllReservations(LocalDate date, LocalTime time)
Removes every reservation for a given time slot (admin action).
	7.	adminCancelReservation(Reservation r)
Allows admin to cancel a specific reservation regardless of user.

Test Cases:
	•	makeReservation() tested to ensure correct blocking of reserved or locked seats.
	•	isSeatAvailable() validated using overlapping reservations.
	•	Pricing functionality tested using mixed-price seat sets.
	•	setSeatingArrangement() tested to ensure locked seats and prices update correctly.

Relationship:
	•	Uses ReservationDatabase for all data storage and retrieval.
	•	Creates and manages Reservation objects for users.
	•	Implements ReservationManager, AdminControls, and UserManager, making it the primary logic for server operations.
	•	Used by the GUI and server to validate requests, enforce rules, and produce reservation outcomes.
	•	Interacts with User, Reservation, and the database to provide a consistent reservation experience.

<---------------------------------------------------------------------------------------->
ReservationManager.java
Overview:
Defines the core interface for all reservation related operations in the system.
Any class implementing ReservationManager must provide functionality for making reservations, canceling reservations, and allowing admins to remove specific reservations.
This ensures consistent reservation handling across the application.
Methods:
	1.	makeReservation(String username, LocalDate date, LocalTime time, List seats)
Attempts to create a reservation for the specified user at the given date, time, and seat list.
Returns true if successful, false if seats are unavailable or the user does not exist.
	2.	cancelReservation(String username, LocalDate date, LocalTime time, List seats)
Cancels a reservation made by the given user that matches the exact date, time, and seat selection.
Returns true if the reservation was found and removed.
	3.	adminCancelReservation(Reservation reservation)
An admin method that cancels a specific reservation object directly
Returns true if the reservation was successfully removed.

Test Cases:
These methods are tested indirectly through the implementation class ReservationHandler, especially in tests confirming:
	•	Correct seat availability tracking
	•	Successful and unsuccessful reservation attempts
	•	Admin overrides
	•	Proper removal of reservation objects

Relationship:
	•	Implemented by: ReservationHandler.java
	•	Used by: Server request handlers, GUI interactions, and admin tools to manage user reservations.
	•	Works with: Reservation, User, and ReservationDatabase through the implementing class.

<---------------------------------------------------------------------------------------->
ReservationSystemTests.java
Overview:
This file contains a full JUnit test class that validates all core functionality in the Reservation System.
It ensures correct behavior across account creation, login, seat reservation, cancellations, admin actions, pricing, locking, seating reconfiguration, and edge case handling.
The tests verify both user operations and internal works of the system via ReservationHandler.

	1.	Account Creation & Login
	•	testCreateAccount()
Ensures new accounts are created correctly and duplicate usernames are rejected.
	•	testLogin()
Confirms valid logins succeed and invalid credentials fail.
	2.	Reservation Operations
	•	testMakeReservation()
Ensures seats can be reserved if available and prevents double-booking.
	•	testCancelReservation()
Ensures cancellation succeeds once and fails if attempted again.
	•	testCancelAllReservations()
Verifies that all reservations for a specific date/time slot are removed.
	3.	Admin Controls
	•	testValidateAdmin()
Verifies the admin key authentication.
	•	testSetHours()
Ensures opening and closing hours can be modified and retrieved.
	•	testSetSeatPrice()
Ensures individual seat pricing can be updated.
	•	testSetSeatingArrangement()
Ensures full seating reconfiguration (rows × cols) works and prices update correctly.
	•	testLockAndUnlockSeats()
Ensures seat locking prevents reservation and unlocking restores availability.
	4.	Pricing
	•	testCalculateTotalPrice()
Confirms total seat cost is computed accurately.
	5.	Seat Availability & Edge Cases
	•	testInvalidSeatIndex()
Ensures out-of-range seat indexes are rejected.
	•	testReservationForNonexistentUser()
Ensures reservations cannot be made for users that don’t exist.

 <---------------------------------------------------------------------------------------->
 Response.java
Overview:
Represents the server’s reply to a client request. Used for all communication between client and server, carrying status information, messages, and optional data.

Fields / Methods:
	1.	isSuccess() / setSuccess(boolean)
Indicates whether the server operation succeeded.
	2.	getMessage() / setMessage(String)
Provides a message about the result.
	3.	getPayload() / setPayload(Object)
Carries additional data returned from the server (e.g., reservation lists, user info).
	4.	toString()
Returns a formatted string representation for debugging/logging.
Relationship:
	•	Sent by the server in response to a Request.
	•	Used by the client to determine next steps (display errors, update UI, handle data).
	•	Complements Request.java as part of the client–server protocol.
<---------------------------------------------------------------------------------------->
SeatingChartGUI.java
Overview:
Provides a graphical interface for users and admins to view and manage seat reservations. Displays a grid of seats with their availability and prices. Allows users to log in, make reservations, and view seat status, while admins can configure seating, set prices, and cancel reservations.
Key Features / Methods:
	1.	User Functions:
	•	Login, create account, reserve seats.
	•	Displays reserved seats in red and available seats in green.
	2.	Admin Functions:
	•	Set operating hours (setHours).
	•	Configure seating layout (setSeatingArrangement).
	•	Set individual seat prices (setSeatPrice).
	•	Cancel individual or all reservations for a time slot.
	3.	Grid Management:
	•	initializeSeatButtons() – creates seat buttons for the grid.
	•	refreshGrid() – updates colors and labels based on reservation status.
	•	rebuildSeatGrid() – rebuilds the seat grid when seating layout changes.
	4.	Date/Time Selection:
	•	dateSelector and timeSelector allow users/admins to select reservation slots.
Relationship:
	•	Uses ReservationHandler to interact with backend logic.
	•	Displays Reservation objects graphically.
	•	Integrates with User and Admin functionality for authentication and management.
Test Cases:
	•	Verified manually by logging in as users and admin, reserving seats, modifying seating/prices, and confirming grid updates.

<---------------------------------------------------------------------------------------->
Server.java
Overview:
Acts as the central server for the reservation system. Listens for incoming client connections, processes requests, and sends responses using ReservationHandler. Supports multiple clients concurrently.
Key Features / Methods:
	1.	start() – Opens a server socket on port 4242 and waits for client connections.
	2.	Client Handling – Uses ClientHandler to process each client’s requests in a separate thread.
	3.	Concurrency – Uses an ExecutorService to manage multiple clients simultaneously.
Relationship:
	•	Uses ReservationHandler for all reservation, user, and admin operations.
	•	Works with ClientHandler to handle serialized Request and Response objects.
	•	Communicates with client-side applications over sockets.
Test Cases:
	•	Tested by connecting multiple clients and performing actions such as login, reservations, and admin updates, confirming that responses are correctly handled.

<---------------------------------------------------------------------------------------->
User.java
Overview:
Represents a user account in the reservation system. Stores the username, password, and admin status. Serializable so that it can be saved to and loaded from the database.
Methods:
	1.	getUsername() – Returns the username.
	2.	getPassword() – Returns the password.
	3.	isAdmin() – Returns true if the user is admin.
Relationship:
	•	Used by ReservationHandler and ReservationDatabase for authentication and account management.
	•	Associated with Reservation objects to track which user made a reservation.
Test Cases:
	•	Verified indirectly via account creation, login, and admin functionality tests in ReservationSystemTests.java.

<---------------------------------------------------------------------------------------->
UserManager.java
Overview:
Defines the interface for user account management. Any class implementing this interface must provide consistent methods to create, authenticate, and delete user accounts.
Methods:
	1.	createAccount(String username, String password) – Creates a new user account. Returns true if successful, false if the username already exists.
	2.	login(String username, String password) – Authenticates a user with the given credentials. Returns true if login succeeds.
	3.	deleteAccount(String username, String password) – Deletes an existing account if credentials are valid. Returns true on successful deletion.
Relationship:
	•	Implemented by ReservationHandler.java.
	•	Used by SeatingChartGUI.java and server side logic to manage user accounts and validate logins.
Test Cases:
	•	Verified indirectly via ReservationSystemTests.java, including account creation, login, and deletion tests.
<---------------------------------------------------------------------------------------->

