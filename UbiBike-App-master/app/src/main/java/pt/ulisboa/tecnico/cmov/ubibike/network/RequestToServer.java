package pt.ulisboa.tecnico.cmov.ubibike.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Joao on 14/04/2016.
 */
public class RequestToServer {

    private String host;
    private int port;
    private InetAddress address;
    private Socket socket;
    private OutputStream os;
    private OutputStreamWriter osw;
    private BufferedWriter bw;

    public RequestToServer (){
        try{
            host = "10.0.2.2";
            port = 4444;
            address = InetAddress.getByName(host);
            socket = new Socket(address, port);
            os = socket.getOutputStream();
            osw = new OutputStreamWriter(os);
            bw = new BufferedWriter(osw);
        }
        catch (Exception exception)
        {
            Log.d("SERVER OFFLINE", "Server unreachable");
        }
    }

    public String tradeInformation(String request) throws Exception{
        try{
            bw.write(request);
            bw.flush();

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String answer = br.readLine();
            return answer;
        }
        catch (Exception e){
            Log.d("SERVER OFFLINE", "Server unreachable");
            throw e;
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e2)
            {
                Log.d("SERVER OFFLINE", "Server unreachable");
                throw e2;
            }
        }
    }
}
