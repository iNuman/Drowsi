package i.numan.drowsi.drowsi_detection_package_;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import i.numan.drowsi.R;

public class DrownDetectActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;
    private ToggleButton n_mode;
    private TextView tv;
    private TextView tv1;
    static int count = 0, count1 = 0;
    private LinearLayout layout;
    private MediaPlayer mp;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private int s_time;
    private static final int RC_HANDLE_GMS = 1000;
    private static final int RC_HANDLE_CAMERA_PERM = 1;
    public int flag = 0;

    private String cameraPermission = Manifest.permission.CAMERA;
    private int PERMISSION_CODE = 21;

    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_drown_detect);
        checkPermissions();
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        layout = (LinearLayout) findViewById(R.id.topLayout);
        n_mode = (ToggleButton) findViewById(R.id.toggleButton);
        n_mode.setTextOn("N-Mode ON");
        n_mode.setText("N-Mode");
        n_mode.setTextOff("N-Mode OFF");
        n_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPreview.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Increase Brightness to maximum for higher accuracy", Toast.LENGTH_LONG).show();

                } else {
                    mPreview.setVisibility(View.VISIBLE);
                }
            }
        });
        tv = (TextView) findViewById(R.id.textView3);
        tv1 = (TextView) findViewById(R.id.textView4);
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int c = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (c == 0) {
            Toast.makeText(getApplicationContext(), "Volume is MUTE", Toast.LENGTH_LONG).show();
        }
        s_time = 2000;


        View decorview = getWindow().getDecorView(); //hide navigation bar
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorview.setSystemUiVisibility(uiOptions);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();

    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Toast.makeText(getApplicationContext(), "Dependencies are not yet available. ", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(45.0f)
                .build();

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();

    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        stop_playing();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ALERT")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public static int incrementer() {
        count++;
        return (count);
    }

    public static int incrementer_1() {
        count1++;
        return (count1);
    }

    public static int get_incrementer() {
        return (count);
    }

    public void play_media() {
        stop_playing();
        mp = MediaPlayer.create(this, R.raw.alarm);
        mp.start();
    }

    public void stop_playing() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    public void alert_box() {
        play_media();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                play_media();
                AlertDialog dig;
                dig = new AlertDialog.Builder(DrownDetectActivity.this)
                        .setTitle("Drowsy Alert !!!")
                        .setMessage("Tracker suspects that the driver is experiencing Drowsiness, Touch OK to Stop the Alarm")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                stop_playing();
                                flag = 0;
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                dig.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        stop_playing();
                        flag = 0;
                    }
                });
            }
        });


    }

    // Graphic Face Tracker

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }


        int state_i, state_f = -1;
        long start, end = System.currentTimeMillis();
        long begin, stop;
        int c;

        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            if (flag == 0) {
                eye_tracking(face);
            }
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
            setText(tv1, "Face Missing");

        }

        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }

        private void setText(final TextView text, final String value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text.setText(value);
                }
            });
        }

        private void eye_tracking(Face face) {
            float l = face.getIsLeftEyeOpenProbability();
            float r = face.getIsRightEyeOpenProbability();
            if (l < 0.50 && r < 0.50) {
                state_i = 0;
            } else {
                state_i = 1;
            }
            if (state_i != state_f) {
                start = System.currentTimeMillis();
                if (state_f == 0) {
                    c = incrementer_1();

                }
                end = start;
                stop = System.currentTimeMillis();
            } else if (state_i == 0 && state_f == 0) {
                begin = System.currentTimeMillis();
                if (begin - stop > s_time) {
                    c = incrementer();
                    alert_box();
                    flag = 1;
                }
                begin = stop;
            }
            state_f = state_i;
            status();
        }

        public void status() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int s = get_incrementer();
                    if (s < 5) {
                        setText(tv1, "Active");
                        tv1.setTextColor(Color.GREEN);
                        tv1.setTypeface(Typeface.DEFAULT_BOLD);
                    } else if (s > 4) {
                        setText(tv1, "Sleepy");
                        tv1.setTextColor(Color.YELLOW);
                        tv1.setTypeface(Typeface.DEFAULT_BOLD);
                    } else if (s > 8) {
                        setText(tv1, "Drowsy");
                        tv1.setTextColor(Color.RED);
                        tv1.setTypeface(Typeface.DEFAULT_BOLD);
                    }


                }
            });

        }

    }

    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(DrownDetectActivity.this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(DrownDetectActivity.this, new String[]{cameraPermission}, PERMISSION_CODE);
            return false;
        }
    }

}

