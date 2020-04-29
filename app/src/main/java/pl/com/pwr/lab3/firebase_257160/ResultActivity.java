package pl.com.pwr.lab3.firebase_257160;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView label;
    private String resultText ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label = findViewById(R.id.label);
        resultText = getIntent().getStringExtra(FinalTextRecognition.FINAL_TEXT);

        label.setText(resultText);

            }
}
