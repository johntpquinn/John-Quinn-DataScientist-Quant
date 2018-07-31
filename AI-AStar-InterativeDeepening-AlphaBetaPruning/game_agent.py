
"""Finish all TODO items in this file to complete the isolation project, then
test your agent's strength against a set of known agents using tournament.py
and include the results in your report.
"""
#import random
import math # math faster than numpy for these simple functions
from copy import copy

class SearchTimeout(Exception):
    """Subclass base exception for code clarity. """
    pass

MAX_STEPS =  7 # Global constant for max number of knight steps worth counting

# Global constanct maximum to meassure distance from center of board
MAX_DISTANCE = 2 * MAX_STEPS**2

MAX_DEPTH = MAX_STEPS ** 2

# DIRECTIONS is the list of 8 possible steps (dy, dx) that a knight can make
DIRECTIONS = [(-2, -1), (-2, 1), (-1, -2), (-1, 2), \
                      (1, -2), (1, 2), (2, -1), (2, 1)]

def get_center_coordinates(game):
    """Helper function to obtain the coordinates of the center Board box
    
    Parameter
    ----------
    game : `isolation.Board`
        An instance of `isolation.Board` encoding the current state of the
        game (e.g., player locations and blocked cells).
    
    Returns
    -------
    (int, int):
        Board center box coordinates
    """
        
    return math.ceil(game.height / 2), math.ceil(game.width / 2)

def get_distances_from_center(center_coordinates, p_coordinates):
    """Helper function to calculate a player's distance from Board center
    
    Parameters
    ----------
    center_coordinates : (int, int)
        The Board center box coordinates obtained 
        from get_center_coordinates()
    p_coordinates : (int, int)
        The player's Board coordinates    
        
    
    Returns
    -------
    (int, int):
        The squared difference of each player's Board center differential
    """
    
    center_y, center_x = center_coordinates
    p_y, p_x = p_coordinates
    return (p_y - center_y)**2 + (p_x - center_x)**2

def get_next_moves(game, p_coordinates, p_moves_to_gameend):
    """Helper function to obtain a list of player's legal next moves
    from that player's given coordinates.
    
    Parameters
    ----------
    game : `isolation.Board`
        An instance of `isolation.Board` encoding the current state of the
        game (e.g., player locations and blocked cells).
    p_coordinates : (int, int)
        The player's Board coordinates from which legal next_moves
        will be returned
    p_moves_to_gameend : list((int, int))
        This list of player coordinates representing nodes of a path
        already taken and thus to be excluded from next_moves
        
    
    Returns
    -------
    list((int, int)):
        The list of coordinates of legal player moves from player's
        coordinates parameter, i.e., the player's next_moves
    """
    
    p_y, p_x = p_coordinates
    next_moves = [(p_y + dy, p_x + dx) for dy, dx in DIRECTIONS
            if (((p_y + dy, p_x + dx) in game.get_blank_spaces())
            and ((p_y + dy, p_x + dx) not in p_moves_to_gameend))]
    return next_moves

def get_player_feature_values(game, center_coordinates, player_either, \
                         p_coordinates, p_legal_moves):
    """Helper function to obtain all feature values utilized in
    the heuristics, custom_score_2 and custom_score_3.
    
    Parameters
    ----------
    game : `isolation.Board`
        An instance of `isolation.Board` encoding the current state of the
        game (e.g., player locations and blocked cells).
    center_coordinates : (int, int)
        The Board center box coordinates obtained
        from get_center_coordinates()
    player_either : object
        A player instance in the current game (i.e., an object corresponding to
        one of the player objects `game.__player_1__` or `game.__player_2__`.)
    p_coordinates : (int, int)
        The player's Board coordinates from which legal next_moves
        will be returned
    p_legal_moves : list((int, int))
        This list of player coordinates representing the Board boxes to which
        this player can legally move from this player's current box/coordinates
        
    
    Returns
    -------
    int:
        legal_move_count: the number of coordinate tuples in p_legal_moves
    int:
        start_center_distance: the player's distance from the center box,
        measured from p_coordinates (before any p_legal_moves)
        via get_distances_from_center()
    int:
        total_next_moves: the sum of the number of legal moves that the player
        can take after any original p_legal_moves (i.e, # of next_moves)
    int:
        max_path_length: the length, measured in steps, of the longest path
        that the player can take from p_coordinates through any p_legal_move
    int:
        path_count: that number of paths the player can take from p_coordinates
        through each p_legal_move
    int:
        min_center_dist: for any path in path_count the closest any node
        in that path is to the Board's center box measured
        by get_distances_from_center()
    """
    
    moves_to_endgame = next_choices = []
    max_path_length = legal_move_count = len(p_legal_moves)
    center_dist = min_center_dist = start_center_distance = \
        get_distances_from_center(center_coordinates, p_coordinates)
    path_count = move_count = total_moves = 0
    for move in p_legal_moves:
        if len(move) > 0:
            moves_to_endgame.append(move)
            move_count += 1
            total_moves += 1
            center_dist = get_distances_from_center(center_coordinates, p_coordinates)
            min_center_dist = min(center_dist, min_center_dist)
        move_copy = tuple(move)
        for i in range(1, MAX_STEPS +1):
            next_choices = get_next_moves(game, move_copy, moves_to_endgame)   
            for next_choice_index, next_choice in enumerate(next_choices):   
                if len(next_choices) > 0:
                    moves_to_endgame.append(next_choice)
                    move_count += 1
                    total_moves += 1
                    center_dist = get_distances_from_center(center_coordinates, next_choice)
                    min_center_dist = min(center_dist, min_center_dist)
                    if move_count == MAX_STEPS:
                        del(next_choices[:])
                        del(moves_to_endgame[:])
                        max_path_length = max(move_count, max_path_length)
                        move_count_copy = copy(move_count)
                        max_path_length = max(move_count_copy, max_path_length)
                        move_count = 0
                        break
                if next_choice_index == next_choices.index(next_choices[-1]):
                    move_copy = next_choice
                        
            if next_choices:
                 del(next_choices[:])
                 if moves_to_endgame:
                     del(moves_to_endgame[:])
                     move_count_copy = copy(move_count)
                     move_count = 0
                     path_count += 1  
        if next_choices:
            del(next_choices[:])
        if moves_to_endgame:
            del(moves_to_endgame[:])
        move_count_copy = copy(move_count)
        move_count = 0
        path_count += 1
    legal_move_count = len(p_legal_moves)
    total_next_moves = total_moves - legal_move_count
     
    return legal_move_count, start_center_distance, total_next_moves, \
            max_path_length, path_count, min_center_dist 
   


def custom_score(game, player):
    """The heuristic value of a game state from the point of view
    of the given player.

    This is the best heuristic function for my project submission.  It
    returns improved when improved isn't zero.  Otherwise, it returns
    the sum of weighted differences of improved, center_dist_diff,
    and improved_next.

    Note: this function should be called from within a Player instance as
    `self.score()` -- you should not need to call this function directly.
    
    
    Parameters
    ----------
    game : `isolation.Board`
        An instance of `isolation.Board` encoding the current state of the
        game (e.g., player locations and blocked cells).

    player : object
        A player instance in the current game (i.e., an object corresponding to
        one of the player objects `game.__player_1__` or `game.__player_2__`.)

    Returns
    -------
    float
        The heuristic value of the current game state to the specified player.
    """
        
    if game.is_loser(player):
        return float("-inf")

    if game.is_winner(player):
        return float("inf")
    
    # First obtain the improved score
    player_legal_moves = game.get_legal_moves(player)
    opponent = game.get_opponent(player)
    opponent_legal_moves = game.get_legal_moves(opponent)
    improved = len(player_legal_moves) - len(opponent_legal_moves)
    if improved != 0:
        return float(improved)
    
    # Second get differences from center
    center_coordinates = center_y, center_x = get_center_coordinates(game)
    player_coordinates = game.get_player_location(player)
    opponent_coordinates = game.get_player_location(opponent)
    player_center_dist = get_distances_from_center(center_coordinates, player_coordinates)
    opponent_center_dist = get_distances_from_center(center_coordinates, opponent_coordinates)
    center_dist_diff = player_center_dist - opponent_center_dist
    
    # Third obtain next_moves
    player_next_moves = [get_next_moves(game, move, list(move)) for move in player_legal_moves]
    opponent_next_moves = [get_next_moves(game, move, list(move)) for move in opponent_legal_moves]    
    improved_next = len(player_next_moves) - len(opponent_next_moves)
    
    # Put player and opponent feature differences in a tuple/vector surrogoate
    feature_diff_vector = (improved, center_dist_diff, improved_next)
        
    # Provide a weighting vector for the features of each player-participant
    weight_vector = (1.5,0.1,1.0)
    # Calculate the return value = weighted difference of players' features
    weighted_difference_dot_product = sum(p*q for p,q, \
                in zip(feature_diff_vector, weight_vector))
    
    return float(weighted_difference_dot_product)
    
def custom_score_2(game, player):
    """The heuristic value of a game state from the point of view
    of the given player.

    This is the a detailed heuristic function, which proved to be too
    time-consuming and, thus, ineffective.

    Note: this function should be called from within a Player instance as
    `self.score()` -- you should not need to call this function directly.
    
    This heuristic/scoriing function takes each of the return values from
    get_player_feature_values() for player and for opponent.  It then weights
    the differences and returns a heuristic score equal to the dot product
    of the differences and the weights.  It returns improved only, however,
    when improved is not zero.

    Parameters
    ----------
    game : `isolation.Board`
        An instance of `isolation.Board` encoding the current state of the
        game (e.g., player locations and blocked cells).

    player : object
        A player instance in the current game (i.e., an object corresponding to
        one of the player objects `game.__player_1__` or `game.__player_2__`.)

    Returns
    -------
    float
        The heuristic value of the current game state to the specified player.
    """
    if game.is_loser(player):
        return float("-inf")

    if game.is_winner(player):
        return float("inf")
    
    # First obtain the base information to calculate player & opponent
    # feature values
    player_legal_moves = game.get_legal_moves(player)
    opponent = game.get_opponent(player)
    opponent_legal_moves = game.get_legal_moves(opponent)
    if len(player_legal_moves) != len(opponent_legal_moves):
        return float(len(player_legal_moves) - len(opponent_legal_moves))
        
    # Get_center_coordinates and opponent.  Then set the list of participants
    center_coordinates = center_y, center_x = get_center_coordinates(game)
    participants = [player, opponent]
    
    # Then, for each participant obtain his/her feature values                
    for participant in participants:
        if participant == player:
            p_legal_moves = player_legal_moves
            player_either = player
            participant_coordinates = p_y, p_x = \
                game.get_player_location(participant)
            player_legal_move_count, player_start_center_distance, \
                player_total_next_moves, player_max_path_length, \
                player_path_count, player_min_center_diff \
                = \
                get_player_feature_values(game, center_coordinates, \
                player_either,participant_coordinates, p_legal_moves)
        else:
            p_legal_moves = opponent_legal_moves
            player_either = opponent
            participant_coordinates = p_y, p_x \
                = game.get_player_location(participant)
            opponent_legal_move_count, opponent_start_center_distance, \
                opponent_total_next_moves, opponent_max_path_length, \
                opponent_path_count, opponent_min_center_diff \
                = \
                get_player_feature_values(game, center_coordinates, \
                player_either, participant_coordinates, p_legal_moves)
    
    # Place each participant's feature values in a tuple/vector surrogate       
    pro_player_vector =  \
        (player_legal_move_count, player_start_center_distance, \
        player_total_next_moves, player_max_path_length, player_path_count, \
        opponent_min_center_diff)
    pro_opponent_vector = \
        (opponent_legal_move_count, opponent_start_center_distance, \
        opponent_total_next_moves, opponent_max_path_length, \
        opponent_path_count, player_min_center_diff)
    
    # Provide a weighting vector for the features 
    weight_vector = (1.5,0.1,1.0,0.001,0.001,0.001)
    
    # Calculate the return value = weighted difference of players' features
    weighted_difference_dot_product = sum(p*(q-r ) for p,q,r \
                in zip(weight_vector, pro_player_vector, pro_opponent_vector))
    
    return float(weighted_difference_dot_product)

def custom_score_3(game, player):
    """Calculate the heuristic value of a game state from the point of view
    of the given player.
    
    This first, naive attempt at a heuristics function simply uses two
    features, move_count_difference and weighted_distance_from_center.
    The former is the difference between the # of the player's and the 
    opponent's legal_moves.  The latter is itself an heuristic measure
    of the given player's distance from the center box; it starts with
    the base measure of vector magnitude and then weights the longer
    differential by 2 since of part of knight move must be 2 boxes long
    and then divides this whole measure by 3, the sum of the weights.
    
    Finally, the two components are weighted.   The first is accorded
    an order of magnitude higher than the second.
    
    The ultimate score returned is the dot product of the two components
    and their weights.  If, however, improved is not zero, improved is
    returned.

    Note: this function should be called from within a Player instance as
    `self.score()` -- you should not need to call this function directly.

    Parameters
    ----------
    game : `isolation.Board`
        An instance of `isolation.Board` encoding the current state of the
        game (e.g., player locations and blocked cells).

    player : object
        A player instance in the current game (i.e., an object corresponding to
        one of the player objects `game.__player_1__` or `game.__player_2__`.)

    Returns
    -------
    float
        The heuristic value of the current game state to the specified player.
    """
    
        
    if game.is_loser(player):
        return float("-inf")

    if game.is_winner(player):
        return float("inf")
    
    player_legal_moves = game.get_legal_moves(player)
    opponent = game.get_opponent(player)
    opponent_legal_moves = game.get_legal_moves(opponent)
    player_legal_move_count, opponent_legal_move_count = \
            len(player_legal_moves), len(opponent_legal_moves)
    move_count_difference = player_legal_move_count - opponent_legal_move_count
    # Find coordinates of center box
    h, w =  get_center_coordinates(game)
    # Retrieve player's coordinates
    y, x = game.get_player_location(player)
    # Obtain coordinate further, closest to origin
    furthest_coord, closest_coord = max(h - y, w -x), min(h - y, w - x)
    # Return weighted, vector-valued length from origin / sum of weights
    weighted_distance_from_center = \
            math.sqrt((closest_coord**2 + 2*(furthest_coord**2)))/3
    feature_vector = (move_count_difference, weighted_distance_from_center)
            
    weight_vector = (1.0,0.1)
    
    weighted_difference_dot_product = sum(p*q for p,q, \
                in zip(weight_vector, feature_vector))        
    
    return float(weighted_difference_dot_product) 

class IsolationPlayer:
    """Base class for minimax and alphabeta agents -- this class is never
    constructed or tested directly.

    ********************  DO NOT MODIFY THIS CLASS  ********************

    Parameters
    ----------
    search_depth : int (optional)
        A strictly positive integer (i.e., 1, 2, 3,...) for the number of
        layers in the game tree to explore for fixed-depth search. (i.e., a
        depth of one (1) would only explore the immediate sucessors of the
        current state.)

    score_fn : callable (optional)
        A function to use for heuristic evaluation of game states.
    timeout : float (optional)
        Time remaining (in milliseconds) when search is aborted. Should be a
        positive value large enough to allow the function to return before the
        timer expires.
    """
    def __init__(self, search_depth=3, score_fn=custom_score, timeout=10):
        self.search_depth = search_depth
        self.score = score_fn
        self.time_left = None
        self.TIMER_THRESHOLD = timeout


class MinimaxPlayer(IsolationPlayer):
    """Game-playing agent that chooses a move using depth-limited minimax
    search. You must finish and test this player to make sure it properly uses
    minimax to return a good move before the search time limit expires.
    """
            
    def get_move(self, game, time_left):
        """Search for the best move from the available legal moves and return a
        result before the time limit expires.

        **************  YOU DO NOT NEED TO MODIFY THIS FUNCTION  *************

        For fixed-depth search, this function simply wraps the call to the
        minimax method, but this method provides a common interface for all
        Isolation agents, and you will replace it in the AlphaBetaPlayer with
        iterative deepening search.

        Parameters
        ----------
        game : `isolation.Board`
            An instance of `isolation.Board` encoding the current state of the
            game (e.g., player locations and blocked cells).

        time_left : callable
            A function that returns the number of milliseconds left in the
            current turn. Returning with any less than 0 ms remaining forfeits
            the game.

        Returns
        -------
      
        (int, int)
            Board coordinates corresponding to a legal move; may return
            (-1, -1) if there are no available legal moves.
        """
        self.time_left = time_left
        
        # Initialize the best move so that this function returns something
        # in case the search fails due to timeout
        best_move = (-1, -1)

        try:
            # The try/except block will automatically catch the exception
            # raised when the timer is about to expire.
            return self.minimax(game, self.search_depth)

        except SearchTimeout:
            pass  # Handle any actions required after timeout as needed

        # Return the best move from the last completed search iteration
        return best_move

    def minimax(self, game, depth):
        """Implement depth-limited minimax search algorithm as described in
        the lectures.

        This should be a modified version of MINIMAX-DECISION in the AIMA text.
        https://github.com/aimacode/aima-pseudocode/blob/master/md/Minimax-Decision.md

        **********************************************************************
            You MAY add additional methods to this class, or define helper
                 functions to implement the required functionality.
        **********************************************************************

        Parameters
        ----------
        game : isolation.Board
            An instance of the Isolation game `Board` class representing the
            current game state

        depth : int
            Depth is an integer representing the maximum number of plies to
            search in the game tree before aborting

                 
        Returns
        -------
                 
        (int, int)
            The board coordinates of the best move found in the current search;
            (-1, -1) if there are no legal moves

        Notes
        -----
            (1) You MUST use the `self.score()` method for board evaluation
                to pass the project tests; you cannot call any other evaluation
                function directly.

            (2) If you use any helper functions (e.g., as shown in the AIMA
                pseudocode) then you must copy the timer check into the top of
                each helper function or else your agent will timeout during
                testing.
        """
       
        if self.time_left() < self.TIMER_THRESHOLD:
            raise SearchTimeout()
            
        return self.minimax_helper(game, self.search_depth, maximizing_player = True)[1]
            
    
    
    def minimax_helper(self, game, depth, maximizing_player=True):
        """mimimax helper function performing the recursive search

        Parameters
        ----------
        game : isolation.Board
            An instance of the Isolation game `Board` class representing the
            current game state

        depth : int
            Depth is an integer representing the maximum number of plies to
            search in the game tree before aborting

             
        Returns
        -------
        float
        The heuristic value of the current game state to the specified player.
               
        (int, int)
        The board coordinates of the best move found in the current search;
        (-1, -1) if there are no legal moves

        """
        if self.time_left() < self.TIMER_THRESHOLD:
            raise SearchTimeout()
            
        # Get all the available moves at the current state
        legal_moves = game.get_legal_moves()

        # Handle a terminal event/exhaustive search completion
        # at least in relation to depth sought
        if (not legal_moves) or (depth == 0):
            if maximizing_player:
                return (self.score(game, game.active_player), (-1, -1))
            else:
                return (self.score(game, game.inactive_player), (-1, -1))

        if maximizing_player: # the current player
            this_score = float("-inf")
            for move in legal_moves:
                next_ply_state = game.forecast_move(move)
                next_ply_score, next_ply_move = self.minimax_helper(next_ply_state,
                        depth-1, False)

                # Identify the maximum score branch for the current player.
                if next_ply_score >= this_score:
                    this_move = move
                    this_score = next_ply_score

        else: # the current player's opponent
            this_score = float("inf")
            for move in legal_moves:
                next_ply_state = game.forecast_move(move)
                next_ply_score, next_ply_move = self.minimax_helper(next_ply_state,
                        depth-1, True)                                         

                # Identify the minimum score branch for the opponent
                if next_ply_score <= this_score:
                    this_move = move
                    this_score = next_ply_score

        return this_score, this_move

            
class AlphaBetaPlayer(IsolationPlayer):
    """Game-playing agent that chooses a move using iterative deepening minimax
    search with alpha-beta pruning. You must finish and test this player to
    make sure it returns a good move before the search time limit expires.
    """

    def get_move(self, game, time_left):
        """Search for the best move from the available legal moves and return a
        result before the time limit expires.

        Modify the get_move() method from the MinimaxPlayer class to implement
        iterative deepening search instead of fixed-depth search.

        **********************************************************************
        NOTE: If time_left() < 0 when this function returns, the agent will
              forfeit the game due to timeout. You must return _before_ the
              timer reaches 0.
        **********************************************************************

        Parameters
        ----------
        game : `isolation.Board`
            An instance of `isolation.Board` encoding the current state of the
            game (e.g., player locations and blocked cells).

        time_left : callable
            A function that returns the number of milliseconds left in the
            current turn. Returning with any less than 0 ms remaining forfeits
            the game.

        Returns
        -------
        (int, int)
            Board coordinates corresponding to a legal move; may return
            (-1, -1) if there are no available legal moves.
        """
        self.time_left = time_left
      
        max_depth = game.height * game.width
        
       
        # FIRST, check initial conditions wrt legal_moves
        legal_moves = game.get_legal_moves()
        # If there are no legal_moves return no move
        if len(legal_moves) == 0:
            return (-1,-1)
        # If there's only one legal_move return it, the only choice
        elif len(legal_moves) == 1:
            return legal_moves[0]
        # Otherwise, initialize best_choice at first legal_move
        else:
            best_move = legal_moves[0]
            try:
                for node_number in range(1, max_depth + 1):
            # This exception handing returns the best_move found
            # thus far in the event of a timeout
                
                    best_move = self.alphabeta(game, node_number)
                return best_move
            except SearchTimeout:
                pass
               
        # Return the best_move found thus far (or ultimately in the event
        # of exhaustive search completion or timeout)
        return best_move


    def alphabeta(self, game, depth, alpha=float("-inf"), beta=float("inf")):
        """Implement depth-limited minimax search with alpha-beta pruning as
        described in the lectures.

        This should be a modified version of ALPHA-BETA-SEARCH in the AIMA text
        https://github.com/aimacode/aima-pseudocode/blob/master/md/Alpha-Beta-Search.md

        **********************************************************************
            You MAY add additional methods to this class, or define helper
                 functions to implement the required functionality.
        **********************************************************************

        Parameters
        ----------
        game : isolation.Board
            An instance of the Isolation game `Board` class representing the
            current game state

        depth : int
            Depth is an integer representing the maximum number of plies to
            search in the game tree before aborting

        alpha : float
            Alpha limits the lower bound of search on minimizing layers

        beta : float
            Beta limits the upper bound of search on maximizing layers

        Returns
        -------
        (int, int)
            The board coordinates of the best move found in the current search;
            (-1, -1) if there are no legal moves

        Notes
        -----
            (1) You MUST use the `self.score()` method for board evaluation
                to pass the project tests; you cannot call any other evaluation
                function directly.

            (2) If you use any helper functions (e.g., as shown in the AIMA
                pseudocode) then you must copy the timer check into the top of
                each helper function or else your agent will timeout during
                testing.
        """
        if self.time_left() < self.TIMER_THRESHOLD:
           raise SearchTimeout()
            
        # FIRST, check initial conditions wrt legal_moves
        legal_moves = game.get_legal_moves()
        # If there are no legal_moves return no move
        if len(legal_moves) == 0:
            return (-1,-1)
        #If there's only one legal_move return it, the only choice
        elif len(legal_moves) == 1:
            return legal_moves[0]
        #Otherwise, initialize best_choice at first legal_move
        else:
            best_move = legal_moves[0]
            
        try:
            best_move = self.alphabeta_helper(game, depth)[1]
        except SearchTimeout:
            return best_move
        
        return self.alphabeta_helper(game, depth)[1]
    
    def alphabeta_helper(self, game, depth, alpha=float("-inf"), \
                         beta=float("inf"), maximizing_player=True):
        """Implement minimax search with alpha-beta pruning as described in the
        lectures.s
        Parameters
        ----------
        game : isolation.Board
            An instance of the Isolation game `Board` class representing the
            current game state
        depth : int
            Depth is an integer representing the maximum number of plies to
            search in the game tree before aborting
        alpha : float
            Alpha limits the lower bound of search on minimizing layers
        beta : float
            Beta limits the upper bound of search on maximizing layers
        maximizing_player : bool
            Flag indicating whether the current search depth corresponds to a
            maximizing layer (True) or a minimizing layer (False)
        Returns
        ----------
        float
            The score for the current search branch
        tuple(int, int)
            The best move for the current branch; (-1, -1) for no legal moves
        """
        if self.time_left() < self.TIMER_THRESHOLD:
            raise SearchTimeout()

        # Get all the available moves at the current state
        legal_moves = game.get_legal_moves()

        # FIRST, test to see if stopped-out
        if len(legal_moves) == 0 or depth == 0:
            if maximizing_player:
                return (self.score(game, game.active_player), (-1, -1))
            else:
                return (self.score(game, game.inactive_player), (-1, -1))

        this_move = legal_moves[0]
        if maximizing_player: # This is the active player at max ply
            this_score = float("-inf")
            for move in legal_moves:
                next_ply_state = game.forecast_move(move)
                next_ply_score, next_ply_move \
                    = \
                    self.alphabeta_helper(next_ply_state, \
                            depth-1, alpha, beta, False)

                if next_ply_score >= this_score:
                    this_score = next_ply_score

                if this_score >= beta:
                    return this_score, move

                if this_score > alpha:
                    this_move = move
                    alpha = this_score

        else: # This is active player with the next min ply for opponent
            this_score = float("inf")
            for move in legal_moves:
                next_ply_state = game.forecast_move(move)
                next_ply_score, next_ply_move \
                    = \
                    self.alphabeta_helper(next_ply_state, \
                        depth-1, alpha, beta, True)

                if next_ply_score <= this_score:
                    this_score = next_ply_score

                if this_score <= alpha:
                    return this_score, move

                
                if this_score < beta:
                    this_move = move
                    beta = this_score

        return this_score, this_move