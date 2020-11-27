package by.mkr.blackberry.textlayouttools;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import java.util.Calendar;

public class AboutActivity extends AppCompatActivity {
    private int _pressedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppThemeHelper.setTheme(this);
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


        final ImageView iconImage = findViewById(R.id.iconView);
        updImg(iconImage);
        iconImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _pressedCount++;
                if (_pressedCount == 3) {
                    Animation animBlink = AnimationUtils.loadAnimation(iconImage.getContext(), R.anim.blink);
                    animBlink.setDuration(1000);
                    iconImage.startAnimation(animBlink);
                    _pressedCount = 0;
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void updImg(ImageView icnImg) {
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int resId = 0;
        if (day == 31 && month == 11) resId = R.drawable.ic_icon_new_year;
        else if (day == 1 && month == 0) resId = R.drawable.ic_icon_new_year;
        else if (day == 7 && month == 0) resId = R.drawable.ic_icon_christmas_2;
        else if (day == 23 && month == 1) resId = R.drawable.ic_icon_defenders;
        else if (day == 8 && month == 2) resId = R.drawable.ic_icon_women;
        else if (day == 9 && month == 4) resId = R.drawable.ic_icon_victory;
        else if (day == 31 && month == 9) resId = R.drawable.ic_icon_halloween;
        else if (day == 25 && month == 11) resId = R.drawable.ic_icon_christmas_1;
        if (resId != 0) { icnImg.setImageResource(resId); }
    }

}
