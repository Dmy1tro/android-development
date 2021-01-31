package dmytro.laskuryk.pz_2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {
    private SeekBar redSeekBar, greenSeekBar, blueSeekBar;
    private View colorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorView = findViewById(R.id.colorView);
        redSeekBar = findViewById(R.id.seekBarR);
        greenSeekBar = findViewById(R.id.seekBarG);
        blueSeekBar = findViewById(R.id.seekBarB);
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = createListener();

        redSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        greenSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        blueSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        updateColor();
    }

    private void updateColor() {
        int redValue = redSeekBar.getProgress();
        int greenValue = greenSeekBar.getProgress();
        int blueValue = blueSeekBar.getProgress();

        int total = 0xff000000 + redValue * 0x10000 + greenValue * 0x100 + blueValue;

        colorView.setBackgroundColor(total);
    }

    private SeekBar.OnSeekBarChangeListener createListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }
}