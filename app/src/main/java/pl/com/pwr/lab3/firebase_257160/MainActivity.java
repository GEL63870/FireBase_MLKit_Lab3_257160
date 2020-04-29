package pl.com.pwr.lab3.firebase_257160;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private ImageView mSelectedImage;
    private TextView textview;
    private Button mButton, mButton_text, mButton_camera, mButton_object;

    private static final int GALLERY_REQUEST_CODE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Bitmap mBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectedImage = findViewById(R.id.imageView);
        textview = findViewById(R.id.label);

        mButton_text = findViewById(R.id.text);
        mButton_camera = findViewById(R.id.camera);
        mButton_object = findViewById(R.id.object);

        FirebaseApp.initializeApp(this);
        pickImage();


        mButton_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                textview.setText("");
            }
        });

        mButton_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromImage();

            }
        });
    }

    // Activate the request of picking an image in the gallery
    public void pickImage() {
        mButton = findViewById(R.id.pick_image);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);
            }
        });
    }

    // Able the user to open the phone camera and take a picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // Pick the image from the gallery and add it on the imageView thanks to Bitmap
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mSelectedImage.setImageBitmap(bitmap);
                //Bundle extras = data.getExtras();
                //Bitmap bitmap = (Bitmap) extras.get("data");

                recognizeMyText(mBitmap);
        }
    }

    private void recognizeMyText (Bitmap bitmap) {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer mFirebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        mFirebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                String text = firebaseVisionText.getText();

                if (text.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No Text Detected", Toast.LENGTH_SHORT).show();
                } else {
                    // Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    // intent.putExtra(FinalTextRecognition.FINAL_TEXT, label);
                    // startActivity(intent);

                    textview.setText(text);
                }
            }
        });
    }

    private void detectTextFromImage() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(mBitmap);
        FirebaseVisionTextRecognizer mFirebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        mFirebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        if (blockList.size() == 0) {
            Toast.makeText(this, "No text found in image", Toast.LENGTH_SHORT).show();
        } else {
            for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                AtomicReference<List<FirebaseVisionText.TextBlock>> text = new AtomicReference<>(firebaseVisionText.getTextBlocks());
                textview.setText((CharSequence) text);
            }

        }
    }


    // Partie créer en regardans ML Kit de Github, + class Utils

    @Override
    protected void onResume() {
        super.onResume();
        if (!Utils.allPermissionsGranted(this)) {
            Utils.requestRuntimePermissions(this);
        }
    }
}



