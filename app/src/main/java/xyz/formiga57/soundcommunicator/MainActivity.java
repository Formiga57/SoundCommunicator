package xyz.formiga57.soundcommunicator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private Button sendBtn;
    private EditText sendText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendBtn = findViewById(R.id.send_Btn);
        sendText = findViewById(R.id.textToSend);
        sendBtn.setOnClickListener(sendBtnClickListener);
    }
    View.OnClickListener sendBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextModulator textModulator = new TextModulator();
            textModulator.PlayText(sendText.getText().toString());
        }
    };
}