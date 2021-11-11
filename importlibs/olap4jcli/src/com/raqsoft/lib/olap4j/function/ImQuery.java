package com.raqsoft.lib.olap4j.function;

import java.sql.SQLException;
import org.olap4j.CellSet;
import com.raqsoft.common.Logger;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Node;

public class ImQuery extends ImFunction {
	boolean bCursor = false;
	public Node optimize(Context ctx) {
		super.optimize(ctx);
		
		return this;
	}

	public Object calculate(Context ctx) {
		String option = getOption();
		if (option!=null && option.equals("c")){
			bCursor = true;
		}
		
		Object o = super.calculate(ctx);
		
		return o;		
	}
	
	public Object doQuery( Object[] objs){		
		String sql = objs[0].toString(); 		
		try {
			CellSet cellset = (CellSet)m_mdx.query(sql);
			if (MdxQueryUtil.m_bSap){
				if(bCursor){
					return new ImSapCursor(cellset, m_ctx);
				}else{					
					return Utils.toSapTable(cellset);
				}	
			}else{
				if(bCursor){
					return new ImCursor(cellset, m_ctx);
				}else{			
					String colNames[] = Utils.getColumnNames(cellset);
					return Utils.toTable(cellset, colNames);
				}	
			}
		} catch (SQLException e) {
			Logger.error(e.getStackTrace());
		}
		
		return null;
	}
}
