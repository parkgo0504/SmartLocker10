package com.example.smartlocker10;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;



import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    Button upload,choose;
    TextView alert;
    ArrayList<Uri> ImageList = new ArrayList<>();
    private Uri ImageUri;
    private ProgressDialog progressDialog;
    private int upload_count = 0;

    private ImageView imageView;
    private ProgressBar progressBar;
    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference("Image");
    private final StorageReference reference = FirebaseStorage.getInstance().getReference();

    private Uri imageUri;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef = mRootRef.child("text");


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alert = findViewById(R.id.alert);
        upload = findViewById(R.id.upload_image);
        choose = findViewById(R.id.chooser);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Image Uploading... please wait..");
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                startActivityForResult(intent,PICK_IMAGE);
            }
        });

        //???????????????????????? ????????? ????????????
        Button Button_load = (Button) findViewById(R.id.image_load);

        Button_load.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) { //????????? ???????????? ????????? ??????
                Intent intent = new Intent(getApplicationContext(), Activity.class);
                startActivity(intent);
            }
        });




        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.show();
                alert.setText("IF loading takes too long please Press the button again");

                StorageReference ImageFolder = FirebaseStorage.getInstance().getReference().child("ImageFolder");

                for(upload_count = 0; upload_count < ImageList.size(); upload_count++){

                    Uri IndividualImage = ImageList.get(upload_count);
                    StorageReference ImageName = ImageFolder.child("Image"+IndividualImage.getLastPathSegment());

                    ImageName.putFile(IndividualImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            ImageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = String.valueOf(uri);

                                    StoreLink(url);
                                }
                            });
                        }
                    });




                }

            }
        });


        //???????????? ????????? ??????
        Button uploadBtn = findViewById(R.id.upload_btn);
        progressBar = findViewById(R.id.progress_View);
        imageView = findViewById(R.id.image_view);

        //?????????????????? ?????????
        progressBar.setVisibility(View.INVISIBLE);
        //????????? ?????? ?????????
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/");
                activityResult.launch(galleryIntent);
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //????????? ???????????? ?????????
                if (imageUri != null) {
                    uploadToFirebase(imageUri);
                } else {
                    Toast.makeText(MainActivity.this, "????????? ??????????????????", Toast.LENGTH_SHORT).show();

                }
            }
        });

    } // onCreate
    private void StoreLink(String url){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("UserOne");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("Imagelink",url);

        databaseReference.push().setValue(hashMap);

        progressDialog.dismiss();
        alert.setText("Image Uploaded Successfully");
        upload.setVisibility(View.GONE);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE){
            if(resultCode == RESULT_OK){
                if(data.getClipData() != null){

                    int countClipData = data.getClipData().getItemCount();

                    int currentImageSelect = 0;
                    while(currentImageSelect < countClipData){

                        ImageUri = data.getClipData().getItemAt(currentImageSelect).getUri();
                        ImageList.add(ImageUri);
                        currentImageSelect = currentImageSelect +1;
                    }

                    alert.setVisibility(View.VISIBLE);
                    alert.setText("YOU HAVE SELECTED"+ImageList.size()+"Images");
                    choose.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(this, "Please choose images", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // ?????? ????????????
    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();

                        imageView.setImageURI(imageUri);
                    }
                }
            });

    private void uploadToFirebase(Uri uri){

        StorageReference fileRef = reference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener((new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // ????????? ????????? ??????
                        Model model = new Model(uri.toString());

                        //?????? ????????? ??????
                        String modelId = root.push().getKey();

                        // ????????? ??????
                        root.child(modelId).setValue(model);

                        // ?????????????????? ??????
                        progressBar.setVisibility(View.INVISIBLE);

                        Toast.makeText(MainActivity.this, "????????? ??????", Toast.LENGTH_SHORT).show();

                        imageView.setImageResource(R.drawable.ic_add_photo);

                    }
                }));
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                // ?????????????????? ????????????
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                // ?????????????????? ??????
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this,"????????? ??????", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //???????????? ????????????
    private String getFileExtension(Uri uri){

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(cr.getType(uri));
    }
}