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
        int n = matrix.length;// ����
        int m = matrix[0].length;// ����
        double[][] result = new double[m][m];// Э�������
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
//        // ������ֵ��ɵĶԽǾ���,eig()��ȡ����ֵ
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
        // ��ֵ���Ļ���ľ���
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
//        Matrix A = new Matrix(eigenVectors);// ����һ��������������
//        double[][] tEigenVectors = A.transpose().getArray();// ��������ת��
        Map<Integer, double[]> principalMap = new HashMap<Integer, double[]>();// key=���ɷ�����ֵ��value=������ֵ��Ӧ����������
        TreeMap<Double, double[]> eigenMap = new TreeMap<Double, double[]>(
                Collections.reverseOrder());// key=����ֵ��value=��Ӧ��������������ʼ��Ϊ��ת����ʹmap��keyֵ��������
        double total = 0;// �洢����ֵ�ܺ�
        int index = 0, n = eigenvalue.length;
        double[] eigenvalueArray = new double[n];// ������ֵ����Խ����ϵ�Ԫ�طŵ�����eigenvalueArray��
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

        // �������ܺ�
        for (int i = 0; i < n; i++) {
            total += eigenvalueArray[i];
        }
        // ѡ��ǰ�������ɷ�
        List<Double> plist = new ArrayList<Double>();// ���ɷ�����ֵ
        int now_component = 0;
        for (double key : eigenMap.keySet()) {
            if (now_component < n_components) {
                now_component += 1;
                plist.add(key);
            } else {
                break;
            }
        }

        // �����ɷ�map����������
        for (int i = 0; i < plist.size(); i++) {
            if (eigenMap.containsKey(plist.get(i))) {
                principalMap.put(i, eigenMap.get(plist.get(i)));
            }
        }

        // ��map���ֵ�浽��ά������
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

    // ѵ������
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
    
    // ������ʹ��ѵ������
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

    // ת������
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

    //  ������ʹ��ת������
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

