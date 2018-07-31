import math
import statistics
import warnings
import numpy as np
from hmmlearn.hmm import GaussianHMM
from sklearn.model_selection import KFold
from asl_utils import combine_sequences


class ModelSelector(object):
    '''
    base class for model selection (strategy design pattern)
    '''

    def __init__(self, all_word_sequences: dict, all_word_Xlengths: dict, this_word: str,
                 n_constant=5,
                 min_n_components=2, max_n_components=10,
                 random_state=14, verbose=False):
        self.words = all_word_sequences
        self.hwords = all_word_Xlengths
        self.sequences = all_word_sequences[this_word]
        self.X, self.lengths = all_word_Xlengths[this_word]
        self.this_word = this_word
        self.n_constant = n_constant
        self.min_n_components = min_n_components
        self.max_n_components = max_n_components
        self.random_state = random_state
        self.verbose = verbose

    def select(self):
        raise NotImplementedError

    def base_model(self, num_states):
        # with warnings.catch_warnings():
        warnings.filterwarnings("ignore", category=DeprecationWarning)
        # warnings.filterwarnings("ignore", category=RuntimeWarning)
        try:
            hmm_model = GaussianHMM(n_components=num_states, covariance_type="diag", n_iter=1000,
                                    random_state=self.random_state, verbose=False).fit(self.X, self.lengths)
            if self.verbose:
                print("model created for {} with {} states".format(self.this_word, num_states))
            return hmm_model
        
        except:
            if self.verbose:
                print("failure on {} with {} states".format(self.this_word, num_states))
            return None


class SelectorConstant(ModelSelector):
    """ select the model with value self.n_constant

    """

    def select(self):
        """ select based on n_constant value

        :return: GaussianHMM object
        """
        best_num_components = self.n_constant
        return self.base_model(best_num_components)


class SelectorBIC(ModelSelector):
    """ select the model with the lowest Bayesian Information Criterion(BIC) score

    http://www2.imm.dtu.dk/courses/02433/doc/ch6_slides.pdf
    Bayesian information criteria: BIC = -2 * logL + p * logN
    """

    def select(self):
        """ select the best model for self.this_word based on
        BIC score for n between self.min_n_components and self.max_n_components
        L = logL =  the Log likelihood the model, given the data
        P = p = the number of free parameters
        N = total quantum (count) of data

        :return: GaussianHMM object
        """
        warnings.filterwarnings("ignore", category=DeprecationWarning)

        # TODO implement model selection based on BIC scores
        BIC_scores = []
        component_range = range(self.min_n_components, self.max_n_components +1)
        try:
            for no_comp in component_range:
                model = self.base_model(no_comp)
                L = model.score(self.X, self.lengths)
                P = no_comp*no_comp + 2*no_comp*model.n_features - 1
                BIC_score = -2 * L + P * math.log(no_comp)
                BIC_scores.append(BIC_score)
                
        except Exception as e:
            pass
        
        if BIC_scores:
            states = component_range[np.argmin(BIC_scores)]
        else:
            states = self.n_constant
        
        return self.base_model(states)


class SelectorDIC(ModelSelector):
    ''' select best model based on Discriminative Information Criterion

    Biem, Alain. "A model selection criterion for classification: Application to hmm topology optimization."
    Document Analysis and Recognition, 2003. Proceedings. Seventh International Conference on. IEEE, 2003.
    http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.58.6208&rep=rep1&type=pdf
    https://pdfs.semanticscholar.org/ed3d/7c4a5f607201f3848d4c02dd9ba17c791fc2.pdf
    
    DIC = log(P(X(i)) - 1/(M-1)SUM(log(P(X(all but i))
    L = the Log likelihood of the model, given the data
    no_components = M = # of components
    other_L = log likelihood of the other models, given the data
    '''

    def select(self):
        warnings.filterwarnings("ignore", category=DeprecationWarning)

        # TODO implement model selection based on DIC scores
        DIC_scores = []
        L_list = []
        component_range = range(self.min_n_components, self.max_n_components +1)
        try:
            for no_comp in component_range:
                model = self.base_model(no_comp)
                L_list.append(model.score(self.X, self.lengths))
            num_components = len(self.n_components)
            for L in L_list:
                other_L = (sum(L_list) - L) / (num_components - 1)
                DIC_score = L - other_L
                DIC_scores.append(DIC_score)
        
        except Exception as exc:
            pass
        
        if DIC_scores:
            states = component_range[np.argmax(DIC_scores)]
        else:
            states = self.n_constant
            
        return self.base_model(states)


class SelectorCV(ModelSelector):
    ''' select best model based on average log Likelihood of cross-validation folds

    '''

    def select(self):
        warnings.filterwarnings("ignore", category=DeprecationWarning)

        # TODO implement model selection using CV
        mean_scores = []
        split_method = KFold()
        component_range = range(self.min_n_components, self.max_n_components +1)
        try:
            for no_comp in component_range:
                model = self.base_model(no_comp)
                fold_scores = []
                for _, test_idx in split_method.split(self.sequences):
                    test_X, test_length = combine_sequences(test_idx, self.sequences)
                    fold_scores.append(model.score(test_X, test_length))

                mean_scores.append(np.mean(fold_scores))
        
        except Exception as exc:
            pass
        
        if mean_scores:
            states = component_range[np.argmax(mean_scores)]
        else:
            states = self.n_constant
        
        return self.base_model(states)
    
class SelectorAIC(ModelSelector):
    ''' select best model based on Akaike information criterion

    Ramsey F., Schafer D. (2013.  The Statistical Sleuth, A Course in
    Methods of Data Analyais (3rd ed.)  Boston, MA: Brooks/Cole CENGAGE Learning.
    
    AIC = -2 * logL + 2 * p
    L = the Log likelihood of the fitted model being seen, given the data
    P = p = the number of free parameters
    misclass_L = log likelihood of the all modesl except the fitted model being seen, given the data
    '''

    def select(self):
        warnings.filterwarnings("ignore", category=DeprecationWarning)
        AIC_scores = []
        component_range = range(self.min_n_components, self.max_n_components +1)
        try:
            for no_comp in component_range:
                model = self.base_model(no_comp)
                L = model.score(self.X, self.lengths)
                P = no_comp*no_comp + 2*no_comp*model.n_features -1
                AIC_score = -2 * L + 2 * P
                AIC_scores.append(AIC_score)
                
        except Exception as exc:
            pass

        if AIC_scores:
            states = component_range[np.argmin(AIC_scores)]
        
        else:
            states = self.n_constant
        
        return self.base_model(states)

        

        