/*******************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.nd4j.linalg.api.ops.impl.transforms;

import lombok.NonNull;
import org.nd4j.autodiff.samediff.SDIndex;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.base.Preconditions;
import org.nd4j.imports.graphmapper.tf.TFGraphMapper;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.DynamicCustomOp;
import org.nd4j.linalg.factory.Nd4j;
import org.tensorflow.framework.AttrValue;
import org.tensorflow.framework.GraphDef;
import org.tensorflow.framework.NodeDef;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pad op
 * @author Alex Black
 */
public class Pad extends DynamicCustomOp {

    public enum Mode {CONSTANT, REFLECT, SYMMETRIC}

    private Mode mode;
    private double constant;

    public Pad(){ }

    public Pad(SameDiff sd, SDVariable in, SDVariable padding, Mode mode, double padValue) {
        super(sd, new SDVariable[]{in, padding}, false);
        Preconditions.checkState(padding.dataType().isIntType(), "Padding array must be an integer datatype, got %s", padding.dataType());
        this.mode = mode;
        addIArgument(mode.ordinal());
        addTArgument(padValue);
    }

    public Pad(@NonNull INDArray in, @NonNull INDArray padding, INDArray out, @NonNull Mode mode, double padValue){
        super(null, new INDArray[]{in, padding}, out == null ? null : new INDArray[]{out});
        Preconditions.checkState(padding.dataType().isIntType(), "Padding array must be an integer datatype, got %s", padding.dataType());
        this.mode = mode;
        addIArgument(mode.ordinal());
        addTArgument(padValue);
    }

    @Override
    public String opName(){
        return "pad";
    }

    @Override
    public String[] tensorflowNames() {
        return new String[]{"Pad", "PadV2"};
    }

    @Override
    public void initFromTensorFlow(NodeDef nodeDef, SameDiff initWith, Map<String, AttrValue> attributesForNode, GraphDef graph) {
        //Based on TF codebase: gen_array_ops.mirror_pad is osed for BOTH REFLECT and SYMMETRIC mode. Hence only constant being imported here
        this.mode = Mode.CONSTANT;
        addIArgument(mode.ordinal());
        //Constant value is resolved just before execution
    }

    @Override
    public List<SDVariable> doDiff(List<SDVariable> i_v) {
        //Pad backprop: it's basically slice op...
        //Inputs to pad: input array (rank N), and padding array (rank 2, shape [N,2])
        //Begin values for slice: given by column 0 of padding array; size is given by input array

        SDVariable shape = arg().shape();
        SDVariable begin = arg(1).get(SDIndex.all(), SDIndex.point(0));

        SDVariable gradAtIn = sameDiff.slice(i_v.get(0), begin, shape);
        SDVariable zeros = sameDiff.zerosLike(arg(1));

        return Arrays.asList(gradAtIn, zeros);
    }

    @Override
    public List<DataType> calculateOutputDataTypes(List<DataType> inputDataTypes){
        Preconditions.checkState(inputDataTypes != null && (inputDataTypes.size() == 1 || inputDataTypes.size() == 2),
                "Expected 1 or 2 input datatypes for %s, got %s", getClass(), inputDataTypes);
        return Collections.singletonList(inputDataTypes.get(0));
    }
}
