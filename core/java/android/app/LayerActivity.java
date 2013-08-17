package android.app;

import android.app.Activity;
import android.os.Bundle;

public class LayerActivity extends Activity {

    private Bundle mSavedInstanceState;
    private boolean mRestarted = false;
    private boolean mShouldFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        mRestarted = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mShouldFinish = mRestarted;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mShouldFinish) {
            mShouldFinish = true;
            return;
        }
        mShouldFinish = false;
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSavedInstanceState != null) {
            finish();
        }
    }
}
