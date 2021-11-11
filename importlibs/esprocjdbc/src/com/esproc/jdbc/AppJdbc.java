package com.esproc.jdbc;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.raqsoft.common.Logger;
import com.raqsoft.dm.DataStruct;
import com.raqsoft.dm.Record;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.Table;

public class AppJdbc {
	private Connection m_con = null;
	private ResultSet m_result=null;
	private Statement m_st=null;
	private java.sql.PreparedStatement m_pst=null;
	private String[] m_columns = null;
	
	public AppJdbc() {
		try {
			Logger.setLevel(Logger.WARNING);
			Class.forName("com.esproc.jdbc.InternalDriver");
			m_con = DriverManager.getConnection("jdbc:esproc:local://");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String[] getColumns(){
		return m_columns!=null ? m_columns : new String[0]; 
	}
	
	private void printResult(java.sql.ResultSet rs) throws SQLException {
		System.out.println("======================");
		java.sql.ResultSetMetaData rsmd = rs.getMetaData();
		int cc = rsmd.getColumnCount();
		while (rs.next()) {
			for (int i = 0; i < cc; i++) {
				if (i > 0)
					System.out.print(",");
				System.out.print(rs.getObject(i + 1));
			}
			System.out.println();
		}
	}
	
	private List<Object[]> convert(ResultSet rs) throws SQLException {
		List<Object[]> list = new ArrayList<Object[]>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cc = rsmd.getColumnCount();
		m_columns = null;
		m_columns = new String[cc];
		
		// header
		for (int i = 0; i < cc; i++) {
			m_columns[i] = rsmd.getColumnName(i+1);
		}
		
		// data
		Object[] objs = null;
		while (rs.next()) {
			objs = new Object[cc];
			for (int i = 0; i < cc; i++) {
				objs[i] = rs.getObject(i + 1);
				if (objs[i] instanceof Sequence){
					objs[i] = convertSequenceToTable((Sequence)objs[i]);
				}
			}
			list.add(objs);
		}
		
		return list;
	}
	
	// return [header:data]
	private Object[] mconvert(ResultSet rs) throws SQLException {
		List<Object[]> list = new ArrayList<Object[]>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cc = rsmd.getColumnCount();
		String[] columns = new String[cc];
		
		// header
		for (int i = 0; i < cc; i++) {
			columns[i] = rsmd.getColumnName(i+1);
		}
		
		// data
		Object[] objs = null;
		while (rs.next()) {
			objs = new Object[cc];
			for (int i = 0; i < cc; i++) {
				objs[i] = rs.getObject(i + 1);
				if (objs[i] instanceof Sequence){
					objs[i] = convertSequenceToTable((Sequence)objs[i]);
				}
			}
			list.add(objs);
		}
		
		return new Object[]{columns, list};
	}
	
	// Sequence转换成表结构
	private Object convertSequenceToTable(Sequence seq){
		//1. title
		Table table = null;
		DataStruct ds = seq.dataStruct();
		if (ds!=null){
			String[] cols = seq.dataStruct().getFieldNames();
			table = new Table(cols);
		}else{
			return seq;
		}
		//2. data of record
		for (int j = 0; j<seq.length(); j++){
			Object obj = seq.get(j+1);
			if (obj instanceof Record){
				Record rc = (Record)obj;
				table.newLast(rc.getFieldValues());
			}						
		}
		return table;
	}
	
	public List<Object[]> convertTableToList(Sequence seq){
		List<Object[]> list = new ArrayList<Object[]>();
		//1. title
		String[] cols = seq.dataStruct().getFieldNames();
		list.add(cols);
		
		//2. data of record
		for (int j = 0; j<seq.length(); j++){
			Object obj = seq.get(j+1);
			if (obj instanceof Record){
				Record rc = (Record)obj;
				Object[] lines = rc.getFieldValues();
				list.add(lines);
			}						
		}
		return list;
	}
//	
//	private String convertTableToJson(Sequence seq){
//		String ret = "[";
//		//1. title
//		String[] cols = seq.dataStruct().getFieldNames();
//		
//		for (int j = 0; j<seq.length(); j++){
//			String line = "{";
//			Object obj = seq.get(j+1);
//			if (obj instanceof Record){
//				Record rc = (Record)obj;
//				Object[] lines = rc.getFieldValues();
//				for(int i=0; i<lines.length; i++){
//					line+=cols[i]+":"+lines[i]+",";
//				}
//				ret+=line.substring(0, line.length()-1)+"},";
//			}						
//		}
//		ret = ret.substring(0, ret.length()-1)+"]";
//		return ret;
//	}

	/**********************************************************
	 * query操作
	 * 
	 * ********************************************************/
	//无参数查询
	public List<Object[]> query(String dfxFile ) {
		return query(dfxFile, new Object[0]);
	}
	//多参数列表查询
	public List<Object[]> query(String dfxFile, List<Object> params) {
		Object[] objs = params.toArray(new Object[params.size()]);
		return query(dfxFile, objs);
	}
	
	//多参数列表查询，对参数本身为数组的，且需要转换成序表
	public List<Object[]> query(String dfxFile, Object...params ) {
		List<Object[]> ret = null;
		try {
			ResultSet set = null;
			if (m_con==null){
				m_con = DriverManager.getConnection("jdbc:esproc:local://");
			}

			if (params==null||params.length==0){
				Statement st = m_con.createStatement();
				set = st.executeQuery(dfxFile);
				ret = convert(set);
				
				st.close();
			}else{
				java.sql.PreparedStatement st = m_con.prepareCall(dfxFile);
				for(int i=0; i<params.length; i++){
					//参数数组转换成序表
					if (params[i] instanceof List){
						Sequence seq = new Sequence();
						List<Object> tmp = (List<Object>)params[i];
						seq.addAll(tmp.toArray(new Object[tmp.size()]));
						st.setObject(i+1, seq);
					}else{
						st.setObject(i+1, params[i]);
					}
				}
				if (st.execute()) {
					set = st.getResultSet();
					ret = convert(set);
				}
				st.close();
			}
			//printResult(set);			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
		
		return ret;
	}
	
	public List<Object> mquery(String dfxFile){
		return mquery(dfxFile, new Object[0]);
	}
	
	//多参数列表查询
	public List<Object> mquery(String dfxFile, List<Object> params) {
		Object[] objs = params.toArray(new Object[params.size()]);
		return mquery(dfxFile, objs);
	}
	
	public List<Object> mquery(String dfxFile, Object...params ) {
		Object[] ret = null;
		List<Object> rets = new ArrayList<Object>();
		
		try {
			ResultSet set = null;
			if (m_con==null){
				m_con = DriverManager.getConnection("jdbc:esproc:local://");
			}

			if (params==null||params.length==0){
				Statement st = m_con.createStatement();
				set = st.executeQuery(dfxFile);
				do{
					set = st.getResultSet();
					ret = mconvert(set);
					rets.add(ret);
				}while (st.getMoreResults());
				
				st.close();
			}else{
				java.sql.PreparedStatement st = m_con.prepareCall(dfxFile);
				for(int i=0; i<params.length; i++){
					//参数数组转换成序表
					if (params[i] instanceof List){
						Sequence seq = new Sequence();
						List<Object> tmp = (List<Object>)params[i];
						seq.addAll(tmp.toArray(new Object[tmp.size()]));
						st.setObject(i+1, seq);
					}else{
						st.setObject(i+1, params[i]);
					}
				}
				if (st.execute()) {
					do{
						set = st.getResultSet();
						ret = mconvert(set);
						rets.add(ret);
					}while (st.getMoreResults());
				}
				st.close();
			}
			//printResult(set);			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
		
		return rets;
	}
	
	/**********************************************************
	 * 游标操作
	 * 
	 * ********************************************************/
	private void reset(){
		m_columns = null;
		m_result=null;
	}
	
	public void cursor(String dfxFile){
		cursor(dfxFile, new Object[0]);
	}	
	
	public void cursor(String dfxFile, List<Object> params) {
		Object[] objs = params.toArray(new Object[params.size()]);
		cursor(dfxFile, objs);
	}
	
	//多参数列表查询，对参数本身为数组的，且需要转换成序表
	public void cursor(String dfxFile, Object...params ) {
		try {		
			reset();
			if (m_con==null){
				m_con = DriverManager.getConnection("jdbc:esproc:local://");
			}
			if (params==null||params.length==0){
				m_st = m_con.createStatement();
				m_result = m_st.executeQuery(dfxFile);
			}else{
				m_pst = m_con.prepareCall(dfxFile);
				for(int i=0; i<params.length; i++){
					//参数数组转换成序表
					if (params[i] instanceof List){ 
						Sequence seq = new Sequence();
						List<Object> tmp = (List)params[i];
						seq.addAll(tmp.toArray(new Object[tmp.size()]));
						m_pst.setObject(i+1, seq);
					}else{
						m_pst.setObject(i+1, params[i]);
					}
				}
				if (m_pst.execute()) {
					m_result = m_pst.getResultSet();
				}
			}
			if (m_result!=null){
				ResultSetMetaData rsmd = m_result.getMetaData();
				int cc = rsmd.getColumnCount();
				m_columns = new String[cc];
				// header
				for (int i = 0; i < cc; i++) {
					m_columns[i] = rsmd.getColumnName(i+1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e.getStackTrace());
		} 
	}
	
	public boolean hasNext(){	
		try{
			if (m_result==null) {
				return false;
			}
			return !m_result.isLast();
		}catch(Exception e){			
		}
		return false;
	}
	
	public List<Object[]> fetch() {
		return fetch(1000);
	}
	
	public List<Object[]> fetch(int size) {
		List<Object[]> ret = null;
		
		try {
			if (m_result==null || size<0){
				return ret;
			}
			int curSize = 0;
			int cc = m_columns.length;
			Object[] objs = null;
			ret = new ArrayList<Object[]>();

			// data
			while (m_result.next()) {
				objs = new Object[cc];
				for (int i = 0; i < cc; i++) {
					objs[i] = m_result.getObject(i + 1);
					if (objs[i] instanceof Sequence){
						objs[i] = convertSequenceToTable((Sequence)objs[i]);
					}
				}
				ret.add(objs);
				curSize++;
				if (curSize>=size){
					break;
				}
			}

			//已经取尽了所有的记录(isLast()有时不对？)
			if (m_result.isLast() || ret.size()==0){
				close();
			}			
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		
		return ret;
	}
	
	public void close(){
		try{
			if (m_result!=null){
				m_result.close();
				m_result = null;
			}
			
			if (m_st!=null){
				m_st.close();
				m_st = null;
			}
			if (m_pst!=null){
				m_pst.close();
				m_pst = null;
			}	
			
			if (m_con != null) {
				m_con.close();
				m_con = null;
			}
			m_columns = null;
			
			//m_server.
		}catch(Exception e){
			Logger.error(e.getMessage());
		}
	}
	
	public List<Object> getList(Object o) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		if (o==null) return null;
		
		if (o instanceof Sequence){
			Sequence seq = (Sequence)o;
			for(int i=0; i<seq.length(); i++){
				Object line=seq.get(i+1);
				if (line instanceof Sequence){
					list.add( ((Sequence)line).toArray() );
				}
			}
		}
		
		return list;
	}
}
