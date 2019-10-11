package by.mkr.blackberry.textlayouttools;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String emailAction = "mailto:mike_113@mail.ru?subject=Text Layout Tools " + BuildConfig.VERSION_NAME + " [" + Build.MODEL + ", " + Build.VERSION.SDK_INT + "]";
                emailIntent.setData(Uri.parse(emailAction));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_email_title)));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
