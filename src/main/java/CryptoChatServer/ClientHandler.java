package CryptoChatServer;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable{

    private String login;
    private String pass;
    private Socket socket;
    private Integer amountThreads;
    private ChatDao chatDao;
    private Map<String, String> stateChat;
    private Map<String, String> socialGraph;

    public ClientHandler(Socket socket, Integer amountThreads, List<String> clients,
                         Map<String, String> stateChat, Map<String, String> socialGraph) {
        this.socket = socket;
        this.stateChat = stateChat;
        this.socialGraph = socialGraph;
        this.amountThreads = amountThreads;
        this.chatDao = new ChatDao(clients);
    }

    public void run() {
        String msg;
        String recipient;
        String pubKeyRecip;

        try (BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream()), true)) {


            WriterWorker WriterWorker = new WriterWorker(output);
            login(input, output);

            while (true) {
                output.println(chatDao.getUsersOnline());
                while ((recipient = input.readLine()).equals("UPDT")) {
                    output.println(chatDao.getUsersOnline());
                }

                pubKeyRecip = chatDao.getPubKeyRecip(recipient);
                output.println(pubKeyRecip);

                socialGraph.put(login, recipient);
                System.out.println("SocialGraph: " + socialGraph);

                WriterWorker.setRecipient(recipient);
                WriterWorker.setStateChat(stateChat);
                WriterWorker.start();
                while (true) {
                    msg = input.readLine();
                    if (msg.equals("CLOSE") || msg.equals("QUIT")) {
                        break;
                    }
                    stateChat.put(login, msg);
                    System.out.println("> [" + login + "]:" +
                            msg.substring(0, 10) + "..." + " -->> [" + recipient + "]");
                }

                if (msg.equals("QUIT")) {
                    chatDao.quit(login, pass);
                    break;
                }
            }
            WriterWorker.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.amountThreads--;
        System.out.println("Client [" + login + "] disconnect");
    }

    public void login(BufferedReader input, PrintWriter output) {
        String status;
        String pubKeyBase64;
        boolean succLogin = false;

        try {
            status = input.readLine();
            if (status.equals("2")) {
                login = input.readLine();
                pass = input.readLine();
                chatDao.registration(login, pass);

                pubKeyBase64 = input.readLine();
                chatDao.setPubKey(login, pass, pubKeyBase64);
                System.out.println("pubKey[" + login + "]:" + pubKeyBase64);
            }

            while (!succLogin) {
                login = input.readLine();
                pass = input.readLine();
                succLogin = chatDao.login(login, pass);
                output.println(succLogin);
            }
            stateChat.put(login, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
