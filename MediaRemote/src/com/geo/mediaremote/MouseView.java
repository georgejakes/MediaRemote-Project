package com.geo.mediaremote;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class MouseView extends Activity implements OnTouchListener {
	
	WifiInfo wifiInfo;
	WifiManager wifiMgr;
	private Socket socket;
	private static final int SERVERPORT = 5000;
	String SERVER_IP;
	int wififlag = 0; //Is set to 1 when Wi-Fi is available
	int x,y,tempX = 0,tempY = 0;
	MouseViewSurface ourSurfaceView;
	int connectIt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ourSurfaceView = new MouseViewSurface(this);
		ourSurfaceView.setOnTouchListener(this);
		
		x = y = 0;
		connectIt = 0;
		
		setContentView(ourSurfaceView);
		
		
	}
	/**
	 * Check Wifi statement
	 * @return
	 */
	private boolean checkwifi() {
		// TODO Auto-generated method stub
		
		//Initializing WiFi
		wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE); //Creates a Wifi Variable
        wifiInfo = wifiMgr.getConnectionInfo();  //Gets current wifi connection Info
        
		if(wifiInfo.getNetworkId() != -1)
		{
			
			int ip = wifiInfo.getIpAddress();
			String ipAddress = Formatter.formatIpAddress(ip);
			
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
            
            SERVER_IP = newIp;
			return true;
		}
		else
		{
			
			return false;
		}
	}

	/**
	 * Runnable for Touch Event
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		//Frame Rate adjustment
		try {
			Thread.sleep(20); //Sleeps every 50ms thus giving a fixed frame rate 
			//desired value attained by ---> (1000/Required FPS)
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		x = (int)event.getX();
		y = (int)event.getY();
		
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			x = y = 0;
		}
		
		return true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		ourSurfaceView.pause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		ourSurfaceView.resume();
	}

	public class MouseViewSurface extends SurfaceView implements Runnable {

		
		SurfaceHolder ourHolder;
		Thread ourThread = null;
		boolean isRunning = false;
		
		public MouseViewSurface(Context context) {
			super(context);
			ourHolder = getHolder();
			ourThread = new Thread(this);
			ourThread.start();
			
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			
			int red,green,blue;
			Random newRand = new Random();
			red = newRand.nextInt(255);
			green = newRand.nextInt(255);
			blue = newRand.nextInt(255);
			
			if(connectIt == 0)
			{
				//Wifi Connection and sOCKET Creation
				if(checkwifi())
				{
					try {
						InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

						socket = new Socket(serverAddr, SERVERPORT);
						connectIt = 1;
						
						
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			
			// TODO Auto-generated method stub
			String str;
			while(isRunning)
			{
				//Following Shows the Drawing on Canvas. Also passes the value if the touch 
				//was intended for motion of mouse or for Click
				
				if(!ourHolder.getSurface().isValid())
					continue;
					Canvas canvas = ourHolder.lockCanvas(); //So no other activities access the canvas
					canvas.drawRGB(02, 02, 150); //background
					Paint paint = new Paint();
					red+=1; red%=255;
					green+=1; green%=255;
					blue+=1; blue%=255;
					
					paint.setColor(Color.rgb(red, green, blue));
					canvas.drawRect(0, canvas.getHeight() - 200, canvas.getWidth(), canvas.getHeight(), paint);
					
					if(x!=tempX && y!=tempY && x!=0 && y!=0)
					{
						tempX = x;
						tempY = y;
						if(y > canvas.getHeight() - 200)
						{
							str = "Click";
						}
						else
						{
							str = "mouse ";
							str += x;
							str += " " + y + " ";
						}
						
						try {
							PrintWriter out;
							out = new PrintWriter(new BufferedWriter(
									new OutputStreamWriter(socket.getOutputStream())),
									true);
							out.println(str);
							out.flush();
							x = y = 0;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					
					ourHolder.unlockCanvasAndPost(canvas);
			}
		}
		
		public void resume(){
			isRunning = true;
			ourThread = new Thread(this); //Creates a new thread and uses Run in this class
			ourThread.start();
		}
		
		/**
		 * On Pause the Thread is closed and an Exit message is sent to the Server
		 */
		public void pause(){
			isRunning = false;
			while(true){
				try {
					String str = "Exit";
					PrintWriter out;
					out = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream())),
							true);
					out.println(str);
					out.flush();
					
					ourThread.join();   //blocks the thread
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				break;
			}
			
			ourThread = null;
		}
		
	}

	

}
