package com.raqsoft.lib.maths.function;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Table;
import com.raqsoft.lib.maths.function.PCA;
import com.raqsoft.resources.EngineMessage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ImPca extends ImFunction {	
    public static double[][] readCSV(String pFilename, int rows, int cols) throws NumberFormatException, IOException {

        double[][] arr = new double[rows][cols];
        BufferedReader br = new BufferedReader(new FileReader(pFilename));
        String line = " ";

        int i = -1;
        while ((line = br.readLine()) != null && i < arr.length) {
            String[] temp = line.split(","); //split spaces
            ++i;

            for (int j = 0; j < arr[i].length; j++) {
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
				throw new RQException("Maths " + mm.getMessage("PCA param error"));
			}
			Table table = new Table(); 
			if (objs[0] instanceof Table){
				table = (Table)objs[0];
			}else if(objs[0] instanceof String){
				String sFile = (String)objs[0];
				table = ImUtils.getTable(sFile);
			}else{
				MessageManager mm = EngineMessage.get();
				throw new RQException("Maths " + mm.getMessage("PCA param error2"));
			}			
		
			if(table==null || table.length()==0){
				MessageManager mm = EngineMessage.get();
				throw new RQException("Maths " + mm.getMessage("No data"));
			}
			
			int components = 10;
			Object[] pM = null;
			double[][] primaryArray = ImUtils.convertTable(table);
			if (objs[1] instanceof Integer){
				components = Integer.parseInt(objs[1].toString());				
				pM = PCA.fitj(primaryArray, components);
			}else if(objs[1] instanceof Object[]){
				pM = (Object[])objs[1];
			}else{
				MessageManager mm = EngineMessage.get();
				throw new RQException("Maths " + mm.getMessage("No data"));
			}
			
			double[][] result = PCA.transformj(pM, primaryArray);
	        	        
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

    public static void main(String[] args) throws NumberFormatException, IOException {
        //double[][] primaryArray = readCSV("tj.csv",111,1555);
    	//pca_ejml.fit(primaryArray,10);
        // ----------------------
        double start = System.currentTimeMillis();
        double[][] primaryArray = {{0.60177289, 0.9746818, 0.99411279, 0.47628226, 0.30623449, 0.82174401, 0.43082238, 0.62836501, 0.05575921, 0.87910952},
                {0.94389766, 0.31591178, 0.5140025, 0.93362612, 0.79677332, 0.16447334, 0.44477391, 0.7770786, 0.60998472, 0.75808904},
                {0.33664828, 0.64493414, 0.76665906, 0.96073503, 0.38936551, 0.79710539, 0.27011443, 0.16456043, 0.63350509, 0.71172067},
                {0.01767005, 0.22288785, 0.13248754, 0.43320863, 0.20743428, 0.33730712, 0.15836896, 0.47770804, 0.44583237, 0.43780899},
                {0.07120606, 0.79340919, 0.46762323, 0.39538276, 0.92212634, 0.43033875, 0.66431554, 0.15921755, 0.85631475, 0.43927297},
                {0.74482916, 0.52055665, 0.427618, 0.06391209, 0.87903509, 0.05148449, 0.33894076, 0.00532227, 0.62918552, 0.19128502},
                {0.22184429, 0.94851998, 0.56542577, 0.6530585, 0.36367859, 0.56006887, 0.97858404, 0.70963757, 0.90077766, 0.2221411},
                {0.00954926, 0.1112845, 0.95355775, 0.41626527, 0.29201236, 0.73247885, 0.36841529, 0.38454106, 0.71186033, 0.22540936},
                {0.63824226, 0.90183385, 0.36368713, 0.6310758, 0.82590917, 0.05712913, 0.57825899, 0.02958154, 0.5063993, 0.56822094},
                {0.7775431, 0.17250066, 0.86923559, 0.80743574, 0.36602709, 0.19102177, 0.72817121, 0.75391726, 0.9597165, 0.88743949}};

        Object[] pM = PCA.fitj(primaryArray, 9);
//        double[] fit = (double[])pM[0];
//        String[] cols = new String[fit.length];
//        for(int i=0; i<fit.length; i++){
//        	cols[i] = String.format("_%d", 1+i);
//        }
//        double[][] result = (double[][])pM[1];
//        Table tbl = new Table(cols);
//        for(int i=0; i<result.length; i++){
//        	double[] lines = result[i];
//        	Object[] r = new Object[lines.length];
//        	for(int j=0; j<lines.length; j++){
//	        	r[j] = lines[j];
//        	}
//        	tbl.newLast(r);
//        }
//        
//        System.out.println();
        
        double[][] result = PCA.transformj(pM, primaryArray);

        for(int i=0; i<result.length; i++){
        	for(int j=0; j<result[i].length; j++){
        		System.out.print(result[i][j]+" ");
        	}
        	System.out.println();
        }
    }
}
