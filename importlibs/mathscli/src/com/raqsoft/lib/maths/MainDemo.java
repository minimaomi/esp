package com.raqsoft.lib.maths;

import java.io.InputStream;
import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.parallel.UnitContext;

public class MainDemo {
	
	 
	 public static void main(String[] args){
		try {
			InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
			ConfigUtil.load(inputStream,ConfigUtil.FROM_ESPROC);//ConfigUtil.load(inputStream, true);
			inputStream.close();
			
			Context ctx = new com.raqsoft.dm.Context();		 
			String dfx = "call(\"D:/works/shell/model/demo.dfx\")";
			//dfx = "call(\"D:/works/shell/python/python.dfx\")";
			dfx = "call(\"D:/works/shell/maths/dpca.dfx\")";
			//dfx = "call(\"D:/works/shell/maths/plsFit.dfx\")";
			//dfx = "call(\"D:/works/shell/maths/timetest2.dfx\")";
			//dfx = "call(\"D:/works/shell/model/fork_12_for_101.dfx\")";
			//dfx = "call(\"D:/works/shell/model/python_spider01.dfx\")";

			Expression exp = new com.raqsoft.expression.Expression(dfx);
			exp.calculate(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	 }

}
