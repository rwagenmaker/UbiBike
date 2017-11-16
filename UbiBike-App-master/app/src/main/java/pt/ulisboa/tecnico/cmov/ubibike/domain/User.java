package pt.ulisboa.tecnico.cmov.ubibike.domain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ivopires on 15/04/16.
 */
public class User {

    private String userName;
    private int userPoints;
    private String ipAddress;
    private Map<String, User> myPeers = new HashMap<String,User>();
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private int idTransaction;
    private List<Transaction> temporaryTransactions = new ArrayList<Transaction>(); //as que recebemos
    //private List<Transaction> transactionsToConfirm = new ArrayList<>(); //as que enviamos
   // private List<String> peersInList = new ArrayList<>(); //resolver problema de mostrar users repetidos na pointsActivity
    private boolean hasBikeBooked = false;
    private String bookedStationName = "none";

    public User(String name, String ipAddress) {
        this.userName = name;
        this.ipAddress = ipAddress;
        this.idTransaction = 0;
    }

    public User(String userName, int points){
        this.userName = userName;
        this.userPoints = points;
        this.idTransaction = 0;
    }

    // ===================
    // 		Getters
    // ===================

    public int getIdTransaction(){
        return this.idTransaction;
    }

    public List<Transaction> getTemporaryTransactions(){
        return this.temporaryTransactions;
    }

    public String getUserName(){
        return this.userName;
    }

    public int getUserPoints(){
        return this.userPoints;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Map<String, User> getPeers(){
        return this.myPeers;
    }

    public User getPeerByName(String peerName){
        if (myPeers.containsKey(peerName))
            return myPeers.get(peerName);
        return null;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getBookedStationName() {return bookedStationName; }

    public boolean hasBikeBooked() {return hasBikeBooked; }



    // ===================
    // 		Setters
    // ===================

    public void setUserName(String name){
        this.userName=name;
    }

    public void setUserPoints(int points){
        this.userPoints=points;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setBookedStationName(String bookedStationName) {this.bookedStationName = bookedStationName; }

    public void setHasBikeBooked(boolean hasBikeBooked) { this.hasBikeBooked = hasBikeBooked;}



    // ===================
    // 		Adders
    // ===================


    public void addTemporaryTransactions(Transaction t){
        this.temporaryTransactions.add(t);
    }

    public void addIdTransaction(){
        this.idTransaction++;
    }

    public void addPeer(String peerName, User peer){
        this.myPeers.put(peerName, peer);
    }

    // ===================
    // 		Other
    // ===================

    public void removePeer(String peerName){
        this.myPeers.remove(peerName);
    }

    public void subtractPoints(int points){
        setUserPoints(getUserPoints()-points);
    }

    public void generateUserKeys(){
        try {
            //initialize key generator
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            //generate key pair
            KeyPair keyPair = keyGen.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    public boolean existsPeer(String peer){
        return getPeers().containsKey(peer);
    }

    /*public List<String> getPeersInList(){return this.peersInList;}

    public void addInPeersInList(String s){this.peersInList.add(s);}*/

    /*public List<Transaction> getTransactionsToConfirm(){
        return this.transactionsToConfirm;
    }

    public void addTransactionsToConfirm(Transaction t){
        this.transactionsToConfirm.add(t);
    }*/

    /*public List<Transaction> getTemporaryTransactions(){
        return this.temporaryTransactions;
    }

    public void addTemporaryTransactions(Transaction t){
        this.temporaryTransactions.add(t);
    }

    public int getIdTransaction(){
        return this.idTransaction;
    }

    public void addIdTransaction(){
        this.idTransaction++;
    }

    public void setUserName(String name){
        this.userName=name;
    }

    public void setUserPoints(int points){
        this.userPoints=points;
    }

    public String getUserName(){
        return this.userName;
    }

    public int getUserPoints(){
        return this.userPoints;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Map<String, User> getPeers(){
        return this.myPeers;
    }

    public void addPeer(String peerName, User peer){
        this.myPeers.put(peerName, peer);
    }

    public void removePeer(String peerName){
        this.myPeers.remove(peerName);
    }

    public User getPeerByName(String peerName){
        if (myPeers.containsKey(peerName))
            return myPeers.get(peerName);
        return null;
    }

    public void subtractPoints(int points){
        setUserPoints(getUserPoints()-points);
    }

    public void generateUserKeys(){
        try {
            //initialize key generator
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            //generate key pair
            KeyPair keyPair = keyGen.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }


    public PublicKey getPublicKey() {
        return publicKey;
    }

    public boolean existsPeer(String peer){
        return getPeers().containsKey(peer);
    }*/

}