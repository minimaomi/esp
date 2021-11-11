package com.raqsoft.lib.maths.function;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;
import java.util.*;

public class PCA {
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
    public static double[][] getVarianceMatrix(double[][] matrix) {
        int n = matrix.length;// 行数
        int m = matrix[0].length;// 列数
        double[][] result = new double[m][m];// 协方差矩阵
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                double temp = 0;
                for (int k = 0; k < n; k++) {
                    temp += matrix[k][i] * matrix[k][j];
                }
                result[i][j] = temp / (n - 1);
            }
        }
        return result;
    }

    public static double[] getEigenvalueMatrix(double[][] matrix) {
        SimpleMatrix X = new SimpleMatrix(matrix);
        SimpleEVD U = X.eig();
        Object[] aa =  U.getEigenvalues().toArray();
        //Object[] bb = getObject(aa);
        double[] cc = PCA.ejmloneObToArr(aa);
//        Arrays.sort(cc);
//        // 由特征值组成的对角矩阵,eig()获取特征值
        double[] result = new double[cc.length];
        for (int i =0; i<result.length;i++){
            result[i] = cc[result.length-1-i];
        }
        return result;
    }

    public static double[][] getEigenVectorMatrix(double[][] matrix) {
        SimpleMatrix X = new SimpleMatrix(matrix);
        SimpleEVD U = X.eig();
        EigenDecomposition aa = U.getEVD();
        double[][] result = new double[matrix.length][matrix.length];
        for (int i =0; i<result.length;i++){
            Matrix cc = aa.getEigenVector(i);
            DMatrixRMaj dd =  (DMatrixRMaj ) cc;
            double[] singleMatrix = dd.data;
            result[i] = singleMatrix;
        }
        double[][] result1 = new double[matrix.length][matrix.length];
        for (int i =0; i<result.length;i++){
            for (int j =0; j<result.length;j++){
                result1[i][result.length-1-j] = result[j][i];
            }
        }

        return result1;
    }

    public static double[] ejmloneObToArr(Object []pracdata) {

        Object[] toss = (Object[]) pracdata;
        List<Object> seconds = Arrays.asList(toss);


        double[]testData = new double[toss.length];

        for (int i =0; i< toss.length; i++) {
            Object bb = seconds.get(i);
            Complex_F64 cc = (Complex_F64) bb;
            testData[i] = cc.real;

        }
        return testData;

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

    public static  double[][] transpose(double[][] matrix) {
        double[][] arrNew=new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                arrNew[j][i]=matrix[i][j];
            }
        }
        return arrNew;
    }
    
    public static double[] getAverage(double[][] primary) {
        // 均值中心化后的矩阵
        int n = primary.length;
        int m = primary[0].length;
        double[] sum = new double[m];
        double[] average = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                sum[i] += primary[j][i];
            }
            average[i] = sum[i] / n;
        }

        return average;

    }

    public static double[][] getPrincipalComponent(double[][] primaryArray,
                                                   double[][] eigenvalue, double[][] eigenVectors,int n_components) {
        SimpleMatrix X = new SimpleMatrix(eigenVectors).transpose();
        double[][] tEigenVectors = PCA.matrix2Array(X);
//        Matrix A = new Matrix(eigenVectors);// 定义一个特征向量矩阵
//        double[][] tEigenVectors = A.transpose().getArray();// 特征向量转置
        Map<Integer, double[]> principalMap = new HashMap<Integer, double[]>();// key=主成分特征值，value=该特征值对应的特征向量
        TreeMap<Double, double[]> eigenMap = new TreeMap<Double, double[]>(
                Collections.reverseOrder());// key=特征值，value=对应的特征向量；初始化为翻转排序，使map按key值降序排列
        double total = 0;// 存储特征值总和
        int index = 0, n = eigenvalue.length;
        double[] eigenvalueArray = new double[n];// 把特征值矩阵对角线上的元素放到数组eigenvalueArray里
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j)
                    eigenvalueArray[index] = eigenvalue[i][j];
            }
            index++;
        }

        for (int i = 0; i < tEigenVectors.length; i++) {
            double[] value = new double[tEigenVectors[0].length];
            value = tEigenVectors[i];
            eigenMap.put(eigenvalueArray[i], value);
        }

        // 求特征总和
        for (int i = 0; i < n; i++) {
            total += eigenvalueArray[i];
        }
        // 选出前几个主成分
        List<Double> plist = new ArrayList<Double>();// 主成分特征值
        int now_component = 0;
        for (double key : eigenMap.keySet()) {
            if (now_component < n_components) {
                now_component += 1;
                plist.add(key);
            } else {
                break;
            }
        }

        // 往主成分map里输入数据
        for (int i = 0; i < plist.size(); i++) {
            if (eigenMap.containsKey(plist.get(i))) {
                principalMap.put(i, eigenMap.get(plist.get(i)));
            }
        }

        // 把map里的值存到二维数组里
        double[][] principalArray = new double[principalMap.size()][];
        Iterator<Map.Entry<Integer, double[]>> it = principalMap.entrySet()
                .iterator();
        for (int i = 0; it.hasNext(); i++) {
            principalArray[i] = it.next().getValue();
        }

        return principalArray;
    }

    public static  List<double[][]> merge(double[] eigenvalueMatrix,double[][] eigenVectorMatrix) {
        double[][] eigVectorT = PCA.transpose(eigenVectorMatrix);
        TreeMap<Double, double[]> eigenMap = new TreeMap<Double, double[]>(
        );//
        for (int i = 0; i < eigVectorT.length; i++) {
            double[] value = eigVectorT[i];
            eigenMap.put(eigenvalueMatrix[i], value);
        }
        double[] eigValue = new double[eigenvalueMatrix.length];
        double[][] eigValueR = new double[eigValue.length][eigValue.length];
        double[][] eigVector = new double[eigVectorT.length][eigVectorT[0].length];

        int i = 0;
        for (double key : eigenMap.keySet()) {
            eigValue[i] = key;
            eigVector[i] =   eigenMap.get(key);
            i++;
        }
        for (int j =0; j<eigValueR.length;j++){
            eigValueR[j][j] = eigValue[j];
        }
        double[][] eigVectorR = PCA.transpose(eigVector);

        List<double[][]> result = new ArrayList<double[][]>();
        result.add(eigValueR);
        result.add(eigVectorR);
        return result;
    }

    public static Object[] mergeObject(Object one,Object two) {
        List<Object> newl = new ArrayList<>();
        newl.add(one);
        newl.add(two);

        return newl.toArray();
    }

    // 训练方法
    public static void fit(double[][] inputData, int n_components) {
        double[][] averageArray = PCA.changeAverageToZero(inputData);
        double[][] varMatrix = PCA.getVarianceMatrix(averageArray);
        double[] eigValueR = PCA.getEigenvalueMatrix(varMatrix);
        double[][] eigVectorR = PCA.getEigenVectorMatrix(varMatrix);

        List<double[][]>  resultList =  merge(eigValueR,eigVectorR);
        double[][] eigenvalueMatrix = resultList.get(0);
        double[][] eigenVectorMatrix = resultList.get(1);
        double[][] principalArray = PCA.getPrincipalComponent(inputData, eigenvalueMatrix, eigenVectorMatrix,n_components);
    }
    
    // 集算器使用训练方法
    public static Object[] fitj(double[][] data, int n_components) {
    	double[] averageVector = getAverage(data);
        double[][] averageArray = PCA.changeAverageToZero(data);
        double[][] varMatrix = PCA.getVarianceMatrix(averageArray);
        double[] eigValueR = PCA.getEigenvalueMatrix(varMatrix);

        double[][] eigVectorR = PCA.getEigenVectorMatrix(varMatrix);
        List<double[][]>  resultList =  merge(eigValueR,eigVectorR);
        double[][] eigenvalueMatrix = resultList.get(0);
        double[][] eigenVectorMatrix = resultList.get(1);
        double[][] principalArray = PCA.getPrincipalComponent(data, eigenvalueMatrix, eigenVectorMatrix,n_components);
        Object[] result = mergeObject(averageVector,principalArray);
        return result;
    }

    // 转换方法
    public static double[][] transform(double[][] principalDouble,double[] averageObject,double[][] testinput){
        double[][] p = principalDouble;
        double[][] t = testinput;
        double[] average =averageObject;
        int n = t.length;
        int m = t[0].length;
        double[][] averageArray = new double[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                averageArray[j][i] = t[j][i] - average[i];
            }
        }
        SimpleMatrix principalMatrix = new SimpleMatrix(p);
        SimpleMatrix primaryMatrix = new SimpleMatrix(averageArray);
        SimpleMatrix resultMatrix = primaryMatrix.mult(principalMatrix.transpose());
        double[][] d =  matrix2Array(resultMatrix);
        return d;
    }

    //  计算器使用转换方法
    public static double[][] transformj(Object[] inputObject,double[][] testinput) {
        double[][] p = (double[][])inputObject[1];
        double[] average =(double[])inputObject[0];
        double[][] t = testinput;
        int n = t.length;
        int m = t[0].length;
        double[][] averageArray = new double[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                averageArray[j][i] = t[j][i] - average[i];
            }
        }
        SimpleMatrix principalMatrix = new SimpleMatrix(p);
        SimpleMatrix primaryMatrix = new SimpleMatrix(averageArray);
        SimpleMatrix resultMatrix = primaryMatrix.mult(principalMatrix.transpose());
        double[][] d =  matrix2Array(resultMatrix);
        
        return d;
    }

}

