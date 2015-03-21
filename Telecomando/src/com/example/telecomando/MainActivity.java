package com.example.telecomando;

/**************************************
* Author: Jacek Filipczuk
* 
* Project: Wireless remote control for a car robot through a tcp connection.
*
*
*Description: this is the main class that inizialize the GUI and starts the comunication with the server.
*
*
*
***************************************/

import android.support.v7.app.ActionBarActivity;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;


@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MainActivity extends ActionBarActivity {

	
	
    public String IP_server;
	public int port_TCP;
	public ToggleButton button_connect;
	public Button button_right,button_left,button_up,button_down,button_stop,button_velocity,button_splitL,button_splitR;
	public Context the_gui;
	public EditText ip_text, port_text,velocity_text;
	public TextView tcp_response;
	public Main_thread the_main_thread;
	

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        
        
        the_gui = this.getApplicationContext();
        
        //set the thread policy (avoid some sdk target errors)
        ThreadPolicy tp = ThreadPolicy.LAX;
        StrictMode.setThreadPolicy(tp);
        
        ip_text = (EditText) findViewById(R.id.ip_address);
        ip_text.setText("192.168.100.1");
        port_text = (EditText) findViewById(R.id.port);
        port_text.setText("25000");
        velocity_text = (EditText) findViewById(R.id.text_velocity);
        velocity_text.setText("Default 90%");
        tcp_response = (TextView)findViewById(R.id.tcp_response);
        tcp_response.setText("Response server");
        
        button_right = (Button) findViewById(R.id.button_right);
        button_left = (Button) findViewById(R.id.button_left);
        button_up = (Button) findViewById(R.id.button_up);
        button_down = (Button) findViewById(R.id.button_down);
        button_stop = (Button) findViewById(R.id.button_stop);
        button_velocity = (Button) findViewById(R.id.button_velocity);
        button_splitL = (Button) findViewById(R.id.button_splitL);
        button_splitR = (Button) findViewById(R.id.button_splitR);
        
        button_connect = (ToggleButton) findViewById(R.id.connectButton);
        button_connect.requestFocus();
        button_connect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) 
			{
				//Get the ip address of the server and the port number
				IP_server = ip_text.getText().toString();
				String port = port_text.getText().toString(); 
				try
				{
					port_TCP = Integer.parseInt(port);
				}
				catch(java.lang.NumberFormatException e)
				{
					AlertDialog alertDialog;
					alertDialog = new AlertDialog.Builder(the_gui).create();
					alertDialog.setTitle("Error port");
					alertDialog.setMessage("enter a number  \n\n (press back to return)");
					alertDialog.show();		
					port_TCP = -1;
					if(button_connect.isChecked()) button_connect.toggle();
				}
				//if everythong goes fine, start comunication with server
				if(!IP_server.isEmpty() && port_TCP!=-1) start_main_thread();
				else  stop_main_thread();
			}
		});
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void start_main_thread()
	{		
		the_main_thread = new Main_thread(this);
		the_main_thread.start();				
	}
    
    public void stop_main_thread()
	{
		if(the_main_thread != null) the_main_thread.stop_thread();	
		the_main_thread=null;
	}
    
    
}
