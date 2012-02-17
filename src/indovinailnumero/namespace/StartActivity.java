package indovinailnumero.namespace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class StartActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        final EditText utente = (EditText)findViewById(R.id.eTextUtente);
		final EditText avversario = (EditText)findViewById(R.id.eTextAvversario);
        Button btnPlay = (Button)findViewById(R.id.button1);
        btnPlay.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View V) {
				Intent intent = new Intent(StartActivity.this,Main.class);
				intent.putExtra("TextUtent", utente.getText().toString());
				intent.putExtra("TextAvvers", avversario.getText().toString());
				startActivity(intent);
			}
		});
    }
}