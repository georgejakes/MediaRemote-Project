/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaserver;

/**
 *
 * @author Shell
 */
// File Name MediaServer.java

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;


/**
 * 
 * @author Shell
 */

public class MediaServer extends Thread
{
   mediaGUI newGUI;
   Robot robot;
   javax.swing.JButton bStop;
   StopEvent stopit;
   int endAll = 0; //If endAll is changed to 1.. then execution is stopped (For Button)
   int a = 0,b = 0, c = 0, i;
   
   String xpos = null,ypos = null;
   private ServerSocket serverSocket;
   
   /**
    * 
    * @param port Specifies the port to be opened for connection
    * @throws IOException 
    */
   public MediaServer(int port) throws IOException
   {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(9000000);
   }

   /**
    * Starts the program server run.
    */
   public void run() 
   {
      
      newGUI = new mediaGUI();  //starts an instance of the GUI
      bStop = newGUI.getButton();   //aquires Button
      stopit = new StopEvent(); //Acquires action listener
      bStop.addActionListener(stopit);  //sets onClickListener
      while(true)
      {
          if(endAll == 1)
          {
              continue;
          }
          
         try
         {
            //System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            newGUI.setText("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();
            newGUI.setText("Just connected to " + server.getRemoteSocketAddress());
            
            DataInputStream in = new DataInputStream(server.getInputStream());
            int X=0, Y=0, sX = 0, sY = 0, fX = 0, fY = 0, dX = 0, dY = 0,mouseX = 0,mouseY = 0; //Initialization for Mouse Events
            PointerInfo pointInfo; //To get Mouse Pointer Info from Screen
            Point point;    //To get point info
            // int attempts = 0;
            while(true)
            {
                if(endAll == 1)
                {
                    break;
                }
                
                String str = in.readLine(); //reading Input from Client
                //if(str.isEmpty())
                //{
                //    attempts++;
                //}
                //else
                //{
                    if(str.isEmpty())
                        continue;
                    
                    newGUI.setText(str);
                    dotherobot(str);    //Passing string to Robot function to find any possible match
                    //Otherwise if string has Exit... then closes current connection and waits for new Socket connections
                    if("Exit".equals(str))
                    {
                        server.close();
                        break;
                    }
                    //If string contains Mouse... Starts Mouse Activity
                    if(str.contains("mouse"))
                    {
                        //Finding  'spaces' in given message format and extracting numerical positions
                        for(i=0;i < str.length();i++)
                        {
                            if(str.charAt(i) == ' ')
                            {
                                a = i; break;
                            }
                            
                        }
                        i++;
                        for(;i < str.length();i++)
                        {
                            if(str.charAt(i) == ' ')
                            {
                                b = i; break;
                            }
                        }
                        i++;
                        for(;i < str.length();i++)
                        {
                            if(str.charAt(i) == ' ')
                            {
                                c = i; break;
                            }
                        }
                        
                        
                        xpos = str.substring(a+1,b);
                        ypos = str.substring(b+1, c);
                        
                        //Mouse point position is found
                        pointInfo = MouseInfo.getPointerInfo();
                        point = pointInfo.getLocation();
                        mouseX = (int) point.getX();
                        mouseY = (int) point.getY();
                        
                        
                        X = Integer.parseInt(xpos);
                        Y = Integer.parseInt(ypos);
                        
                        //Robot initialized
                        robot = new Robot();
                        
                        //Conditional statements for mouse movement
                        if(sX == 0 && sY == 0)
                        {
                            sX = X;
                            sY = Y;
                            continue;
                        }
                        else if (sX == X && sY == Y)
                        {
                            continue;
                        }
                        else
                        {
                            fX = X;
                            fY = Y;
                            dX = fX - sX;
                            dY = fY - sY;
                            
                            newGUI.setText("sX: "+sX+" sY: "+sY);
                            newGUI.setText("fX: "+fX+" fY: "+dY);
                            newGUI.setText("dX: "+dX+" dY: "+dY);
                            
                           
                            
                            mouseX = mouseX + dX * 3;
                            mouseY = mouseY + dY * 3;
                            if(dX < 30 && dY < 30 && dY > -30 && dX > -30)
                            {
                                robot.mouseMove(mouseX, mouseY);
                            }
                                
                            
                            dX = 0;
                            dY = 0;
                            sX = fX;
                            sY = fY;
                            
                        }
                        
                    
                    
                        
                    }
                      
                //}
                
                
                
               /* if(attempts > 1000)
                {
                    break;
                }
                else
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MediaServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } */
            }
            
            
            
         }catch(SocketTimeoutException s)
         {
            newGUI.setText("Socket timed out!");
            break;
         }catch(IOException e)
         {
            e.printStackTrace();
            break;
         } catch (AWTException ex) {
              Logger.getLogger(MediaServer.class.getName()).log(Level.SEVERE, null, ex);
              break;
          }
      }
   }
   public static void main(String [] args)
   {
      int port = 5000;
      try
      {
          //Starting Thread...
         Thread t = new MediaServer(port);
         t.start();
      }catch(IOException e)
      {
         e.printStackTrace();
         
      }
   }
   
   /**
    * 
    * @param str The Action command to be done by the Robot
    */
    private void dotherobot(String str) {
        try {
            robot = new Robot();
            switch(str)
            {
                case "Play":
                    robot.keyPress(KeyEvent.VK_SPACE);
                    break;
                case "Previous":
                    robot.keyPress(KeyEvent.VK_LEFT);
                    break;
                case "Next":
                    robot.keyPress(KeyEvent.VK_RIGHT);    
                    break;
                case "Vup":
                    robot.keyPress(KeyEvent.VK_UP);    
                    break;
                case "Vdwn":
                    robot.keyPress(KeyEvent.VK_DOWN);  
                    break;
                case "Click":
                    robot.mousePress(MouseEvent.BUTTON1_MASK);
                    robot.mouseRelease(MouseEvent.BUTTON1_MASK);
                    robot.delay(500);
                    break;
                case "Image":
                    Rectangle rectngl = new Rectangle();
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    rectngl.height = (int) screenSize.getHeight();
                    rectngl.width = (int) screenSize.getWidth();
                    BufferedImage screenCapture = robot.createScreenCapture(rectngl);
                    break;
                           
                
                
            }
            
        } catch (AWTException ex) {
            Logger.getLogger(MediaServer.class.getName()).log(Level.SEVERE, null, ex);
            newGUI.setText("Robot Exception...");
        }
        
    }
    /**
     * Function for Buttons Action Listener
     */
    private class StopEvent implements ActionListener
	{
		
		public void actionPerformed(ActionEvent e)
		{
                    if(endAll == 1)
                    {
                        newGUI.setText("End All to 0");
                        endAll = 0;
                    }
                    else
                    {
                        newGUI.setText("End All to 1");
                        endAll = 1;
                    }
		}
	}
}
