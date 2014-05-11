import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class DisplayClient  {
  PrintWriter output; 
  protected NumberFormat format = new DecimalFormat("#####.##");

  public DisplayClient(String host) {
    InetAddress address;
    try {
      address = InetAddress.getByName(host);
      Socket server = new Socket(address, 5065);
      output = new PrintWriter(server.getOutputStream());
    }
    catch (UnknownHostException e) {
      System.err.println("I can't find a host called "+host+". Are you sure you got the name right?");
      System.err.println(e);
      System.exit(-1);
    }
    catch (IOException e) {
      System.err.println("I can't connect to the DisplayServer running on "+host+".\n");
      System.err.println("Did you remember to start the DisplayServer?");
      System.err.println(e);
      System.exit(-1);
    }
  }

  public void clear() {
    output.println("clear");
  }

  public void traceOn() {
    output.println("traceon");
  }

  public void traceOff() {
    output.println("traceoff");
  }

  public void update(int numVehicles, double gvX[], double gvY[], double gvTheta[])
  {
    StringBuffer message = new StringBuffer();
    message.append(numVehicles);
    message.append(" ");
    for (int i = 0; i < numVehicles; i++) {
      message.append(format.format(gvX[i])+" "+format.format(gvY[i])+" "+
		     format.format(gvTheta[i])+" ");
    }
    //System.out.println("Sent "+message);
    output.println(message);
    output.flush();
  }
  
  /**
   * The message goes like this
   * airports size x1 y1 x2 y2 x3 y3
   * 
   * e.g.:
   * airports 2 25 25 75 75
   * 
   * @param airportList: list of airports
   */
  public void sendAirportMessage(ArrayList<Airport> airportList){
	  StringBuffer message = new StringBuffer();
	  message.append("airports");
	  message.append(" ");
	  message.append(airportList.size());
	  message.append(" ");
	  for (int i = 0; i < airportList.size(); i ++){
		  Airport a = airportList.get(i);
		  message.append(a.getX() + " ");
		  message.append(a.getY() + " ");
	  }
	  output.println(message);
	  output.flush();
  }
  
  public static void main(String argv[]) throws IOException {
    if (argv.length == 0) {
      System.err.println("Usage: DisplayClient <hostname>\n"+
			 "where <hostname> is where DisplayServer is running");
      System.exit(-1);
    }
    String host = argv[0];

    DisplayClient server = new DisplayClient(host);
    double gvX[] = new double[2];
    double gvY[] = new double[2];
    double gvTheta[] = new double[2];
      
    for (int i = 0; i < 2; i++) {
      gvX[i] = Math.random()*100;
      gvY[i] = Math.random()*100;
      gvTheta[i] = Math.PI*i;
    }

    server.update(2, gvX, gvY, gvTheta);
    System.out.print("Press return to continue...");
    System.in.read();
    server.traceOn();
    gvX[0] = 10;
    gvY[0] = 10;
    gvX[1] = 30;
    gvY[1] = 30;
    server.update(2, gvX, gvY, gvTheta);
    gvX[0] = 90;
    gvY[0] = 10;
    gvX[1] = 70;
    gvY[1] = 30;
    server.update(2, gvX, gvY, gvTheta);
    gvX[0] = 90;
    gvY[0] = 90;
    gvX[1] = 70;
    gvY[1] = 70;
    server.update(2, gvX, gvY, gvTheta);
    gvX[0] = 10;
    gvY[0] = 90;
    gvX[1] = 30;
    gvY[1] = 70;
    server.update(2, gvX, gvY, gvTheta);
    gvX[0] = 10;
    gvY[0] = 10;
    gvX[1] = 30;
    gvY[1] = 30;
    server.update(2, gvX, gvY, gvTheta);
    System.out.print("Press return to continue...");
    System.in.read();
    server.traceOff();
    server.clear();

    for (int i = 0; i < 2; i++) {
      gvX[i] = Math.random()*100;
      gvY[i] = Math.random()*100;
      gvTheta[i] = Math.PI*i;
    }

    server.update(2, gvX, gvY, gvTheta);
    System.out.print("Press return to exit...");
    System.in.read();
  }
}