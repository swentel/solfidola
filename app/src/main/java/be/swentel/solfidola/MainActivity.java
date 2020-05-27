package be.swentel.solfidola;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Menu drawerMenu;
    DrawerLayout drawer;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    public static final int CREATE_EXERCISE = 1001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_EXERCISE && resultCode == RESULT_OK) {
            Snackbar.make(drawer, getString(R.string.exercise_created), Snackbar.LENGTH_SHORT).show();

            int id = 0;
            Bundle extras = data.getExtras();
            if (extras != null) {
                id = extras.getInt("exercise");
            }

            if (id > 0) {
                startExercise(id);
            }
            else {
                startFragment(new ExerciseList());
            }

        }
    }

    /**
     * Set first navigation view.
     */
    private void setFirstItemNavigationView() {
        navigationView.setCheckedItem(R.id.nav_exercises);
        drawerMenu.performIdentifierAction(R.id.nav_exercises, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerMenu = navigationView.getMenu();
        setFirstItemNavigationView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //getSupportFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            closeDrawer(false);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        boolean close = false;

        Fragment fragment = null;
        switch (item.getItemId()) {

            case R.id.nav_solfege:
                close = true;
                fragment = new Solfege();
                break;

            case R.id.nav_exercises:
                close = true;
                fragment = new ExerciseList();
                break;

            case R.id.nav_tuner:
                close = true;
                fragment = new TunerFragment();
                break;

        }

        if (close) {
            closeDrawer(false);
        }

        // Update main content frame.
        if (fragment != null) {
            startFragment(fragment);
        }

        return true;
    }

    /**
     * Start a fragment.
     *
     * @param fragment
     *   Start a fragment.
     */
    public void startFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.nav_host_fragment, fragment);
        ft.commit();
    }

    /**
     * Start exercise.

     * @param id
     *   The exercise id.
     */
    public void startExercise(int id) {
        Bundle bundle = new Bundle();
        bundle.putInt("exercise", id);
        Fragment fragment = new Solfege();
        fragment.setArguments(bundle);
        startFragment(fragment);
    }

    /**
     * Open the navigation drawer.
     *
     * @param id
     *   The menu item id to perform an action on.
     */
    public void openDrawer(int id) {
        if (drawer != null) {
            drawer.openDrawer(GravityCompat.START);
        }

        if (id > 0) {
            navigationView.setCheckedItem(id);
            drawerMenu.performIdentifierAction(id, 0);
        }
    }

    /**
     * Close drawer.
     */
    public void closeDrawer(boolean checkIfOpened) {
        if (drawer == null) {
            return;
        }

        if (checkIfOpened && !drawer.isDrawerOpen(GravityCompat.START)) {
            return;
        }

        drawer.closeDrawer(GravityCompat.START);
    }

}
