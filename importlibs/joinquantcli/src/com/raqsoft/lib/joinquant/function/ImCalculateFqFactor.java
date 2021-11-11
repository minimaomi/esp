package com.raqsoft.lib.joinquant.function;

import java.io.File;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Node;
import com.raqsoft.resources.EngineMessage;

public class ImCalculateFqFactor extends ImFunction {
	private String m_storageFile; 

	public Node optimize(Context ctx) {
		return this;
	}
	
	//get_calc_factor(token,path)
	public Object doQuery(Object[] objs){
		try {
			if (objs.length<3){
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant " + mm.getMessage("add param error"));
			}
			
			String tocken = null;
			if (objs[0] instanceof String){
				tocken = objs[0].toString();
			}
			//如果后后缀为btx或csv则为文件名，否则为目录
			if (objs[1] instanceof String){
				String path = null;
				String tmp = objs[1].toString().toLowerCase();
				if (tmp.endsWith(".btx") ||tmp.endsWith(".csv")) {
					m_storageFile = tmp;
					File fp=new File(m_storageFile);
					path = fp.getParent();
				}else {
					path = tmp;
					m_storageFile = String.format("%s/download_stock_xrxd.btx", path);
				}
				
				File f=new File(path);
				if (!f.exists()) {
					f.mkdirs();
				}
			}
			
			if (tocken==null || m_storageFile == null ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant get_fq_factor " + mm.getMessage("function.missingParam"));
			}
				
			return null;
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public static void main(String[] args){
		ImCalculateFqFactor cls = new ImCalculateFqFactor();
		String path = "d:/tmp/data/jq/download";
		Object[] os = new Object[] {"5b6a9ba1b5f073bb20667f2f06ca0ab9adea132c", path};
		cls.doQuery(os);
		System.out.println("end...");
		
	}
	
}
