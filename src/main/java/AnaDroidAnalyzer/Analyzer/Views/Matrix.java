package AnaDroidAnalyzer.Analyzer.Views;

import java.lang.reflect.Array;

public class Matrix<E> {

    private E [][] matrix;
    private int rowLen;
    private int columnLen;


    public Matrix(E[][] matrix) {
        this.rowLen = matrix.length;
        this.columnLen = matrix[0].length;
        @SuppressWarnings("unchecked")
        E[][] array = (E[][])new Object[rowLen][columnLen];
        this.matrix= array;
        for (int i = 0; i < matrix.length ; i++) {
            for (int j = 0; j < columnLen ; j++) {
                this.matrix[i][j] = matrix[i][j];
            }
        }
    }

    public E[][] getMatrix() {
        return matrix;
    }

    public int getRowLen() {
        return rowLen;
    }

    public int getColumnLen() {
        return columnLen;
    }
}
