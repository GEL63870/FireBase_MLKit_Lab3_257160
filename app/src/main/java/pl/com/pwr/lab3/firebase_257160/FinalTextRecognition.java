package pl.com.pwr.lab3.firebase_257160;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class FinalTextRecognition extends Application {

    public static final String FINAL_TEXT = "Final_Text";
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}