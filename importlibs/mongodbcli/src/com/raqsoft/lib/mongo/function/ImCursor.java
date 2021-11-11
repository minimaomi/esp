package com.raqsoft.lib.mongo.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.mongodb.DBObject;
import com.mongodb.Cursor;
import com.raqsoft.common.*;
import com.raqsoft.dm.*;
import com.raqsoft.dm.cursor.ICursor;

public class ImCursor extends ICursor {
	private ImMongoDB db;
	private Cursor cursor;
	private DataStruct ds;
	private ArrayList<DataStruct> subDsList = new ArrayList<DataStruct>();
	
	public ImCursor(ImMongoDB db, Cursor cursor, String opt, Context ctx) {
		this.cursor = cursor;
		this.ctx = ctx;
		
		if (ctx != null) {
			ctx.addResource(this);
		}
		
		if (opt != null && opt.indexOf('x') != -1) {
			this.db = db;
		}
	}

	protected long skipOver(long n) {
		Cursor cursor = this.cursor;
		if (cursor == null || n == 0) return 0;

		long count = 0;
		while (count < n && cursor.hasNext()) {
			cursor.next();
			count++;
		}
		
		if (count < n) {
			close();
		}
		
		return count;
	}

	public synchronized void close() {
		super.close();
		
		try {
			if (ctx != null) ctx.removeResource(this);
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			
			if (db != null) {
				db.close();
				db = null;
			}
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}

	protected Sequence get(int n) {
		Cursor cursor = this.cursor;
		if (cursor == null || n < 1) return null;

		DBObject obj = null;
		List<DBObject> ls = new ArrayList<DBObject>();
		Set<String> set = null, lastSet = null;
		Set<String> result = new LinkedHashSet<String>();
		int nTotal = 100, nOff=0;
		if (n<nTotal) nTotal =n;
		String []fnames = null;
		if (ds == null) {
			while (cursor.hasNext()) {
				obj = cursor.next();
				set = obj.keySet();
				ls.add(obj);
				
				if (!set.equals(lastSet)){
					lastSet = set; 
					result.addAll(set);
				}
				if (++nOff>=nTotal){
					break;
				}
			}

			if (result.isEmpty()) {
				close();
				return null;
			}
			
			fnames = new String[result.size()];
			result.toArray(fnames);
			ds = new DataStruct(fnames);
		}
		
		Table table;
		if (n > INITSIZE) {
			table = new Table(ds, INITSIZE);
		} else {
			table = new Table(ds, n);
		}

		Map<String, Integer> mCol= new HashMap<String, Integer>(); 
		for(int i=0; i<fnames.length; i++){
			mCol.put(fnames[i], i);
		}
		
		ArrayList<DataStruct> subDsList = this.subDsList;
		ListBase1 mems = table.getMems();
		
		if (ls.size()>0) {
			for(DBObject o:ls){
				Record r = ImMongoDB.toRecord(ds, mCol, o, subDsList);
				mems.add(r);
				n--;
			}
		}
			
		while (n > 0 && cursor.hasNext()) {
			obj = cursor.next();
			Record r = ImMongoDB.toRecord(ds, mCol, obj, subDsList);
			mems.add(r);
			n--;
		}
		
		if (n > 0) {
			close();

			if (table.length() > 0) {
				table.trimToSize();
				return table;
			} else {
				return null;
			}
		} else {
			return table;
		}
	}

	protected void finalize() throws Throwable {
		close();
	}
}
