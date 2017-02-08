package si.majeric.smarthouse.wear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.widget.Toast;

import java.util.Map;

import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.client.ResponseListener;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.TriggerConfig;

/**
 * Created by Uros Majeric on 17/05/16.
 */
public class OpenDoorsActivity extends Activity implements ResponseListener,
                                                                   DelayedConfirmationView.DelayedConfirmationListener {

    private Client _client;
    private DelayedConfirmationView mDelayedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_doors);

        _client = ((SmartHouseApplication) getApplication()).getClient();

        mDelayedView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
        mDelayedView.setListener(this);

        // Two seconds to cancel the action
        mDelayedView.setTotalTimeMs(3000);
        // Start the timer
        mDelayedView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        view.setPressed(true);
//        Notification notification = new Notification.Builder(this)
//                                            .setSmallIcon(R.mipmap.ic_launcher)
//                                            .setContentTitle(getString(R.string.notification_title))
//                                            .setContentText(getString(R.string.notification_timer_selected))
//                                            .build();
//        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);
        // Prevent onTimerFinished from being heard.
        ((DelayedConfirmationView) view).setListener(null);
        finish();

        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Canceled");
        startActivity(intent);
        // User canceled, abort the action
//        Toast.makeText(OpenDoorsActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimerSelected(View view) {
        // User didn't cancel, perform the action
        mDelayedView.setEnabled(false);

        _client.invokeStateChange(new TriggerConfig("VhodVrataOdpri_vrata"), this);
//        Toast.makeText(OpenDoorsActivity.this, "Opening the doors...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Opening the doors...");
        startActivity(intent);

        ((DelayedConfirmationView) view).setListener(null);
        finish();
//        Notification notification = new Notification.Builder(this)
//                                            .setSmallIcon(R.mipmap.ic_launcher)
//                                            .setContentTitle(getString(R.string.notification_title))
//                                            .setContentText(getString(R.string.notification_timer_finished))
//                                            .build();
//        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);
    }

    @Override
    public void stateChanged(Map<String, PinState> swtch) {
//        Toast.makeText(getApplicationContext(), "Doors opened", Toast.LENGTH_LONG).show();
        this.finish();
    }

    @Override
    public void errorChangingState(Throwable t) {
//        Toast.makeText(getApplicationContext(), "Error has occured: " + t.getMessage(), Toast.LENGTH_LONG).show();
    }
}