package com.tamer.alna99.watertabdriver;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.tamer.alna99.watertabdriver.fragments.AboutUsFragment;
import com.tamer.alna99.watertabdriver.fragments.ConcatUsFragment;
import com.tamer.alna99.watertabdriver.fragments.DashboardFragment;
import com.tamer.alna99.watertabdriver.fragments.MapFragment;
import com.tamer.alna99.watertabdriver.fragments.NotificationFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawerOpen, R.string.drawerClose);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        DashboardFragment dashboardFragment = new DashboardFragment();
        moveFragment(dashboardFragment);
        toolbar.setTitle(getString(R.string.homepage));
    }


    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        // View header = navigationView.getHeaderView(0);

    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homepage:
                DashboardFragment dashboardFragment = new DashboardFragment();
                moveFragment(dashboardFragment);
                toolbar.setTitle(getString(R.string.homepage));
                toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                drawerLayout.closeDrawers();
                break;
            case R.id.ride:
                MapFragment mapFragment = new MapFragment();
                moveFragment(mapFragment);
                toolbar.setTitle(getString(R.string.Ride));
                toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                drawerLayout.closeDrawers();
                break;
            case R.id.settings:
                // TODO: Settings activity
                break;
            case R.id.about_us:
                AboutUsFragment aboutUsFragment = new AboutUsFragment();
                moveFragment(aboutUsFragment);
                toolbar.setTitle(getString(R.string.about_us));
                toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                drawerLayout.closeDrawers();
                break;
            case R.id.concat_us:
                ConcatUsFragment concatUsFragment = new ConcatUsFragment();
                moveFragment(concatUsFragment);
                toolbar.setTitle(getString(R.string.concat_us));
                toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                drawerLayout.closeDrawers();
                break;
            case R.id.notification:
                NotificationFragment notificationFragment = new NotificationFragment();
                moveFragment(notificationFragment);
                toolbar.setTitle(getString(R.string.notification));
                toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                drawerLayout.closeDrawers();
                break;
        }
        return false;
    }

    public void moveFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in,
                android.R.animator.fade_out).replace(R.id.patient_frameLayout, fragment).commit();
    }
}

