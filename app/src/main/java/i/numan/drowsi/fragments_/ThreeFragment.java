package i.numan.drowsi.fragments_;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import i.numan.drowsi.emergency_.EmergencyActivity;
import i.numan.drowsi.helper_.GPSTracker;
import i.numan.drowsi.main_.MainActivity;
import i.numan.drowsi.R;


public class ThreeFragment extends Fragment {

    double mLatitude;
    double mLongitude;

    GPSTracker mGPSTracker;
    String mAddress, mCity, mState, mCountry, mPostalAddress, mKnownName, nAddress;
    Button mSearchRideButton;
    Button mSearchGoogleButton;
    Button mEmergencyButton;

    View mView;
    TextView mNoteTextView;
    WebView mWebView;

    int a;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_3, container, false);

        mSearchRideButton = (Button) mView.findViewById(R.id.bt_ride);
        mNoteTextView = (TextView) mView.findViewById(R.id.tv_note);
        mSearchGoogleButton = (Button) mView.findViewById(R.id.bt_searchCab);
        mEmergencyButton = (Button) mView.findViewById(R.id.bt_emergencyButton);

        mSearchRideButton.setText("Find Emergency / Help Nearby.");
        a = 0;

        if (!isInternetOn()) {
            mSearchRideButton.setText("Internet connection not found!");
            a = 1;
        }

        mWebView = (WebView) mView.findViewById(R.id.wv_myWebview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.getJavaScriptEnabled();
        mWebView.setWebViewClient(new customWebView());

        mSearchRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmergencyButton.setVisibility(View.GONE);
                if (a == 0) {
                    mSearchRideButton.setText("Finding...");
                    Toast.makeText(getActivity(), "Finding your location ...", Toast.LENGTH_SHORT).show();


                    mGPSTracker = new GPSTracker(getActivity());


                    if (mGPSTracker.canGetLocation()) {
                        mLatitude = 34.0053276;//.getLatitude();
                        mLongitude = 71.4439204;///mGPSTracker.getLongitude();
                    } else {
                        mGPSTracker.showSettingsAlert();
                    }

                    Thread timer = new Thread() {
                        public void run() {
                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
                            try {
//                                    addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
                                addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
                                Log.v("ffnet", "lat lng: " + mLatitude + " " + mLongitude);
                                mAddress = addresses.get(0).getAddressLine(1);
                                nAddress = addresses.get(0).getAddressLine(0);
                                mCity = addresses.get(0).getLocality();
                                mState = addresses.get(0).getAdminArea();
                                mCountry = addresses.get(0).getCountryName();
                                mPostalAddress = addresses.get(0).getPostalCode();
                                mKnownName = addresses.get(0).getFeatureName();
                            } catch (Exception e) {
                                Log.v("ffnet", "Exc: " + e.getMessage());
                            }
                        }
                    };
                    timer.start();

                    try {
                        timer.join();
                        Toast.makeText(getActivity(), "Lat " + mLatitude + " Lon " + mLongitude + "\nAddress: " + mAddress + "\nCity: " + mCity + "\nState: " + mState + "\nCountry: " + mCountry + "\nPostal Address: " + mPostalAddress + " " + mKnownName + "\n" + nAddress, Toast.LENGTH_LONG).show();


                        mWebView.loadUrl("https://www.google.co.in/search?q=" + "Hospitals Nearby " + nAddress + " " + mCity);

                        mWebView.setVisibility(View.VISIBLE);
                        mSearchRideButton.setVisibility(View.INVISIBLE);
                        mSearchGoogleButton.setVisibility(View.INVISIBLE);
                        mNoteTextView.setVisibility(View.INVISIBLE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mEmergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), EmergencyActivity.class);
                startActivity(i);
            }
        });

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getView() == null) {
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    startActivity(new Intent(getContext(), MainActivity.class));
                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });
    }

    public final boolean isInternetOn() {
        ConnectivityManager connec = (ConnectivityManager) getActivity().getSystemService(getActivity().getBaseContext().CONNECTIVITY_SERVICE);
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            return true;
        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    private class customWebView extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

    }


}


