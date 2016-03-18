package com.seafooler.lxc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is responsible to start a given container to execute the task
 * @author seafooler
 *
 */



public class ContainerHandler implements Runnable{

	private Container cont;
	
	public ContainerHandler(Container cont){
		this.cont = cont;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		boolean preres = cont.prepareContainer();
		if(preres)
			cont.
		
	}
	
	
}
