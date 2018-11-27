package movies.flag.pt.moviesapp.screens;

import android.animation.Animator;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import movies.flag.pt.moviesapp.R;

public class SplashScreen extends Screen {

    //private ImageView splashImage;
    private TextView logoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor));
        }

        findViews();
        addListeners();

    }

    private void findViews() {
        logoTextView = findViewById(R.id.logo_text_view);
    }

    private void addListeners() {

        int SPLASH_DISPLAY_LENGTH = 2000;

        logoTextView.setScaleX(0);
        logoTextView.setScaleY(0);
        logoTextView.animate().scaleX(1).scaleY(1).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Intent intent = new Intent(SplashScreen.this, ActionsScreen.class);
                        SplashScreen.this.startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        SplashScreen.this.finish();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                }).setDuration(SPLASH_DISPLAY_LENGTH).start();

    }

}
