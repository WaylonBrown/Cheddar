package cheddar;

import android.app.Application;

import com.hound.android.fd.Houndify;

import cheddar.util.StatefulRequestInfoFactory;

/**
 * Created by waylon.brown on 4/6/16.
 */
public class App extends Application {
    private static final String CLIENT_ID = "A12MtANVQwzQ5PV92Hc7zA==";
    private static final String CLIENT_KEY = "332Jfz-JPLPAGOau1C0aM2PeSDHxKJC2TqX5SYZPIWIZDq88ius5tT50JQS7N5_vi4OnOhKQGQVu8PkhibHhGA==";

    @Override
    public void onCreate() {
        super.onCreate();
        
        Houndify.get(this).setClientId(CLIENT_ID);
        Houndify.get(this).setClientKey(CLIENT_KEY);
        Houndify.get(this).setRequestInfoFactory(StatefulRequestInfoFactory.get(this));
    }
}
