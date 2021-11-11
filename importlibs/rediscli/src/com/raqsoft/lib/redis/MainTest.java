package com.raqsoft.lib.redis;

import java.io.InputStream;
import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.parallel.UnitContext;

public class MainTest {
	 public static void main(String[] args){
		try {
			InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
			ConfigUtil.load(inputStream,ConfigUtil.FROM_ESPROC);//ConfigUtil.load(inputStream, true);
			inputStream.close();
			
			Context ctx = new com.raqsoft.dm.Context();		 
			String dfx = "call(\"D:/works/shell/redis/rediscli.dfx\")";
			dfx = "call(\"D:/works/shell/redis/redis2.dfx\")";
			//dfx = "call(\"D:/works/shell/redis/rediscli_set.dfx\")";
			
			Expression exp = new com.raqsoft.expression.Expression(dfx);
			exp.calculate(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	 }
	
}
