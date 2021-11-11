package com.raqsoft.lib.mongo.function;

import com.raqsoft.common.*;
import com.raqsoft.resources.EngineMessage;
import com.raqsoft.expression.*;
import com.raqsoft.dm.*;
import com.raqsoft.dm.cursor.ICursor;

public class ImInsert extends MemberFunction {
	private ImMongoDB mdb;

	public void releaseDotLeftObject() {
		mdb = null;
	}

	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("shell" + mm.getMessage("function.missingParam"));
		}

		if (param.getType()!=IParam.Comma){
		//if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("shell" + mm.getMessage("function.invalidParam"));
		}
		String tableName=null;
		Object val = null;
		Object obj = null;
		for(int i=0; i<param.getSubSize(); i++){
			if (param.getSub(i) == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("shell" + mm.getMessage("function.invalidParam"));
			}
			obj = param.getSub(i).getLeafExpression().calculate(ctx);
			if (i == 0) {				
				if ((obj instanceof ImMongoDB)) {
					mdb = (ImMongoDB) obj;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("shell" + mm.getMessage("function.paramTypeError"));
				}
			} else if(i==1) {
				if (!(obj instanceof String)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("shell " + mm.getMessage("function.paramTypeError"));
				}
				tableName = (String)obj;
			} else if(i==2) {
				if ((obj instanceof Table) || (obj instanceof String) || (obj instanceof ICursor)) {
					;
				}else{
					MessageManager mm = EngineMessage.get();
					throw new RQException("shell " + mm.getMessage("function.paramTypeError"));
				}
				val = obj;
			}
		}

		return mdb.insert(tableName, val);
	}
}
