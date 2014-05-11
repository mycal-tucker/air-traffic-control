import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;


public class GeneralInetAddress {
	
  /**
   * Returns an InetAddress representing the address of the localhost.
   * If no other address can be found, the loopback will
   * be returned.     
   * 
   * @return InetAddress - the address of localhost
   * @throws UnknownHostException - if there is a 
   * problem determing the address
   */

  public static InetAddress getLocalHost() throws UnknownHostException {
    InetAddress localHost = InetAddress.getLocalHost();
    if(!localHost.isLoopbackAddress()) 
      return localHost;

    InetAddress[] addrs = getAllLocalUsingNetworkInterface();
    for (int i=0; i<addrs.length; i++) {
      if(addrs[i].isLoopbackAddress()) 
	continue;
      if (Pattern.matches("\\d+\\.\\d+\\.\\d+\\.\\d+", 
			  addrs[i].getHostAddress()))
	return addrs[i];
    }
    return localHost;	
  }
	
  /**
   * This method attempts to find all InetAddresses for this machine in a
   * conventional way (via InetAddress).  If only one address is found and
   * it is the loopback, an attempt is made to determine the addresses for
   * this machine using NetworkInterface.
   * 
   * @return InetAddress[] - all addresses assigned to the local machine
   * @throws UnknownHostException - if there is a  problem determining 
   * addresses
   */

  public static InetAddress[] getAllLocal() throws 
    UnknownHostException {
    InetAddress[] iAddresses = InetAddress.getAllByName("127.0.0.1");
    if(iAddresses.length != 1) 
      return iAddresses;
    if(!iAddresses[0].isLoopbackAddress()) 
      return iAddresses;
    return getAllLocalUsingNetworkInterface();
  }
	
  /**
   * Utility method that delegates to the methods of NetworkInterface to
   * determine addresses for this machine.
   * 
   * @return InetAddress[] - all addresses found from the NetworkInterfaces
   * @throws UnknownHostException - if there is a problem determining 
   * addresses
   */

  private static InetAddress[] getAllLocalUsingNetworkInterface() 
    throws UnknownHostException {
    ArrayList<InetAddress> addresses = new ArrayList<InetAddress>();
    Enumeration e = null;
    try {
      e = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException ex) {
      throw new UnknownHostException("127.0.0.1");
    }
    while(e.hasMoreElements()) {
      NetworkInterface ni = (NetworkInterface)e.nextElement();
      for(Enumeration e2 = ni.getInetAddresses(); e2.hasMoreElements();) {
	InetAddress a = (InetAddress)(e2.nextElement());
	addresses.add(a);
      }	
    }
    InetAddress[] iAddresses = new InetAddress[addresses.size()];
    for(int i=0; i<iAddresses.length; i++) {
      iAddresses[i] = (InetAddress)addresses.get(i);
    }
    return iAddresses;
  }

  public static void main(String args[]) {
    try {
      System.out.println(GeneralInetAddress.getLocalHost().getHostAddress());
    } 
    catch (UnknownHostException e) {
      System.out.println("Could not determine hostname");
    }
  }

}
