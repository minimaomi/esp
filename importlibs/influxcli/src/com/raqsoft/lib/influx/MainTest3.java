package com.raqsoft.lib.influx;

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
			String s = "call(\"D:/works/shell/influx/start.dfx\")";
			s = "call(\"D:/works/shell/influx/water.dfx\")";
			s = "call(\"D:/works/shell/influx/cluster_water.dfx\")";
			//s = "call(\"D:/works/shell/influx/into.dfx\")";
			//s = "call(\"D:/works/shell/influx/continuous.dfx\")";
			//s = "call(\"D:/works/shell/influx/insert.dfx\")";

			Expression exp = new com.raqsoft.expression.Expression(s);
			exp.calculate(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	 }

}
