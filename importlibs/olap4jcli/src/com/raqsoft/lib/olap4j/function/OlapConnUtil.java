package com.raqsoft.lib.olap4j.function;
//https://sourceforge.net/p/olap4j/discussion/577988/thread/93729c12/
import java.sql.DriverManager;  
import java.sql.SQLException;  
  
import org.olap4j.OlapConnection;  
import org.olap4j.OlapException;  
import org.olap4j.OlapWrapper;

import com.raqsoft.common.Logger;  
  
public class OlapConnUtil {  
  
    private static final String DRIVER_CLASS_NAME = "org.olap4j.driver.xmla.XmlaOlap4jDriver";  
  
    static {  
        try {  
            Class.forName(DRIVER_CLASS_NAME);  
        } catch (ClassNotFoundException e) {  
        	Logger.error(e.getStackTrace());  
        }  
    }  
  
	public static OlapConnection getOlapConn(String server, String catalog,  
            String user, String password) throws SQLException {  
        String url = "jdbc:xmla:Server=" + server;  
  
        OlapConnection conn = null;  
        try {  
            conn = (OlapConnection) DriverManager.getConnection(url, user, password);  
        } catch (SQLException e) {  
            throw e;  
        }  
  
        if (conn != null) {  
            try {
            	if (catalog!=null){
            		OlapWrapper wrapper = (OlapWrapper)conn;
                    
            		wrapper.unwrap(OlapConnection.class);
            		conn.setCatalog(catalog);  
            	}
            } catch (OlapException e) {  
            	Logger.error(e.getStackTrace());
                throw e;  
            }  catch(Throwable t) {
            	conn.close();
            	Logger.error(t.getStackTrace());
            	return null;
            }
  
            OlapWrapper wrapper = (OlapWrapper) conn;  
            OlapConnection olapConn = null;  
            try {  
                olapConn = wrapper.unwrap(OlapConnection.class);  
                return olapConn;  
            } catch (SQLException e) {  
            	Logger.error(e.getStackTrace());
            }  
        }  
  
        return null;  
    }  
  
}  