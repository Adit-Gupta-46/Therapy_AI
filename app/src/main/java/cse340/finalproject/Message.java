package cse340.finalproject;

/**
 This class represents a message that can be sent by either the user or the bot.
 It contains the message text and the sender ID indicating whether the message was sent by the user or the bot.
 */
public class Message {

    /**
     * Constant value representing a message sent by the user.
     */
    public static final int SEND_BY_ME = 1;

    /**
     * Constant value representing a message sent by the bot.
     */
    public static final int SEND_BY_BOT = 2;

    private String message; // The message text
    private int sender;     // The sender ID

    /**
     * Creates a new Message object with the specified message text and sender ID.
     * @param message The message text.
     * @param sender The sender ID indicating whether the message was sent by the user or the bot.
     */
    public Message(String message, int sender) {
        this.message = message;
        this.sender = sender;
    }

    /**
     * Gets the message text.
     * @return The message text.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message text.
     * @param message The new message text.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the sender ID.
     * @return The sender ID indicating whether the message was sent by the user or the bot.
     */
    public int getSender() {
        return sender;
    }

    /**
     * Sets the sender ID.
     * @param sender The new sender ID indicating whether the message was sent by the user or the bot.
     */
    public void setSender(int sender) {
        this.sender = sender;
    }
}
