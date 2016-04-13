package cheddar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.cheddar.R;
import com.hound.android.fd.Houndify;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cheddar.util.CamManager;
import cheddar.util.HoundifyManager;
import cheddar.util.PermissionsManager;
import cheddar.util.StatefulRequestInfoFactory;
import rx.Observable;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements HoundifyManager.MatchedResponseListener{

    private static final int PERMISSION_REQUESTCODE = 1;
    @Bind(R.id.texture) AutoFitTextureView cameraTextureView;
    @Bind(R.id.takePicText) TextView takePicText;
    @Bind(R.id.flipCamText) TextView flipCamText;

    public static final CamManager.WhichCam DEFAULT_CAM = CamManager.WhichCam.FRONT_CAM;
    public static final boolean IS_DEBUG = false;

    private HoundifyManager houndifyManager;
    private CamManager camManager;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        permissionsManager = new PermissionsManager.Builder(this)
                .add(Manifest.permission.RECORD_AUDIO)
                .add(Manifest.permission.ACCESS_COARSE_LOCATION)
                .add(Manifest.permission.ACCESS_FINE_LOCATION)
                .add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .add(Manifest.permission.CAMERA)
                .build();

        houndifyManager = new HoundifyManager(this, this);
        camManager = new CamManager(this, cameraTextureView);

        //show debug options on screen, allows you to flip and take pictures without voice commands
        if (IS_DEBUG) {
            takePicText.setVisibility(View.VISIBLE);
            flipCamText.setVisibility(View.VISIBLE);
            takePicText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMatchedResponse(StatefulRequestInfoFactory.KEY_SELFIE);
                }
            });
            flipCamText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMatchedResponse(StatefulRequestInfoFactory.KEY_FLIP_CAM);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if we don't, we must still be listening for "ok houndifyManager" so teardown the phrase spotter
        houndifyManager.stopTextToSpeechManager();
    }

    @Override
    protected void onPause() {
        camManager.closeCamera();
        camManager.stopBackgroundThread();
        super.onPause();
        // if we don't, we must still be listening for "ok houndifyManager" so teardown the phrase spotter
        houndifyManager.stopPhraseSpotting();
    }

    @Override
    public void onMatchedResponse(String key) {
        if (key.equals(StatefulRequestInfoFactory.KEY_SELFIE) ||
                key.equals(StatefulRequestInfoFactory.KEY_TIMER_5) ||
                key.equals(StatefulRequestInfoFactory.KEY_TIMER_10) ||
                key.equals(StatefulRequestInfoFactory.KEY_TIMER_15)) {

            int timerSeconds;
            if (key.equals(StatefulRequestInfoFactory.KEY_SELFIE)) {
                timerSeconds = 1;
            } else if (key.equals(StatefulRequestInfoFactory.KEY_TIMER_5)) {
                timerSeconds = 5;
            } else if (key.equals(StatefulRequestInfoFactory.KEY_TIMER_10)) {
                timerSeconds = 10;
            } else {
                timerSeconds = 15;
            }

            Observable.timer(timerSeconds, TimeUnit.SECONDS)
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            MainActivity.this.camManager.takePicture();
                        }
                    });

        } else if (key.equals(StatefulRequestInfoFactory.KEY_FLIP_CAM)) {
            camManager.flipCamera();
        }
    }

    /**z
     * The HoundifyVoiceSearchActivity returns its result back to the calling Activity
     * using the Android's onActivityResult() mechanism.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Houndify.REQUEST_CODE) {
            houndifyManager.activityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        startCamera();
    }

    private void startCamera() {
        if (permissionsManager.allPermissionsGranted()) {
            houndifyManager.startPhraseSpotting();
            camManager.startBackgroundThread();
            if (camManager.currentCam == null) {
                camManager.currentCam = DEFAULT_CAM;
            }

            if (cameraTextureView.isAvailable()) {
                camManager.openCamera(camManager.currentCam, cameraTextureView.getWidth(), cameraTextureView.getHeight());
            } else {
                cameraTextureView.setSurfaceTextureListener(camManager.getSurfaceTextureListener());
            }
        }
        else {
            showNeedsPermission();
        }
    }


    private void showNeedsPermission() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.permissions_title));
        builder.setMessage(getString(R.string.permissions_message));
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                permissionsManager.ask(PERMISSION_REQUESTCODE);
            }
        });
        builder.show();
    }
}
