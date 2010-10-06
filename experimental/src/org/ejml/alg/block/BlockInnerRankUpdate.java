/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.block;

import org.ejml.data.D1Matrix64F;
import org.ejml.data.D1Submatrix64F;


/**
 * Performs rank-n update operations on the inner blocks of a {@link org.ejml.data.BlockMatrix64F}
 *
 * It is assumed and not checked that the submatrices are aligned along the matrix's blocks.
 *
 * @author Peter Abeles
 */
public class BlockInnerRankUpdate {

    /**
     * <p>
     * Performs:<br>
     * <br>
     * A = A + &alpha; B <sup>T</sup>B
     * </p>
     * 
     * @param blockLength Size of the block in the block matrix.
     * @param alpha scaling factor for right hand side.
     * @param A Block aligned submatrix.
     * @param B Block aligned submatrix.
     */
    public static void rankNUpdate( int blockLength , double alpha ,
                                    D1Submatrix64F A , D1Submatrix64F B )
    {

        int heightB = B.row1-B.row0;
        if( heightB > blockLength )
            throw new IllegalArgumentException("Height of B cannot be greater than the block length");

        int N = B.col1-B.col0;

        if( A.col1-A.col0 != N )
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's width");
        if( A.row1-A.row0 != N )
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's width");

        for( int i = B.col0; i < B.col1; i += blockLength ) {

            int indexB_i = B.row0*B.original.numCols + i*heightB;
            int widthB_i = Math.min(blockLength,B.col1-i);

            int rowA = i-B.col0+A.row0;
            int heightA = Math.min( blockLength , A.row1 - rowA);

            for( int j = B.col0; j < B.col1; j += blockLength ) {

                int widthB_j = Math.min(blockLength,B.col1-j);

                int indexA = rowA * A.original.numCols + (j-B.col0+A.col0)*heightA;
                int indexB_j = B.row0*B.original.numCols + j*heightB;


                BlockInnerMultiplication.multTransABlockAdd(alpha,
                        B.original,B.original,A.original,
                        indexB_i,indexB_j,indexA,heightB,widthB_i,widthB_j);
            }
        }
    }

    /**
     * <p>
     * Rank N update function for a symmetric inner submatrix and only operates on the upper
     * triangular portion of the submatrix.<br>
     * <br>
     * A = A - B <sup>T</sup>B
     * </p>
     */
    public static void symmRankNMinus_U( int blockLength ,
                                          D1Submatrix64F A , D1Submatrix64F B )
    {

        int heightB = B.row1-B.row0;
        if( heightB > blockLength )
            throw new IllegalArgumentException("Height of B cannot be greater than the block length");

        int N = B.col1-B.col0;

        if( A.col1-A.col0 != N )
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's width");
        if( A.row1-A.row0 != N )
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's width");


        for( int i = B.col0; i < B.col1; i += blockLength ) {

            int indexB_i = B.row0*B.original.numCols + i*heightB;
            int widthB_i = Math.min(blockLength,B.col1-i);

            int rowA = i-B.col0+A.row0;
            int heightA = Math.min( blockLength , A.row1 - rowA);

            for( int j = i; j < B.col1; j += blockLength ) {

                int widthB_j = Math.min(blockLength,B.col1-j);

                int indexA = rowA * A.original.numCols + (j-B.col0+A.col0)*heightA;
                int indexB_j = B.row0*B.original.numCols + j*heightB;

                if( i == j ) {
                    // only the upper portion of this block needs to be modified since it is along a diagonal
                    multTransABlockMinus_U( B.original,A.original,
                            indexB_i,indexB_j,indexA,heightB,widthB_i,widthB_j);
                } else {
                    multTransABlockMinus( B.original,A.original,
                            indexB_i,indexB_j,indexA,heightB,widthB_i,widthB_j);
                }
            }
        }
    }

    /**
     * <p>
     * Rank N update function for a symmetric inner submatrix and only operates on the lower
     * triangular portion of the submatrix.<br>
     * <br>
     * A = A - B*B<sup>T</sup><br>
     * </p>
     */
    public static void symmRankNMinus_L( int blockLength ,
                                         D1Submatrix64F A , D1Submatrix64F B )
    {
        int widthB = B.col1-B.col0;
        if( widthB > blockLength )
            throw new IllegalArgumentException("Width of B cannot be greater than the block length");

        int N = B.row1-B.row0;

        if( A.col1-A.col0 != N )
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's height");
        if( A.row1-A.row0 != N )
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's height");

        for( int i = B.row0; i < B.row1; i += blockLength ) {


            int heightB_i = Math.min(blockLength,B.row1-i);
            int indexB_i = i*B.original.numCols + heightB_i*B.col0;

            int rowA = i-B.row0+A.row0;
            int heightA = Math.min( blockLength , A.row1 - rowA);

            for( int j = B.row0; j <= i; j += blockLength ) {
                // todo j == i do only a triangle block

                int widthB_j = Math.min(blockLength,B.row1-j);

                int indexA = rowA * A.original.numCols + (j-B.row0+A.col0)*heightA;
                int indexB_j = j*B.original.numCols + widthB_j*B.col0;

                if( i == j ) {
                    multTransBBlockMinus_L( B.original,A.original,
                            indexB_i,indexB_j,indexA,widthB,heightB_i,widthB_j);
                } else {
                    multTransBBlockMinus( B.original,A.original,
                            indexB_i,indexB_j,indexA,widthB,heightB_i,widthB_j);
                }
            }
        }
    }

    /**
     * <p>
     * Performs the following operation on a block:<br>
     * <br>
     * c = c - a<sup>T</sup>a<br>
     * </p>
     */
    protected static void multTransABlockMinus( D1Matrix64F A , D1Matrix64F C,
                                                int indexA, int indexB, int indexC,
                                                final int heightA, final int widthA, final int widthC ) {
//        for( int i = 0; i < widthA; i++ ) {
//            for( int k = 0; k < heightA; k++ ) {
//
//                double valA = dataA[k*widthA + i + indexA];
//                for( int j = 0; j < widthC; j++ ) {
//                    dataC[ i*widthC + j + indexC ] -= valA * dataA[k*widthC + j + indexB];
//                }
//            }
//        }

        for( int k = 0; k < heightA; k++ ) {
            int a = k*widthA + indexA;
            int c = indexC;
            int endA = a + widthA;

            int rowB = k*widthC + indexB;
            int endB = rowB + widthC;
            while( a != endA ) {
                double valA = A.get(a++);

                int b = rowB;
                while( b != endB ) {
                    C.minus( c++ , valA * A.get(b++));
                }
            }
        }
    }

    /**
     * <p>
     * Performs the following operation on the upper triangular portion of a block:<br>
     * <br>
     * c = c - a<sup>T</sup>a<br>
     * </p>
     */
    protected static void multTransABlockMinus_U( D1Matrix64F A, D1Matrix64F C,
                                                  int indexA, int indexB, int indexC,
                                                  final int heightA, final int widthA, final int widthC ) {
//        for( int i = 0; i < widthA; i++ ) {
//            for( int k = 0; k < heightA; k++ ) {
//
//                double valA = dataA[k*widthA + i + indexA];
//                for( int j = i; j < widthC; j++ ) {
//                    dataC[ i*widthC + j + indexC ] -= valA * dataA[k*widthC + j + indexB];
//                }
//            }
//        }

        for( int i = 0; i < widthA; i++ ) {
            for( int k = 0; k < heightA; k++ ) {

                double valA = A.get(k*widthA + i + indexA );
                int b = k*widthC + indexB + i;
                int c = i*widthC + indexC + i;

                int endC = (c-i)+widthC;

                while( c != endC ) {
//                for( int j = i; j < widthC; j++ ) {
                    C.minus( c++ , valA * A.get(b++) );
                }
            }
        }
    }

    /**
     * <p>
     * Performs the following operation on a block:<br>
     * <br>
     * c = c - a*a<sup>T</sup><br>
     * </p>
     */
    protected static void multTransBBlockMinus( D1Matrix64F A, D1Matrix64F C,
                                                int indexA, int indexB, int indexC,
                                                final int widthA, final int heightA, final int widthC ) {
//        for( int i = 0; i < heightA; i++ ) {
//            for( int j = 0; j < widthC; j++ ) {
//                double sum = 0;
//                for( int k = 0; k < widthA; k++ ) {
//                    sum += dataA[i*widthA + k + indexA] * dataA[j*widthA + k + indexB];
//                }
//                dataC[ i*widthC + j + indexC ] -= sum;
//            }
//        }
        for( int i = 0; i < heightA; i++ ) {
            int rowA = i*widthA+indexA;
            int endA = rowA + widthA;
            int rowB = indexB;
            int rowC = i*widthC + indexC;
            for( int j = 0; j < widthC; j++ , rowB += widthA) {
                double sum = 0;

                int a = rowA;
                int b = rowB;

                while( a != endA ) {
                    sum += A.get(a++) * A.get(b++);
                }
                C.minus( rowC + j , sum );
            }
        }
    }

    /**
     * <p>
     * Performs the following operation on the lower triangular portion of a block:<br>
     * <br>
     * c = c - a*a<sup>T</sup><br>
     * </p>
     */
    protected static void multTransBBlockMinus_L( D1Matrix64F A , D1Matrix64F C,
                                                  int indexA, int indexB, int indexC,
                                                  final int widthA, final int heightA, final int widthC ) {
//        for( int i = 0; i < heightA; i++ ) {
//            for( int j = 0; j <= i; j++ ) {
//                double sum = 0;
//                for( int k = 0; k < widthA; k++ ) {
//                    sum += dataA[i*widthA + k + indexA] * dataA[j*widthA + k + indexB];
//                }
//                dataC[ i*widthC + j + indexC ] -= sum;
//            }
//        }

        for( int i = 0; i < heightA; i++ ) {
            int rowA = i*widthA+indexA;
            int endA = rowA + widthA;
            int rowB = indexB;
            int rowC = i*widthC + indexC;
            for( int j = 0; j <= i; j++ , rowB += widthA) {
                double sum = 0;

                int a = rowA;
                int b = rowB;

                while( a != endA ) {
                    sum += A.get(a++) * A.get(b++);
                }
                C.minus( rowC + j , sum );
            }
        }

    }
}
