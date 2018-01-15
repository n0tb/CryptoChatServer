package CryptoChatServer;

import java.io.PrintWriter;
import java.util.Map;

public class WriterWorker extends Thread{

    String recipient;
    PrintWriter output;
    Map<String, String> stateChat;

    public WriterWorker(PrintWriter output) {
        this.output = output;
    }

    @Override
    public void run() {
        String respMsg;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                respMsg = stateChat.get(recipient);
                if (!respMsg.isEmpty()){
                    output.println(respMsg);
                    stateChat.put(recipient, "");
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setStateChat(Map<String, String> stateChat) {
        this.stateChat = stateChat;
    }
}
