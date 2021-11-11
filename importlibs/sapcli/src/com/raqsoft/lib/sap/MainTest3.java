package com.raqsoft.lib.sap;

import java.io.InputStream;
import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.parallel.UnitContext;

import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;

public class MainTest3 {

	 public static void main(String[] args){
		try {
			InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
			ConfigUtil.load(inputStream,ConfigUtil.FROM_ESPROC);//ConfigUtil.load(inputStream, true);
			inputStream.close();
			
			Context ctx = new com.raqsoft.dm.Context();		 
			String s = "call(\"D:/works/shell/sap/cli_open.dfx\")";
			s = "call(\"D:/works/shell/sapcli_execute.dfx\")";
			s = "call(\"D:/works/shell/sap/sapcli2.dfx\")";
			s = "call(\"D:/works/shell/sap/sapcli2.dfx\")";

			Expression exp = new com.raqsoft.expression.Expression(s);
			exp.calculate(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	 }

}
