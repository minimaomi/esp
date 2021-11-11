package com.raqsoft.lib.mongo.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Cursor;
import com.mongodb.BasicDBObject;
//import com.mongodb.DBRefBase;
import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBList;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;

import org.bson.BSONObject;
import org.bson.types.ObjectId;
import com.raqsoft.common.ArgumentTokenizer;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.common.Sentence;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.DataStruct;
import com.raqsoft.dm.IResource;
import com.raqsoft.dm.Record;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.Table;
import com.raqsoft.dm.cursor.ICursor;
import com.raqsoft.resources.EngineMessage;

public class ImMongoDB implements IResource {
	private MongoClient mongo;
	private DB db;
	private Context ctx;
	
	// mongo://ip:port/db?arg=v
	@SuppressWarnings("deprecation")
	public ImMongoDB(String str, Context ctx) {
		this.ctx = ctx;

		try {
			MongoClientURI conn = new MongoClientURI(str);
			mongo = new MongoClient(conn);
			db = mongo.getDB(conn.getDatabase());
			if (ctx != null) ctx.addResource(this);
		} catch (Exception e) {
			throw new RQException(e);
		}
	}
	
	// user=v&password=p
	private static void readArgs(String str, int index, ArrayList<String>attrList, ArrayList<String>valList) {
		char []chars = str.toCharArray();
		int len = chars.length;
		
		while (index < len) {
			int keyStart = index;
			int keyEnd = -1;
			int valStart = -1;
			
			while (index < len) {
				if (chars[index] == '=') {
					keyEnd = index;
					index++;
					valStart = index;
					break;
				} else {
					index++;
				}
			}
			
			if (valStart == -1) break;
			
			int valEnd = len;
			while (index < len) {
				if (chars[index] == '&') {
					valEnd = index;
					index++;
					break;
				} else {
					index++;
				}
			}
			
			attrList.add(str.substring(keyStart, keyEnd));
			valList.add(str.substring(valStart, valEnd));
		}
	}
	
	public void close() {
		if (ctx != null) ctx.removeResource(this);
		mongo.close();
	}
	
	public ICursor find(String tableName, String opt) {
		DBCollection collection = db.getCollection(tableName);
		DBCursor cursor = collection.find();
		return new ImCursor(this, cursor, opt, ctx);
	}

	public ICursor find(String tableName, String filter, String fields, 
			String sort, Number limit, String opt) {
		com.mongodb.DBObject ref;
		if (filter == null || filter.length() == 0) {
			ref = new BasicDBObject();
		} else {
	        Object val = JSON.parse(filter);
	        if (!(val instanceof com.mongodb.DBObject)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("find" + mm.getMessage("function.invalidParam"));
	        }
	        
			ref = (com.mongodb.DBObject)val;
		}
		
		DBCollection collection = db.getCollection(tableName);
		DBCursor cursor;
		if (fields == null || fields.length() == 0) {
			cursor = collection.find(ref);
		} else {
			Object val = JSON.parse(fields);
	        if (!(val instanceof com.mongodb.DBObject)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("find" + mm.getMessage("function.invalidParam"));
	        }

			cursor = collection.find(ref, (com.mongodb.DBObject)val);
		}
		
		if (sort != null && sort.length() > 0) {
			Object val = JSON.parse(sort);
	        if (!(val instanceof com.mongodb.DBObject)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("find" + mm.getMessage("function.invalidParam"));
	        }
			
	        cursor = cursor.sort((com.mongodb.DBObject)val);
		}
		
		if (limit != null) {
			cursor = cursor.limit(limit.intValue());
		}
		
		return new ImCursor(this, cursor, opt, ctx);
	}

	public static Record toRecord(com.mongodb.DBObject obj, ArrayList<DataStruct> subDsList) {
		Set<Map.Entry<String, Object>> set = obj.toMap().entrySet();
		int size = set.size();
		String []names = new String[size];
		Object []vals = new Object[size];
		int i = 0;
        for (Map.Entry<String, Object> entry : set) {
        	names[i] = entry.getKey();
        	vals[i] = mongoDataToDMData(entry.getValue(), subDsList);
        	i++;
        }
        
        for (DataStruct ds : subDsList) {
        	if (ds.isCompatible(names)) {
        		return new Record(ds, vals);
        	}
        }
        
        DataStruct ds = new DataStruct(names);
        subDsList.add(ds);
		return new Record(ds, vals);
	}
	
	public static Object mongoDataToDMData(Object val, ArrayList<DataStruct> subDsList) {
		if (val instanceof List) {
			List<Object> list = (List<Object>)val;
			int size = list.size();
			Sequence seq = new Sequence(size);
			for (int i = 0; i < size; ++i) {
				val = list.get(i);
				seq.add(mongoDataToDMData(val, subDsList));
			}
			
			return seq;
		} else if (val instanceof com.mongodb.DBObject) {
			return toRecord((com.mongodb.DBObject)val, subDsList);
//		} else if (val instanceof DBRefBase) {
//			return toRecord(((DBRefBase)val).fetch(), subDsList);
		} else if (val instanceof ObjectId) {
			return ((ObjectId)val).toString();
		} else {
			return val;
		}
	}
	
	public static Record toRecord(DataStruct ds, Map<String, Integer> cols, com.mongodb.DBObject obj, ArrayList<DataStruct> subDsList) {
		int fcount = cols.size();
		Object []vals = new Object[fcount];
		Set<String> keys = obj.keySet();
		int i = 0;
		for (String key : keys) { 
			Object val = obj.get(key);
			i = cols.get(key);
			vals[i] = mongoDataToDMData(val, subDsList);
		}
		
		return new Record(ds, vals);
	}
	
	public long count(String tableName) {
		DBCollection collection = db.getCollection(tableName);
		return collection.count();
	}

	public long count(String tableName, String filter) {
		if (filter == null || filter.length() == 0) {
			return count(tableName);
		} else {
	        Object val = JSON.parse(filter);
	        if (!(val instanceof com.mongodb.DBObject)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("count" + mm.getMessage("function.invalidParam"));
	        }
	        
			DBCollection collection = db.getCollection(tableName);
			return collection.count((com.mongodb.DBObject)val);
		}
	}
	
	public Table aggregate(String tableName, String arg) {
		Object val = ImJSON.parse(arg);
		if (!(val instanceof List)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("aggregate" + mm.getMessage("function.invalidParam"));
		}
		
		AggregationOptions aggregationOptions = AggregationOptions.builder()
		.batchSize(ICursor.FETCHCOUNT)
		.allowDiskUse(true)
		.build();
        
		List<com.mongodb.DBObject> list = (List<com.mongodb.DBObject>)val;
		DBCollection collection = db.getCollection(tableName);
		Cursor cursor = collection.aggregate(list, aggregationOptions);
		ICursor tmp = new ImCursor(this, cursor, null, ctx);
		return (Table)tmp.fetch();
	}
	
	public Sequence distinct(String tableName, String field, String filter) {
		DBCollection collection = db.getCollection(tableName);
		List<Object> list;
		if (filter == null || filter.length() == 0) {
			list = collection.distinct(field);
		} else {
			Object val = JSON.parse(filter);
	        if (!(val instanceof com.mongodb.DBObject)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("distinct" + mm.getMessage("function.invalidParam"));
	        }
	        
	        list = collection.distinct(field, (com.mongodb.DBObject)val);
		}
		
		int size = list.size();
		ArrayList<DataStruct> subDsList = new ArrayList<DataStruct>();
		Sequence seq = new Sequence(size);
		for (int i = 0; i < size; ++i) {
			seq.add(mongoDataToDMData(list.get(i), subDsList));
		}
		
		return seq;
	}
	
	public Object shell(String cmd, String opt) {
		cmd = cmd.trim();
		int limit = cmd.indexOf('(');
		int i = cmd.lastIndexOf('.', limit);
		if (i < 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + cmd);
		}
		
		String tableName = cmd.substring(0, i);
		DBCollection collection = db.getCollection(tableName);
		if (collection == null) {
			return null;
		}
		
		return shell(collection, cmd, i + 1, opt);
	}
	
	private ArrayList<String> scanParam(String param) {
		if (param == null) return null;
		
		param = param.trim();
		if (param.length() == 0) return null;
		
		ArrayList<String> paramList = new ArrayList<String>();
		ArgumentTokenizer arg = new ArgumentTokenizer(param);
		while (arg.hasMoreElements()) {
			String cur = arg.nextToken();
			if (cur == null) {
				paramList.add(null);
			} else {
				paramList.add(cur.trim());
			}
		}

		return paramList;
	}
	
	private DBCursor shell(DBCursor dbCursor, String cmd, int index) {
		int len = cmd.length();
		if (index == len) {
			return dbCursor;
		} else if (cmd.charAt(index) != '.') {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + cmd);
		}
		
		index++;
		int i = cmd.indexOf('(', index);
		if (i < 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + cmd);
		}
		
		int end = Sentence.scanParenthesis(cmd, i);
		if (end < 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("(,)" + mm.getMessage("Expression.illMatched"));
		}
		
		String func = cmd.substring(index, i);
		String param = cmd.substring(i + 1, end);
		ArrayList<String> paramList = scanParam(param);
		end++;
		
		if (func.equals("sort")) {
			if (paramList == null || paramList.size() != 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("sort" + mm.getMessage("function.invalidParam"));
			}
			
			Object val = JSON.parse(paramList.get(0));
	        if (!(val instanceof com.mongodb.DBObject)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("sort" + mm.getMessage("function.invalidParam"));
	        }
			
	        dbCursor = dbCursor.sort((com.mongodb.DBObject)val);
		} else if (func.equals("limit")) {
			if (paramList == null || paramList.size() != 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("limit" + mm.getMessage("function.invalidParam"));
			}
			
			Object val = JSON.parse(paramList.get(0));
	        if (!(val instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("limit" + mm.getMessage("function.invalidParam"));
	        }
			
	        dbCursor = dbCursor.limit(((Number)val).intValue());
		} else if (func.equals("pretty")) {
			cmd = cmd.replace(".pretty()", "");
			end = cmd.length();
		} else if (func.equals("skip")) {
//			cmd = cmd.replace(".pretty()", "");
//			end = cmd.length();
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownFunction") + func);
		}

		return shell(dbCursor, cmd, end);
	}
	
	private Object shell(DBCollection collection, String cmd, int index, String opt) {
		int i = cmd.indexOf('(', index);
		if (i < 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + cmd);
		}
		
		int end = Sentence.scanParenthesis(cmd, i);
		if (end < 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("(,)" + mm.getMessage("Expression.illMatched"));
		}
		
		String func = cmd.substring(index, i);
		String param = cmd.substring(i + 1, end);
		ArrayList<String> paramList = scanParam(param);
		
		Object result;
		int len = cmd.length();
		end++;
		
		if (func.equals("find")) {
			DBCursor dbCursor = find(collection, paramList);
			dbCursor = shell(dbCursor, cmd, end);
			return new ImCursor(this, dbCursor, opt, ctx);
		} else if (func.equals("count")) {
			if (end != len) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + cmd.substring(end));
			}
			
			long count = count(collection, paramList);
			result = new Long(count);
		} else if (func.equals("distinct")) {
			if (end != len) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + cmd.substring(end));
			}
			
			result = distinct(collection, paramList);
		} else if (func.equals("aggregate")) {
			if (end != len) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + cmd.substring(end));
			}
			
			result = aggregate(collection, paramList);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownFunction") + func);
		}
		
		if (opt != null && opt.indexOf('x') != -1) {
			close();
		}
		
		return result;
	}
	
	private ICursor aggregate(DBCollection collection, ArrayList<String> paramList) {
	//private Sequence aggregate(DBCollection collection, ArrayList<String> paramList) {
		if (paramList == null || paramList.size() < 1) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("aggregate" + mm.getMessage("function.invalidParam"));
		}
		
		if(paramList.get(0).indexOf("ISODate")!=-1){
			return aggregateDate(collection, paramList);
		}else{
			Object val = ImJSON.parse(paramList.get(0));
			if (!(val instanceof List)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("find" + mm.getMessage("function.invalidParam"));
			}
			
			AggregationOptions aggregationOptions = AggregationOptions.builder()
			.batchSize(ICursor.FETCHCOUNT)
			.allowDiskUse(true)
			.build();
	        
			List<DBObject> list = (List<DBObject>)val;
			List<DBObject> newLs = new ArrayList<DBObject>();
			Cursor cursor = collection.aggregate(list, aggregationOptions);
	
			ICursor tmp = new ImCursor(this, cursor, null, ctx);
			return tmp;
		}
	}
	
	private ICursor aggregateDate(DBCollection collection, ArrayList<String> paramList) {
		//private Sequence aggregate(DBCollection collection, ArrayList<String> paramList) {
		try{
			if (paramList == null || paramList.size() < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("aggregate" + mm.getMessage("function.invalidParam"));
			}
			//System.out.println(paramList.get(0));
						
			
			
			AggregationOptions aggregationOptions = AggregationOptions.builder()
			.batchSize(ICursor.FETCHCOUNT)
			.allowDiskUse(true)
			.build();
	        
			//$match:{pdate:{$lte:A,$gte:B}}
			List<DBObject> list = new ArrayList<DBObject>();	
			for(String param: paramList){
				Object val = ImJSON.parse(param);
				if (!(val instanceof BasicDBObject)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("find" + mm.getMessage("function.invalidParam"));
				}
				
				DBObject doc = (DBObject)val;
				if (doc.containsField("$match")){
					BasicDBObject query= new BasicDBObject();
					BasicDBList subDoc = (BasicDBList)doc.get("$match");
					for(Object obj : subDoc){ //match
						DBObject obb = (DBObject)obj;
						Set<String> keys = obb.keySet();
						for(String k:keys){ //fields				
							DBObject line=(DBObject)obb.get(k);
							Set<String> vals = line.keySet();
							BasicDBObject[] array = new BasicDBObject[vals.size()];
							int n = 0;
							for(String key:vals){
								Object o = line.get(key);
								String d = ImJSON.fmt.format(o);  
							    Date date=ImJSON.fmt.parse(d); 
								array[n++] = new BasicDBObject(k, new BasicDBObject(key, date));
							}
							
							query.append("$and", array);
							//System.out.println("key="+k+";val="+line);
						}
					}
					BasicDBObject match = new BasicDBObject("$match", query); 
					list.add(match);
				}else{
					list.add((DBObject)val);
				}
			}
			Cursor cursor = collection.aggregate(list, aggregationOptions);
			if (cursor.hasNext()){
				DBObject bojb = cursor.next();
				System.out.println(bojb);
			}
			
			ICursor tmp = new ImCursor(this, cursor, null, ctx);
			return tmp;
		}catch(Exception e){
			
		}
		
		return null;
	}
	
	private void doMapParse(Map<String,Object> map, BasicDBList lls){
		for(Map.Entry<String, Object> entry : map.entrySet()){
		    String mapKey = entry.getKey();
		    Object mapValue = entry.getValue();
		    if (mapValue instanceof com.mongodb.DBObject ){
		    	BasicDBList subll = new BasicDBList();;
			    com.mongodb.DBObject dbObj = (com.mongodb.DBObject)mapValue;
			    if (dbObj!=null){
			    	doMapParse(dbObj.toMap(), lls);
			    }
			    System.out.println(mapKey+"=0=>"+mapValue);
			    lls.add(new BasicDBObject(mapKey, lls));
		    }else{
		    	System.out.println(mapKey+"=1=>"+mapValue);
		    	lls.add(new BasicDBObject(mapKey, mapValue));
		    }
		    
		}
	}
	
	public boolean insert(String tableName, Object val) {
		boolean bRet = false;
		try{
			if (tableName==null || tableName.isEmpty()){
				System.out.println("collection name is empty");
				return bRet;
			}
			
			if (val==null){
				System.out.println("collection data is empty");
				return bRet;
			}
			
			DBCollection collection = db.getCollection(tableName);
			if (collection == null) {
				System.out.println("collection:"+tableName+" is existed");
				return bRet;
			}
			if (val instanceof Table) {					
				Table table = (Table)val;				
				
				String[] fields = table.dataStruct().getFieldNames();
				insertTable(collection, fields, table);
			}else if (val instanceof String) {					
				insertJson(collection, (String)val);
			}else if(val instanceof ICursor ){
				ICursor c = (ICursor)val;
				Table table = (Table)c.fetch(100000);
				String[] fields = table.dataStruct().getFieldNames();
				while(table!=null){
					insertTable(collection, fields, table);
					table = (Table)c.fetch(100000);
				}
			}
			
			bRet = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return bRet;
	}
	
	private boolean insertTable(DBCollection c, String[] fields, Table tbl){
		boolean bRet = false;
		int i = 0, n=0;
		try{
			// print tableData
			for (i = 0; i < tbl.length(); i++) {
				DBObject dbo = new BasicDBObject();
				Record rc = tbl.getRecord(i + 1);
				Object[] objs = rc.getFieldValues();
				n = 0;
				for (Object o : objs) {
					dbo.put(fields[n++], o);
				}
				c.insert(dbo);
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		return bRet;
	}
	
	private boolean insertJson(DBCollection c, String sJson){
		boolean bRet = false;
		try{
			DBObject bson = (DBObject)JSON.parse(sJson);
			
			Set<String> keys = bson.keySet();
			DBObject[] totalrecords = new BasicDBObject[keys.size()];
			int i = 0;
			for (String key : keys) { 
				Object val = bson.get(key);
				if (val instanceof BasicDBObject){
					BasicDBObject rc = (BasicDBObject)val;
					totalrecords[i++] = rc;
				}
			}
	        c.insert(totalrecords);
			bRet = true;
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		return bRet;
	}
	
	private long count(DBCollection collection, ArrayList<String> paramList) {
		if (paramList == null) {
			return collection.count();
		}
		
		if (paramList.size() != 1) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("count" + mm.getMessage("function.invalidParam"));
		}
		
        Object val = JSON.parse(paramList.get(0));
        if (!(val instanceof com.mongodb.DBObject)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("count" + mm.getMessage("function.invalidParam"));
        }
        
		return collection.count((com.mongodb.DBObject)val);
	}

	private Sequence distinct(DBCollection collection, ArrayList<String> paramList) {
		if (paramList == null || paramList.size() > 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("distinct" + mm.getMessage("function.invalidParam"));
		}
		
		String field = paramList.get(0);
		String filter = null;
		if (paramList.size() > 1) filter = paramList.get(1);
		
		List<Object> list;
		if (filter == null || filter.length() == 0) {
			list = collection.distinct(field);
		} else {
			Object val = JSON.parse(filter);
	        if (!(val instanceof com.mongodb.DBObject)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("distinct" + mm.getMessage("function.invalidParam"));
	        }
	        
	        list = collection.distinct(field, (com.mongodb.DBObject)val);
		}
		
		int size = list.size();
		ArrayList<DataStruct> subDsList = new ArrayList<DataStruct>();
		Sequence seq = new Sequence(size);
		for (int i = 0; i < size; ++i) {
			seq.add(mongoDataToDMData(list.get(i), subDsList));
		}
		
		return seq;
	}
	
	private DBCursor find(DBCollection collection, ArrayList<String> paramList) {
		if (paramList == null) {
			return collection.find();
		}
		
		int size = paramList.size();
		if (size > 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("find" + mm.getMessage("function.invalidParam"));
		}
		
		String filter = paramList.get(0);
		com.mongodb.DBObject ref;
		if (filter == null || filter.length() == 0) {
			ref = new BasicDBObject();
		} else {
	        Object val = JSON.parse(filter);
	        if (!(val instanceof com.mongodb.DBObject)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("find" + mm.getMessage("function.invalidParam"));
	        }
	        
			ref = (com.mongodb.DBObject)val;
		}

		if (size == 1) {
			return collection.find(ref);
		} else {
			String fields = paramList.get(1);
			if (fields == null || fields.length() == 0) {
				return collection.find(ref);
			} else {
				Object val = JSON.parse(fields);
		        if (!(val instanceof com.mongodb.DBObject)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("find" + mm.getMessage("function.invalidParam"));
		        }

				return collection.find(ref, (com.mongodb.DBObject)val);
			}
		}
	}
}
