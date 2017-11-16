package cmov.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private static Socket socket;
	
	private static UserManager userManager = new UserManager();
	private static BikeStationManager bikeStationManager = new BikeStationManager();
	private static RequestParser requestParser = new RequestParser(userManager, bikeStationManager);
	private static String spliter = "class pt.ulisboa.tecnico.cmov.ubibike.parsers.EofIndicatorClass";
	
	private static void populate(){
		User user = new User("Joao","joao@hotmail.com","olaola", 500);
		User ivo = new User("Ivo","ivo@hotmail.com","ivo",200);
		User ricardo = new User("Ricardo", "ricardo@hot.com", "ricky", 1);
		userManager.addUser(user);
		userManager.addUser(ivo);
		userManager.addUser(ricardo);
		//BikeStation tagus = new BikeStation(-9.302326, 38.736735, "Tagus", 10);
		//BikeStation tagus2 = new BikeStation(-9.3106965, 38.7357849, "Tagus2", 200);
		//BikeStation tagus3 = new BikeStation(-9.3081538, 38.7364293, "Tagus3", 5);
		BikeStation tagus = new BikeStation(-9.20676827, 38.75322986, "Station-Tagus", 10);
		BikeStation tagus2 = new BikeStation(-9.19113, 38.75077, "Station-Tagus2", 200);
		BikeStation tagus3 = new BikeStation(-9.18283225, 38.7601071, "Station-Tagus3", 5);
		bikeStationManager.addBikeStation(tagus);
		bikeStationManager.addBikeStation(tagus2);
		bikeStationManager.addBikeStation(tagus3);
		String route = "Ivo:-9.204868333333334,38.752568333333336;-9.205158333333333,38.75197;-9.202518333333334,38.75104833333333;-9.191203333333334,38.749248333333334;-9.191128333333333,38.75077;-9.18824,38.752808333333334;-9.185726666666666,38.75577333333333;-9.184996666666667,38.760196666666666;-9.182831666666667,38.760106666666665";
		String route1 = "Ivo:-9.204868333333334,38.752568333333336;-9.205158333333333,38.75197;-9.202518333333334,38.75104833333333;-9.191203333333334,38.749248333333334;-9.191128333333333,38.75077;-9.18824,38.752808333333334;-9.185726666666666,38.75577333333333;-9.184996666666667,38.760196666666666;-9.182831666666667,38.760106666666665";
		requestParser.saveNewTrajectory(route);
		requestParser.saveNewTrajectory(route1);
		//userManager.getUserByName("Joao").addEntryMapTransactions("Ivo", 1); //Testa replay attack
	}
	 
    public static void main(String[] args)
    {	
    	populate();
        try
        {
            ServerSocket serverSocket = new ServerSocket(4444);
            System.out.println("Server Started and listening to the port 4444");
 
            //Server is running always. This is done using this while(true) loop
            while(true)
            {
                //Reading the message from the client
                socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String request = "";
                String line = br.readLine();
                if (line.contains("Send PublicKey") || line.contains("New Transaction")){
                    while (br.ready()){
                        line += br.readLine();
                    }
                }
                request = line;
                System.out.println("Message received from client is "+ request.replaceAll(spliter, " "));
                String returnMessage = requestParser.parserIntoSubParser(request);
 
                //Sending the response back to the client.
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                if(returnMessage != null)
                	bw.write(returnMessage + "\n");
                System.out.println("Message sent to the client is " + returnMessage.replaceAll(spliter, " "));
                bw.flush();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
    }
}
