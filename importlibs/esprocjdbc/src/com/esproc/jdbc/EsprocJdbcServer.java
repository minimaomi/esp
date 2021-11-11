package com.esproc.jdbc;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.parallel.UnitContext;

import py4j.GatewayServer;

public class EsprocJdbcServer {
	private AppJdbc m_app;
	 
    public EsprocJdbcServer() {
    	loadFunction();
    	m_app = new AppJdbc();
    }
    
    public AppJdbc getApp() {
        return m_app;
    }
    
    private void loadFunction(){
		try {
			InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
			ConfigUtil.load(inputStream, true);//ConfigUtil.load(inputStream, true);
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
   
    public static void main(String[] args) throws UnknownHostException {
    	if (1==1){
    		int port = 25333;
    		String ip = "127.0.0.1";
	        GatewayServer gatewayServer = null;
	        if (args.length>=1){
	        	ip = args[0];
	        }
	        if (args.length>=2){
	        	port = Integer.parseInt(args[1]);
	        }
   		    InetAddress addr = InetAddress.getByName(ip);
   		    System.out.println("Server info: ip="+ip+"; port="+port);
	        gatewayServer = new GatewayServer(new EsprocJdbcServer(), port, port, addr, addr, 0, 0, null);
	        gatewayServer.start();
	        System.out.println("EsprocJdbc Server Started");
    	}else{
    		EsprocJdbcServer esp = new EsprocJdbcServer();
    		AppJdbc app = esp.getApp();
    		String dfx = "call test";
    		dfx = "select * from aaaa4.txt";
    		dfx = "call test";
    		dfx = "call storage";
    		dfx = "call createSub";
    		if (1==1){
    			//app.query("call tadd(?, ?)", 3, 5);
    			List<Object> result = app.mquery(dfx);
    			System.out.println(result);
    		}else{    		
	    		app.cursor(dfx);
	    		int total = 0;
	    		while(app.hasNext()){
	    			List<Object[]> ls = app.fetch(5);
	    			total += ls.size();
	    			System.out.println("cur="+ls.size()+"; total="+total);
	    		}
    		}
    	}
    }
}
