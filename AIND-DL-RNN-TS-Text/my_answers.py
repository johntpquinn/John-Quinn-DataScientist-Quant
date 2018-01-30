import numpy as np

from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers import LSTM
import keras


# TODO: fill out the function below that transforms the input series 
# and window-size into a set of input/output pairs for use with our RNN model
def window_transform_series(series, window_size):
    # containers for input/output pairs  
    X = []
    y = []

    # Get the number of pairs to create with window size T
    # We create P - T pairs
    char_cnt = len(series)
    window_cnt = char_cnt - window_size      
    # Input pair is a vector of length window_size
    # e.g. window size 4 ->
    #   Input: <s1,s2, s3, s4>   Output: s5
    #   Input: <s2, s3, s4, s5>  Output: s6
    X = [series[i: (i + window_size)] for i in range(window_cnt)]
    
    # Output is the original sequence from window_size
    y = series[window_size:]
     
    # reshape each 
    X = np.asarray(X)
    X.shape = (np.shape(X)[0:2])
    y = np.asarray(y)
    y.shape = (len(y),1)

    return X,y

# TODO: build an RNN to perform regression on our time series input/output data
def build_part1_RNN(window_size):
    #pass
    model = Sequential()
    model.add(LSTM(5, input_shape=(window_size, 1))) #dim(4) for 4 operations + dim(1) for (LTM_t, STM_t)
    model.add(Dense(1)) #return value of regression = price (scalar)

    return model    


### TODO: return the text input with only ascii lowercase and the punctuation given below included.
def cleaned_text(text):
    punctuation = ['!', ',', '.', ':', ';', '?']
    text = ''.join([c.lower() for c in text if ((ord(c) > 96 and ord(c) < 123) or (c in punctuation) or (c == ' '))])

    return text

### TODO: fill out the function below that transforms the input text and window-size into a set of input/output pairs for use with our RNN model
def window_transform_text(text, window_size, step_size):
    # containers for input/output pairs
    text_cnt = len(text)
    window_cnt = text_cnt - window_size
    inputs = []
    outputs = []
    inputs = [text[i: (i + window_size)] for i in range(0, window_cnt, step_size)]
    outputs = [text[i] for i in range(window_size, text_cnt, step_size)]
    
    return inputs, outputs

# TODO build the required RNN model: 
# a single LSTM hidden layer with softmax activation, categorical_crossentropy loss 
def build_part2_RNN(window_size, num_chars):
    #pass
    model = Sequential()

    # Create network
    model.add(LSTM(200, input_shape=(window_size, num_chars)))
    model.add(Dense(num_chars))
    model.add(Activation('softmax'))
    
    return model