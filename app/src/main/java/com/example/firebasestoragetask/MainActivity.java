package com.example.firebasestoragetask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PICK_IMAGE_REQUEST = 234;
    private ImageView imageView;
    private Button buttonShowAll, buttonUpload,buttonChoose;
    private Uri filePath;
    private StorageReference storageReference;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Image");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageReference = FirebaseStorage.getInstance().getReference();
        //root= FirebaseDatabase.getInstance().getReference("Image");

        imageView = (ImageView) findViewById(R.id.imageView);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        buttonChoose=(Button) findViewById(R.id.buttonChoose);
        buttonShowAll = (Button) findViewById(R.id.buttonShow);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);

        buttonShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ShowActivity.class));
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });
    }

    private void chooseImage(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select an Image"),PICK_IMAGE_REQUEST);
    }

    private void uploadFile(){
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference profRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(filePath));

            profRef.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Model model = new Model(uri.toString());
                            String modelId = root.push().getKey();
                            model.setStorageKey(profRef.getName());
                            root.child(modelId).setValue(model);

                            Toast.makeText(getApplicationContext(), "File Uplaoded Successfully", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            imageView.setImageResource(R.drawable.ic_launcher_foreground);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //progressBar.setVisibility(View.INVISIBLE);
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Uploading Failed!", Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress=(100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    progressDialog.setMessage(((int) progress)+"% Uploaded...");
                }
            });
        }else {
        }

    }

    private String getFileExtension(Uri mUri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(mUri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data !=null && data.getData() != null) {
            filePath = data.getData();
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), filePath);
                Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onClick(View view) {
        if(view==buttonChoose){
            //Choose image
            chooseImage();
        }
        else if(view==buttonUpload){
            //Upload image
            uploadFile();
        }
    }


}