package com.raqsoft.lib.joinquant.function;

import java.util.HashMap;
import java.util.Map;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Table;
import com.raqsoft.expression.Node;
import com.raqsoft.resources.EngineMessage;

public class ImMarginCashStocks extends ImFunction {
	public Node optimize(Context ctx) {
		return this;
	}
	//get_margincash_stocks(token,date)
	public Object doQuery(Object[] objs){
		try {
			if (objs.length<1){
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant " + mm.getMessage("add param error"));
			}
			String tocken = null;
			
			if (objs[0] instanceof String){
				tocken = objs[0].toString();
			}
						
			if (tocken==null  ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinquant get_margincash_stocks " + mm.getMessage("function.missingParam"));
			}
	
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("method", "get_margincash_stocks");
			paramMap.put("token", tocken);
			if (objs.length>=2 && objs[1] instanceof String){
				paramMap.put("date", objs[1].toString());
			}
									
			String[] body = JQNetWork.GetNetArrayData(paramMap);
			Table tbl = DataType.toTable(body, new String[] {"code"});
			return tbl;
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}
