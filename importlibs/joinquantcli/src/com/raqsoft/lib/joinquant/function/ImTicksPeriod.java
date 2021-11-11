package com.raqsoft.lib.joinquant.function;

import java.util.HashMap;
import java.util.Map;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Table;
import com.raqsoft.expression.Node;
import com.raqsoft.resources.EngineMessage;

public class ImTicksPeriod extends ImFunction {
	public Node optimize(Context ctx) {
		return this;
	}
	//get_ticksperiod(token,code,date,end_date)
	public Object doQuery(Object[] objs){
		try {
			if (objs.length<3){
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant " + mm.getMessage("add param error"));
			}
			String tocken = null;
			String code = null;
			int count = 0;
			
			if (objs[0] instanceof String){
				tocken = objs[0].toString();
			}
			if (objs[1] instanceof String){
				code = objs[1].toString();
			}
			
			Map<String, Object> paramMap = new HashMap<>();
			if (objs[2]!=null && objs[2] instanceof Integer){
				count = Integer.parseInt(objs[2].toString());
				paramMap.put("count", count);
			}
					
			if (tocken==null || code == null ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant get_ticks_period " + mm.getMessage("function.missingParam"));
			}
			
			paramMap.put("method", "get_ticks_period");
			paramMap.put("token", tocken);
			paramMap.put("code", code);
			
			if (objs.length>=4 && objs[3] instanceof String){
				paramMap.put("end_date", objs[3].toString());
			}
			
			if (objs.length>=5 && objs[4] instanceof Boolean){
				paramMap.put("skip", Boolean.parseBoolean(objs[4].toString()));
			}
						
			String[] body = JQNetWork.GetNetArrayData(paramMap);
			Table tbl = DataType.toTable(body);
			return tbl;
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}
