package com.raqsoft.lib.olap4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raqsoft.dm.Context;
import com.raqsoft.dm.Sequence;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.FunctionLib;

public class Test2 {

  
	private static void loadFunction(){
		 FunctionLib.addFunction("olap_open", "com.raqsoft.lib.olap4j.function.ImOpen");
		 FunctionLib.addFunction("olap_close", "com.raqsoft.lib.olap4j.function.ImClose");
		 FunctionLib.addFunction("olap_query", "com.raqsoft.lib.olap4j.function.ImQuery");
		 //FunctionLib.addFunction("olap_cursor", "com.raqsoft.lib.olap4j.function.ImCursor");
		// FunctionLib.addFunction("olap_execute", "com.raqsoft.lib.olap4j.function.ImExecute");
	 }
	
	 public static void main(String[] args){
		// main2(); 
		 try {
				String os = System.getProperty("os.name");  
				if(os.toLowerCase().startsWith("win")){  
				  Sequence.readLicense((byte) 1,new FileInputStream("D:\\backup\\OneDrive\\授权文件\\内部测试版2019\\集算器内部授权20191231.xml"));
				} else {
					Sequence.readLicense((byte) 1,new FileInputStream("license_20181231.xml"));
				}			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		loadFunction();
		
		Context ctx = new com.raqsoft.dm.Context();		 
		Expression exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/olap/sap_001.dfx\")");
		//Expression exp = new com.raqsoft.expression.Expression("call(\"D:/works/shell/hivefile.dfx\")");
		exp.calculate(ctx);
	 }
	 
	 public static void main2() {  
	       String org = "[Dim Customer].[full name].[All]";
	   	String regExp =  "([\\s\\S]*)\\[(.*)\\]\\.\\[All.*?\\]$";
	   	String regExp2 = "([\\s\\S]*)\\[(.*)\\]\\.\\[All.*?\\]$";
	   	regExp = "([\\s\\S]*)\\[(.*)\\]\\.[\\[all.*?\\] | &\\[\\s\\S]*]$";
	   //	([\s\S]*)\[(.*)\]\.[\[All.*\] | \&\[\s\S]*]$
	   	org = "[Dim Product].[Product Model Lines].[Product Line].&[S]";
		String name="",caption = "";
		
			Pattern p = Pattern.compile(regExp);
			Matcher m = p.matcher(org);
			boolean bFind = m.find();
			int nn = m.groupCount();
			caption = m.group(1);
			caption = m.group(2);
			while(m.find()) { 
			     System.out.println(m.group()); 
			} 
//			
		
//		 Pattern p=Pattern.compile("([a-z]+)(\\d+)"); 
//		 Matcher m=p.matcher("aaa2223bb"); 
//		 boolean bFind = m.find();   //匹配aaa2223 
////		int nCnt =  m.groupCount();   //返回2,因为有2组 
////		int s = m.start(1);   //返回0 返回第一组匹配到的子字符串在字符串中的索引号 
////		 s = m.start(2);   //返回3 
////		int E =  m.end(1);   //返回3 返回第一组匹配到的子字符串的最后一个字符在字符串中的索引位置. 
////		E =  m.end(2);   //返回7 
//		String rt =  m.group(1);   //返回aaa,返回第一组匹配到的子字符串 
//		rt =  m.group(2);   //返回2223,返回第二组匹配到的子字符串
//		 
			//System.out.println(result);
		
	    }  
	    
}
