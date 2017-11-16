package pt.ulisboa.tecnico.cmov.ubibike.domain;

/**
 * Created by Joao on 4/29/2016.
 */
public class Transaction {

    private String senderUsername;
    private String receiverUsername;
    private String points;
    private int id;

    public Transaction(String sender, String receiver, String points, int id){
        this.senderUsername = sender;
        this.receiverUsername = receiver;
        this.points = points;
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }


    public String getReceiverUsername() {
        return receiverUsername;
    }


    public String getPoints() {
        return points;
    }


    public int getId() {return this.id;}

    @Override
    public boolean equals (Object object){
        if (object == null)
            return false;
        final Transaction transaction = (Transaction) object;
        if (getSenderUsername().equals(transaction.getSenderUsername())){
            if (getReceiverUsername().equals(transaction.getReceiverUsername())){
                if (getPoints().equals(transaction.getPoints())){
                    if (getId() == (transaction.getId())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        return getSenderUsername().hashCode() * getReceiverUsername().hashCode() *
                getPoints().hashCode() * getId();
    }
}