package be.swentel.solfidola;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

abstract public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
