import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;

    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
    }
    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done){
            Socket client = server.accept();
            ConnectionHandler handler = new ConnectionHandler(client);
            connections.add(handler);
            pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for(ConnectionHandler ch : connections) {
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown () {
        try {
            done = true;
        if(!server.isClosed()){
                server.close();
            }
        //Shutting down all connections
        for(ConnectionHandler ch: connections) {
            ch.shutdown();
        }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;

        private PrintWriter out;

        private String name;

        @Override
        public  void run() {
            try{
                //Initializing reader and writer
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                System.out.println("Enter name");
                name = in.readLine();
                //TODO: Add security if loops
                System.out.println(name + "connected");
                broadcast(name + "joined the chat");
                String message;
                while((message = in.readLine()) != null){
                    if(message.startsWith("/quit")) {
                        shutdown();
                        broadcast(name + "eft the chat");
                    }
                    else{
                        broadcast(name + ": " + message);
                    }
                }

            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMessage(String message){
            System.out.println(message);
        }

        public void shutdown(){
            try {
                in.close();
                out.close();
            if(!client.isClosed()){
                client.close();
            }
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }

        public ConnectionHandler(Socket client) {
            this.client = client;

        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
