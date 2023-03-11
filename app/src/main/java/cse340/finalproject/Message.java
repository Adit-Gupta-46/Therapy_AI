package cse340.finalproject;

public class Message {

    public static final int SEND_BY_ME = 1;
    public static final int SEND_BY_BOT = 2;

    private String message;
    private int sender;

    public Message(String message, int sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }
}
