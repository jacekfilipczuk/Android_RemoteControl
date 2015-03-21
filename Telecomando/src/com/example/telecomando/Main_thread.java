
package com.example.telecomando;

/**************************************
* Author: Jacek Filipczuk
* 
* Project: Wireless remote control for a car robot through a tcp connection.
*
*
*Description: this is the thread where the comunication happens. When one button is pressed, it sends
*a String message through the socket and reads from it. The readed message is then displayed on the GUI.
*
*
*
***************************************/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;


public class Main_thread extends Thread	
{
	static final String TAG = "main_thread";
	
	/** Reference to the main activity*/
	MainActivity the_gui;	
	public Button button_right,button_left,button_up,button_down,button_stop,button_velocity,button_splitL,button_splitR;
	public ToggleButton button_connect;
	public TextView textFromServer,textVelocity;

	//***************************************************************   synchronization   ***************************************************************/
	/** true: thread should be stopped */
	boolean STOP 			= false;
	
	/** true: reconnect to tcp server. */
	boolean RECONNECT_TCP	= true;
	

	//*****************************************   TCP   ***************************************************************/
	/** Port opened for the TCP socket used to connect to the server.*/
	int port_TCP;
	
	/** Ip address given by main activity*/
	String ip_address_server;
	
	/** TCP socket*/
	Socket the_TCP_socket;
	
	/** IP address of the server.*/
	InetSocketAddress serverAddr_TCP;
	
	/** Used to write to the TCP socket.*/
	BufferedWriter out;
	
	/** Used to read on the TCP socket.*/
	BufferedReader input;
	
	/** Counter used to check the state of the TCP connection to the server. */
	int counter_TCP_check			= 0;
	
	/**  Check tcp connection to the server every 500 timesteps. */
	static int TCP_CHECK_RATE		= 500;
	
	/**  Timeout (ms) for connecting tcp socket */
	static int CONNECT_TIMEOUT 		= 5000;
	
	/**  Timeout (ms) when reading on tcp socket.*/
	static int READ_TIMEOUT 		= 20;	

	
	/** 
	 * Constructor main thread.
	 * */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public Main_thread(MainActivity gui)
	{
		ThreadPolicy tp = ThreadPolicy.LAX;
        StrictMode.setThreadPolicy(tp);
        
		
		Log.e("main_thread"," init ");
		the_gui = gui;
		ip_address_server = the_gui.IP_server;	
		port_TCP = the_gui.port_TCP;
		textFromServer = the_gui.tcp_response;
		textVelocity = the_gui.velocity_text;
		button_right = the_gui.button_right;
		button_left = the_gui.button_left;
		button_up = the_gui.button_up;
		button_down = the_gui.button_down;
		button_stop = the_gui.button_stop;
		button_velocity = the_gui.button_velocity;
		button_splitL = the_gui.button_splitL;
		button_splitR = the_gui.button_splitR;
		button_connect = the_gui.button_connect;
		
		/** Listener for input buttons, sends params and reads from server   */
		OnClickListener sendParamListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String button ="";
				try{
					switch (v.getId()) {
					case R.id.button_right:
						button="RIGHT";
						RECONNECT_TCP = !send_param_tcp("RIGHT");
						read_tcp();
						break;
					case R.id.button_left:
						button = "LEFT";
						RECONNECT_TCP = !send_param_tcp("LEFT");
						read_tcp();
						break;
					case R.id.button_up:
						button="UP";
						RECONNECT_TCP = !send_param_tcp("UP");
						read_tcp();
						break;
					case R.id.button_down:
						button="DOWN";
						RECONNECT_TCP = !send_param_tcp("DOWN");
						read_tcp();
						break;
					case R.id.button_velocity:
						button="VELOCITY";
						RECONNECT_TCP = !send_param_tcp("VELOCITY="+textVelocity.getText().toString());
						read_tcp();
						break;
					case R.id.button_splitL:
						button="SL";
						RECONNECT_TCP = !send_param_tcp("Y");
						read_tcp();
						break;	
					case R.id.button_splitR:
						button="SR";
						RECONNECT_TCP = !send_param_tcp("X");
						read_tcp();
						break;	
					default:
						button="STOP";
						RECONNECT_TCP = !send_param_tcp("STOP");
						read_tcp();
						break;
					}
				}
				catch(Exception e){
					Log.e("Button exception",button,e);
				}
			}
		};
		button_right.setOnClickListener(sendParamListener);
		button_left.setOnClickListener(sendParamListener);
		button_up.setOnClickListener(sendParamListener);
		button_down.setOnClickListener(sendParamListener);
		button_stop.setOnClickListener(sendParamListener);
		button_splitL.setOnClickListener(sendParamListener);
		button_splitR.setOnClickListener(sendParamListener);
		
		
	}
	

	/**  Starting the tcp connection   */
	@Override
	public final void run() 
	{	
		Log.e("main_thread"," start_tcp ");
		start_tcp();							// connect to the server
		//check_tcp();		//Function that checks if the connection is estabilished
		//stop_tcp();       //Function that stops the tcp connection and so the socket
		
		
	}

	/** stops the thread*/
	public synchronized void stop_thread()
	{
		STOP = true;
	}

	
	
	
	
	
	//********************************************************************************************************************************************************************/
	//***********************************************************************   TCP  *************************************************************************************/
	//********************************************************************************************************************************************************************/

	/** 
	 Starts the tcp connection, inizialize the writer and the reader to comunicate with the socket.
	 * */
	private void start_tcp()
	{
		while(RECONNECT_TCP==true && STOP==false)			//try to (re)connect
		{
			serverAddr_TCP = new InetSocketAddress(ip_address_server,port_TCP);
			try 
			{	
				Log.e("start_tcp"," trying to connect to socket ");
				the_TCP_socket = new Socket();	
				the_TCP_socket.connect(serverAddr_TCP, CONNECT_TIMEOUT);				//connect with timeout  (ms)
				//the_TCP_socket.setSoTimeout(READ_TIMEOUT);								//read with timeout  (ms) (if needed decomment)
				out = new BufferedWriter(new OutputStreamWriter(the_TCP_socket.getOutputStream()));
				input = new BufferedReader(new InputStreamReader(the_TCP_socket.getInputStream()));
				
				the_gui.runOnUiThread(new Runnable() //update gui on its own thread
				{
					@Override
					public void run() 
					{
						Log.e("read_tcp","Connessione riuscita");
						the_gui.button_connect.setChecked(true);
					}
				}); 
				
				Log.e("start_tcp"," trying to send_param ");
				RECONNECT_TCP = !send_param_tcp("TCP_CHECK");		//send parameters to the server. if problem, reconnect
				read_tcp();
				the_gui.runOnUiThread(new Runnable() //update gui on its own thread
				{
					@Override
					public void run() 
					{
						if(the_gui.button_connect.isChecked()==false) the_gui.button_connect.setChecked(true);                
					}
				}); 
			}
			catch(java.io.IOException e) 
			{
				RECONNECT_TCP = true;
				the_gui.runOnUiThread(new Runnable() //update gui on its own thread
				{
					@Override
					public void run() 
					{
						Log.e("read_tcp","Connessione fallita ");
						the_gui.button_connect.setChecked(false);
					}
				}); 
				
				Log.e("tcp","error connect: " + e);
			}
		}
	}

	/**
	 * Close input and output streams, and close the tcp socket.
	 * */
	private void stop_tcp()
	{
		try 
		{	
			if(out != null)		out.close();
			if(input != null)	input.close();
			the_TCP_socket.close();				//Close connection
		} 
		catch (IOException e) {	Log.e("tcp","error close: ", e);}
		Log.i("tcp","tcp client stopped ");
	}
	

	/**Sends params to the server * */
	private boolean send_param_tcp(String message)
	{
		Log.e("send_param"," trying to write to server");
		String message_TCP = new String();
		message_TCP = message;		
		
		try	
		{	
			out.write(message_TCP+"\r\n");
			out.flush();
			return true;
		}
		catch(Exception e)
		{
			Log.e(TAG, "error send_param_tcp" + e); 
			return false;
		}
	}
	
	/**
	 * Checks tcp connection by trying to send the message "TCP_CHECK" to the server every 500 timesteps.
	 * If disconnected, function will stop everything and try to reconnect to the server.
	 *  */	
	private void check_tcp()
	{
		counter_TCP_check++;
		if(counter_TCP_check==TCP_CHECK_RATE)	
		{
			counter_TCP_check=0;
			try 
			{
				out.write("TCP_CHECK\r\n");
				out.flush();				
			} 
			catch (IOException e) 					// if connection is lost	...cannot send/write on socket
			{	
				Log.e("tcp","error write: ", e); 
				RECONNECT_TCP = true;				
			} 		
			
			if(RECONNECT_TCP == true)
			{
				Log.i("tcp","reconnect");
				stop_tcp();							// close properly
				start_tcp();						// reconnect to server
			}
		}	
	}

	/**
	 * Read tcp socket and perform action corresponding to message. */	
	private boolean read_tcp()
	{
		String st=null;		
		boolean output=false;
		while(!output){
			try
			{				
				st = input.readLine();
			}
			catch (java.net.SocketTimeoutException e) {Log.e("read_timeout","Errore lettura dati da server");}	
			catch (IOException e) {	Log.e("tcp","error read: ", e);}
			output=true;
		}
		output=false;

		if(st != null)
		{	        	
			final String[]sss= st.split("/");
			
			output=true;
			the_gui.runOnUiThread(new Runnable() //update gui on its own thread
			{
				@Override
				public void run() 
				{
					Log.e("read_tcp"," Risposta dal server: "+sss[0]);
					the_gui.tcp_response.setText(sss[0]);
				}
			}); 
		}
		return output;
	}

	

}