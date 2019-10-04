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
    // Class socket that represents the request
    this.socket = inSocket;
  }
  public void processRequest() throws Exception {
    /* The next few lines were obtained from the class textbook*/
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
      // This statement checks to see if the current file name is not anything other than "index.html" variant
      if(filename.contains("index") && filename.endsWith(".html") && !filename.equals("index.html")) {
        String contentType = "HTTP/1.0 301 Moved Permanently\r\n";
        String location = "Location: http://localhost:6789/index.html\r\n";
        outToClient.writeBytes(contentType);
        outToClient.writeBytes(location);
      }
      else {
        File file = new File(filename);
        String statusLine;
        // Serve the 404 html page when it is not found
        if(!file.exists()) {
          statusLine = getStatusLine(false);
          // change the file name to the 404 page
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
        if(filename != "404.html") {
          outToClient.writeBytes("Last-Modified: " + new java.util.Date(attr.lastModifiedTime().toMillis()) + "\r\n");
        }
        int numOfBytes = (int) file.length();
        FileInputStream inFile = new FileInputStream(filename);
        byte[] fileInBytes = new byte[numOfBytes];
        inFile.read(fileInBytes);
        outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
        // Write the response body
        outToClient.writeBytes(contentType);
        outToClient.writeBytes("\r\n");
        outToClient.write(fileInBytes, 0, numOfBytes);
      }
      socket.close();
    }
  }
  
  // This is a helper method to help determine what type of content is related to the request
  // The arguments are the name of the file so that I can display the corrext formay
  public String getContentType(String filename) {
    String res = "Content-Type: ";
    // Check what the file ends with in order to display correct type
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
  // Acceps a boolean values that indicates whether the file exists in the directory
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
