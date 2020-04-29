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

import com.google.firebase.FirebaseApp;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ImageView mSelectedImage;
    private TextView textview;
    private Button mButton, mButton_text, mButton_camera, mButton_object;

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int PERMISSION_CODE = 1000;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private Uri image_uri;

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

        // Preparing the CAMERA Button to be click
        mButton_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                textview.setText("");

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

        mButton_text.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                //detectTextFromImage();
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

    // Pick the image from the gallery and add it on the imageView thanks to Bitmap OR add the Camera Picture thanks to Uri
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

            //recognizeMyText(mBitmap);
        }
        // Need to bee called when image was captured from camera
        if (resultCode == RESULT_OK) {
            mSelectedImage.setImageURI(image_uri); // Set the image capture on our layout
        }
    }


    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    //handling permission result
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


 //   private void recognizeMyText(Bitmap bitmap) {
//        FirebaseVisionTextRecognizer mFirebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
  //      mFirebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
    //        @Override
      //      public void onSuccess(FirebaseVisionText firebaseVisionText) {
        //        String text = firebaseVisionText.getText();
//
  //              if (text.isEmpty()) {
    ///                Toast.makeText(MainActivity.this, "No Text Detected", Toast.LENGTH_SHORT).show();
       //         } else {
         //           // Intent intent = new Intent(MainActivity.this, ResultActivity.class);
           //         // intent.putExtra(FinalTextRecognition.FINAL_TEXT, label);
             //       // startActivity(intent);
//
  //                  textview.setText(text);
    //            }
      //      }
        //});
    //}

  //  private void detectTextFromImage() {
     //   FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(mBitmap);
    //    FirebaseVisionTextRecognizer mFirebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
     //   mFirebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
     //       @Override
      //      public void onSuccess(FirebaseVisionText firebaseVisionText) {
       //         displayTextFromImage(firebaseVisionText);
      //      }
     //   }).addOnFailureListener(new OnFailureListener() {
      ////      @Override
      //      public void onFailure(@NonNull Exception e) {
      //          Toast.makeText(MainActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
      //      }
     //   });

  //  }

    //private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
       // List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
     //   if (blockList.size() == 0) {
       //     Toast.makeText(this, "No text found in image", Toast.LENGTH_SHORT).show();
       // } else {
         //   for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
         //       AtomicReference<List<FirebaseVisionText.TextBlock>> text = new AtomicReference<>(firebaseVisionText.getTextBlocks());
         //       textview.setText((CharSequence) text);
         //   }

       // }
  //  }
}