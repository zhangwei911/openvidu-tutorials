package viz.commonlib.openvidu;

/**
 * @author zhangwei
 * @title: Message
 * @projectName OpenVidu Android
 * @description:
 * @date 2020/6/23 10:42
 */
public class WebSocketMessage {
    private String message;
    private String nickname;
    private String type;
    private String from;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", nickname='" + nickname + '\'' +
                ", type='" + type + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}
