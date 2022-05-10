/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.dense.row.misc;

import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;

/**
 * Implementations of common ops routines for {@link DMatrixRMaj}. In general
 * there is no need to directly invoke these functions.
 *
 * @author Peter Abeles
 */
public class ImplCommonOps_DDMA {
    public static void extract( DMatrix src,
                                int srcY0, int srcX0,
                                DMatrix dst,
                                int dstY0, int dstX0,
                                int numRows, int numCols ) {
        for (int y = 0; y < numRows; y++) {
            for (int x = 0; x < numCols; x++) {
                double v = src.get(y + srcY0, x + srcX0);
                dst.set(dstY0 + y, dstX0 + x, v);
            }
        }
    }
}
