package com.seafooler.lxc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


/**
 * This class is used to reserve containers
 * @author seafooler
 *
 */

public class ReserveContainer {
	
	private DBHelper dbh;
	private int nrContainerToReserve; //number of containers to be reserved
	
	public ReserveContainer(DBHelper dbh,int num){
		this.dbh = dbh;
		this.nrContainerToReserve = num;
	}
	
	public boolean reserve(){
		//check if there is enough(>=num) containers reserved
		int nrContainerOld=0; //number of containers having been reserved before
		
		try {
			dbh.dbUpdate("lock tables lxc write");
			String sql = "select count(*) from lxc where status = '0'";
			ResultSet rs = dbh.dbSelect(sql);
			rs.next();
			nrContainerOld = rs.getInt("rowCount");
			dbh.dbUpdate("unlock tables");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		//if there is enough containers reserved, return
		if(nrContainerOld >= nrContainerToReserve)
			return true;
		else {//else, we have to create new containers and update the database 
			boolean creCon;
			for(int i=0; i<nrContainerToReserve-nrContainerOld; i++){
				creCon = createNewContainer();
				if(!creCon)
					return false;
			}
			return true;
			
		}
	}
	
	//create new container and update the database
	private boolean createNewContainer(){
		String name = UUID.randomUUID().toString();
		String ip = getAvailableIP();
		String out = executeCommand("newlxc -n " + name + " --ip=" + ip);
		if (out.isEmpty()){
			String sql = "insert into lxc (name, ip, status) values ('" + name + "','" + ip + "', " + ContainerState.STOPPED +")";
			dbh.dbInsert(sql);
			dbh.dbUpdate("unlock tables");
			return true;
		}

		return false;	
	}
	
	private String getAvailableIP(){
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
	
	
	private String executeCommand(String command) {

		try {
			Process p = Runtime.getRuntime().exec(command);
			// you can pass the system command or a script to exec command.
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// read the output from the command
			StringBuilder sb = new StringBuilder();
			String s = "";
			while ((s = stdInput.readLine()) != null) {
//				System.out.println("Std OUT: "+s);
				sb.append(s);
			}

			while ((s = stdError.readLine()) != null) {
				System.out.println("Std ERROR : "+s);
			}

			return sb.toString();

		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}
	
	
}
