package i.numan.drowsi.fragments_;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;

import i.numan.drowsi.drowsi_detection_package_.DrownDetectActivity;
import i.numan.drowsi.R;
import i.numan.drowsi.helper_.SessionManager;


public class OneFragment extends Fragment {

    View mView;
    String name = "Numan";
    String number = "03127746663";
    Button mSMSButton;
    Button mCallButton;
    Button mEmergencyButton;
    SessionManager sessionManager;
    private String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    private String callPermission = Manifest.permission.CALL_PHONE;
    private String smsPermission = Manifest.permission.SEND_SMS;
    private int PERMISSION_CODE = 21;

    LocationManager locationManager;
    Location location;
    double latitude;
    double longitude;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_1, container, false);

        sessionManager = new SessionManager(getContext());

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        name = user.get(SessionManager.KEY_NAME);
        number = user.get(SessionManager.KEY_EMAIL);
        Log.d("ffnet", "onCreate: Name " + name + "\n" + " Number " + number);


        mSMSButton = (Button) mView.findViewById(R.id.bt_smsHelp);
        mCallButton = (Button) mView.findViewById(R.id.bt_callHelp);
        mEmergencyButton = (Button) mView.findViewById(R.id.bt_emergencyButton_one);

        mSMSButton.setText("Send Help SMS to : " + number);
        mCallButton.setText("Make a Call to : " + number);
//        mEmergencyButton.setText("Emergency Numbers");

        try {

            if (checkPermissions()) {
                locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);


                if (location != null) {
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.d("ffnet", "onCreateView: lat " + latitude + " lon " + longitude);
                        Toast.makeText(getContext(), "lat " + location.getLatitude() + "\n" + " lng " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.d("ffnet", "onCreateView: Error :" + e.getMessage());
        }


        mSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions()) {
                    try {


                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        alertDialog.setTitle("Send Help SMS?");
                        alertDialog.setMessage("\nSend SMS to " + number + " ?\n\n\nContent of SMS : \n\n" + "Contact " + name + ", he/she is not in a condition to drive & needs your help to get back home from location : \'Google Maps URL of your location\' \n\n");
                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SmsManager smsManager = SmsManager.getDefault();
                                String smsBody;
                                smsBody = "http://maps.google.com/?q=" + latitude + "," + longitude;
                                smsManager.sendTextMessage(number, null, "Contact " + name + ", he/she is not in a condition to drive & needs your help to get back home from location : " + smsBody, null, null);
                            }
                        });
                        alertDialog.show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        });


        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions()) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle("Are you sure ?");
                    alertDialog.setMessage("Make a call to " + number + " ?");
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //
                        }
                    });

                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + number));
                            startActivity(callIntent);
                        }
                    });
                    alertDialog.show();
                }
            }
        });
//
        mEmergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), DrownDetectActivity.class);
                startActivity(i);
            }
        });
//
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getView() == null) {
            getActivity().finish();
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });
    }
    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(getContext(), locationPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{locationPermission, callPermission, smsPermission}, PERMISSION_CODE);
            return false;
        }
    }

}

