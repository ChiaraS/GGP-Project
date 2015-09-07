% Library :
library_directory('/home/csironi/YAPplayer/prologFiles').
:- use_module(library(description)).
:- use_module(library(random)).
:- use_module(library(prandom)).
% :- use_module(library(lists)).
% :- use_module(library(charsio)).

:- dynamic true/1.
:- dynamic does/2.

%%%%%%%%%%%%%%%%%%%%%%% ASSERTING PREDICATES %%%%%%%%%%%%%%%%%%%%%%%%%

/**
 * This predicate asserts clauses that are true in the state that we
 * want to represent in prolog.
 */
add_true_clauses([]). 
add_true_clauses([_x|_l]) :- asserta(true(_x)), add_true_clauses(_l).

/**
 * This predicate asserts clauses that represent that a role performed
 * an action.
 */
add_does_clauses([], []).
add_does_clauses([_p|_l], [_m|_ll]) :- asserta(does(_p,_m)), add_does_clauses(_l, _ll).

%%%%%%%%%%%%%%% PREDICATES PROCESSING RESULTS FOR JAVA %%%%%%%%%%%%%%%

/**
 * This predicate processes a list of terms into a list of strings.
 * It's needed to format the lists that will be passed to java as results of queries.
 */
processList([], []).
% Not needed because term_to_atom succeeds also if a term is already an atom.
% If left, this rule will produce extra results (i.e. x2 for each atom in the list).
% processList([_a|_l],[string(_a)|_ll]) :- atom(_a), processList(_l, _ll).
processList([_a|_l],[string(_s)|_ll]) :- term_to_atom(_a, _s), processList(_l, _ll).

% Not needed because term_to_atom succeeds also if a term is already an atom.
% If left, this rule will produce an extra result.
% processString(_e, _e) :- atom(_e).
processString(_e, _s) :- term_to_atom(_e, _s).

%%%%%%%%%%%%%%%%%%%%%% OTHER USEFUL PREDICATES %%%%%%%%%%%%%%%%%%%%%%%

/**
 * If there is at least one solution, it will return the list with that solution,
 * otherwise returns the empty list instead of failing!
 */
failsafe_bagof(_x, _y, _ll) :- bagof(_x,_y,_ll), !, remove_duplicates(_ll, _llll).
failsafe_bagof(_x, _y, []).


choose([], []).
choose(_l, _e) :- length(_l, _le), random(0, _le, _i), nth0(_i, _l, _e).



perform_depth_charge(_lr, [_s|_l]) :- perform_depth(_lr, _n, _l), atom_number(_s, _n).
perform_depth(_lr, 0, _ll) :- is_terminal, !, bagof(_x, true(_x), _l), remove_duplicates(_l, _ll).
perform_depth(_lr, _n, _l) :- get_random_joint_move(_lr, _ll), get_next_state(_lr, _ll, _lll), perform_depth(_lr, _n2, _l), _n is _n2+1.

get_random_next_state(_l, _r, _m, _ll) :- get_random_joint_moveg(_l, _r, _m, _lll), get_next_state(_l, _lll, _ll).

%%%%%%%%%%%%%%%% PREDICATES COMPUTING THE NEXT STATE %%%%%%%%%%%%%%%%%

/**
 * Computes the next state of the game given a joint move, then retracts the current state and asserts the new one.
 * ATTENTION!: what if bagof fails because the next state has no true propositions? The asserted 'does' propositions won't be
 * retracted and Yap Prolog will be in an inconsistent state.
 */
% get_next_state(_l, _lll, _llll) :- add_does_clauses(_l, _lll), bagof(_x, next(_x), _ll), remove_duplicates(_ll, _llll), retractall(does(_,_)), computel(_llll).

/**
 * Computes the next state of the game given a joint move, then retracts the current state and asserts the new one.
 * ATTENTION!: this predicate overcomes the previously mentioned problem by using the definition of 'failsafe_bagof',
 * that never fails.
 */
get_next_state(_l, _lll, _llll) :- add_does_clauses(_l, _lll), failsafe_bagof(_x, next(_x), _ll), remove_duplicates(_ll, _llll), retractall(does(_,_)), update_state(_llll).



get_random_joint_moveg([], _, _, []).
get_random_joint_moveg([_p|_l], _p, _m, [_m|_lll]) :- get_random_joint_moveg(_l, _p, _m, _lll).
get_random_joint_moveg([_p|_l], _r, _m, [_e|_lll]) :- get_random_joint_moveg(_l, _r, _m, _lll), get_legal_moves(_p, _ll), choose(_ll, _e).

get_random_joint_move([], []).
get_random_joint_move([_p|_l], [_e|_lll]) :- get_random_joint_move(_l, _lll), get_legal_moves(_p, _ll), choose(_ll, _e).

get_random_move(_p, _s) :- get_legal_moves(_p, _l), choose(_l, _e), processString(_e, _s).

%%%%%%%%%%%%%%%%%%% GET LEGAL MOVES FOR ONE PLAYER %%%%%%%%%%%%%%%%%%%%

get_legal_moves(_p, _ll) :- bagof(_x, _p^legal(_p, _x), _l), remove_duplicates(_l, _ll).

%%%%%%%%%%%%%%%%%%%% UPDATE PROLOG MACHINE STATE %%%%%%%%%%%%%%%%%%%%%%

% compute_state(_l, _s) :- computel(_l), atom_concat('d', '', _s).
% computel(_l) :- retractall(true(_)), add_true_clauses(_l).

update_state(_l) :- retractall(true(_)), add_true_clauses(_l).

%%%%%%%%%%%%%%%%% CHECK IF CURRENT STATE IS TERMINAL %%%%%%%%%%%%%%%%%%

is_terminal :- terminal.

%%%%%%%%%%%%%%%%%%%%% GET STATE GOAL FOR A ROLE %%%%%%%%%%%%%%%%%%%%%%%

% get_goal(_p, _s) :- goal(_p, _n), atom_number(_s, _n).
% get_goal(_p, '').

get_goal(_p, _l) :- bagof(_n, _p^goal(_p, _n), _l).

%%%%%%%%%%%%%%%%%%%%% INITIAL STATE COMPUTATION %%%%%%%%%%%%%%%%%%%%%%%
/**
 * Computes the list _ll of propositions _x that are true in the initial state (init(_x)),
 * asserting them as true (true(_x)) on Yap Prolog side.
 * !ATTENTION: don't use this predicate again after the first call for games that have no INIT propositions
 * because "bagof" fails before the currently asserted true propositions can be retracted and so Yap prolog
 * won't be in the initial state.
 */
% initialize_state(_ll) :- bagof(_x, init(_x), _l), remove_duplicates(_l, _ll), retractall(true(_)), add_true_clauses(_ll).
/**
 * Computes the list _ll of propositions _x that are true in the initial state (init(_x)),
 * asserting them as true (true(_x)) on Yap Prolog side.
 * !ATTENTION: this definition of the predicate overcomes the previously mentioned problem.
 * If there are no true clauses in the initial state this predicate fails.
 */
%initialize_state(_ll) :- retractall(true(_)), bagof(_x, init(_x), _l), remove_duplicates(_l, _ll), add_true_clauses(_ll).
/**
 * Computes the list _ll of propositions _x that are true in the initial state (init(_x)),
 * asserting them as true (true(_x)) on Yap Prolog side.
 * !ATTENTION: also this definition of the predicate overcomes the previously mentioned problem.
 * Moreover, if there are no true clauses in the initial state this predicate, instead of failing, returns an empty list.
 */
initialize_state(_ll) :- failsafe_bagof(_x, init(_x), _l), remove_duplicates(_l, _ll), retractall(true(_)), add_true_clauses(_ll).

%%%%%%%%%%%%%%%%%%%%%%%%%% ROLES COMPUTATION %%%%%%%%%%%%%%%%%%%%%%%%%%

/**
 * Computes the roles in the game always in the same order (?).
 */
get_roles(_ll) :- failsafe_bagof(_x, role(_x), _l), remove_duplicates(_l, _ll).

%%%%%%%%%%%%% EXTRA PREDICATES TO UNDERTSAND GDL GRAMMAR %%%%%%%%%%%%%%

% To understand the Gdl grammar :
distinct(_x, _y) :- _x \= _y. 

or(_x, _y) :- _x ; _y. 
or(_x, _y, _z) :- _x ; _y ; _z. 
or(_x, _y, _z, _a) :- _x ; _y ; _z ; _a.
or(_x, _y, _z, _a, _b) :- _x ; _y ; _z ; _a ; _b.
or(_x, _y, _z, _a, _b, _c) :- _x ; _y ; _z ; _a ; _b ; _c.
or(_x, _y, _z, _a, _b, _c, _d) :- _x ; _y ; _z ; _a ; _b ; _c ; _d.
or(_x, _y, _z, _a, _b, _c, _d, _e) :- _x ; _y ; _z ; _a ; _b ; _c ; _d ; _e.
or(_x, _y, _z, _a, _b, _c, _d, _e, _f) :- _x ; _y ; _z ; _a ; _b ; _c ; _d ; _e ; _f.
or(_x, _y, _z, _a, _b, _c, _d, _e, _f, _g) :- _x ; _y ; _z ; _a ; _b ; _c ; _d ; _e ; _f ; _g.

and(_x, _y) :- _x, _y.

get_number_of_init(_l, _n) :- bagof(_x, init(_x), _l), length(_l, _n). 