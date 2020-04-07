package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.chatapp.Adapters.SectionPagerAdapter;
import com.example.chatapp.Fragments.ChatFragment;
import com.example.chatapp.Fragments.FriendsFragment;
import com.example.chatapp.Fragments.RequestFragment;
import com.example.chatapp.Login.StartActivity;
import com.example.chatapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;

    private Toolbar toolbar;

    private ViewPager viewPager;
    private SectionPagerAdapter sectionPagerAdapter;

    private TabLayout tabLayout;

    private Context mContext = MainActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupFirebase();
        setupToolbar();
        setupViewPager();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            sendToStart();
        }else {
            mRef.child(getString(R.string.dbname_users))
                    .child(currentUser.getUid()).child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null){
            mRef.child(getString(R.string.dbname_users))
                    .child(mAuth.getCurrentUser().getUid())
                    .child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void setupViewPager() {
        Log.d(TAG,"setupViewpager");

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);

        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        sectionPagerAdapter.addFragment(new RequestFragment(),"REQUEST");
        sectionPagerAdapter.addFragment(new ChatFragment(),"CHATS");
        sectionPagerAdapter.addFragment(new FriendsFragment(),"FRIENDS");

        viewPager.setAdapter(sectionPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupToolbar() {
        Log.d(TAG,"setupToolbar");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
    }

    private void sendToStart(){
        Intent startIntent = new Intent(mContext, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.log_out:
                mAuth.signOut();
                sendToStart();
                break;
            case R.id.account_settings:
                Intent intent = new Intent(MainActivity.this, AccountSettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.all_users:
                Intent intent2 = new Intent(MainActivity.this, AllUsersActivity.class);
                startActivity(intent2);
                break;
        }

        return true;
    }
}
