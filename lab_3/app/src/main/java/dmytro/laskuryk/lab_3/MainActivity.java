package dmytro.laskuryk.lab_3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final String Input = "Expression";
    final String Result = "Result";
    TextView inputField;
    TextView resultField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultField = findViewById(R.id.resultField);
        inputField = findViewById(R.id.inputField);
    }

    public void onOperationClick(View view){
        String expression = inputField.getText().toString()
                .trim()
                .replace(',', '.');

        try {
            Double res = Calculator.eval("0" + expression);

            resultField.setText(res.toString());
        } catch (Exception ex) {
            resultField.setText("Failed to calculate");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Input, inputField.getText().toString());
        outState.putString(Result, resultField.getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String input = savedInstanceState.getString(Input);
        String result = savedInstanceState.getString(Result);

        inputField.setText(input);
        resultField.setText(result);
    }

    public void onNumberClick(View view){
        Button button = (Button)view;
        inputField.append(button.getText());
    }

    public void onClearClick(View view) {
        resultField.setText("");
        inputField.setText("");
    }

    public void onRemoveLastClick(View view) {
        String current = inputField.getText().toString();

        if (!current.isEmpty()) {
            inputField.setText(current.substring(0, current.length() - 1));
        }
    }
}
