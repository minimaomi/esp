package com.raqsoft.lib.maths.function;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Table;
import com.raqsoft.lib.maths.function.PLS;
import com.raqsoft.resources.EngineMessage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ImPls extends ImFunction {	
    public static double[][]  readCSV(String pFilename,int rows,int cols) throws NumberFormatException, IOException {

        double[][] arr = new double[rows][cols];
        BufferedReader br = new BufferedReader(new FileReader(pFilename));
        String line = " ";

        int i = -1;
        while ((line = br.readLine())!= null && i < arr.length){
            String [] temp = line.split(","); //split spaces
            ++i;

            for (int j = 0; j<arr[i].length; j++) {
                arr[i][j] = Double.parseDouble(temp[j]);
            }

        }

        br.close();
        return arr;
    }

    public Object doQuery(Object[] objs){
		try {
			if (objs.length<2){
				MessageManager mm = EngineMessage.get();
				throw new RQException("Maths " + mm.getMessage("PLS param error"));
			}
			Table table = new Table(); 
			if (objs[0] instanceof Table){
				table = (Table)objs[0];
			}else if(objs[0] instanceof String){
				String sFile = (String)objs[0];
				table = ImUtils.getTable(sFile);
			}else{
				MessageManager mm = EngineMessage.get();
				throw new RQException("Maths " + mm.getMessage("PLS param error"));
			}
			
			if(table==null || table.length()==0){
				MessageManager mm = EngineMessage.get();
				throw new RQException("Maths " + mm.getMessage("No data"));
			}

			double[] coef_ = null;
			if(objs[1] instanceof double[]){
				coef_ = (double[])objs[1];
			}else{
				MessageManager mm = EngineMessage.get();
				throw new RQException("Maths " + mm.getMessage("No coef data"));
			}
			double[][] arrX = ImUtils.convertTable(table);
	        double[][] result = PLS.predictY(arrX,coef_);
	        
	        // set columns
	        String[] cols = new String[]{"Value"};
	        int nCol = result[0].length;
	        if (nCol>1){
	        	cols = new String[nCol];
		        for(int i=0; i<nCol; i++){
		        	cols[i] = "_"+(i+1);
		        }
	        }
	        Table tbl = new Table(cols);
	        for(int i=0; i<result.length; i++){
	        	double[] lines = result[i];
	        	Object[] r = new Object[lines.length];
	        	for(int j=0; j<lines.length; j++){
		        	r[j] = lines[j];
	        	}
	        	tbl.newLast(r);
	        }

	        return tbl;

		} catch (Exception e) {
			throw new RQException(e.getMessage());
		} 
	}

    
    public static void main(String[] args) throws NumberFormatException, IOException{
        double[][] x = readCSV("D:/tmp/zhh/pls/test20_20.csv",20,20);
        double[][] y = readCSV("D:/tmp/zhh/pls/testy.csv",20,1);

        if (1==2){
//	        Object[] X_object = PLS.ArrToOb(x);
//	        Object[] y_object = PLS.ArrToOb(y);
//	        PLS z = new PLS();
//	        Object[] coef = z.fitt(X_object,y_object,5);
//	        result = z.predictt(X_object,coef);
        }else{
        	double[] coef_ = PLS.fit(x,y, 10);
	        double[][] result = PLS.predictY(x,coef_);
	        for(int i=0; i<result.length; i++){
	        	for(int j=0; j<result[i].length; j++){
	        		System.out.print(result[i][j]+" ");
	        	}
	        	System.out.println();
	        }
	        System.out.println();
        }
    }
    
}