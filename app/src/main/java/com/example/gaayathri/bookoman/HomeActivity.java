package com.example.gaayathri.bookoman;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;

    TextView navUserName;
    TextView navUserEmail;
    ImageView navUserProfilePic;

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView imageView = findViewById(R.id.mysearch);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, InstantSearchActivity.class));
            }
        });

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        //Navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frm,new HomeFragment()).addToBackStack(null).commit();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userName = user.getDisplayName();
        String userEmail = user.getEmail();
        String userProfilePic = user.getPhotoUrl().toString();

        IChatUser iChatUser = new ChatUser(user.getUid(), userName);

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        ChatManager.Configuration mChatConfiguration =
                new ChatManager.Configuration.Builder(getString(R.string.google_app_id))
                        .firebaseUrl("https://bookoman-3f038.firebaseio.com/")
                        .storageBucket("gs://bookoman-3f038.appspot.com")
                        .build();

        ChatManager.start(this, mChatConfiguration, iChatUser);

        ChatUI.getInstance().setContext(this);

        ChatUI.getInstance().processRemoteNotification(getIntent());

        TextView txtProfileName = navigationView.getHeaderView(0).findViewById(R.id.navUserName);
        TextView txtProfileEmail = navigationView.getHeaderView(0).findViewById(R.id.navUserEmail);
        ImageView imgProfilePic = navigationView.getHeaderView(0).findViewById(R.id.navUserProfilePic);

        txtProfileName.setText(userName);
        txtProfileEmail.setText(userEmail);

        Glide.with(HomeActivity.this).load(userProfilePic).into(imgProfilePic);

    }

    @Override
    protected void onResume() {
        ChatManager.getInstance().getMyPresenceHandler().connect();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        ChatManager.getInstance().getMyPresenceHandler().dispose();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int fragments = getSupportFragmentManager().getBackStackEntryCount();
            if (fragments == 1) {
                finish();
            } else {
                if (getFragmentManager().getBackStackEntryCount() > 1) {
                    getFragmentManager().popBackStack();
                } else {
                    super.onBackPressed();
                }
            }
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        int id = item.getItemId();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        if (id == R.id.nav_home) {

            fragmentTransaction.replace(R.id.frm,new HomeFragment()).addToBackStack(null).commit();

        } else if (id == R.id.nav_entire_library) {

            fragmentTransaction.replace(R.id.frm,new EntireLibraryFragment()).addToBackStack(null).commit();

        } else if (id == R.id.nav_categories) {

            fragmentTransaction.replace(R.id.frm,new CatagoriesFragment()).addToBackStack(null).commit();

        } else if (id == R.id.nav_my_profile) {

            fragmentTransaction.replace(R.id.frm,new MyProfileFragment()).addToBackStack(null).commit();

        } else if (id == R.id.nav_my_materials) {

            fragmentTransaction.replace(R.id.frm,new MyMaterialsFragment()).addToBackStack(null).commit();

        } else if (id == R.id.nav_my_favorites) {

            fragmentTransaction.replace(R.id.frm,new MyFavoritesFragment()).addToBackStack(null).commit();

        } else if (id == R.id.nav_conversations) {

            ChatUI.getInstance().openConversationsListActivity();

        } else if (id == R.id.nav_settings) {

            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));

        } else if (id == R.id.nav_logout) {

            Logout();

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void Logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(HomeActivity.this, EntryActivity.class));
    }

}
