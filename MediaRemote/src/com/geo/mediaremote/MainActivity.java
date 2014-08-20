package com.geo.mediaremote;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	
	WakeLock wL;
	Handler handler;
	private Socket socket;
	Button Play,Previous,Next,VolumeUp,VolumeDown,Reconnect,MouseOpen;
	WifiInfo wifiInfo;
	WifiManager wifiMgr;
	TextView status;
	private static final int SERVERPORT = 5000;
	String SERVER_IP;
	int wififlag = 0; //Is set to 1 when Wi-Fi is available
	
	
	/**
	 * Initialization and First Connection
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	PowerManager pM = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wL = pM.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Anything to descibe wakelock"); //Keeps screen on
        
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initializing TextView
        status = (TextView) findViewById(R.id.status);
        
        //Initializing Buttons
        Play = (Button) findViewById(R.id.bPlay);
        Previous = (Button) findViewById(R.id.bPrevious);
        Next = (Button) findViewById(R.id.bNext);
        VolumeUp = (Button) findViewById(R.id.bVup);
        VolumeDown = (Button) findViewById(R.id.bVdwn);
        Reconnect = (Button) findViewById(R.id.bReconnect);
        MouseOpen = (Button) findViewById(R.id.bMouse);
        
       
        
        //Initializing OnClickListener 
        Play.setOnClickListener(this);
        Next.setOnClickListener(this);
        Previous.setOnClickListener(this);
        VolumeUp.setOnClickListener(this);
        VolumeDown.setOnClickListener(this);
        Reconnect.setOnClickListener(this);
        MouseOpen.setOnClickListener(this);
        
        wL.acquire();
        
        if(checkwifi()) new Thread(new ClientThread()).start();
        
        
        
        
    }

	
    /**
     * Checks if WiFi is available and sets the SERVER_IP if Yes
     * @return Provides boolean true if WIFI is present and IP address is found.. Else false
     */
	private boolean checkwifi() {
		// TODO Auto-generated method stub
		
		//Initializing WiFi
		wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE); //Creates a Wifi Variable
        wifiInfo = wifiMgr.getConnectionInfo();  //Gets current wifi connection Info
        
		if(wifiInfo.getNetworkId() != -1)
		{
			status.setText("Wifi Connected");
			int ip = wifiInfo.getIpAddress();
			String ipAddress = Formatter.formatIpAddress(ip);
			status.setText(ipAddress);
            int i;
            
            for(i=ipAddress.length()-1;i>0;i--)
            {
            	if(ipAddress.charAt(i) == '.')
            	{
            		break;
            	}
            }
            String newIp = ipAddress.substring(0, i+1);
            newIp = newIp + "1";
            status.setText(newIp);
            SERVER_IP = newIp;
			return true;
		}
		else
		{
			status.setText("No wifi connection");
			return false;
		}
	}
	
	/**
	 * OnClickAction listener
	 */
	@Override
	public void onClick(View arg0)  {
		// TODO  Auto-generated method stub
		switch(arg0.getId())
		{
		case R.id.bPlay:
	
			if(sendMessage("Play") == 0)
				if(checkwifi()) new Thread(new ClientThread()).start();
			
			break;
		case R.id.bPrevious:
		
			if(sendMessage("Previous") == 0)
			if(checkwifi()) new Thread(new ClientThread()).start();

			break;
		case R.id.bNext:
			if(sendMessage("Next") == 0)
			if(checkwifi()) new Thread(new ClientThread()).start();
			
			break;
			
		case R.id.bVup:
			if(sendMessage("Vup") == 0)
			if(checkwifi()) new Thread(new ClientThread()).start();
			break;
			
		case R.id.bVdwn:
	
			if(sendMessage("Vdwn") == 0)
			if(checkwifi()) new Thread(new ClientThread()).start();

			break;
			
		case R.id.bReconnect:
			if(sendMessage("Checking") == 0)
			if(checkwifi()) new Thread(new ClientThread()).start();	
			break;
			
		case R.id.bMouse:
			Intent a = new Intent(MainActivity.this,MouseView.class);
			startActivity(a);
			break;
		
		}
		
	}
	
	/**
	 * Function to send message. Closes socket on send error.
	 * @param str Message to be sent
	 * @return 0 on Print Error. 1 on Success
	 */
	private int sendMessage(String str) {
		// TODO Auto-generated method stub
		try {
			
			
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())),
					true);
			out.println(str);
			out.flush();
			status.setText(str);
			if(out.checkError())
			{
				socket.close();
				out.close();
				return 0;
				
			}
			else
			{
				return 1;
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	/**
	 * Client Thread Executed to initialize the Socket
	 * 
	 *
	 */
	class ClientThread implements Runnable {

		@Override
		public void run() {
			
			status.setText("Thread Running");

			try {
				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

				socket = new Socket(serverAddr, SERVERPORT);
				
				
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * OnDestroy and OnPause classes sends messages for the Server to close socket
	 */
		
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		sendMessage("Exit");
		wL.release();
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sendMessage("Exit");
	}



	
    
    
}
