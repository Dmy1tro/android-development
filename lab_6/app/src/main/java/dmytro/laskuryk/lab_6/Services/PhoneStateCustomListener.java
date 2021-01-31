package dmytro.laskuryk.lab_6.Services;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneStateCustomListener extends PhoneStateListener {
    public interface IActionCallback {
        void action();
    }

    public boolean isMusicPausedByCall = false;
    private IActionCallback onCallStartedCallback;
    private IActionCallback onCallEndedCallback;

    public void setOnCallStarted(IActionCallback actionCallback) {
        this.onCallStartedCallback = actionCallback;
    }

    public void setOnCallEnded(IActionCallback actionCallback) {
        this.onCallEndedCallback = actionCallback;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                onCallStartedCallback.action();
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                onCallEndedCallback.action();
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                break;
        }
    }
}
