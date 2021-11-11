package com.raqsoft.lib.maths.function;

import java.io.IOException;

import com.raqsoft.common.RQException;
import com.raqsoft.dm.FileObject;
import com.raqsoft.dm.Record;
import com.raqsoft.dm.Table;
import com.raqsoft.dm.cursor.FileCursor;

public class ImUtils {
	
	public static Object[] splitTable(Table table) throws NumberFormatException, IOException {
		if (table==null) return null;
		
    	int rows = table.length();
    	int cols = table.dataStruct().getFieldCount();
    	double[][] arrX = new double[rows][cols-1];
    	double[][] arrY = new double[rows][1];

        for(int i = 0; i<rows; i++){
            Record r = (Record)table.get(i+1);
            if (r.getFieldValue(0) instanceof Double){
            	arrY[i][0] = (Double)r.getFieldValue(0);
            }else if (r.getFieldValue(0) instanceof Integer){
            	arrY[i][0] = Double.parseDouble(r.getFieldValue(0).toString());
            }else if (r.getFieldValue(0) instanceof String){
            	arrY[i][0] = Double.parseDouble(r.getFieldValue(0).toString());
            }
            for (int j = 1; j<r.getFieldCount(); j++) {
            	if (r.getFieldValue(j) instanceof Double){
            		arrX[i][j-1] = (Double)r.getFieldValue(j);
                }else if (r.getFieldValue(j) instanceof Integer){
                	arrX[i][j-1] = Double.parseDouble(r.getFieldValue(j).toString());
                }else if (r.getFieldValue(j) instanceof String){
                	arrX[i][j-1] = Double.parseDouble(r.getFieldValue(j).toString());
                }
            }
        }
        Object[] objs = new  Object[2];
        objs[0] = arrX;
        objs[1] = arrY;
        
        return objs;
    }
	
	public static double[][] convertTable(Table table) throws NumberFormatException, IOException {
		if (table==null) return null;
		
    	int rows = table.length();
    	int cols = table.dataStruct().getFieldCount();
    	double[][] arrX = new double[rows][cols];

        for(int i = 0; i<rows; i++){
            Record r = (Record)table.get(i+1);
            
            for (int j = 0; j<r.getFieldCount(); j++) {
            	if (r.getFieldValue(j) instanceof Double){
            		arrX[i][j] = (Double)r.getFieldValue(j);
                }else if (r.getFieldValue(j) instanceof Integer){
                	arrX[i][j] = Double.parseDouble(r.getFieldValue(j).toString());
                }else if (r.getFieldValue(j) instanceof String){
                	arrX[i][j] = Double.parseDouble(r.getFieldValue(j).toString());
                }
            }
        }
        
        return arrX;
    }
	
	public static Table getTable(String sFile){
    	Table table = null;
    	try{
	    	FileObject f = new FileObject(sFile);
			
			if ( f.isExists()){
				;
			}else{
				throw new RQException("file:"+sFile+" not existed");
			}
			
			if (sFile.endsWith(".csv")){
				String param = "qc";
				// 生成游标
				FileCursor cursor = new FileCursor(f, 0, -1, null, param, null);
				// 预读数据
				table = (Table) cursor.fetch(cursor.getFetchCount());
			}
    	}catch(Exception e){
    		throw new RQException(e.getMessage());
    	}
    	return table;
    }
}
