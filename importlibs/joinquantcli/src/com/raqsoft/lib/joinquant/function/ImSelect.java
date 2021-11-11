package com.raqsoft.lib.joinquant.function;

import java.util.HashMap;
import java.util.Map;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Record;
import com.raqsoft.dm.Table;
import com.raqsoft.expression.Node;
import com.raqsoft.resources.EngineMessage;

import net.sf.json.JSONObject;

public class ImSelect extends ImFunction {
	public Node optimize(Context ctx) {
		return this;
	}
	//jq_select(token, param(jsonString));
	public Object doQuery(Object[] objs){
		try {
			if (objs.length<1){
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant select " + mm.getMessage("add param error"));
			}
			String tocken = null;
			String param = null;
			
			Map<String, Object> paramMap = new HashMap<>();
			if (objs[0] instanceof String){
				tocken = objs[0].toString();
				paramMap.put("token", tocken);
			}
			
			String[] body = null;
			if (objs[1] instanceof String){
				param = objs[1].toString();
				JSONObject json = JSONObject.fromString(param);
				json.remove("token");
				json.put("token", tocken);
				body = JQNetWork.GetNetArrayData(json.toString());
			}else if(objs[1] instanceof Record) {
				Record r = (Record)objs[1];
				String[] cols = r.dataStruct().getFieldNames();
				Object[] vs = r.getFieldValues();
				int n = 0;
				for(Object o:vs) {
					if (cols[n].compareTo("token")==0) {
						n++;
						continue;
					}
					paramMap.put(cols[n++], o);
				}
				body = JQNetWork.GetNetArrayData(paramMap);
			}
				
			Table tbl = DataType.toTable(body);
			return tbl;
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}
