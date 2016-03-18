package com.seafooler.lxc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Container implements Serializable {

	//private static final long serialVersionUID = -6097868122333778588L;
	private static final long serialVersionUID = 1L;

	private int 			id = -1;
	private String 			name; 
	private String 			ip = "10.0.4.2";
	private int				status;
	private int				portForDir = 4322;
	private int				mem = 50;
	private String 			cpuset = null;
	private	int				cpushare = 1024;
	private DBHelper		dbh = null;

	public Container() {
		this.status = ContainerState.UNKNOWN;
	}

	public Container(String name) {
		this.name	= name;
		this.status = ContainerState.UNKNOWN;
		//this.type	= detectType(this.name);
	}

	public Container(String name, String ip, int status) {
		this.name	= name;
		this.ip		= ip;
		this.status = status;
	}
	
	public Container(String name, String ip, int status, int mem, String cpuset, int cpushare) {
		this.name	= name;
		this.ip		= ip;
		this.status = status;
		this.mem 	= mem;
		this.cpuset = cpuset;
		this.cpushare = cpushare;
	}
	
	protected void finalize() throws Throwable{
		super.finalize();  
		dbh.dbClose();
        System.out.println("The Container Object was destroyed!");
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		try{
			if(this.dbh == null){
				dbh = new DBHelper();
			}
			ResultSet rs = dbh.dbSelect("select status from lxc where name = '" + this.name + "'"); 
			if(rs.next()){
				int status = rs.getInt("status");
				this.status = status;
			}
		} catch (SQLException e) {
            e.printStackTrace();
        } 
		return this.status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		try{
			if(this.dbh == null){
				dbh = new DBHelper();
			}
			dbh.dbUpdate("update lxc set status = " + status + " where name = '" + this.name + "'"); 
		}  catch (Exception e) {
            e.printStackTrace();
        } 
		this.status = status;
	}

	/**
	 * @return the portForPhone
	 */
	public int getPortForDir() {
		return portForDir;
	}

	/**
	 * @param portForPhone the portForPhone to set
	 */
	public void setPortForDir(int portForDir) {
		this.portForDir = portForDir;
	}

	/**
	 * @return the mem
	 */
	public int getMem() {
		return mem;
	}

	/**
	 * @param mem the mem to set
	 */
	public void setMem(int mem) {
		this.mem = mem;
	}
	
	/**
	 * @return the cpuset
	 */
	public String getCpuset() {
		return cpuset;
	}

	/**
	 * @param Cpuset the Cpuset to set
	 */
	public void setCpuset(String cpuset) {
		this.cpuset = cpuset;
	}
	
	/**
	 * @return the cpushare
	 */
	public int getCpushare() {
		return mem;
	}

	/**
	 * @param cpushare the cpushare to set
	 */
	public void setCpushare(int cpushare) {
		this.cpushare = cpushare;
	}
	
	

	public void describeContainer() {
		System.out.println(name + " " + id + " " + ip);
	}

/*	public void initializeContainer(Configuration config) {
		this.portForContainer	= config.getContainerPortForContainers();
		this.portForPhone	= config.getContainerPortForPhones();
	}*/

	/**
	 * Find which is the status of this clone and make it available for the phone.
	 * @return True if it was possible to make the clone available<br>
	 * False otherwise
	 */
	public boolean prepareContainer() {
		System.out.println("Preparing the LXC " + this.name);

		if (startContainer()) {
			System.out.println("Started the LXC " + this.name);
			this.status = ContainerState.ASSIGNING;
			return true;
		}
		else {
			System.err.println("Could not start the LXC" + this.name);
			this.status = ContainerState.STOPPED;
			return false;
		}
	}

	/**
	 * Start a container from stopped status
	 * @throws IllegalStateException
	 */
	public boolean startContainer() {

		//String out = executeCommand("VBoxManage startvm " + this.name + " --type headless");
		String out = executeCommand("lxc-start -n " + this.name + " -s lxc.network.ipv4=" + this.ip + "/24" );

		if (out.isEmpty()) 
			return true;

		return false;
	}
	
	public boolean startExecution() {
		
	}
	
	/**
	 * Create a container from scratch
	 * @throws IllegalStateException
	 */
	public boolean createContainer() {

		//String out = executeCommand("VBoxManage startvm " + this.name + " --type headless");
		String out = executeCommand("newlxc -n " + this.name + " --ip=" + this.ip);

		if (out.isEmpty()) 
			return true;

		return false;
	}

	/*
	public boolean resumeContainer() {
		executeCommand("lxc-unfreeze -n " + this.name);

		switch(getTheStateOfLXC()) {
		case ContainerState.STOPPED:
			return false;
		case ContainerState.PAUSED:
			return false;
		case ContainerState.RESUMED:
			return true;
		}

		return false;
	}

	public boolean pauseContainer() {
		
		executeCommand("lxc-freeze -n " + this.name);

		switch(getTheStateOfLXC()) {
		case ContainerState.STOPPED:
			return false;
		case ContainerState.PAUSED:
			return true;
		case ContainerState.RESUMED:
			return false;
		}

		return false;

	}

	public boolean stopContainer() {
		
		executeCommand("lxc-stop -n " + this.name + " -k");

		switch(getTheStateOfLXC()) {
		case ContainerState.STOPPED:
			return true;
		case ContainerState.PAUSED:
			return false;
		case ContainerState.RESUMED:
			return false;
		}

		return false;

	}
	*/
	
	
/*	public boolean releaseContainer() {
		
		this.setStatus(ContainerState.RESUMED);

		return true;

	}*/

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

	public static void printInfoAboutContainerName() {
		System.out.println("The name of the clone should start with vb- for VirtualBox clones, and with amazon- for Amazon clones.");
	}
}
