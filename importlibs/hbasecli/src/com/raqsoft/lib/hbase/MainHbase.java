package com.raqsoft.lib.hbase;

import java.io.InputStream;
import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.parallel.UnitContext;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;

public class MainHbase {
	 public static void main(String[] args){
		try {
			InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
			ConfigUtil.load(inputStream,ConfigUtil.FROM_ESPROC);//ConfigUtil.load(inputStream, true);
			inputStream.close();
			
			Context ctx = new com.raqsoft.dm.Context();		 
			String s = "call(\"D:/works/shell/hbase/student.dfx\")";
			s = "call(\"D:/works/shell/hbase/hbase.dfx\")";
			s = "call(\"D:/works/shell/hbase/hbase_compare.dfx\")";
			s = "call(\"D:/works/shell/hbase/hbase_filter2.dfx\")";
//			s = "call(\"D:/works/shell/hbase/hbase_get.dfx\")";
			Expression exp = new com.raqsoft.expression.Expression(s);
			exp.calculate(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	 }
}
