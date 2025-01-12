// Server.java
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Set<Socket> clientSockets = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcastMessage(message);
                }
            } catch (IOException e) {
                System.err.println("Client disconnected: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientSockets.remove(clientSocket);
                System.out.println("Client disconnected.");
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clientSockets) {
                for (Socket socket : clientSockets) {
                    if (!socket.isClosed() && socket != clientSocket) {
                        try {
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            out.println(message);
                        } catch (IOException e) {
                            System.err.println("Error sending message: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
