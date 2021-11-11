package com.raqsoft.lib.mongo;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.raqsoft.app.config.ConfigUtil;
import com.raqsoft.app.config.RaqsoftConfig;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.parallel.UnitContext;

public class ImMain {
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
		
//		 FunctionLib.addFunction("hive_client", "com.raqsoft.lib.hive2_1_1.function.CreateClient");
//		 FunctionLib.addFunction("hive_close", "com.raqsoft.lib.hive2_1_1.function.HiveClose");
//		 FunctionLib.addFunction("hive_query", "com.raqsoft.lib.hive2_1_1.function.HiveQuery");
//		 FunctionLib.addFunction("hive_cursor", "com.raqsoft.lib.hive2_1_1.function.HiveCursor");
//		 FunctionLib.addFunction("hive_execute", "com.raqsoft.lib.hive2_1_1.function.HiveExecute");
	 }

	public static void main(String[] args) {
		loadFunction();
		Context ctx = new com.raqsoft.dm.Context();
		Expression exp = null;

		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo_a/my_find.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo_a/merge_telphone.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo_cookie/import_scores3.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo_a/1026_sub_course_insert2.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo3/1001_join.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo3/data_test_sort.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo3/loginHistory_group2.dfx\")");
		
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo/entity.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo/put.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo/get2.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo/foobar.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo/tname.dfx\")");
		//exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo/books_test.dfx\")");
		exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/mongo/devicestatus.dfx\")");
		
		exp.calculate(ctx);
	}

}
