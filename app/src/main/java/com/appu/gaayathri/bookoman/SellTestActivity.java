package com.appu.gaayathri.bookoman;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.bluehomestudio.steps.CircleImageSteps;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SellTestActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    CircleImageSteps circleImageSteps;

    Button prvBtn, nextBtn, prv1Btn, next1Btn, finishbtn;
    LinearLayout firstLayout, bookDetails, sampleAd;
    private SimpleDraweeView swView, sampleView, sampleApprovalView;

    public static final int MY_REQUEST_CAMERA   = 10;
    public static final int MY_REQUEST_WRITE_CAMERA   = 11;
    public static final int CAPTURE_CAMERA   = 12;

    public static final int MY_REQUEST_READ_GALLERY   = 13;
    public static final int MY_REQUEST_WRITE_GALLERY   = 14;
    public static final int MY_REQUEST_GALLERY   = 15;

    public File filen = null;

    private FirebaseAuth firebaseAuth;
    private EditText title, author, mrp, price, sellerMsg;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private ProgressDialog progressDialog;
    Dialog picDialog, myDialog;
    private StorageReference storageReference;
    String downloadUri, oldEntryName;

    ProgressBar progressBar;

    SharedPreferences sharedPreferences;

    public static final String mypreference = "mypref";
    public static final String city = "cityKey";
    public static final String name = "nameKey";

    Spinner spinner, spinner1;

    private ProgressDialog progressDialog1;

    private PrefManager prefManager;

    TextView sampleTitle, sampleAuthor, sampleDegree, sampleSpecial, sampleMrp, samplePrice, sampleSellerMsg, adLookLike, imageLookLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_sell_test);

        circleImageSteps = findViewById(R.id.cis_steps);
        circleImageSteps.addSteps(R.drawable.ic_camera_enhance_white_24dp, R.drawable.ic_format_list_bulleted_white_24dp
                , R.drawable.ic_check_white_24dp);

        picDialog = new Dialog(this);
        picDialog.setContentView(R.layout.dialog_select_photo);

        myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.dialog_adplaced);

        Spinner degreeSpinner = findViewById(R.id.etDegree);
        Spinner specialSpinner = findViewById(R.id.etSpecialization);

        LinearLayout llSpecialization = findViewById(R.id.llSpecialization);

        llSpecialization.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.degrees_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        degreeSpinner.setAdapter(adapter);

        degreeSpinner.setOnItemSelectedListener(this);

        setViews();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        final Client client = new Client("H9P3XBA9GD", "3058fee363b2c4b8afe53e9d9eab642f");
        Index index = client.getIndex("books");

        sharedPreferences = SellTestActivity.this.getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        if ((sharedPreferences.contains(city)) & (sharedPreferences.contains(name))) {

            String userCityL = sharedPreferences.getString(city, "");
            TextView sampleLocation = findViewById(R.id.sampleLocation);
            sampleLocation.setText(userCityL);

            String userNameL = sharedPreferences.getString(name, "");
            TextView sampleUser = findViewById(R.id.sampleUser);
            sampleUser.setText(userNameL);
        }

        setVisiblity();

        setBundleViews();

        progressDialog = new ProgressDialog(this);

        Long tsLong = System.currentTimeMillis()/1000;
        final String timeStamp = tsLong.toString();

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
                        final String lphone = document.getString("phone");


                        finishbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {

                                progressDialog.setMessage("Placing your ad...");
                                progressDialog.show();

                                if (oldEntryName != null) {

                                    firestore.collection("books")
                                            .document(oldEntryName)
                                            .delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    //Toast.makeText(getApplicationContext(), "Old material has been deleted!", Toast.LENGTH_SHORT).show();

                                                    StorageReference imageReference1 = storageReference.child(firebaseAuth.getCurrentUser().getEmail()).child("bookimages").child(oldEntryName);

                                                    imageReference1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            //Toast.makeText(SellTestActivity.this, "Book image deleted successfully!!!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception exception) {
                                                            Toast.makeText(SellTestActivity.this, "Cannot delete book image", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                                }
                                            });

                                    final Client client = new Client("H9P3XBA9GD", "3058fee363b2c4b8afe53e9d9eab642f");
                                    Index index = client.getIndex("books");
                                    index.deleteObjectAsync(oldEntryName, null);

                                }

                                sampleApprovalView.setDrawingCacheEnabled(true);
                                sampleApprovalView.buildDrawingCache();
                                Bitmap bitmap1 = sampleApprovalView.getDrawingCache();

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data1 = baos.toByteArray();

                                StorageReference imageReference = storageReference.child(firebaseAuth.getCurrentUser().getEmail()).child("bookimages").child(entryName);
                                UploadTask uploadTask = imageReference.putBytes(data1);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SellTestActivity.this, "Failed to upload image and ad. Error: " + e, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        Spinner degreeSpinner = findViewById(R.id.etDegree);
                                        Spinner specialSpinner = findViewById(R.id.etSpecialization);

                                        downloadUri = taskSnapshot.getDownloadUrl().toString();

                                        final String uid = firebaseAuth.getCurrentUser().getUid();

                                        final Map<String, String> bookMap = new HashMap<>();

                                        String ltitleLL = title.getText().toString();
                                        final String ltitle = ltitleLL.replaceAll("/", " ");

                                        String lauthorLL = author.getText().toString();
                                        final String lauthor = lauthorLL.replaceAll("/", " ");

                                        final String ldegree = degreeSpinner.getSelectedItem().toString();

                                        String lspecialization = null;
                                        try {
                                            lspecialization = specialSpinner.getSelectedItem().toString();
                                            bookMap.put("specialization", lspecialization);
                                        } catch (Exception e){

                                        }

                                        final String lmrp = mrp.getText().toString();
                                        final String lprice = price.getText().toString();
                                        final String entryName = timeStamp + email;

                                        String lsellerMsgLL = sellerMsg.getText().toString();
                                        final String lsellerMsg = lsellerMsgLL.replaceAll("/", " ");

                                        bookMap.put("title", ltitle);
                                        bookMap.put("author", lauthor);
                                        bookMap.put("degree", ldegree);
                                        //bookMap.put("specialization", lspecialization);
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
                                        bookMap.put("phone", lphone);

                                        final String finalLspecialization = lspecialization;
                                        firestore.collection("books").document(timeStamp + email).set(bookMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firestore.collection("users").document(email).collection("books").document(ltitle).set(bookMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Index index = client.initIndex("books");

                                                        try {
                                                            index.addObjectAsync(new JSONObject()

                                                                    .put("title", ltitle)
                                                                    .put("author", lauthor)
                                                                    .put("degree", ldegree)
                                                                    .put("specialization", finalLspecialization)
                                                                    .put("mrp", "₹ " + lmrp)
                                                                    .put("price", "₹ " + lprice)
                                                                    .put("user", name)
                                                                    .put("location", llocation)
                                                                    .put("timestamp", timeStamp)
                                                                    .put("entryName", entryName)
                                                                    .put("sellerMsg", lsellerMsg)
                                                                    .put("downloadUri", downloadUri)
                                                                    .put("uid", uid)
                                                                    .put("phone", lphone)
                                                                    .put("objectID", entryName)
                                                                    .put("email", email), null);

                                                        } catch (JSONException e) {

                                                            e.printStackTrace();

                                                            Toast.makeText(SellTestActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                        }

                                                        progressDialog.dismiss();

                                                        myDialog.show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(SellTestActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SellTestActivity.this, "location unavailable", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                } else {
                    Toast.makeText(SellTestActivity.this, "location unavailable", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });

        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);
    }

    private void setBundleViews() {

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            Glide.with(SellTestActivity.this).load(bundle.getString("UpdateNoteDownloadUri")).into(swView);
            Glide.with(SellTestActivity.this).load(bundle.getString("UpdateNoteDownloadUri")).into(sampleApprovalView);
            Glide.with(SellTestActivity.this).load(bundle.getString("UpdateNoteDownloadUri")).into(sampleView);

            title.setText(bundle.getString("UpdateNoteTitle"));
            author.setText(bundle.getString("UpdateNoteAuthor"));

            String withRupees = bundle.getString("UpdateNotePrice");
            String withoutRupees = withRupees.replaceAll("₹", "");
            price.setText(withoutRupees);

            String withRupees1 = bundle.getString("UpdateNoteMrp");
            String withoutRupees1 = withRupees1.replaceAll("₹", "");
            mrp.setText(withoutRupees1);

            sellerMsg.setText(bundle.getString("UpdateNoteSellerMsg"));
            oldEntryName = bundle.getString("UpdateNoteEntryName");

            String UpdateNoteDegree = bundle.getString("UpdateNoteDegree");

            Spinner degreeSpinner = findViewById(R.id.etDegree);
            Spinner specialSpinner = findViewById(R.id.etSpecialization);

            if (UpdateNoteDegree.equals("Architecture")){
                degreeSpinner.setSelection(1);
            } else if (UpdateNoteDegree.equals("Arts")) {
                degreeSpinner.setSelection(2);
            } else if (UpdateNoteDegree.equals("Commerce")) {
                degreeSpinner.setSelection(3);
            } else if (UpdateNoteDegree.equals("Computer Applications")) {
                degreeSpinner.setSelection(4);
            } else if (UpdateNoteDegree.equals("Education")) {
                degreeSpinner.setSelection(5);
            } else if (UpdateNoteDegree.equals("Engineering")) {
                degreeSpinner.setSelection(6);
            } else if (UpdateNoteDegree.equals("Literature")) {
                degreeSpinner.setSelection(7);
            } else if (UpdateNoteDegree.equals("Law")) {
                degreeSpinner.setSelection(8);
            } else if (UpdateNoteDegree.equals("Medical")) {
                degreeSpinner.setSelection(9);
            } else if (UpdateNoteDegree.equals("Science")) {
                degreeSpinner.setSelection(10);
            }

            nextBtn.setVisibility(View.VISIBLE);

        }

    }

    public void setViews(){

        prvBtn = findViewById(R.id.previous);
        nextBtn = findViewById(R.id.next);
        prv1Btn = findViewById(R.id.previous1);
        next1Btn = findViewById(R.id.next1);
        finishbtn = findViewById(R.id.finish);
        bookDetails = findViewById(R.id.bookDetails);
        firstLayout = findViewById(R.id.firstLayout);
        swView = findViewById(R.id.bookPic);
        sampleView = findViewById(R.id.sampleBookPic);
        sampleApprovalView = findViewById(R.id.sampleApprovalImage);
        sampleAd = findViewById(R.id.sampleAd);
        sampleTitle = findViewById(R.id.sampleTitle);
        sampleAuthor = findViewById(R.id.sampleAuthor);
        sampleDegree = findViewById(R.id.sampleDegree);
        sampleSpecial = findViewById(R.id.sampleSpecial);
        sampleMrp = findViewById(R.id.sampleMrp);
        samplePrice = findViewById(R.id.samplePrice);
        sampleSellerMsg = findViewById(R.id.sampleSellerMsg);
        adLookLike = findViewById(R.id.adLookLike);
        imageLookLike = findViewById(R.id.imageLookLike);
        sampleAd = findViewById(R.id.sampleAd);
        title = findViewById(R.id.etMaterialTitle);
        author = findViewById(R.id.etAuthor);
        mrp = findViewById(R.id.etMrp);
        price = findViewById(R.id.etExpectedPrice);
        sellerMsg = findViewById(R.id.etNoteToBuyer);

    }

    public void setVisiblity(){

        prvBtn.setVisibility(View.GONE);
        prv1Btn.setVisibility(View.GONE);
        next1Btn.setVisibility(View.GONE);
        sampleAd.setVisibility(View.GONE);
        finishbtn.setVisibility(View.GONE);
        adLookLike.setVisibility(View.GONE);
        sampleApprovalView.setVisibility(View.GONE);
        imageLookLike.setVisibility(View.GONE);
        nextBtn.setVisibility(View.GONE);
        bookDetails.setVisibility(View.GONE);

    }

    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.next:
                circleImageSteps.nextStep();
                prvBtn.setVisibility(View.VISIBLE);
                firstLayout.setVisibility(View.GONE);
                bookDetails.setVisibility(View.VISIBLE);
                prv1Btn.setVisibility(View.GONE);
                nextBtn.setVisibility(View.GONE);
                adLookLike.setVisibility(View.GONE);
                sampleApprovalView.setVisibility(View.GONE);
                imageLookLike.setVisibility(View.GONE);
                next1Btn.setVisibility(View.VISIBLE);

                break;

            case R.id.next1:

                Spinner degreeSpinner = findViewById(R.id.etDegree);
                Spinner specialSpinner = findViewById(R.id.etSpecialization);
                LinearLayout llSpecialization = findViewById(R.id.llSpecialization);

                String a = title.getText().toString();
                String b = author.getText().toString();
                String c = degreeSpinner.getSelectedItem().toString();
                String d = " ";
                if (llSpecialization.isShown()){
                    d = specialSpinner.getSelectedItem().toString();
                }
                String e = mrp.getText().toString();
                String f = price.getText().toString();
                String g = sellerMsg.getText().toString();

                if ((a.equals("")) || (b.equals("")) || (c.equals("")) || (e.equals("")) || (f.equals("")) || (g.equals("")) || (c.equals("Select a degree")) || (d.equals("Select specialization"))){

                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();

                }else{

                    circleImageSteps.nextStep();
                    next1Btn.setVisibility(View.GONE);
                    prvBtn.setVisibility(View.GONE);
                    bookDetails.setVisibility(View.GONE);
                    firstLayout.setVisibility(View.GONE);
                    sampleApprovalView.setVisibility(View.VISIBLE);
                    imageLookLike.setVisibility(View.VISIBLE);

                    adLookLike.setVisibility(View.VISIBLE);
                    finishbtn.setVisibility(View.VISIBLE);
                    sampleAd.setVisibility(View.VISIBLE);
                    prv1Btn.setVisibility(View.VISIBLE);

                    final String ltitle = a;
                    final String lauthor = b;
                    final String ldegree = c;
                    final String lspecialization;
                    if (specialSpinner.isShown()){
                        lspecialization = specialSpinner.getSelectedItem().toString();
                        sampleSpecial.setText(lspecialization);
                    } else {
                        d = "";
                        sampleSpecial.setText(d);
                    }
                    final String lmrp = e;
                    final String lprice = f;
                    final String lsellerMsg = g;


                    sampleTitle.setText(ltitle);
                    sampleAuthor.setText(lauthor);
                    sampleDegree.setText(ldegree);
                    //sampleSpecial.setText(lspecialization);
                    sampleMrp.setText("₹" + lmrp);
                    samplePrice.setText("₹" + lprice);
                    sampleSellerMsg.setText(lsellerMsg);

                    sampleMrp.setPaintFlags(sampleMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                }

                break;

            case R.id.previous:
                circleImageSteps.previousStep();
                sampleAd.setVisibility(View.GONE);
                bookDetails.setVisibility(View.GONE);
                prvBtn.setVisibility(View.GONE);
                finishbtn.setVisibility(View.GONE);
                next1Btn.setVisibility(View.GONE);
                prv1Btn.setVisibility(View.GONE);
                adLookLike.setVisibility(View.GONE);
                sampleApprovalView.setVisibility(View.GONE);
                imageLookLike.setVisibility(View.GONE);

                firstLayout.setVisibility(View.VISIBLE);
                nextBtn.setVisibility(View.VISIBLE);

                break;

            case R.id.previous1:
                circleImageSteps.previousStep();
                prv1Btn.setVisibility(View.GONE);
                finishbtn.setVisibility(View.GONE);
                firstLayout.setVisibility(View.GONE);
                sampleAd.setVisibility(View.GONE);
                adLookLike.setVisibility(View.GONE);
                sampleApprovalView.setVisibility(View.GONE);
                imageLookLike.setVisibility(View.GONE);

                bookDetails.setVisibility(View.VISIBLE);
                prvBtn.setVisibility(View.VISIBLE);
                next1Btn.setVisibility(View.VISIBLE);

                break;

            case R.id.finish:
                //Toast.makeText(this, "Uploading ad!!!", Toast.LENGTH_SHORT).show();
                break;

            case R.id.barCodeScannerBtn:

                prefManager = new PrefManager(this);
                if (prefManager.isBarcodeFirstTimeLaunch()) {

                    final Dialog barcodeDialog;
                    barcodeDialog = new Dialog(SellTestActivity.this);
                    barcodeDialog.setContentView(R.layout.dialog_barcode);
                    barcodeDialog.show();

                    prefManager.barcodeSetFirstTimeLaunch(false);

                    Button btnGotIt = barcodeDialog.findViewById(R.id.btnGotIt);
                    btnGotIt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            barcodeDialog.dismiss();

                            IntentIntegrator integrator = new IntentIntegrator(SellTestActivity.this);
                            integrator.setPrompt("Scan the barcode at the back cover of your book");
                            integrator.setCameraId(0);  // Use a specific camera of the device
                            integrator.setOrientationLocked(true);
                            integrator.setBeepEnabled(true);
                            integrator.setCaptureActivity(CaptureActivityPortrait.class);
                            integrator.initiateScan();

                            //Glide.with(SellTestActivity.this).load(R.drawable.add_imge_manually).into(swView);
                            swView.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            title.setText("");
                            author.setText("");
                            nextBtn.setVisibility(View.GONE);
                        }
                    });
                } else {

                    IntentIntegrator integrator = new IntentIntegrator(SellTestActivity.this);
                    integrator.setPrompt("Scan the barcode at the back cover of your book");
                    integrator.setCameraId(0);  // Use a specific camera of the device
                    integrator.setOrientationLocked(true);
                    integrator.setBeepEnabled(true);
                    integrator.setCaptureActivity(CaptureActivityPortrait.class);
                    integrator.initiateScan();

                    swView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    title.setText("");
                    author.setText("");
                    nextBtn.setVisibility(View.GONE);

                }

                break;

            case R.id.bookPic:
                picDialog.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                picDialog.show();
                break;

            case R.id.btnCamera:
                checkPermissionCW();
                picDialog.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                picDialog.dismiss();
                break;

            case R.id.btnGallery:
                checkPermissionRG();
                picDialog.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                picDialog.dismiss();
                break;

            case R.id.btnHome:
                Intent intent = new Intent(SellTestActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.btnAnotherAd:
                Intent backintent = new Intent(SellTestActivity.this, SellTestActivity.class);
                startActivity(backintent);
                finish();
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                swView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");

                Toast.makeText(this, "ISBN scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        checkinOpenLibrary(result.getContents());

                        EditText tvTitle = findViewById(R.id.etMaterialTitle);
                        if (tvTitle.getText().length() == 0) {
                            checkinGoogleAPI(result.getContents());
                        }
                    }
                });

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode != RESULT_OK){
            Log.e("msg", "photo not get");
            return;
        }

        switch (requestCode) {

            case CAPTURE_CAMERA:

                swView.setImageURI(Uri.parse("file:///" + filen));

                nextBtn.setVisibility(View.VISIBLE);

                sampleApprovalView.setImageURI(Uri.parse("file:///" + filen));

                sampleView.setImageURI(Uri.parse("file:///" + filen));
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

                    nextBtn.setVisibility(View.VISIBLE);

                    sampleApprovalView.setImageURI(Uri.parse("file:///" + filen));//fresco library

                    sampleView.setImageURI(Uri.parse("file:///" + filen));//fresco library

                } catch (Exception e) {

                    Log.e("", "Error while creating temp file", e);
                }
                break;

        }
    }

    private void checkPermissionCA(){
        int permissionCheck = ContextCompat.checkSelfPermission(SellTestActivity.this, android.Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    SellTestActivity.this, new String[]{android.Manifest.permission.CAMERA}, MY_REQUEST_CAMERA);
        } else {
            catchPhoto();
        }
    }
    private void checkPermissionCW(){
        int permissionCheck = ContextCompat.checkSelfPermission(SellTestActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    SellTestActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_WRITE_CAMERA);
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
                Toast.makeText(SellTestActivity.this, "please check your sdcard status", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SellTestActivity.this, "please check your sdcard status", Toast.LENGTH_SHORT).show();
        }
    }
    private void checkPermissionRG(){
        int permissionCheck = ContextCompat.checkSelfPermission(SellTestActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    SellTestActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_READ_GALLERY);
        } else {
            checkPermissionWG();
        }
    }
    private void checkPermissionWG(){
        int permissionCheck = ContextCompat.checkSelfPermission(SellTestActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // int permissionCheck2 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    SellTestActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_WRITE_GALLERY);
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

    private void checkinOpenLibrary(String contents) {

        final String isbn = contents;

        String bookOpenApi = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&jscmd=data&format=json";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(bookOpenApi)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // ... check for failure using `isSuccessful` before proceeding

                // Read data on the worker thread
                final String responseData = response.body().string();
                JSONObject resultObject = null;

                try {

                    resultObject = new JSONObject(responseData);
                    final JSONObject isbnObject = resultObject.getJSONObject("ISBN:" + isbn);

                    try{
                        final String titleL = isbnObject.getString("title");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EditText tvTitle = findViewById(R.id.etMaterialTitle);
                                tvTitle.setText(titleL);
                            }
                        });

                    } catch(JSONException jse){
                        jse.printStackTrace();
                    }

                    try{

                        JSONArray authors = isbnObject.getJSONArray("authors");
                        JSONObject author = authors.getJSONObject(0);

                        final String authorName = author.getString("name");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EditText tvAuthor = findViewById(R.id.etAuthor);
                                tvAuthor.setText(authorName);
                            }
                        });

                    } catch(JSONException jse){
                        jse.printStackTrace();
                    }

                    try{

                        JSONObject imageObject = isbnObject.getJSONObject("cover");
                        final String imageUrl = imageObject.getString("large");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //SimpleDraweeView ivBookImage = findViewById(R.id.bookPic);
                                SimpleDraweeView sampleBookPic = findViewById(R.id.sampleBookPic);
                                SimpleDraweeView sampleApprovalImage = findViewById(R.id.sampleApprovalImage);

                                if (!(imageUrl == null)){

                                    Glide.with(SellTestActivity.this).load(imageUrl).listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            swView.setVisibility(View.VISIBLE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            swView.setVisibility(View.VISIBLE);
                                            return false;
                                        }
                                    }).into(swView);

                                    Glide.with(SellTestActivity.this).load(imageUrl).into(sampleApprovalImage);
                                    Glide.with(SellTestActivity.this).load(imageUrl).into(sampleBookPic);

                                }
                            }
                        });

                    } catch(final JSONException jse){
                        jse.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void checkinGoogleAPI(String contents){

        String bookSearchString = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + contents;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(bookSearchString)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String googleResponseData = response.body().string();
                JSONObject resultObject = null;

                try {

                    resultObject = new JSONObject(googleResponseData);
                    JSONArray items = resultObject.getJSONArray("items");
                    JSONObject volumeInfo = items.getJSONObject(0);
                    JSONObject volumeObject = volumeInfo.getJSONObject("volumeInfo");
                    final String TitleL = volumeObject.getString("title");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            EditText tvTitle = findViewById(R.id.etMaterialTitle);
                            tvTitle.setText(TitleL);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {

                    JSONArray items = resultObject.getJSONArray("items");
                    JSONObject volumeInfo = items.getJSONObject(0);
                    JSONObject volumeObject = volumeInfo.getJSONObject("volumeInfo");
                    JSONArray authorsArray = volumeObject.getJSONArray("authors");
                    final String authorName = authorsArray.getString(0);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            EditText tvAuthor = findViewById(R.id.etAuthor);
                            tvAuthor.setText(authorName);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try{
                    JSONArray items = resultObject.getJSONArray("items");
                    JSONObject volumeInfo = items.getJSONObject(0);
                    JSONObject volumeObject = volumeInfo.getJSONObject("volumeInfo");
                    JSONObject imageObject = volumeObject.getJSONObject("imageLinks");
                    final String bookImageUrl = imageObject.getString("thumbnail");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SimpleDraweeView ivBookImage = findViewById(R.id.bookPic);
                            SimpleDraweeView sampleBookPic = findViewById(R.id.sampleBookPic);
                            SimpleDraweeView sampleApprovalImage = findViewById(R.id.sampleApprovalImage);

                            //Glide.with(SellTestActivity.this).load(bookImageUrl).into(ivBookImage);

                            if (!(bookImageUrl == null)){

                                Glide.with(SellTestActivity.this).load(bookImageUrl).listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        progressBar.setVisibility(View.GONE);
                                        swView.setVisibility(View.VISIBLE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        progressBar.setVisibility(View.GONE);
                                        swView.setVisibility(View.VISIBLE);
                                        return false;
                                    }
                                }).into(swView);

                                Glide.with(SellTestActivity.this).load(bookImageUrl).into(sampleApprovalImage);
                                Glide.with(SellTestActivity.this).load(bookImageUrl).into(sampleBookPic);

                                nextBtn.setVisibility(View.VISIBLE);

                            }
                        }
                    });

                } catch (final JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            swView.setVisibility(View.VISIBLE);
                            Glide.with(SellTestActivity.this).load(R.drawable.add_imge_manually).into(swView);
                        }
                    });

                }

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String degree = parent.getItemAtPosition(position).toString();

        //Toast.makeText(this, degree, Toast.LENGTH_SHORT).show();

        Spinner specialSpinner = findViewById(R.id.etSpecialization);
        LinearLayout llSpecialization = findViewById(R.id.llSpecialization);

        if(degree.contentEquals("Engineering")) {

            //specialSpinner.setVisibility(View.VISIBLE);
            llSpecialization.setVisibility(View.VISIBLE);

            List<String> list = new ArrayList<String>();
            list.add("Select specialization");
            list.add("All branches");
            list.add("CSE");
            list.add("ECE");
            list.add("EEE");
            list.add("E&I");
            list.add("Information Technology");
            list.add("ICE");
            list.add("Mechanical Engineering");
            list.add("Mechatronics");
            list.add("Production Engineering");
            list.add("Others");

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dataAdapter.notifyDataSetChanged();
            specialSpinner.setAdapter(dataAdapter);

            final Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String UpdateNoteSpecialization = bundle.getString("UpdateNoteSpecialization");
                if (UpdateNoteSpecialization.equals("All branches")){
                    specialSpinner.setSelection(1);
                } else if (UpdateNoteSpecialization.equals("CSE")) {
                    specialSpinner.setSelection(2);
                } else if (UpdateNoteSpecialization.equals("ECE")) {
                    specialSpinner.setSelection(3);
                } else if (UpdateNoteSpecialization.equals("EEE")) {
                    specialSpinner.setSelection(4);
                } else if (UpdateNoteSpecialization.equals("E&I")) {
                    specialSpinner.setSelection(5);
                } else if (UpdateNoteSpecialization.equals("Information Technology")) {
                    specialSpinner.setSelection(6);
                } else if (UpdateNoteSpecialization.equals("ICE")) {
                    specialSpinner.setSelection(7);
                } else if (UpdateNoteSpecialization.equals("Mechanical Engineering")) {
                    specialSpinner.setSelection(8);
                } else if (UpdateNoteSpecialization.equals("Mechatronics")) {
                    specialSpinner.setSelection(9);
                } else if (UpdateNoteSpecialization.equals("Production Engineering")) {
                    specialSpinner.setSelection(10);
                } else if (UpdateNoteSpecialization.equals("Others")) {
                    specialSpinner.setSelection(11);
                }
            }
        }

        if(degree.contentEquals("Medical")) {

            //specialSpinner.setVisibility(View.VISIBLE);
            llSpecialization.setVisibility(View.VISIBLE);

            List<String> list = new ArrayList<String>();
            list.add("Select specialization");
            list.add("General Medicine");
            list.add("Dental");
            list.add("Pharmacy");
            list.add("Nursing");
            list.add("Sidha & Ayurvedha");
            list.add("Others");
            ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dataAdapter2.notifyDataSetChanged();
            specialSpinner.setAdapter(dataAdapter2);

            final Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String UpdateNoteSpecialization = bundle.getString("UpdateNoteSpecialization");
                if (UpdateNoteSpecialization.equals("General Medicine")){
                    specialSpinner.setSelection(1);
                } else if (UpdateNoteSpecialization.equals("Dental")) {
                    specialSpinner.setSelection(2);
                } else if (UpdateNoteSpecialization.equals("Pharmacy")) {
                    specialSpinner.setSelection(3);
                } else if (UpdateNoteSpecialization.equals("Nursing")) {
                    specialSpinner.setSelection(4);
                } else if (UpdateNoteSpecialization.equals("Sidha & Ayurvedha")) {
                    specialSpinner.setSelection(5);
                } else if (UpdateNoteSpecialization.equals("Others")) {
                    specialSpinner.setSelection(6);
                }
            }
        }

        if(degree.contentEquals("Architecture")) {

            //specialSpinner.setVisibility(View.VISIBLE);
            llSpecialization.setVisibility(View.VISIBLE);

            List<String> list = new ArrayList<String>();
            list.add("Select specialization");
            list.add("Landscape Architecture");
            list.add("Urban Planner");
            list.add("Restoration Architecture");
            list.add("Research Architecture");
            list.add("Lighting Architecture");
            list.add("Political Architecture");
            list.add("Others");
            ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dataAdapter3.notifyDataSetChanged();
            specialSpinner.setAdapter(dataAdapter3);

            final Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String UpdateNoteSpecialization = bundle.getString("UpdateNoteSpecialization");
                if (UpdateNoteSpecialization.equals("Landscape Architecture")){
                    specialSpinner.setSelection(1);
                } else if (UpdateNoteSpecialization.equals("Urban Planner")) {
                    specialSpinner.setSelection(2);
                } else if (UpdateNoteSpecialization.equals("Restoration Architecture")) {
                    specialSpinner.setSelection(3);
                } else if (UpdateNoteSpecialization.equals("Research Architecture")) {
                    specialSpinner.setSelection(4);
                } else if (UpdateNoteSpecialization.equals("Lighting Architecture")) {
                    specialSpinner.setSelection(5);
                } else if (UpdateNoteSpecialization.equals("Political Architecture")) {
                    specialSpinner.setSelection(6);
                } else if (UpdateNoteSpecialization.equals("Others")) {
                    specialSpinner.setSelection(6);
                }
            }
        }

        if (degree.contentEquals("Arts")) {

            //specialSpinner.setVisibility(View.GONE);
            llSpecialization.setVisibility(View.GONE);

        }

        if (degree.contentEquals("Commerce")) {

            //specialSpinner.setVisibility(View.GONE);
            llSpecialization.setVisibility(View.GONE);

        }

        if (degree.contentEquals("Computer Applications")) {

            //specialSpinner.setVisibility(View.GONE);
            llSpecialization.setVisibility(View.GONE);

        }

        if (degree.contentEquals("Education")) {

            //specialSpinner.setVisibility(View.GONE);
            llSpecialization.setVisibility(View.GONE);

        }

        if (degree.contentEquals("Literature")) {

            //specialSpinner.setVisibility(View.VISIBLE);
            llSpecialization.setVisibility(View.VISIBLE);

            List<String> list = new ArrayList<String>();
            list.add("Select specialization");
            list.add("English");
            list.add("Tamil");
            list.add("Telugu");
            list.add("Malayalam");
            list.add("Others");
            ArrayAdapter<String> dataAdapter4 = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dataAdapter4.notifyDataSetChanged();
            specialSpinner.setAdapter(dataAdapter4);

            final Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String UpdateNoteSpecialization = bundle.getString("UpdateNoteSpecialization");
                if (UpdateNoteSpecialization.equals("English")){
                    specialSpinner.setSelection(1);
                } else if (UpdateNoteSpecialization.equals("Tamil")) {
                    specialSpinner.setSelection(2);
                } else if (UpdateNoteSpecialization.equals("Telugu")) {
                    specialSpinner.setSelection(3);
                } else if (UpdateNoteSpecialization.equals("Malayalam")) {
                    specialSpinner.setSelection(4);
                } else if (UpdateNoteSpecialization.equals("Others")) {
                    specialSpinner.setSelection(6);
                }
            }

        }

        if (degree.contentEquals("Law")) {

            //specialSpinner.setVisibility(View.GONE);
            llSpecialization.setVisibility(View.GONE);

        }

        if (degree.contentEquals("Science")) {

            //specialSpinner.setVisibility(View.VISIBLE);
            llSpecialization.setVisibility(View.VISIBLE);

            List<String> list = new ArrayList<String>();
            list.add("Select specialization");
            list.add("Physics");
            list.add("Chemistry");
            list.add("Mathematics");
            list.add("Life Science");
            list.add("Others");
            ArrayAdapter<String> dataAdapter5 = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dataAdapter5.notifyDataSetChanged();
            specialSpinner.setAdapter(dataAdapter5);

            final Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String UpdateNoteSpecialization = bundle.getString("UpdateNoteSpecialization");
                if (UpdateNoteSpecialization.equals("Physics")){
                    specialSpinner.setSelection(1);
                } else if (UpdateNoteSpecialization.equals("Chemistry")) {
                    specialSpinner.setSelection(2);
                } else if (UpdateNoteSpecialization.equals("Mathematics")) {
                    specialSpinner.setSelection(3);
                } else if (UpdateNoteSpecialization.equals("Life Science")) {
                    specialSpinner.setSelection(4);
                } else if (UpdateNoteSpecialization.equals("Others")) {
                    specialSpinner.setSelection(6);
                }
            }

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
