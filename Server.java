import java.io.*;
import java.net.*;
import java.util.*;
class Server {
  public static int port = 6789;
  
  public static void main(String[] args) throws Exception {
    // This is the socket that makes the initial connection to initiate the connection
    ServerSocket welcomeSocket = new ServerSocket(port);
    System.out.println("Listening on port: " + port);
    try {
      while(true) {
        // Accept the connection and open a new socket
        Socket socket = welcomeSocket.accept();
        System.out.println("Serving request for " + socket.getInetAddress() + " from port " + socket.getPort());
        // Invoke the handler to handle the http request
        HttpService handler = new HttpService(socket);
        // Spawn off a thread to handle multiple connections
        Thread worker = new Thread(handler);
        worker.start();
      }
    }
    catch(Exception e) {
      e.printStackTrace(System.out);
    }
    finally {
      welcomeSocket.close();
    }
  }
}
