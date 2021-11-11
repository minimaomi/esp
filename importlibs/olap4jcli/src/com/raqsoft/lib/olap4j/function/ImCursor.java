package com.raqsoft.lib.olap4j.function;

import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.Table;
import com.raqsoft.dm.cursor.ICursor;

public class ImCursor extends ICursor {
	private Context m_ctx;
	private CellSet m_cellSet;
	private boolean m_bEnd = false;
	private String m_colNames[];
	private int m_nIndex = 0;

	public ImCursor(CellSet cellset, Context ctx) {
		this.m_cellSet = cellset;
		this.m_ctx = ctx;
		ctx.addResource(this);
		m_colNames = Utils.getColumnNames(cellset);
	}

	protected long skipOver(long n) {
		int count = 0;

		if (m_cellSet==null) {
			return 0;
		}
	
		 if (m_cellSet.getAxes().size() == 2) {
			 m_nIndex += n;
			 //int left = m_cellSet.getAxes().size()-m_nIndex;
			 
			 count = (int)n;
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
			return null;
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
		if (m_cellSet == null) return null;
		
		int nCount = 0,nCur = 0, k=0;
		Table table = new Table(m_colNames);
		Object[] objs = new Object[m_colNames.length];


		
		if (m_cellSet.getAxes().size() == 2) {
			for (Position row : m_cellSet.getAxes().get(1)) {
				nCur++;
				if (m_nIndex>nCur) continue;
				k = 0;
				 for (Member member : row.getMembers()) {
					 objs[k++] = member.getName();
				 }
				
				for (Position column : m_cellSet.getAxes().get(0)) {
					final Cell cell = m_cellSet.getCell(column, row);
					objs[k++] = cell.getFormattedValue();
				}
				table.newLast(objs);
				if (nCount++ >= n-1) {
					break;
				}
			}
			m_nIndex += nCount;
		}else if (m_cellSet.getAxes().size() == 1) {
			for (Position column : m_cellSet.getAxes().get(0)) {
				final Cell cell = m_cellSet.getCell(column);
				objs[k++] = cell.getFormattedValue();
			}
			table.newLast(objs);
		}

		return table;
	}
}
