package CryptoChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CryptoChatServerApp {

    public static void main(String[] args) {

        Socket socket;
        Integer amountThreads = 0;
        ExecutorService pool;
        List<String> clients = new ArrayList<>();
        Map<String, String> stateChat = new ConcurrentHashMap<>();
        Map<String, String> socialGraph = new ConcurrentHashMap<>();

        try (ServerSocket listener = new ServerSocket(9999)) {
            System.out.println("Server is waiting to connect clients...\n");
            pool = Executors.newFixedThreadPool(10);
            while (true) {
                socket = listener.accept();
                System.out.println("Connection with the new client is established");
                System.out.println("Handling the client in "
                        + ++amountThreads + " thread...\n");
                pool.execute(new ClientHandler(socket, amountThreads,
                        clients, stateChat, socialGraph));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Sever stopped!");
    }
}
