package com.raqsoft.lib.kafka;

import java.io.InputStream;

import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.parallel.UnitContext;

public class ImMain {
	
	 public static void main(String[] args){
			try {
				InputStream inputStream = UnitContext.getUnitInputStream("raqsoftConfig.xml");
				ConfigUtil.load(inputStream,ConfigUtil.FROM_ESPROC);
				inputStream.close();
				
				Context ctx = new com.raqsoft.dm.Context();		 
				String s = "call(\"D:/works/shell/kafka/start2.dfx\")";
//				s = "call(\"D:/works/shell/kafka/serial.dfx\")";
//				s = "call(\"D:/works/shell/kafka/es_post.dfx\")";
//				s = "call(\"D:/works/shell/kafka/cluster.dfx\")";
//				s = "call(\"D:/works/shell/kafka/cluster_consumer.dfx\")";
//				s = "call(\"D:/works/shell/kafka/words.dfx\")";
				Expression exp = new com.raqsoft.expression.Expression(s);
				exp.calculate(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}		
		 }
	

}
