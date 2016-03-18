package com.seafooler.lxc;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * This class helps to acquire a candidate container for task execution
 * @author seafooler
 *
 */
public class ContainerHelper {
	private DBHelper dbh;
	
	public ContainerHelper(DBHelper dbh){
		this.dbh = dbh;
	}
	
	public Container findAvailableContainer(){
		try {
			dbh.dbUpdate("lock tables lxc write"); 
			String sql = "select * from lxc where status = '0' limit 1";
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
				
			}
			else {
				dbh.dbUpdate("unlock tables");
//				Container con = new Container();
				return null;
			}
		} catch (SQLException e) {
			// TODO: handle exception
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
