package org.azki.smishing;

import android.app.Activity;
import android.app.Application;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class SmishingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        GA.init(this);
    }

    public static void showAd(Activity activity) {
        final InterstitialAd interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId("ca-app-pub-1808062271143441/1059303618");
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }
}
