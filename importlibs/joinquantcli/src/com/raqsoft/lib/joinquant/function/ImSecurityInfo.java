package com.raqsoft.lib.joinquant.function;

import java.util.HashMap;
import java.util.Map;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Table;
import com.raqsoft.expression.Node;
import com.raqsoft.resources.EngineMessage;

// hive_close(hive_client)
public class ImSecurityInfo extends ImFunction {
	public Node optimize(Context ctx) {
		return this;
	}

	public Object doQuery(Object[] objs){
		try {
			if (objs.length!=2){
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant " + mm.getMessage("add param error"));
			}
			String tocken = null;
			String code = null;
			
			if (objs[0] instanceof String){
				tocken = objs[0].toString();
			}
			if (objs[1] instanceof String){
				code = objs[1].toString();
			}
			
			if (tocken==null || code == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant " + mm.getMessage("function.missingParam"));
			}
	
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("method", "get_security_info");
			paramMap.put("token", tocken);
			paramMap.put("code", code);
			
			String[] body = JQNetWork.GetNetArrayData(paramMap);
			if (body!=null) {
				Table tbl = DataType.toTable(body);
				return tbl;
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}
