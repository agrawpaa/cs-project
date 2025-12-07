import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 4242;
    private final ReservationHandler handler;
    private ServerSocket serverSocket;
    private ExecutorService clientPool;

    public Server() {
        handler = new ReservationHandler();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        clientPool = Executors.newCachedThreadPool(); // allows multiple clients

        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            clientPool.submit(new ClientHandler(clientSocket, handler));
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }
}
