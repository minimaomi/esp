package com.raqsoft.lib.extract;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.app.config.RaqsoftConfig;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.parallel.UnitContext;

public class MainTest {
	static RaqsoftConfig m_rc = null;
	public static InputStream getConfigIS(String configFile) throws Exception {
		return UnitContext.getUnitInputStream(configFile);
	}
	
	public static RaqsoftConfig loadRaqsoftConfig() throws Exception {
		InputStream inputStream = getConfigIS("raqsoftConfig.xml");
		RaqsoftConfig raqsoftConfig = ConfigUtil.load(inputStream,ConfigUtil.FROM_ESPROC);//ConfigUtil.load(inputStream, true);
		inputStream.close();
		return raqsoftConfig;
	}
	
	private static void loadFunction(){
		try {
			m_rc = loadRaqsoftConfig();
			//ConfigUtil.loadExtLibs("demo",rc);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }

	 public static void main(String[] args){				 
		loadFunction();
		
		Context ctx = new com.raqsoft.dm.Context();		 
		String dfx = "call(\"D:/works/shell/web/crawl_stock.dfx\")";
		//dfx = "call(\"D:/works/shell/web/crawl_sina_zxtx.dfx\")";
		dfx = "call(\"D:/works/shell/web/p3.dfx\")";
				
		Expression exp = new com.raqsoft.expression.Expression(dfx);
		exp.calculate(ctx);
	 }
}
