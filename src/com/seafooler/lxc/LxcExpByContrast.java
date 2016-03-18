package com.seafooler.lxc;

public class LxcExpByContrast implements Runnable {

//	private int nrReservedCont = 10; // number of containers reserved
	private int nrTask = 100; //number of tasks
	
	public void run() {
		// TODO Auto-generated method stub
		//reserve the containers
		DBHelper dbh = new DBHelper();
		ReserveContainer rc = new ReserveContainer(dbh, nrTask);
		Container cont = null;
		ContainerHelper contHelper = new ContainerHelper(dbh);
		
		for(int i=0; i<nrTask; i++)
		{
			try {
				dbh.dbUpdate("lock tables lxc write");
				cont = contHelper.findAvailableContainer();
				if(cont != null){
					new Thread(new ContainerHandler(cont)).start();
				} else {
					System.out.println("Error!!!!!!!!!!!");
					return;
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			Container con = ContainerHandler
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		new Thread( new LxcExpByContrast()).start();

	}

}				


