import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Paths;

class HttpService implements Runnable {
  // This is the socket
  Socket socket;
  public HttpService(Socket inSocket) {
    this.socket = inSocket;
  }
  public void processRequest() throws Exception {
    String requestMessageLine;
    String filename;
    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
    requestMessageLine = inFromClient.readLine();
    StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);
    if(tokenizedLine.nextToken().equals("GET")) {
      filename = tokenizedLine.nextToken();
      if(filename.startsWith("/")) {
        filename = filename.substring(1);
      }
      File file = new File(filename);
      String statusLine;
      if(!file.exists()) {
        statusLine = getStatusLine(false);
        file = new File("404.html");
        filename = "404.html";
      }
      else {
        statusLine = getStatusLine(true);
      }
      // Write the status line of the response
      outToClient.writeBytes(statusLine);
      
      // Write the connection close
      outToClient.writeBytes("Connection Close\r\n");
      
      
      // Get the content type of the request
      String contentType = getContentType(filename);
      
      // Get the current datetime when the response is formed
      outToClient.writeBytes("Date: " + java.util.Calendar.getInstance().getTime() + "\r\n");
      
      // Get file meta data
      BasicFileAttributes attr = Files.readAttributes(Paths.get(filename), BasicFileAttributes.class);
      outToClient.writeBytes("Last-Modified: " + new java.util.Date(attr.lastModifiedTime().toMillis()) + "\r\n");
      int numOfBytes = (int) file.length();
      FileInputStream inFile = new FileInputStream(filename);
      byte[] fileInBytes = new byte[numOfBytes];
      inFile.read(fileInBytes);
      outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
      
      // Write the response body
      outToClient.writeBytes(contentType);
      outToClient.writeBytes("\r\n");
      outToClient.write(fileInBytes, 0, numOfBytes);
      socket.close();
    }
  }
  
  // This is a helper method to help determine what type of content is related to the request
  public String getContentType(String filename) {
    String res = "Content-Type: ";
    if(filename.endsWith(".jpg")) {
      res += "image/jpg";
    }
    else if(filename.endsWith(".jpeg")) {
      res += "image/jpeg";
    }
    else if(filename.endsWith(".png")) {
      res += "image/png";
    }
    else if(filename.endsWith(".html") || filename.endsWith(".txt")) {
      res += "text/html";
    }
    return res + "\r\n";
  }
  
  // returns the http response string for the status part of the response
  public String getStatusLine(boolean exists) {
    String code = exists ? "200":"404";
    String end = exists ? " OK\r\n": " Not Found\r\n";
    return "HTTP/1.0 " + code + end;
  }
  
  public void run() {
    try {
      processRequest();
    }
    catch(Exception e)
    {
      e.printStackTrace(System.out);
    }
  }
}
