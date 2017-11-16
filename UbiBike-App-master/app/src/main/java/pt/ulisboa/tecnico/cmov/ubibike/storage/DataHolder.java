package pt.ulisboa.tecnico.cmov.ubibike.storage;

import android.content.ServiceConnection;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Message;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Trajectory;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Transaction;
import pt.ulisboa.tecnico.cmov.ubibike.domain.User;

/**
 * Created by rwagenmaker on 2016/04/14.
 */
public class DataHolder {
    private String string;
    private SimWifiP2pManager mManager;
    private Channel mChannel;
    private ServiceConnection mConnection;
    private User appUser;
    private String closeStation = "None";
    private String closeBike = "None";
    private Boolean isNearToStation = false;
    private Boolean isNearBike = false;

    public SimWifiP2pSocketServer getmSrvSocket() {
        return mSrvSocket;
    }

    public void setmSrvSocket(SimWifiP2pSocketServer mSrvSocket) {
        this.mSrvSocket = mSrvSocket;
    }

    private SimWifiP2pSocketServer mSrvSocket = null;

    private Map<String,List<Message>> messagesHolder = new HashMap<>();
    private List<SimWifiP2pDevice> newPeersAvailable = new ArrayList<>();
    private List<Transaction> pointsToCommit = new ArrayList<>();

    private List<String> signaturesToCommit = new ArrayList<String>();
    private List<SimWifiP2pDevice> oldPeers = new ArrayList<>();
    private List<Trajectory> trajectories = new ArrayList<>();
    private List<Trajectory> trajectoriesUpdated = new ArrayList<>();

    //METHODS

    public List<String> getSignaturesToCommit() { return signaturesToCommit;}

    public Boolean getIsNearToBike() {  return isNearBike;  }
    public void setIsNearToBike(Boolean nearBike) { isNearBike = nearBike; }

    public String getCloseStation() { return closeStation; }
    public void setCloseStation(String closeStation) { this.closeStation = closeStation;  }

    public String getCloseBike() { return closeBike; }
    public void setCloseBike(String closeBike) { this.closeBike = closeBike;  }

    public Boolean getIsNearToStation() { return isNearToStation; }
    public void setIsNearToStation(Boolean nearToStation) { isNearToStation = nearToStation; }

    public List<Transaction> getPointsToCommit() {
        return pointsToCommit;
    }

    public List<Trajectory> getTrajectories() {return this.trajectories;}
    public void setTrajectories(List<Trajectory> trajectories){this.trajectories = trajectories;}
    public void addTrajectory(Trajectory trajectory){
        this.trajectories.add(trajectory);
    }

    public List<Trajectory> getTrajectoriesUpdated() {return this.trajectoriesUpdated;}
    public void setTrajectoriesUpdated(List<Trajectory> trajectoriesUpdated){this.trajectoriesUpdated = trajectoriesUpdated;}
    public void addTrajectoryToUpdate(Trajectory trajectoryFromServer){
        this.trajectoriesUpdated.add(trajectoryFromServer);
    }

    public List<SimWifiP2pDevice> getOldPeers() {
        return oldPeers;
    }
    public void setOldPeers(List<SimWifiP2pDevice> oldPeers) {
        this.oldPeers = oldPeers;
    }

    public void setNewPeersAvailable(List<SimWifiP2pDevice> newPeersAvailable) {
        this.newPeersAvailable = newPeersAvailable;
    }
    public List<SimWifiP2pDevice> getNewPeersAvailable() {
        return newPeersAvailable;
    }
    public void addToNewPeers(SimWifiP2pDevice newPeer){
        this.newPeersAvailable.add(newPeer);
    }

    public String getUsername() {return string;}
    public void setUsername(String string) {this.string = string;}

    public User getAppUser() {return appUser;}

    public SimWifiP2pManager getmManager() {return mManager;}
    public void setmManager(SimWifiP2pManager mManager) {this.mManager = mManager;}

    public Channel getmChannel() {return mChannel;}
    public void setmChannel(Channel mChannel) {this.mChannel = mChannel;}

    public ServiceConnection getmConnection() {return mConnection;}
    public void setmConnection(ServiceConnection mConnection) {this.mConnection = mConnection;}

    public Map<String,List<Message>> getMessageHolder() {return messagesHolder;}
    public void addMessageToHolder(String userName, Message message){
        if (messagesHolder.containsKey(userName)) {
            messagesHolder.get(userName).add(message);
        } else {
            messagesHolder.put(userName,new ArrayList<Message>());
            messagesHolder.get(userName).add(message);
        }
    }


    public void addSignatureToCommit(String msgAndSignature) {
        this.signaturesToCommit.add(msgAndSignature);
    }

    public Message getLastMessage (String userName) {
        Log.d("Data Holder", "\t\t\t\t\t getLastMessage.userName ==> " + userName);

        if (messagesHolder.containsKey(userName)) {
            List<Message> messageList = messagesHolder.get(userName);
            Message msg = messageList.get(messageList.size()-1);

            return msg;
        }

        return new Message("NomeMaluco", "deu buraco no data holder", false);
    }

    public List<Message> getMessagesListForName (String userName) {
        if (messagesHolder.containsKey(userName)) {
            List<Message> messageList = messagesHolder.get(userName);


            return messageList;
        }

        return new ArrayList<Message>();
    }

    public void addPointsToCommit(Transaction t){
        pointsToCommit.add(t);
    }

    public void createUser(String username, int points){
        this.appUser = new User(username, points);
        this.appUser.generateUserKeys();
    }

    public boolean existsUserInNewPeers(String peerName){
        for (SimWifiP2pDevice newPeer : getNewPeersAvailable()){
            if (peerName.equals(newPeer.deviceName))
                return true;
        }
        return false;
    }

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}


}