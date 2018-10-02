package com.example.gaayathri.bookoman;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.bumptech.glide.Glide;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SellActivity extends AppCompatActivity {

    public static final int MY_REQUEST_CAMERA   = 10;
    public static final int MY_REQUEST_WRITE_CAMERA   = 11;
    public static final int CAPTURE_CAMERA   = 12;

    public static final int MY_REQUEST_READ_GALLERY   = 13;
    public static final int MY_REQUEST_WRITE_GALLERY   = 14;
    public static final int MY_REQUEST_GALLERY   = 15;

    private SimpleDraweeView swView;

    public File filen = null;

    private FirebaseAuth firebaseAuth;
    private EditText title;
    private EditText author;
    private EditText degree;
    private EditText specialization;
    private EditText mrp;
    private EditText price;
    private EditText sellerMsg;
    private Button placead;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private ProgressDialog progressDialog;
    Toolbar toolbarSell;
    Dialog picDialog;
    Dialog myDialog;
    private StorageReference storageReference;
    Uri photocUri;
    String downloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_sell);

        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference();

        final Client client = new Client("H9P3XBA9GD", "3058fee363b2c4b8afe53e9d9eab642f");
        Index index = client.getIndex("books");

        picDialog = new Dialog(this);
        picDialog.setContentView(R.layout.selectphotodialog);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        swView = findViewById(R.id.img1);

        swView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                picDialog.show();

            }
        });

        Button btnCamera = picDialog.findViewById(R.id.btnCamera);
        Button btnGallery = picDialog.findViewById(R.id.btnGallery);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCW();
                picDialog.dismiss();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionRG();
                picDialog.dismiss();
            }
        });


        myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.adplaceddialog);

        Button btnLHome = myDialog.findViewById(R.id.btnHome);
        Button btnLAnotherAd = myDialog.findViewById(R.id.btnAnotherAd);

        btnLHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SellActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLAnotherAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SellActivity.this, SellActivity.class);
                startActivity(intent);
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);

        title = findViewById(R.id.etMaterialTitle);
        author = findViewById(R.id.etAuthor);
        degree = findViewById(R.id.etDegree);
        specialization = findViewById(R.id.etSpecialization);
        mrp = findViewById(R.id.etMrp);
        price = findViewById(R.id.etExpectedPrice);
        placead = findViewById(R.id.btnPlaceAd);
        sellerMsg = findViewById(R.id.etNoteToBuyer);

        Long tsLong = System.currentTimeMillis()/1000;
        final String timeStamp = tsLong.toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        final String name = user.getDisplayName();

        final String entryName = timeStamp + email;


        DocumentReference docRef = firestore.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        final String llocation = document.getString("city");


                        placead.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {

                                progressDialog.setMessage("Placing your ad...");
                                progressDialog.show();

                                swView.setDrawingCacheEnabled(true);
                                swView.buildDrawingCache();
                                Bitmap bitmap1 = swView.getDrawingCache();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data1 = baos.toByteArray();

                                StorageReference imageReference = storageReference.child(firebaseAuth.getCurrentUser().getEmail()).child("bookimages").child(entryName);
                                UploadTask uploadTask = imageReference.putBytes(data1);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SellActivity.this, "Failed to upload image and ad!!!", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        downloadUri = taskSnapshot.getDownloadUrl().toString();

                                        final String uid = firebaseAuth.getCurrentUser().getUid();

                                        final Map<String, String> bookMap = new HashMap<>();

                                        final String ltitle = title.getText().toString();
                                        final String lauthor = author.getText().toString();
                                        final String ldegree = degree.getText().toString();
                                        final String lspecialization = specialization.getText().toString();
                                        final String lmrp = mrp.getText().toString();
                                        final String lprice = price.getText().toString();
                                        final String entryName = timeStamp + email;
                                        final String lsellerMsg = sellerMsg.getText().toString();

                                        bookMap.put("title", ltitle);
                                        bookMap.put("author", lauthor);
                                        bookMap.put("degree", ldegree);
                                        bookMap.put("specialization", lspecialization);
                                        bookMap.put("mrp", "₹ " + lmrp);
                                        bookMap.put("price", "₹ " + lprice);
                                        bookMap.put("user", name);
                                        bookMap.put("location", llocation);
                                        bookMap.put("timestamp", timeStamp);
                                        bookMap.put("entryName", entryName);
                                        bookMap.put("sellerMsg", lsellerMsg);
                                        bookMap.put("downloadUri", downloadUri);
                                        bookMap.put("uid", uid);
                                        bookMap.put("email", email);

                                        firestore.collection("books").document(timeStamp + email).set(bookMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firestore.collection("users").document(email).collection("books").document(ltitle).set(bookMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Index index = client.initIndex("contacts");
                                                        try {
                                                            index.addObjectAsync(new JSONObject()

                                                                    .put("title", ltitle)
                                                                    .put("author", lauthor)
                                                                    .put("degree", ldegree)
                                                                    .put("specialization", lspecialization)
                                                                    .put("mrp", "₹ " + lmrp)
                                                                    .put("price", "₹ " + lprice)
                                                                    .put("user", name)
                                                                    .put("location", llocation)
                                                                    .put("timestamp", timeStamp)
                                                                    .put("entryName", entryName)
                                                                    .put("sellerMsg", lsellerMsg)
                                                                    .put("downloadUri", downloadUri)
                                                                    .put("uid", uid)
                                                                    .put("objectID", entryName)
                                                                    .put("email", email), null);

                                                        } catch (JSONException e) {

                                                            e.printStackTrace();

                                                        }

                                                        progressDialog.dismiss();

                                                        myDialog.show();

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(SellActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });

                            }
                        });

                    } else {
                        Toast.makeText(SellActivity.this, "location unavailable", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                } else {
                    Toast.makeText(SellActivity.this, "location unavailable", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK){
            Log.e("msg", "photo not get");
            return;
        }

        switch (requestCode) {

            case CAPTURE_CAMERA:

                swView.setImageURI(Uri.parse("file:///" + filen));
                break;


            case MY_REQUEST_GALLERY:
                try {

                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    filen = getFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(filen);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    fileOutputStream.close();
                    inputStream.close();
                    swView.setImageURI(Uri.parse("file:///" + filen));//fresco library

                } catch (Exception e) {

                    Log.e("", "Error while creating temp file", e);
                }
                break;

        }
    }


    private void checkPermissionCA(){
        int permissionCheck = ContextCompat.checkSelfPermission(SellActivity.this, android.Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    SellActivity.this, new String[]{android.Manifest.permission.CAMERA}, MY_REQUEST_CAMERA);
        } else {
            catchPhoto();
        }
    }
    private void checkPermissionCW(){
        int permissionCheck = ContextCompat.checkSelfPermission(SellActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    SellActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_WRITE_CAMERA);
        } else {
            checkPermissionCA();
        }
    }
    private void catchPhoto() {
        filen = getFile();
        if(filen!=null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                Uri photocUri = Uri.fromFile(filen);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photocUri);
                startActivityForResult(intent, CAPTURE_CAMERA);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(SellActivity.this, "please check your sdcard status", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SellActivity.this, "please check your sdcard status", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissionRG(){
        int permissionCheck = ContextCompat.checkSelfPermission(SellActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    SellActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_READ_GALLERY);
        } else {
            checkPermissionWG();
        }
    }
    private void checkPermissionWG(){
        int permissionCheck = ContextCompat.checkSelfPermission(SellActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // int permissionCheck2 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    SellActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_WRITE_GALLERY);
        } else {
            getPhotos();
        }
    }
    private void getPhotos() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, MY_REQUEST_GALLERY);
    }

    public File getFile(){

        File fileDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        if (!fileDir.exists()){
            if (!fileDir.mkdirs()){
                return null;
            }
        }

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        File mediaFile = new File(fileDir.getPath() + File.separator + ts + ".jpg");
        return mediaFile;
    }

    public void onRequestPermissionsResult (int requestCode, String[] permissions,  int[] grantResults)
    {

        switch (requestCode) {
            case MY_REQUEST_CAMERA:
                catchPhoto();
                break;
            case MY_REQUEST_WRITE_CAMERA:
                checkPermissionCA();
                break;
            case MY_REQUEST_READ_GALLERY:
                checkPermissionWG();
                break;
            case MY_REQUEST_WRITE_GALLERY:
                getPhotos();
                break;
        }
    }

}
