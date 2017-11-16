package cmov.test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
	private String username;
	private String email;
	private String hashPassword;
	private int userPoints;
	private List<Trajectory> trajectories;
	private PublicKey publicKey;
	private Map<String, List<Integer>> mapIdTransactions = new HashMap<String,List<Integer>>();
	private List<Integer> transactionsCommited = new ArrayList<Integer>(); //transacoes aceites, enviadas por este user
	private List<Integer> transactionsAborted = new ArrayList<Integer>(); //transacoes abortadas, deste user, porque foram tampered!
	private Map<Integer, Integer> numberOfQuestionsPerTransactionId = new HashMap<Integer, Integer>();
	private int idLastTransaction;
	
	private Map<String, User> friends;
	
	public User(String username, String email, String pass, int userPoints){
		setUsername(username);
		setEmail(email);
		setPassword(pass);
		setUserPoints(userPoints);
		this.friends = new HashMap<String, User>();
		this.trajectories = new ArrayList<Trajectory>();
		this.idLastTransaction = 0;
	}
	
	public PublicKey getUserPublicKey(){
		return this.publicKey;
	}
	
	public void setUserPublicKey(PublicKey publicKey){
		this.publicKey = publicKey;
	}
	
	public Map<String,List<Integer>> getMapIdTransactions(){
		return this.mapIdTransactions;
	}
	
	public void addEntryMapTransactions(String username, int id){
		List<Integer> pastTransactions = new ArrayList<Integer>();
		pastTransactions.add(id);
		this.mapIdTransactions.put(username, pastTransactions);
	}
	
	public void updateEntryMapTransactions(String username, int id){
		List<Integer> pastTransactions = this.mapIdTransactions.get(username);
		pastTransactions.add(id);
		this.mapIdTransactions.replace(username, pastTransactions);
	}
	
	public void setIdLastTransaction(int id){
		this.idLastTransaction = id;
	}
	
	public int getIdLastTransaction(){
		return this.idLastTransaction;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	public String getHashPassword(){
		return this.hashPassword;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public void setPassword(String pass){
		this.hashPassword = hash(pass);
	}
	
	public int getUserPoints() {
		return userPoints;
	}

	public void setUserPoints(int userPoints) {
		this.userPoints = userPoints;
	}
	
	public boolean verifyUserPassword(String password){
		if(this.hashPassword.equals(hash(password)))
			return true;
		return false;
	}
	
	public void addFriend(User friend){
		this.friends.put(friend.getUsername(), friend);
	}
	
	public User getFriendByName(String friendName){
		return this.friends.get(friendName);
	}
	
	public Collection<User> getAllUserFriends(){
		return this.friends.values();
	}
	
	public void addTrajectory(Trajectory t){
		trajectories.add(t);
	}
	
	public List<Trajectory> getTrajectories(){
		return this.trajectories;
	}
	
	public List<Integer> getCommitedTransactions(){
		return this.transactionsCommited;
	}
	
	public void addToCommitedList(Integer id){
		this.transactionsCommited.add(id);
	}
	
	public List<Integer> getAbortedTransactions(){
		return this.transactionsAborted;
	}
	
	public void addToAbortedList(Integer id){
		this.transactionsAborted.add(id);
	}
	
	private String hash(String pass){
		String digestPass = "";
		try{
			byte[] passBytes = pass.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digestBytes = md.digest(passBytes);
			digestPass = new String(digestBytes, StandardCharsets.UTF_8);
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
		return digestPass;
	}
	
}

