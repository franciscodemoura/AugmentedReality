package pintosoft.augmentedreality;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.info).setVisibility(View.VISIBLE);
                findViewById(R.id.button).setVisibility(View.VISIBLE);
                findViewById(R.id.image_credit).setVisibility(View.VISIBLE);
            }
        }, 3 * 1000);
    }

    public void startARActivity(View view){
        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
    }
}
