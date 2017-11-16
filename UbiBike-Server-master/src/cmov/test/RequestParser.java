package cmov.test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

public class RequestParser {
	
	private UserManager userManager;
	private BikeStationManager bikeStationManager;
	private String ERROR_NO_BIKES_AVAILABLE = "-1";
	private static String spliter = "class pt.ulisboa.tecnico.cmov.ubibike.parsers.EofIndicatorClass";
	
	public RequestParser(UserManager userManager, BikeStationManager bikeStationManager){
		this.userManager = userManager;
		this.bikeStationManager = bikeStationManager;
	}
	
	public String parserIntoSubParser(String requestFromUser){
		System.out.println("REQUEST : "+requestFromUser.replace(spliter, " "));
		String[] requestToParse = requestFromUser.split("_", -1);
		String request = requestToParse[0];
		String parameters = null;
		if(!requestToParse[1].isEmpty())
			parameters = requestToParse[1];
		
		switch(request){
			case "Get Stations":
				return getAllStations();
			case "Login":
				return checkLogin(parameters);
			case "Create Account":
				return createAccount(parameters);
			case "Book":
				return bookBike(parameters);
			case "Get Points":
				return getPointsFromUser(parameters);
			case "New Transaction":
				return commitChangesToUserPoints(parameters);
			case "New Trajectory":
				return saveNewTrajectory(parameters);
			case "Get Routes":
				return getRoutesFromUser(parameters);
			case "Send PublicKey":
				return savePublicKey(parameters);
			case "Confirm Transactions":
				return confirmTransactions(parameters);
			case "Inform":
				return informStation(parameters);
		}
		return null;
	}

	private String confirmTransactions(String parameters){
		String answer = "";
		String spliter = "class pt.ulisboa.tecnico.cmov.ubibike.parsers.EofIndicatorClass";
		String[] content = parameters.split(spliter, -1);
		String username = content[0];
		Integer id;
		List<Integer> transactionsCommited = this.userManager.getUserByName(username).getCommitedTransactions();
		List<Integer> transactionsAborted = this.userManager.getUserByName(username).getAbortedTransactions();
		for (int i=1; i<content.length; i++){
			id = Integer.valueOf(content[i]);
			if (transactionsCommited.contains(id))
				answer += id + spliter + "Commited" + spliter;
			else if (transactionsAborted.contains(id))
				answer += id + spliter + "Aborted" + spliter;
			else
				answer += id + spliter + "Not received" + spliter;
		}
		answer = answer.substring(0, answer.length() - spliter.length());
		return answer;
	}
	
	private String checkLogin(String parametersToLogin) {
		String[] parameters = parametersToLogin.split(",",-1);
		String userName = parameters[0];
		String userPass = parameters[1];
		System.out.println("Parametros de Login: "+ userName + "," + userPass);
		if(this.userManager.login(userName, userPass))
			return "Login Success";
		return "Login Fail";
	}
	
	private String createAccount(String parametersToCreate){
		String[] parameters = parametersToCreate.split(",",-1);
		String userName = parameters[0];
		String email = parameters[1];
		String pass = parameters[2];
		
		String create = this.userManager.create(userName, email, pass);
		return create;
	}
	
private String bookBike(String stationToBook){
		
		String response = this.bikeStationManager.bookBikeByStation(stationToBook);
		if (response.equals(ERROR_NO_BIKES_AVAILABLE)) {
			return response+"_0";
		}else {
			return stationToBook+"_"+response;
		}
	}
	
	private String informStation(String parametersToInform){
		
		String[] parameters = parametersToInform.split(",",-1);
		String typeOfInfo = parameters[0];
		String stationName = parameters[1];
		String hasReservation = parameters[2];
		
		String response = null;
		
		if(typeOfInfo.equals("PickBike") ) {
			response = this.bikeStationManager.pickInformStationByName(stationName, hasReservation);
		} else if (typeOfInfo.equals("DeliverBike")) {
			response = this.bikeStationManager.deliverInformStationByName(stationName);
		} else {
			return "InformError";
		}
		
		return response;
	}
	
	private String getAllStations(){
		return this.bikeStationManager.getAllBikeStations();
	}
	
	private String getPointsFromUser(String parameters) {
		return new Integer(this.userManager.getUserByName(parameters).getUserPoints()).toString();
	}
	
	public String saveNewTrajectory(String parameters) {
		String[] parametersWithoutUserName = parameters.split(":", -1);
		String[] gpsPoints = parametersWithoutUserName[1].split(";", -1);
		String userNameToAdd = parametersWithoutUserName[0];
		String[] coordinates = null;
		List<String> listLatitudes = new ArrayList<String>(); 
		List<String> listLongitudes = new ArrayList<String>();
		for(String points : gpsPoints){
			coordinates = points.split(",", -1);
			String longitude = coordinates[0];
			String latitude = coordinates[1];
			listLongitudes.add(longitude);
			listLatitudes.add(latitude);
		}
		Trajectory trajectory = new Trajectory(listLatitudes, listLongitudes);
		this.userManager.addTrajectoryToUser(userNameToAdd, trajectory);
		
		return "OK";
	}
	
	private String getRoutesFromUser(String parameters){
		String userName = parameters;
		List<Trajectory> trajectoriesFromUser = this.userManager.getUserByName(userName).getTrajectories();
		String answerToClient = new String();
		
		if(!trajectoriesFromUser.isEmpty()){
			for(Trajectory trajectory : trajectoriesFromUser){
				System.out.println("Numero de Trajetorias: " + trajectoriesFromUser.size());
				for(int i = 0; i < trajectory.getLatitudes().size(); i++ ){
					answerToClient += trajectory.getLongitudes().get(i) + "," + trajectory.getLatitudes().get(i)+";";
				}
				answerToClient = answerToClient.substring(0, answerToClient.length()-1);
				answerToClient += "|";
			}
			answerToClient = answerToClient.substring(0, answerToClient.length()-1);
		}else
			answerToClient = "EMPTY";
		
		return answerToClient;
	}
	
	private String commitChangesToUserPoints(String parametersToCommit){
		String spliter = "class pt.ulisboa.tecnico.cmov.ubibike.parsers.EofIndicatorClass";
		String[] content = parametersToCommit.split(spliter, -1);
		
		String sender, receiver, points, senderSig, pointsSig, actionSig, idTransaction, idTransactionInsideSignature, signedMsg;
		String answer = "";
		
		for (int i=0; i<content.length; i++){
			sender = content[i];
			i++;
			receiver = content[i];
			i++;
			points = content[i];
			i++;
			idTransaction = content[i];
			if(!sender.equals("Riding")){
				i++;
				senderSig = content[i];
				i++;
				pointsSig = content[i];
				i++;
				actionSig = content[i];
				i++;
				idTransactionInsideSignature = content[i];
				i++;
				signedMsg = content[i];
				
				String reconstructedMsg = senderSig + spliter + pointsSig
						+ spliter + actionSig
						+ spliter + idTransaction;
				PublicKey senderPublicKey = this.userManager.getUserByName(senderSig).getUserPublicKey();
				
				int pointsSender;
				int pointsReceiver;
				if (checkSignatures(reconstructedMsg, signedMsg, senderPublicKey)){
					
					if (checkIdTransaction(sender, receiver, idTransaction)){
						this.userManager.commitNewPointsTransactions(sender, receiver, points);
						pointsSender = this.userManager.getUserByName(sender).getUserPoints();
						pointsReceiver = this.userManager.getUserByName(receiver).getUserPoints();
						//this.userManager.getUserByName(sender).addToCommitedList(Integer.valueOf(idTransaction));
						
						answer += sender + spliter + pointsSender + spliter + receiver + spliter + pointsReceiver + spliter; 
						System.out.println("Transacao valida. Foi feito o commit da transacao");
					}
					else{
						pointsSender = this.userManager.getUserByName(sender).getUserPoints();
						pointsReceiver = this.userManager.getUserByName(receiver).getUserPoints();
						answer += sender + spliter + pointsSender + spliter + receiver + spliter + pointsReceiver + spliter; 
						System.out.println("Transacao invalida! A mensagem e repetida");
					}
				}
				else{
					//this.userManager.getUserByName(sender).addToAbortedList(Integer.valueOf(idTransaction));
					pointsSender = this.userManager.getUserByName(sender).getUserPoints();
					pointsReceiver = this.userManager.getUserByName(receiver).getUserPoints();
					answer += sender + spliter + pointsSender + spliter + receiver + spliter + pointsReceiver + spliter; 
					System.out.println("Transacao invalida! A mensagem foi alterada");
				}
			}else{
				if (checkIdTransaction(sender, receiver, idTransaction)){
					this.userManager.commitNewPointsTransactions(sender, receiver, points);
					answer += sender + spliter + receiver + spliter + points + spliter + idTransaction + spliter;
					System.out.println("Transacao valida. Pontos ganhos por riding. Foi feito o commit da transacao.");
				}
				else
					System.out.println("Transacao invalida no riding! A mensagem foi alterada ou é repetida!");
			}
		}
		if (!answer.equals(""))
			answer = answer.substring(0, answer.length() - spliter.length());
		return answer;
	}
	
	private boolean checkSignatures(String msg, String signedMsg, PublicKey publicKey){
		try{
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initVerify(publicKey);
            signature.update(msg.getBytes());
            if(signature.verify(Base64.decodeBase64(signedMsg.getBytes())))
                return true;
            return false;
        }
        catch (NoSuchAlgorithmException | InvalidKeyException |
                SignatureException e){
            System.out.println("Invalid Signature Exception");
            e.printStackTrace();
        }
        return false;
	}
	
	private boolean checkIdTransaction(String sender, String receiver, String idStr){
		int idTransaction = Integer.parseInt(idStr);
		Map<String,List<Integer>> mapTransactions;
		int lastTransaction = 0;
		if (sender.equals("Riding")){
			mapTransactions = this.userManager.getUserByName(receiver).getMapIdTransactions();
			if (mapTransactions.containsKey("Riding"))
				lastTransaction = mapTransactions.get("Riding").get(mapTransactions.get("Riding").size()-1);
			else{
				this.userManager.getUserByName(receiver).addEntryMapTransactions("Riding", 0);
			}
			if (idTransaction > lastTransaction){
				this.userManager.getUserByName(receiver).updateEntryMapTransactions("Riding", idTransaction);
				return true;
			}
		}
		else{
			mapTransactions = this.userManager.getUserByName(sender).getMapIdTransactions();
			if (mapTransactions.containsKey(receiver))
				lastTransaction = mapTransactions.get(receiver).get(mapTransactions.get(receiver).size()-1);
			else{
				this.userManager.getUserByName(sender).addEntryMapTransactions(receiver, 0);
				lastTransaction = 0;
			}
			if (idTransaction > lastTransaction){
				this.userManager.getUserByName(sender).updateEntryMapTransactions(receiver, idTransaction);
				return true;
			}
		}
	
		System.out.println("Mensagem repetida...!");
		return false;
	}
	
	private String savePublicKey(String parameters){
		String [] nameAndKey = parameters.split(";;;",-1);
		String name = nameAndKey[0];
		String publicKeyString = nameAndKey[1];
		this.userManager.addPublicKey(name, publicKeyString);
		return "OK";
	}
}

