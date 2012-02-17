package indovinailnumero.namespace;

import indovinailnumero.namespace.Main.Stato;

import java.util.Timer;
import java.util.TimerTask;

import android.R.string;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements MessageReceiver{
	private static final int SHOW_TOAST = 0;
	ConnectionManager connection;
	enum Stato{
		WAIT_FOR_START,WAIT_FOR_START_ACK,
		WAIT_FOR_SELECT,WAIT_FOR_BET,
		USER_BETTING,
		USER_SELECTING,WAIT_FOR_NUMBER_SELECTION
		}
	Stato statoCorrente;
	Handler handler;
	Button b;
	
	private String selectedNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		String nomeMio,nomeAvversario;
		nomeMio= getIntent().getExtras().getString("TextUtent");
		nomeAvversario= getIntent().getExtras().getString("TextAvvers");
		
		Button btn1 = (Button)findViewById(R.id.button1);
		Button btn2 = (Button)findViewById(R.id.button2);
		Button btn3 = (Button)findViewById(R.id.button3);
		
		btn1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			numberSelected(v);	
			}});
		btn2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				numberSelected(v);
			}});
		btn3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				numberSelected(v);
			}});
		
		TextView txMain = (TextView)findViewById(R.id.TxViewMain);
		txMain.setText(nomeMio+" Vs "+nomeAvversario);
		connection = new ConnectionManager(nomeMio, nomeAvversario, this);
		Timer timer = new Timer();
		TimerTask sendStart = new TimerTask() {
			
			@Override
			public void run() {
				
				if (statoCorrente==Stato.WAIT_FOR_START_ACK){
					connection.send("Start");
				}else{
					Log.d("ATTENZIONE","Sending START but the state is "+ statoCorrente);
				}
			}
		};
		//decido chi comincia
				if (nomeAvversario.hashCode()<nomeMio.hashCode()){
					//inizio per primo
					timer.schedule(sendStart, 1000,5000);
					statoCorrente=Stato.WAIT_FOR_START_ACK;
				}else{
					//inizia avversario e io aspetto il pacchetto
					statoCorrente=Stato.WAIT_FOR_START;
				}
			//creo handler 
				handler = new Handler(){
					@Override
					public void handleMessage(android.os.Message msg){
						switch(msg.what){
						case Main.SHOW_TOAST:
							Toast.makeText(Main.this, msg.getData().getString("toast"), Toast.LENGTH_LONG).show();
							break;
							default:
								super.handleMessage(msg);
						}
					}
				};
				
	}

	@Override
	public void receiveMessage(String msg) {
		// TODO Auto-generated method stub
		if(msg.equals("START")){
			if(statoCorrente==Stato.WAIT_FOR_START){
				//mando ACK
				connection.send("STARTACK");
				Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
				Bundle b = new Bundle();
				b.putString("toast", "scegli un numero");
				osmsg.setData(b);
				handler.sendMessage(osmsg);
				statoCorrente=Stato.USER_SELECTING;
			}else{
				Log.e("ATTENZIONE","Ricevuto START ma lo stato e' "+statoCorrente);
			}
		}
		//ricevo start_ack e aspetto che avversario gioca
		else if(msg.equals("STARTACK")){
			if(statoCorrente==Stato.WAIT_FOR_START_ACK){
				statoCorrente=Stato.WAIT_FOR_NUMBER_SELECTION;
			}else{
				Log.e("ATTENZIONE","Ricevuto STARTACK ma lo stato e' "+statoCorrente);
			}
		}else if(msg.startsWith("SELECTED")){
			if (statoCorrente==Stato.WAIT_FOR_NUMBER_SELECTION){
				selectedNumber=msg.split(" : ")[1];
				Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
				Bundle b = new Bundle();
				b.putString("toast", "indovina il numero");
				osmsg.setData(b);
				handler.sendMessage(osmsg);
				statoCorrente=Stato.USER_BETTING;
			}else{
				Log.e("ATTENZIONE","Ricevuto SELECTED ma lo stato e' :"+statoCorrente);
			}
		}else if(statoCorrente==Stato.USER_BETTING){
			String bet = b.getText().toString();
			connection.send("BET: "+bet);
			if (bet.equals(selectedNumber)){
				Toast.makeText(Main.this, "Bravo hai indovinato, ora tocca a te", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(Main.this,"Peccato non hai indovinato, ora tocca a te", Toast.LENGTH_LONG).show();
			}
			statoCorrente=Stato.USER_SELECTING;
		}else if(msg.startsWith("BET")){
			if (statoCorrente==Stato.WAIT_FOR_BET){
				String result = msg.split(" : ")[1];
				Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
				Bundle b = new Bundle();
				if (result.equals("Y"))
					b.putString("toast", "Hai Perso, il tuo avversario ha indovinato");
				else
					b.putString("toast","Hai vinto, il tuo avversario ha sbagliato");
				osmsg.setData(b);
				handler.sendMessage(osmsg);
				statoCorrente=Stato.WAIT_FOR_NUMBER_SELECTION;
			}else{
				Log.e("ATTENZIONE","Ricevuto SELECTED ma lo stato e' : "+statoCorrente);
			}
		}
	}

	
	public void numberSelected(View v) {
		Button b= (Button) v;
		//b.getText().toString();
		if (statoCorrente==Stato.USER_SELECTING){
			connection.send("SELECTED: "+b.getText().toString());
			statoCorrente=Stato.WAIT_FOR_BET;
		}
	}
}
