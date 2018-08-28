package com.example.gaayathri.bookoman;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;

import ss.com.bannerslider.Slider;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frm,new HomeFragment()).commit();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        IChatUser iChatUser = new ChatUser(user.getUid(), user.getDisplayName());

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        ChatManager.Configuration mChatConfiguration =
                new ChatManager.Configuration.Builder(getString(R.string.google_app_id))
                        .firebaseUrl("https://bookoman-3f038.firebaseio.com/")
                        .storageBucket("gs://bookoman-3f038.appspot.com")
                        .build();

        ChatManager.start(this, mChatConfiguration, iChatUser);

        ChatUI.getInstance().setContext(this);

        ChatUI.getInstance().processRemoteNotification(getIntent());
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
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        if (id == R.id.nav_home) {

            fragmentTransaction.replace(R.id.frm,new HomeFragment()).commit();

        } else if (id == R.id.nav_entire_library) {

            fragmentTransaction.replace(R.id.frm,new EntireLibraryFragment()).commit();

        } else if (id == R.id.nav_categories) {

        } else if (id == R.id.nav_my_profile) {

            fragmentTransaction.replace(R.id.frm,new MyProfileFragment()).commit();

        } else if (id == R.id.nav_my_materials) {

            fragmentTransaction.replace(R.id.frm,new MyMaterialsFragment()).commit();

        } else if (id == R.id.nav_my_favorites) {

            fragmentTransaction.replace(R.id.frm,new MyFavoritesFragment()).commit();

        } else if (id == R.id.nav_conversations) {

            ChatUI.getInstance().openConversationsListActivity();

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {

            Logout();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void Logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(HomeActivity.this, EntryActivity.class));
    }
}
