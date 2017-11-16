package cmov.test;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

public class UserManager {
	
	private Map<String, User> users;
	
	public UserManager(){
		this.users = new HashMap<String, User>();
	}
	
	public void addUser(User user){
		if(!this.existsUser(user.getUsername()))
			this.users.put(user.getUsername(), user);
	}
	
	public User getUserByName(String userName){
		return this.users.get(userName);
	}
	
	private boolean existsUser(String userName){
		if(this.users.containsKey(userName))
			return true;
		return false;
	}
	
	private boolean verifyPassword(String userName, String userPass) {
		return this.getUserByName(userName).verifyUserPassword(userPass);
	}
	
	private boolean validUsername(String username){
		if (username.length() >= 4)
			return true;
		return false;
	}
	
	private boolean validEmail(String email){
		if (!email.isEmpty() && email.contains("@") && email.contains(".")){
			String[] splitEmail = email.split("@", -1);
			//System.out.println(splitEmail[0]);
			if (!splitEmail[0].isEmpty()){
				String[] splitTwice = splitEmail[1].split("\\.",-1);
				if(!splitTwice[0].isEmpty() || !splitTwice[1].isEmpty())
					return true;
			}
		}
		return false;
	}
	
	private boolean validPassword(String password){
		if (password.length() >= 4)
			return true;
		return false;
	}
	
	public boolean login(String userName, String userPass){
		if(existsUser(userName)){
			if(verifyPassword(userName, userPass))
				return true;
		}
		return false;
	}
	
	public String create(String username, String email, String pass){
		if (!existsUser(username)){
			if (validUsername(username)){
				if (validEmail(email)){
					if (validPassword(pass)){
						User newUser = new User(username, email, pass, 0);
						addUser(newUser);
						return "OK";
					}
					return "Invalid Password. At least 4 characters";
				}
				return "Invalid email format";
			}
			return "Invalid Username. At least 4 characters";
		}
		return "Username already exists";
	}
	
	public void commitNewPointsTransactions(String sender, String receiver, String points){
		int intPoints = Integer.parseInt(points);
		if (!sender.equals("Riding")){
			User senderUser = this.users.get(sender);
			senderUser.setUserPoints(senderUser.getUserPoints()-intPoints);
		}
		User receiverUser = this.users.get(receiver);
		receiverUser.setUserPoints(receiverUser.getUserPoints()+intPoints);
	}
  
	public void addPublicKey(String username, String publicKeyString){
		try{
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        byte[] publicKeyBytes = Base64.decodeBase64(publicKeyString.getBytes());
	        KeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
	        PublicKey publicKey = keyFactory.generatePublic(keySpec);
	        this.users.get(username).setUserPublicKey(publicKey);;
	        
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException  e){
			e.printStackTrace();
		}
	}
  
	
	public void addTrajectoryToUser(String userNameToAdd, Trajectory trajectory){
		this.users.get(userNameToAdd).addTrajectory(trajectory);
	}
	
	public List<Trajectory> getTrajectoryFromUser(String userName){
		return this.users.get(userName).getTrajectories();
	}
}
