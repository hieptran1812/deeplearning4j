//
// @author raver119@gmail.com
//

#ifndef PROJECT_CONVOLUTIONLAYER_H
#define PROJECT_CONVOLUTIONLAYER_H

#include <layers/layers.h>
#include <layers/generic/BaseLayer.h>

namespace nd4j {
    namespace layers {

        template<typename T>
        class ConvolutionLayer: public BaseLayer<T> {

            void feedForward() {
                // to be implemented
            }

            void backPropagate() {
                // to be implemented
            }
        };
    }
}

#endif //PROJECT_CONVOLUTIONLAYER_H
