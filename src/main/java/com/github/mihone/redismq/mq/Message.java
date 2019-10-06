package com.github.mihone.redismq.mq;

/**
 * The message bean in the mq.
 * <p>Normally,<code>messageId</code> is the timestamp when the producer send this message
 * {@code className} is the real type of the body,And,{@code body}is the data of the message
 * <p>Apparently,{@code body.getClass().getName().equals(className)} is always {@code true}.
 * However,{@code false} only if can not resolve the class from the className ,and the body will be
 * {@code byte[] }
 *
 * @author mihone
 * @since 2019/10/4
 */
public class Message {
    private String messageId;
    private String className;
    private long timeStamp;

    public long getTimeStamp() {
        return timeStamp;
    }

    private Object body;

    public Message(String messageId, String className, long timeStamp, Object body) {
        this.messageId = messageId;
        this.className = className;
        this.timeStamp = timeStamp;
        this.body = body;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getClassName() {
        return className;
    }


    public Object getBody() {
        return body;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Message{").append("messageId='").append(messageId).append("'").append(",className='").append(className).append("'").append(",timeStamp='").append(timeStamp).append("'").append(", body=").append(body).append("}").toString();
    }
}
