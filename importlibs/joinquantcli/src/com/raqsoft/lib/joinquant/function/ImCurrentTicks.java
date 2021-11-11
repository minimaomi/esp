package com.raqsoft.lib.joinquant.function;

import java.util.HashMap;
import java.util.Map;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.Table;
import com.raqsoft.expression.Node;
import com.raqsoft.resources.EngineMessage;

public class ImCurrentTicks extends ImFunction {
	public Node optimize(Context ctx) {
		return this;
	}
	
	//get_current_tick(token,code)
	public Object doQuery(Object[] objs){
		try {
			if (objs.length!=2){
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant " + mm.getMessage("add param error"));
			}
			String tocken = null;
			String code = "";
			
			if (objs[0] instanceof String){
				tocken = objs[0].toString();
			}
			if (objs[1] instanceof String){
				code = objs[1].toString();
			}else if(objs[1] instanceof Sequence) {
				Sequence seq = (Sequence)objs[1];
				for(int n=0; n<seq.length(); n++) {
					code+=seq.get(n+1).toString()+",";
				}
				code = code.substring(0,code.length()-1);
			}
			
			if (tocken==null || code == null  ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant get_current_ticks " + mm.getMessage("function.missingParam"));
			}
	
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("method", "get_current_ticks");
			paramMap.put("token", tocken);
			paramMap.put("code", code);
						
			String[] body = JQNetWork.GetNetArrayData(paramMap);
			Table tbl = DataType.toTable(body);
			
			return tbl;
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		return null;
	}
}
