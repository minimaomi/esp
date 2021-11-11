package com.raqsoft.lib;

import java.io.InputStream;

import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.parallel.UnitContext;


public class MainTest {
	
	 public static void main(String[] args){
		try {
			InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
			ConfigUtil.load(inputStream, true);//ConfigUtil.load(inputStream, true);
			inputStream.close();
			
			Context ctx = new com.raqsoft.dm.Context();		 
			String s = "call(\"D:/works/shell/ftp/start.dfx\")";
			
			Expression exp = new com.raqsoft.expression.Expression(s);
			exp.calculate(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	 }

}
