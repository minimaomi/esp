package com.raqsoft.lib.hdfs;

import java.io.InputStream;
import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.parallel.UnitContext;

public class HdfsMain {
		 
	 public static void main(String[] args){
			try {
				InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
				ConfigUtil.load(inputStream,ConfigUtil.FROM_ESPROC);//ConfigUtil.load(inputStream, true);
				inputStream.close();
				
				Context ctx = new com.raqsoft.dm.Context();		 
				String s = "call(\"D:/works/shell/hdfs/hdfsfile.dfx\")";
				
				Expression exp = new com.raqsoft.expression.Expression(s);
				exp.calculate(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}		
		 }
}
