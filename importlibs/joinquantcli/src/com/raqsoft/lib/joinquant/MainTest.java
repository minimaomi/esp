package com.raqsoft.lib.joinquant;

import java.io.FileNotFoundException;
import java.io.InputStream;
import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.app.config.RaqsoftConfig;
import com.raqsoft.parallel.UnitContext;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;

public class MainTest {
	public static RaqsoftConfig loadRaqsoftConfig() throws Exception {
		InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
		RaqsoftConfig raqsoftConfig = ConfigUtil.load(inputStream,ConfigUtil.FROM_ESPROC);//ConfigUtil.load(inputStream, true);
		inputStream.close();
		return raqsoftConfig;
	}
	
	private static void loadFunction(){
		try {
			loadRaqsoftConfig();
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
		//Expression exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/joinquant/fq.dfx\")");
		Expression exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/joinquant/download.dfx\")");
		//Expression exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/joinquant/jsonStr.dfx\")");
		exp.calculate(ctx);
	 }
	 

	

}
