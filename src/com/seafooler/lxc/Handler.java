package com.seafooler.lxc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.jason.lxcoff.lib.ControlMessages;

import de.tlabs.thinkAir.dirService.Container;
import de.tlabs.thinkAir.dirService.ContainerState;


public class Handler implements Runnable {

	//lxc connect socket
	private Socket 					conSocket;
	private InputStream				conis;
	private OutputStream			conos;
	private ObjectOutputStream		conoos;
	private ObjectInputStream		conois;
		
	private String					handlerID;
	static 	String 					appName;						// the app name sent by the phone	
	static	String 					apkFilePath;					// the path where the apk is installed
	static 	String 					objToExecute = null;	// the object to be executed sent by the phone
	static 	String 					methodName;						// the method to be executed
	static 	Class<?>[] 				pTypes;							// the types of the parameters passed to the method
	static 	Object[] 				pValues;						// the values of the parameteres to be passed to the method
		
	private Container				worker_container = null;
		
	private final int 				BUFFER = 8192;
		
	private DBHelper				dbh;
	private static String			logFileName = null;
	private static FileWriter 		logFileWriter = null;
	private String 					RequestLog = null;
	
	public Handler(DBHelper dbh, int handlerID){
		this.dbh = dbh;
		this.handlerID = handlerID;
		this.logFileName = "/root/dirservice/ExecRecord/execrecord.txt"; 
		File needlog = new File("/root/dirservice/ExecRecord/needlog");
		
		if(needlog.exists()){
			try {
				File logFile = new File(logFileName);
				logFile.createNewFile(); // Try creating new, if doesn't exist
				logFileWriter = new FileWriter(logFile, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void run() {
		// TODO Auto-generated method stub
		long startTime , dura;
		boolean connected;
		HashMap<String, String> result;
		
		
		//connect to container
		appName = ;
		apkFilePath = ControlMessages.DIRSERVICE_APK_DIR + appName + ".apk";
		
		
		System.out.println("Execute handlerID:"+handlerID);
		
		connected = false;
		
		startTime = System.nanoTime();
		//when the old worker_container can't work now, we need find a new available container
		if(this.worker_container == null || this.worker_container.getStatus() != ContainerState.RESUMED){
			do{
				this.worker_container = findAvailableContainer();
				
				boolean preres = worker_container.prepareContainer();
				
				//while starting the container,we should wait for container to connect
				if(preres){
					connected = waitForContainerToAuthenticate(worker_container);
				}
				
			}while(!connected);
		}
		//otherwise we can still use the old container
		else{
			do{
				connected = waitForContainerToAuthenticate(worker_container);
			}while(!connected);
		}
		
		dura = System.nanoTime() - startTime;
		
		//资源准备
		this.RequestLog += dura/1000000 + " ";
		
		
	}
	
	
	private Container findAvailableContainer() {
		try{
			dbh.dbUpdate("lock tables lxc write"); 
			String sql = "select * from lxc where status in (2,1,0) order by status desc limit 1 ";
			ResultSet rs = dbh.dbSelect(sql);
			
			if(rs.next()){
				String name = rs.getString("name");
				String ip = rs.getString("ip");
				int status = rs.getInt("status");
				int mem = rs.getInt("mem");
				String cpuset = rs.getString("cpuset");
				int cpushare = rs.getInt("cpushare");
				//only update status in database so that nobody will choose this lxc anymore, 
				//while the container object has old status for prepareContainer deciding what operation to do
				dbh.dbUpdate("update lxc set status = " + ContainerState.ASSIGNING + " where name = '" + name + "'"); 
				dbh.dbUpdate("unlock tables");
				
				if(ip == null){
					ip = getAvailableIp();
					dbh.dbUpdate("update lxc set ip = '" + ip + "' where name = '" + name + "'");
				}
				
				Container con = new Container(name, ip, status, mem, cpuset, cpushare);
				return con;
			}else{
				dbh.dbUpdate("unlock tables");
				Container con = startNewContainer();
				return con;
				
			}
		} catch (SQLException e) {
            e.printStackTrace();
        } 
		return null;
		
	}
	
	private String getAvailableIp(){
		String ip = null;
		try{
			//find available ip for container
			dbh.dbUpdate("lock tables ip write"); 
			ResultSet rs = dbh.dbSelect("select ip from ip where inuse=0 limit 1");
			if(rs.next()){
				ip = rs.getString("ip");
			}
			
			if(ip != null){
				dbh.dbUpdate("update ip set inuse=1 where ip='" + ip + "'");
			}
			
			dbh.dbUpdate("unlock tables");
			return ip;
		} catch (SQLException e) {
            e.printStackTrace();
        } 
		
		return null;
		
	}

}
