package com.appu.gaayathri.bookoman;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.multidex.MultiDex;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.activites.ContactListActivity;
import org.chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import org.chat21.android.ui.messages.listeners.OnMessageClickListener;
import org.chat21.android.utils.IOUtils;

import static org.chat21.android.core.ChatManager._SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER;

public class AppContext extends Application {

    private static final String TAG = AppContext.class.getName();

    private static AppContext instance;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initChatSDK();

    }

    public void initChatSDK() {

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        ChatManager.Configuration mChatConfiguration =
                new ChatManager.Configuration.Builder(getString(R.string.chat_firebase_appId))
                        .firebaseUrl("https://bookoman-3f038.firebaseio.com/")
                        .storageBucket("gs://bookoman-3f038.appspot.com")
                        .build();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            IChatUser iChatUser = (IChatUser) IOUtils.getObjectFromFile(instance,
                    _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER);



            ChatManager.start(this, mChatConfiguration, iChatUser);
            Log.i(TAG, "chat has been initialized with success");

            ChatUI.getInstance().setContext(instance);
            ChatUI.getInstance().enableGroups(true);

            ChatUI.getInstance().setOnMessageClickListener(new OnMessageClickListener() {
                @Override
                public void onMessageLinkClick(TextView message, ClickableSpan clickableSpan) {
                    String text = ((URLSpan) clickableSpan).getURL();

                    Uri uri = Uri.parse(text);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(browserIntent);
                }
            });

            final IChatUser support = null;
            ChatUI.getInstance().setOnNewConversationClickListener(new OnNewConversationClickListener() {
                @Override
                public void onNewConversationClicked() {
                    if (support != null) {
                        ChatUI.getInstance().openConversationMessagesActivity(support);
                    } else {
                        Intent intent = new Intent(instance, ContactListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context

                        startActivity(intent);
                    }
                }
            });

            Log.i(TAG, "ChatUI has been initialized with success");

        } else {
            Log.w(TAG, "chat can't be initialized because chatUser is null");
        }
    }
}
