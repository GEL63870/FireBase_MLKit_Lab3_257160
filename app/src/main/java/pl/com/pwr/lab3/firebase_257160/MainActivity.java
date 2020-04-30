package pl.com.pwr.lab3.firebase_257160;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ImageView mSelectedImage;
    private TextView textview;
    private Button mButton, mButton_text, mButton_camera, mButton_object;

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int PERMISSION_CODE = 1000;
    private static final int REQUEST_IMAGE_CAPTURE = 10;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private Uri image_uri;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectedImage = findViewById(R.id.imageView);
        textview = findViewById(R.id.label);

        mButton_text = findViewById(R.id.text);
        mButton_object = findViewById(R.id.object);

        FirebaseApp.initializeApp(this);
        pickImage();
        pickCameraImage();
        startTextRecognition();
        startImageRecognition();
    }

    // Activate the request of picking an image in the gallery
    public void pickImage() {
        mButton = findViewById(R.id.pick_image);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedImage.setImageResource(0);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);
            }
        });
    }

    // Activate the request of picking an image thanks to CAMERA
    public void pickCameraImage() {
        mButton_camera = findViewById(R.id.camera);
        mButton_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ask to be able to enter the camera system only if you have permission (ask Manifest + Pop-up)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        //permission not enabled, request it
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup to request permissions
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        //permission already granted
                        openCamera();
                    }
                } else {
                    //system os < marshmallow
                    openCamera();
                }
            }
        });
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    //Request handling permission result (Picture Taken with CAMERA)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup was granted
                    openCamera();
                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Pick the image from the gallery and add it on the imageView thanks to Bitmap OR add the Camera Picture thanks to Uri
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Part for getting the result of pick Image from GALLERY
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            mSelectedImage.setImageBitmap(bitmap);
        }

        // Part for getting the result of pick Image from CAMERA
        if (resultCode == RESULT_OK) {
            //Bitmap bitmap = MediaStore.Images.Media.getBitmap();
            mSelectedImage.setImageURI(image_uri); // Set the image capture on our layout
        }
    }

    // Initiate the process of Text Recognition on the Selected Image which is one the ImageView layout
    private void startTextRecognition() {
        mButton_text.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                textview.setText(" ");
                runTextRecognition();
            }
        });
    }

    private void runTextRecognition() {
        mSelectedImage.buildDrawingCache();
        Bitmap bmap = mSelectedImage.getDrawingCache();

        FirebaseVisionImage mFbImage = FirebaseVisionImage.fromBitmap(bmap);
        FirebaseVisionTextRecognizer mFbTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        mButton_text.setEnabled(false);

        mFbTextRecognizer.processImage(mFbImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                textview.setText(" ");
                String text = firebaseVisionText.getText();

                if (text.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No Text Detected", Toast.LENGTH_SHORT).show();
                    mButton_text.setEnabled(true);
                } else {
                    mButton_text.setEnabled(true);
                    textview.setText(text);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mButton_text.setEnabled(true);
                textview.setText(R.string.text_not_foun);
                e.printStackTrace();
            }
        });
    }

    private void startImageRecognition() {
        mButton_object.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runImageRecognition();
            }
        });
    }

    private void runImageRecognition() {
        mSelectedImage.buildDrawingCache();
        Bitmap bmap = mSelectedImage.getDrawingCache();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bmap);

        // Set option to maximise the threshold of your detector
        FirebaseVisionOnDeviceImageLabelerOptions options = new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build();

        // Run Image recognition process to analyse the image
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options);
        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                // Put Text and Confidence in string to pu it in final text view
                for (FirebaseVisionImageLabel label : labels) {
                    String text = label.getText();
                    float confidence = label.getConfidence();

                    String probability = Float.toString(confidence);
                    String text_result = (text + "\n"+ "Probability = " + probability);
                    textview.setText(text_result);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                textview.setText(R.string.label_not_found);
                e.printStackTrace();
            }
        });
    }


}

