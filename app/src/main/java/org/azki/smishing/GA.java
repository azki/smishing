package org.azki.smishing;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class GA {

    private static final String SCREEN_NAME = "&cd";
    private static Tracker tracker;

    public static void init(Application application) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(application);
        tracker = analytics.newTracker("UA-43968401-1");
        tracker.enableAdvertisingIdCollection(true);
    }

    public static void sendView(String name) {
        if (tracker == null) {
            return;
        }

        HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder();
        builder.set(SCREEN_NAME, name);
        tracker.send(builder.build());

        Log.d("taggg", "sendView : " + name);
    }

    public static void sendEvent(String category, String action, String label, Long value) {
        if (tracker == null) {
            return;
        }

        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
        builder.setCategory(category).setAction(action).setLabel(label);
        if (value != null) {
            builder.setValue(value);
        }
        tracker.send(builder.build());

        Log.d("taggg", "sendEvent : " + category + " ( " + action + " )");
    }

    public static void sendTransaction(String id, String name, String sku, double price, long quantity) {
        if (tracker == null) {
            return;
        }

        tracker.send(new HitBuilders.ItemBuilder()
                .setTransactionId(id)
                .setName(name)
                .setSku(sku)
                .setPrice(price)
                .setQuantity(quantity)
                .build());
    }
}
