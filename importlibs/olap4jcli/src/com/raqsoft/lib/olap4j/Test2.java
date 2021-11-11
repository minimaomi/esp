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
				  Sequence.readLicense((byte) 1,new FileInputStream("D:\\backup\\OneDrive\\��Ȩ�ļ�\\�ڲ����԰�2019\\�������ڲ���Ȩ20191231.xml"));
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
//		 boolean bFind = m.find();   //ƥ��aaa2223 
////		int nCnt =  m.groupCount();   //����2,��Ϊ��2�� 
////		int s = m.start(1);   //����0 ���ص�һ��ƥ�䵽�����ַ������ַ����е������� 
////		 s = m.start(2);   //����3 
////		int E =  m.end(1);   //����3 ���ص�һ��ƥ�䵽�����ַ��������һ���ַ����ַ����е�����λ��. 
////		E =  m.end(2);   //����7 
//		String rt =  m.group(1);   //����aaa,���ص�һ��ƥ�䵽�����ַ��� 
//		rt =  m.group(2);   //����2223,���صڶ���ƥ�䵽�����ַ���
//		 
			//System.out.println(result);
		
	    }  
	    
}
