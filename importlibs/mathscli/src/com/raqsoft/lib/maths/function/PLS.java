package com.raqsoft.lib.maths.function;

import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class PLS {
    public static double[] fit(double[][] X_input,double[][] Y_input,int components){
        double[][] X = changeAverageToZero(X_input);
        double[][] Y = changeAverageToZero(Y_input);
        SimpleMatrix X_matrix = new SimpleMatrix(X);
        SimpleMatrix Y_matrix = new SimpleMatrix(Y);
        SimpleMatrix x_scores_store = new SimpleMatrix(new double[X_input.length][components]);
        SimpleMatrix y_scores_store = new SimpleMatrix(new double[X_input.length][components]);
        SimpleMatrix x_weights_store = new SimpleMatrix(new double[X_input[0].length][components]);
        SimpleMatrix y_weights_store = new SimpleMatrix(new double[Y_input[0].length][components]);
        SimpleMatrix x_loadings_store = new SimpleMatrix(new double[X_input[0].length][components]);
        SimpleMatrix y_loadings_store = new SimpleMatrix(new double[Y_input[0].length][components]);
        for (int k=0;k<components;k++) {
            //内部循环，估计权重
            SimpleMatrix ii = X_matrix.transpose().mult(Y_matrix);
            List<double[][]> svd_result = svd_cross_product(ii);
            double[][] x_weights = svd_result.get(0);
            double[][] y_weights = svd_result.get(1);
            SimpleMatrix x_weights_matrix = new SimpleMatrix(x_weights);
            SimpleMatrix y_weights_matrix = new SimpleMatrix(y_weights);

            //计算得分
            SimpleMatrix x_scores_matrix = X_matrix.mult(x_weights_matrix);
            SimpleMatrix y_scores_matrix = Y_matrix;

            //缩放
            SimpleMatrix x_loadings_matrix = ((X_matrix.transpose()).mult(x_scores_matrix)).divide(x_scores_matrix.transpose().mult(x_scores_matrix).get(0, 0));
            SimpleMatrix a = x_scores_matrix.mult(x_loadings_matrix.transpose());
            X_matrix = X_matrix.minus(a);

            SimpleMatrix y_loadings = Y_matrix.transpose().mult(x_scores_matrix).divide(x_scores_matrix.transpose().mult(x_scores_matrix).get(0, 0));
            Y_matrix = Y_matrix.minus(x_scores_matrix.mult(y_loadings.transpose()));

            //存储weights，scores，loadings

            ravel(x_scores_store,x_scores_matrix,k);
            ravel(y_scores_store,y_scores_matrix,k);
            ravel(x_weights_store,x_weights_matrix,k);
            ravel(y_weights_store,y_weights_matrix,k);
            ravel(x_loadings_store,x_loadings_matrix,k);
            ravel(y_loadings_store,y_loadings,k);

        }
        SimpleMatrix pinv2_matrix = pinv2(x_loadings_store.transpose().mult(x_weights_store));
        SimpleMatrix x_rotations_matrix =x_weights_store.mult(pinv2_matrix);

        SimpleMatrix coef_ = x_rotations_matrix.mult(y_loadings_store.transpose());

        double[] coef_array = matrix2Array(coef_.transpose())[0];
        double b = compute_b(X_input,coef_,Y_input);
        double[] coef = new double[coef_array.length+1];
        coef[0] = b;
        for (int i=1;i< coef_array.length+1;i++) {
            coef[i] = coef_array[i-1];
        }
        return coef;
    }

    public static double[][] predictY(double[][] X_input ,double[] coef){
        double b0 = coef[0];
        double[][] b = new double[X_input.length][1];
        for (int i= 0;i< X_input.length;i++){
            b[i][0] = b0;
        }

        double[][] coef_other = new double[coef.length-1][1];
        for (int i =0;i<coef.length-1;i++){
            coef_other[i][0] = coef[i+1];
        }

        SimpleMatrix b_matrix = new SimpleMatrix(b);
        SimpleMatrix coef_other_matrix = new SimpleMatrix(coef_other);
        SimpleMatrix X_matrix = new SimpleMatrix(X_input);
        SimpleMatrix Ypred = X_matrix.mult(coef_other_matrix).plus(b_matrix);
        return matrix2Array(Ypred);
    }

    public static double compute_b(double[][] X_input, SimpleMatrix coef_matrix,double[][] Y_input){
        double[][] X1 = X_input.clone();//复制
        double[][] X2 = changeAverageToZero(X1);//减均值

        SimpleMatrix X1_matrix = new SimpleMatrix(X1);
        SimpleMatrix X2_matrix = new SimpleMatrix(X2);

        double[] sum = new double[Y_input.length];
        double[] y_mean = new double[Y_input.length];
        for (int i = 0; i < Y_input.length; i++) {
            for (int j = 0; j < Y_input[0].length; j++) {
                sum[0] += Y_input[i][j];
            }
            y_mean[0] = sum[0] / Y_input.length;
        }
        double b = X2_matrix.mult(coef_matrix).get(0,0) + y_mean[0] -X1_matrix.mult(coef_matrix).get(0,0);
        return b;
    }

    public static  SimpleMatrix pinv2(SimpleMatrix input) {
        List<double[][]> uvh = svd_cross_product(input);
        double[][] u = uvh.get(0);
        double[][] vh = uvh.get(1);
        double[] s_tmp = get_singularValues(input);
        Arrays.sort(s_tmp);
        double[] s = new double[s_tmp.length];
        for (int i=0;i<s_tmp.length;i++) s[i]= s_tmp[s_tmp.length-1-i];
        
        DoubleSummaryStatistics stat = Arrays.stream(s).summaryStatistics();
        double cond = 0.0000000000000001;
        int rank = s.length;
        u = slice_cols(u,0,rank);
        u = two_array_divide(u,s);

        SimpleMatrix u_matrix = new SimpleMatrix(u);
        SimpleMatrix vh_matrix = new SimpleMatrix(vh).transpose();
        SimpleMatrix result = u_matrix.mult(vh_matrix).transpose();

        return result;
    }

	public static double[][] two_array_divide(double[][] input, double[] divied) {
	        double[][] result = new double[input.length][divied.length];
	    for (int i = 0;i<input.length;i++){
	        for (int j = 0; j < divied.length; j++) {
	            result[i][j] = input[i][j]/divied[j];
	        }
	    }
	    return result;
	}


    public static double[][] slice_cols(double[][] input,int s,int e){
        double[][] result = new double[input.length][e-s];
        for (int i = 0;i<input.length;i++){
            for (int j = s; j < e; j++) {
                result[i][j-s] = input[i][j];
            }
        }
        return result;
    }

    public static void ravel(SimpleMatrix x_store,SimpleMatrix x_before,int k){
        for (int i=0 ;i < x_store.numRows();i++){
            x_store.set(i,k,x_before.get(i,0));
        }
    }

    public static  double[][] matrix2Array(SimpleMatrix matrix) {
        double[][] array = new double[matrix.numRows()][matrix.numCols()];
        for (int r = 0; r < matrix.numRows(); r++) {
            for (int c = 0; c < matrix.numCols(); c++) {
                array[r][c] = matrix.get(r, c);
            }
        }
        return array;
    }

//    public static int largest(double[][] matrix){
//        int rows = matrix.length;;
//        int cols = matrix[0].length;
//        int index = 0;
//        double max = 0;
//        for (int i = 0; i<cols;i++){
//            double squareSum = 0.0;
//            for (int j = 0;j<rows;j++){
//                squareSum = squareSum +matrix[j][i] * matrix[j][i];
//            }
//            if (squareSum > max){
//                max = squareSum;
//                index = i;
//            }
//        }
//        return index;
//    }

//    public static double Euclidean(double[] vect){
//        double result = 0;
//        for (int i = 0; i< vect.length;i++){
//            result = result +Math.pow(vect[i] ,2);
//        }
//        result = Math.sqrt(result);
//        return result;
//    }

    public static double MACHEPS = 2E-16;
//    public static SimpleMatrix pinv(SimpleMatrix x){
////        if (x.rank() < 1) {return null;}
//        if (x.numCols() > x.numRows()){
//            return pinv(x.transpose()).transpose();
//        }
//        SimpleSVD svd = x.svd();
//        double[] singularValues = svd.getSingularValues();
//        double tol = Math.max(x.numRows(),x.numRows())*singularValues[0] *MACHEPS;
//        double[] singularValuesRecip = new double[singularValues.length];
//        for (int i = 0;i< singularValues.length;i++){
//            singularValuesRecip[i] = Math.abs(singularValues[i]) < tol ? 0:(1.0/singularValues[i]);
//        }
//        SimpleBase u = svd.getU();
//        SimpleMatrix uu = (SimpleMatrix) u;
//        double[][] U = chooseU(matrix2Array(uu), x.numRows(), x.numCols());
//
//
//        SimpleBase v = svd.getV();
//        SimpleMatrix vv = (SimpleMatrix) v;
//        double[][] V = matrix2Array(vv);
//
//        int min = Math.min(x.numCols(), U[0].length);
//        double[][] inverse = new double[x.numCols()][x.numRows()];
//        for (int i = 0; i < x.numCols(); i++){
//            for (int j = 0; j < U.length; j++){
//                for (int k = 0; k < min; k++){
//                    inverse[i][j] += V[i][k] * singularValuesRecip[k] * U[j][k];
//                }
//            }
//        }
//        return new SimpleMatrix((inverse));
//    }


    public static double[][] chooseU(double[][] U, int m,int n){
        int cols = Math.min(m+1,n);
        double[][] result = new double[m][cols];
        for (int i=0;i<m;i++){
            for (int j=0;j<cols;j++){
                result[i][j] = U[i][j];
            }
        }
        return result;
    }


    public static double[][] changeAverageToZero(double[][] primary) {
        int n = primary.length;
        int m = primary[0].length;
        double[] sum = new double[m];
        double[] average = new double[m];
        double[][] averageArray = new double[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                sum[i] += primary[j][i];
            }
            average[i] = sum[i] / n;
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                averageArray[j][i] = primary[j][i] - average[i];
            }
        }
        return averageArray;
    }

    public static double[] get_singularValues(SimpleMatrix x){
        SimpleSVD svd = x.svd();
        double[] singularValues = svd.getSingularValues();
        double tol = Math.max(x.numRows(),x.numRows())*singularValues[0] *MACHEPS;
        double[] singularValuesRecip = new double[singularValues.length];
        for (int i = 0;i< singularValues.length;i++){
            singularValuesRecip[i] = Math.abs(singularValues[i]) < tol ? 0:(1.0/singularValues[i]);
        }
        SimpleBase u = svd.getU();
        SimpleMatrix uu = (SimpleMatrix) u;
        double[][] U = chooseU(matrix2Array(uu), x.numRows(), x.numCols());

        SimpleBase v = svd.getV();
        SimpleMatrix vv = (SimpleMatrix) v;
        double[][] V = matrix2Array(vv);
        Arrays.sort(singularValues);
        return singularValues;
    }
    
    public static List<double[][]> svd_cross_product(SimpleMatrix x){
        SimpleSVD svd = x.svd();
        double[] singularValues = svd.getSingularValues();
        double tol = Math.max(x.numRows(),x.numRows())*singularValues[0] *MACHEPS;
        double[] singularValuesRecip = new double[singularValues.length];
        for (int i = 0;i< singularValues.length;i++){
            singularValuesRecip[i] = Math.abs(singularValues[i]) < tol ? 0:(1.0/singularValues[i]);
        }
        SimpleBase u = svd.getU();
        SimpleMatrix uu = (SimpleMatrix) u;
        double[][] U = chooseU(matrix2Array(uu), x.numRows(), x.numCols());

        SimpleBase v = svd.getV();
        SimpleMatrix vv = (SimpleMatrix) v;
        double[][] V = matrix2Array(vv);
        return Arrays.asList(U,V);
    }

}
