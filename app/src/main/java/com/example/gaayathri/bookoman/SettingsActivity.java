package com.example.gaayathri.bookoman;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    public static final String mypreference = "mypref";

    public static String name = "nameKey";
    public static String phone = "phoneKey";
    public static String background = "backgroundKey";
    public static String city = "cityKey";
    public static String profilePicUrl = "profilePicUrlKey";
    public static String email = "emailKey";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        ImageView back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                finish();
            }
        });

        // load settings fragment
        getFragmentManager().beginTransaction().replace(R.id.frm, new MainPreferenceFragment()).commit();

        
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            final Dialog userDataDialog;
            userDataDialog = new Dialog(getActivity());
            userDataDialog.setContentView(R.layout.dialog_edit_profile);

            final EditText etname = userDataDialog.findViewById(R.id.et_name);
            final EditText etphone = userDataDialog.findViewById(R.id.et_phone);
            final EditText etemail = userDataDialog.findViewById(R.id.et_email);
            final Spinner spcity = userDataDialog.findViewById(R.id.spCity);
            final Spinner spbackground = userDataDialog.findViewById(R.id.spBackground);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.degrees_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spbackground.setAdapter(adapter);

            ArrayAdapter<CharSequence> cityadapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.cities_array, android.R.layout.simple_spinner_item);
            cityadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spcity.setAdapter(cityadapter);

            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            final String mail = firebaseAuth.getCurrentUser().getEmail();


            DocumentReference docRef = firestore.collection("users").document(mail);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                            String nameLL = document.getString("name");
                            String phoneLL = document.getString("phone");
                            String backgroundLL = document.getString("background");
                            String cityLL = document.getString("city");

                            etname.setText(nameLL);
                            etemail.setText(mail);
                            etphone.setText(phoneLL);

                            if (backgroundLL.equals("Architecture")){
                                spbackground.setSelection(1);
                            } else if (backgroundLL.equals("Arts")) {
                                spbackground.setSelection(2);
                            } else if (backgroundLL.equals("Commerce")) {
                                spbackground.setSelection(3);
                            } else if (backgroundLL.equals("Computer Applications")) {
                                spbackground.setSelection(4);
                            } else if (backgroundLL.equals("Education")) {
                                spbackground.setSelection(5);
                            } else if (backgroundLL.equals("Engineering")) {
                                spbackground.setSelection(6);
                            } else if (backgroundLL.equals("Literature")) {
                                spbackground.setSelection(7);
                            } else if (backgroundLL.equals("Law")) {
                                spbackground.setSelection(8);
                            } else if (backgroundLL.equals("Medical")) {
                                spbackground.setSelection(9);
                            } else if (backgroundLL.equals("Science")) {
                                spbackground.setSelection(10);
                            }

                            if (cityLL.equals("Ariyalur")){
                                spcity.setSelection(1);
                            } else if (cityLL.equals("Chennai")) {
                                spcity.setSelection(2);
                            } else if (cityLL.equals("Coimbatore")) {
                                spcity.setSelection(3);
                            } else if (cityLL.equals("Cuddalore")) {
                                spcity.setSelection(4);
                            } else if (cityLL.equals("Dharmapuri")) {
                                spcity.setSelection(5);
                            } else if (cityLL.equals("Dindigul")) {
                                spcity.setSelection(6);
                            } else if (cityLL.equals("Erode")) {
                                spcity.setSelection(7);
                            } else if (cityLL.equals("Kancheepuram")) {
                                spcity.setSelection(8);
                            } else if (cityLL.equals("Karur")) {
                                spcity.setSelection(9);
                            } else if (cityLL.equals("Krishnagiri")) {
                                spcity.setSelection(10);
                            } else if (cityLL.equals("Madurai")) {
                                spcity.setSelection(11);
                            } else if (cityLL.equals("Nagapattinam")) {
                                spcity.setSelection(12);
                            } else if (cityLL.equals("Kanyakumari")) {
                                spcity.setSelection(13);
                            } else if (cityLL.equals("Namakkal")) {
                                spcity.setSelection(14);
                            } else if (cityLL.equals("Perambalur")) {
                                spcity.setSelection(15);
                            } else if (cityLL.equals("Pudukottai")) {
                                spcity.setSelection(16);
                            } else if (cityLL.equals("Ramanathapuram")) {
                                spcity.setSelection(17);
                            } else if (cityLL.equals("Salem")) {
                                spcity.setSelection(18);
                            } else if (cityLL.equals("Sivagangai")) {
                                spcity.setSelection(19);
                            } else if (cityLL.equals("Thanjavur")) {
                                spcity.setSelection(20);
                            } else if (cityLL.equals("Theni")) {
                                spcity.setSelection(21);
                            } else if (cityLL.equals("Thiruvallur")) {
                                spcity.setSelection(22);
                            } else if (cityLL.equals("Thiruvarur")) {
                                spcity.setSelection(23);
                            } else if (cityLL.equals("Tuticorin")) {
                                spcity.setSelection(24);
                            } else if (cityLL.equals("Trichirappalli")) {
                                spcity.setSelection(25);
                            } else if (cityLL.equals("Thirunelveli")) {
                                spcity.setSelection(26);
                            } else if (cityLL.equals("Tiruppur")) {
                                spcity.setSelection(27);
                            } else if (cityLL.equals("Thiruvannamalai")) {
                                spcity.setSelection(28);
                            } else if (cityLL.equals("The Nilgiris")) {
                                spcity.setSelection(29);
                            } else if (cityLL.equals("Vellore")) {
                                spcity.setSelection(30);
                            } else if (cityLL.equals("Virudhunagar")) {
                                spcity.setSelection(31);
                            }

                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });


            Button btnCancel = userDataDialog.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userDataDialog.dismiss();
                }
            });

            Button btnUpdate = userDataDialog.findViewById(R.id.btnUpdate);
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String nameL = etname.getText().toString();
                    String phoneL = etphone.getText().toString();
                    String emailL = etemail.getText().toString();
                    String cityL = spcity.getSelectedItem().toString();
                    String backgroundL = spbackground.getSelectedItem().toString();

                    SharedPreferences sharedpreferences = getActivity().getSharedPreferences(mypreference, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(name, nameL);
                    editor.putString(phone, phoneL);
                    editor.putString(background, backgroundL);
                    editor.putString(city, cityL);
                    editor.commit();

                    Map<String, String> userMap = new HashMap<>();

                    userMap.put("name", nameL);
                    userMap.put("email", emailL);
                    userMap.put("phone", phoneL);
                    userMap.put("city", cityL);
                    userMap.put("background", backgroundL);

                    firestore.collection("users").document(mail).set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            userDataDialog.dismiss();

                            Toast.makeText(getActivity(), "Profile updated successfully!!!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            String error = e.getMessage();
                            Toast.makeText(getActivity(), "Cannot update profile. Error:" + error, Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            });


            // gallery EditText change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_gallery_name)));

            // notification preference change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_notifications_new_message_ringtone)));

            // feedback preference click listener
            Preference myPref = findPreference(getString(R.string.key_send_feedback));
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    sendFeedback(getActivity());
                    return true;
                }
            });

            Preference editMyProfile = findPreference("editMyProfile");
            editMyProfile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    userDataDialog.show();
                    return true;
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        //preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        /*sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));*/
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(R.string.summary_choose_ringtone);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("key_gallery_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact.bookoman@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));

    }

    private void sendToEditProfile() {



    }
}
