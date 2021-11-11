package com.raqsoft.lib.mongo.function;

import com.raqsoft.common.*;
import com.raqsoft.resources.EngineMessage;
import com.raqsoft.expression.*;
import com.raqsoft.dm.*;

// mdb.shell@x(s)
public class ImShell extends MemberFunction {
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
			MessageManager mm = EngineMessage.get();
			throw new RQException("shell" + mm.getMessage("function.invalidParam"));
		}
		
		Object obj = null;
		for(int i=0; i<param.getSubSize(); i++){
			if (param.getSub(i) == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("client" + mm.getMessage("function.invalidParam"));
			}

			if (i == 0) {
				obj = param.getSub(i).getLeafExpression().calculate(ctx);
				if ((obj instanceof ImMongoDB)) {
					mdb = (ImMongoDB) obj;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("client" + mm.getMessage("function.paramTypeError"));
				}
			} else {
				obj = param.getSub(i).getLeafExpression().calculate(ctx);			
				if (!(obj instanceof String)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("shell" + mm.getMessage("function.paramTypeError"));
				}
			}
		}

		return mdb.shell((String)obj, option);
	}
}
