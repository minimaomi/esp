package com.raqsoft.lib.zip;

import java.io.InputStream;
import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.parallel.UnitContext;

import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;

public class MainTest {

	 public static void main(String[] args){
		try {
			InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
			ConfigUtil.load(inputStream,ConfigUtil.FROM_ESPROC);//ConfigUtil.load(inputStream, true);
			inputStream.close();
			
			Context ctx = new com.raqsoft.dm.Context();		 
			String s = "call(\"D:/works/shell/unzip/demo.dfx\")";

			Expression exp = new com.raqsoft.expression.Expression(s);
			exp.calculate(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	 }

}