package com.raqsoft.lib.olap4j.function;

import java.util.List;

import org.olap4j.CellSet;
import org.olap4j.driver.xmla.XMLAByteBufferTable;
import org.olap4j.driver.xmla.XmlaOlap4jCellSet;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.Table;
import com.raqsoft.dm.cursor.ICursor;

public class ImSapCursor extends ICursor {
	private Context m_ctx;
	private XMLAByteBufferTable m_xtable;
	private boolean m_bEnd = false;
	private String m_colNames[];
	private int m_nIndex = 0;

	public ImSapCursor(CellSet cellset, Context ctx) {
		this.m_ctx = ctx;
		ctx.addResource(this);
		m_xtable = (XMLAByteBufferTable)((XmlaOlap4jCellSet)cellset).getDataBuf();
		if (m_xtable!=null){
			List<String> ls = m_xtable.getColumn();					
			m_colNames = new String[ls.size()];
			m_xtable.getColumn().toArray(m_colNames);
		}
	}

	protected long skipOver(long n) {
		int count = 0;
		m_nIndex += n;
		if (m_xtable == null) {
			return 0;
		}
		if (n<0){
			return 0;
		}
		int left = m_xtable.rowCount() - m_nIndex;
		if (left<0){
			close();
		}

		return count;
	}

	public synchronized void close() {
		super.close();

		try {
			if (m_ctx != null) {
				m_ctx.removeResource(this);
				m_ctx = null;
			}
			m_bEnd = true;
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}

	public Sequence get(int n) {
		Table table = getTable(n);
		if (table == null) {
			close();
			return null;
		}

		if (table.length() < n && n < ICursor.INITSIZE) {
			close();
		}

		return table;
	}

	protected void finalize() throws Throwable {
		close();
	}

	public boolean isEnd() {
		return m_bEnd;
	}

	private Table getTable(int n) {
		if (n < 0) return null;
		Table table = new Table(m_colNames);
		Object[] objs = new Object[m_colNames.length];

		do{
			if (m_xtable == null) break;
			if (n<0) break;
			int left = m_xtable.rowCount() - m_nIndex;
			if (left<0) break;
			
			int nFetch = n;
			if (nFetch> left){
				nFetch = left;
			}
			for(int i=0; i<nFetch; i++){
	    	  for(int j=0; j<m_colNames.length; j++){
	    		  objs[j] = m_xtable.getValueAt(j, i+m_nIndex);
	    	  }
	    	  table.newLast(objs);
	        }
			m_nIndex+=nFetch;
		}while(false);
		
		return table;
	}
}
