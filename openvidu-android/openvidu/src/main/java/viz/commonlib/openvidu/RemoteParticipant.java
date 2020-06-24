package viz.commonlib.openvidu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;

public class RemoteParticipant extends Participant {

    private View view;
    private SurfaceViewRenderer videoView;
    private TextView participantNameText;
    @SuppressLint("ResourceType")
    @IdRes
    int remoteViewId = 10000;

    public RemoteParticipant(String connectionId, String participantName, Session session) {
        super(connectionId, participantName, session);
        this.session.addRemoteParticipant(this);
    }

    public View getView() {
        return this.view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public SurfaceViewRenderer getVideoView() {
        return this.videoView;
    }

    public void setVideoView(SurfaceViewRenderer videoView) {
        this.videoView = videoView;
    }

    public TextView getParticipantNameText() {
        return this.participantNameText;
    }

    public void setParticipantNameText(TextView participantNameText) {
        this.participantNameText = participantNameText;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void createRemoteParticipantVideo(Activity activity, ConstraintLayout views_container) {
        activity.runOnUiThread(() -> {
            Handler mainHandler = new Handler(activity.getMainLooper());
            Runnable myRunnable = () -> {
                View rowView = activity.getLayoutInflater().inflate(R.layout.peer_video, null);
                ConstraintSet set = new ConstraintSet();
                set.clone(views_container);
                int rowId = remoteViewId;
                rowView.setId(rowId);
                views_container.addView(rowView);
                DisplayMetrics dm = activity.getResources().getDisplayMetrics();
                set.constrainWidth(rowId, (int) (dm.widthPixels * 0.3));
                set.constrainHeight(rowId, (int) (dm.widthPixels * 0.4));
                set.connect(
                        rowId, ConstraintSet.TOP, views_container.getId(), ConstraintSet.TOP, 10
                );
                set.connect(
                        rowId, ConstraintSet.END, views_container.getId(), ConstraintSet.END, 10
                );
                set.applyTo(views_container);
                SurfaceViewRenderer videoView = (SurfaceViewRenderer) ((ViewGroup) rowView).getChildAt(0);
                setVideoView(videoView);
                videoView.setMirror(false);
                EglBase rootEglBase = EglBase.create();
                videoView.init(rootEglBase.getEglBaseContext(), null);
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
}
