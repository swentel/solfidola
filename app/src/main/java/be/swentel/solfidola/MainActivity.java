package be.swentel.solfidola;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
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

    /**
     * Set first navigation view.
     */
    private void setFirstItemNavigationView() {
        navigationView.setCheckedItem(R.id.nav_home);
        drawerMenu.performIdentifierAction(R.id.nav_home, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerMenu = navigationView.getMenu();
        setFirstItemNavigationView();
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

            case R.id.nav_home:
                close = true;
                fragment = new HomeFragment();
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
