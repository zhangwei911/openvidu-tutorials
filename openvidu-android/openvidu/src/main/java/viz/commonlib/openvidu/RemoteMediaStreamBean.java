package viz.commonlib.openvidu;

import org.webrtc.MediaStream;

/**
 * @author zhangwei
 * @title: RemoteMediaStreamBean
 * @projectName OpenVidu Android
 * @description:
 * @date 2020/6/16 11:19
 */
public class RemoteMediaStreamBean {
    private MediaStream mediaStream;
    private RemoteParticipant remoteParticipant;

    public RemoteMediaStreamBean(MediaStream mediaStream, RemoteParticipant remoteParticipant) {
        this.mediaStream = mediaStream;
        this.remoteParticipant = remoteParticipant;
    }

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }

    public RemoteParticipant getRemoteParticipant() {
        return remoteParticipant;
    }

    public void setRemoteParticipant(RemoteParticipant remoteParticipant) {
        this.remoteParticipant = remoteParticipant;
    }
}
