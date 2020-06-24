package viz.commonlib.openvidu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.webrtc.AudioSource;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

import java.util.ArrayList;
import java.util.Collection;

public class LocalParticipant extends Participant {

    private Context context;
    private SurfaceViewRenderer localVideoView;
    private SurfaceTextureHelper surfaceTextureHelper;
    private VideoCapturer videoCapturer;

    private Collection<IceCandidate> localIceCandidates;
    private SessionDescription localSessionDescription;
    private TextView participantNameText;
    private View view;
    @SuppressLint("ResourceType")
    @IdRes
    int localViewId = 10001;

    public LocalParticipant(String participantName, Session session, Context context, SurfaceViewRenderer localVideoView) {
        super(participantName, session);
        this.localVideoView = localVideoView;
        this.context = context;
        this.participantName = participantName;
        this.localIceCandidates = new ArrayList<>();
        session.setLocalParticipant(this);
    }

    public LocalParticipant(String participantName, Session session, Activity activity, ConstraintLayout views_contains) {
        super(participantName, session);
        this.context = activity;
        createLocalVideoView(activity, views_contains);
        this.participantName = participantName;
        this.localIceCandidates = new ArrayList<>();
        session.setLocalParticipant(this);
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public TextView getParticipantNameText() {
        return participantNameText;
    }

    public void setParticipantNameText(TextView participantNameText) {
        this.participantNameText = participantNameText;
    }

    public SurfaceViewRenderer getLocalVideoView() {
        return localVideoView;
    }

    public void setLocalVideoView(SurfaceViewRenderer localVideoView) {
        this.localVideoView = localVideoView;
    }

    public void leaveRoom() {
        localVideoView.clearImage();
        localVideoView.release();
    }

    private void createLocalVideoView(Activity activity, ConstraintLayout views_container) {
        activity.runOnUiThread(() -> {
            Handler mainHandler = new Handler(activity.getMainLooper());
            Runnable myRunnable = () -> {
                View rowView = activity.getLayoutInflater().inflate(R.layout.peer_video, null);
                ConstraintSet set = new ConstraintSet();
                set.clone(views_container);
                int rowId = localViewId;
                rowView.setId(rowId);
                views_container.addView(rowView, 0);
                DisplayMetrics dm = activity.getResources().getDisplayMetrics();
                set.constrainWidth(rowId, dm.widthPixels);
                set.constrainHeight(rowId, dm.heightPixels);
                set.connect(
                        rowId, ConstraintSet.TOP, views_container.getId(), ConstraintSet.TOP
                );
                set.connect(
                        rowId, ConstraintSet.END, views_container.getId(), ConstraintSet.END
                );
                set.connect(
                        rowId, ConstraintSet.START, views_container.getId(), ConstraintSet.START
                );
                set.connect(
                        rowId, ConstraintSet.BOTTOM, views_container.getId(), ConstraintSet.BOTTOM
                );
                set.applyTo(views_container);
                SurfaceViewRenderer videoView = (SurfaceViewRenderer) ((ViewGroup) rowView).getChildAt(0);
                this.localVideoView = videoView;
                EglBase rootEglBase = EglBase.create();
                videoView.init(rootEglBase.getEglBaseContext(), null);
                videoView.setMirror(true);
                videoView.setEnableHardwareScaler(true);
                videoView.setZOrderMediaOverlay(true);
                View textView = ((ViewGroup) rowView).getChildAt(1);
                setParticipantNameText((TextView) textView);
                setView(rowView);
                getParticipantNameText().setText(getParticipantName());
                getParticipantNameText().setPadding(20, 3, 20, 3);
            };
            mainHandler.post(myRunnable);
        });
    }

    public void startCamera() {

        final EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();
        PeerConnectionFactory peerConnectionFactory = this.session.getPeerConnectionFactory();

        // create AudioSource
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        this.audioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);
        // create VideoCapturer
        VideoCapturer videoCapturer = createCameraCapturer();
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
        videoCapturer.startCapture(480, 640, 30);

        // create VideoTrack
        this.videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
        // display in localView
        this.videoTrack.addSink(localVideoView);
    }

    private VideoCapturer createCameraCapturer() {
        CameraEnumerator enumerator;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            enumerator = new Camera2Enumerator(this.context);
        } else {
            enumerator = new Camera1Enumerator(false);
        }
        final String[] deviceNames = enumerator.getDeviceNames();

        // Try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    public void storeIceCandidate(IceCandidate iceCandidate) {
        localIceCandidates.add(iceCandidate);
    }

    public Collection<IceCandidate> getLocalIceCandidates() {
        return this.localIceCandidates;
    }

    public void storeLocalSessionDescription(SessionDescription sessionDescription) {
        localSessionDescription = sessionDescription;
    }

    public SessionDescription getLocalSessionDescription() {
        return this.localSessionDescription;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (videoTrack != null) {
            videoTrack.removeSink(localVideoView);
            videoCapturer.dispose();
            videoCapturer = null;
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
    }
}
