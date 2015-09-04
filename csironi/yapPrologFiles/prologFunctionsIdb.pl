% Library :
library_directory('C:\Users\c.sironi\BITBUCKET REPOS\GGP-Base\csironi\yapPrologFiles').
:- use_module(library(description)).
:- use_module(library(random)).
:- use_module(library(prandom)).

% To requests :
add_true_clauses([]).
add_true_clauses([_x|_l]) :- recorda(1, _x, _), add_true_clauses(_l).

add_does_clauses([], []).
add_does_clauses([_p|_l], [_m|_ll]) :- recorda(2, does(_p,_m), _), add_does_clauses(_l, _ll).

processList([], []).
processList([_a|_l], [string(_a)|_ll]) :- atom(_a), processList(_l, _ll).
processList([_a|_l], [string(_s)|_ll]) :- term_to_atom(_a, _s), processList(_l, _ll).

choose([], []).
choose(_l, _e) :- length(_l, _le), random(0, _le, _i), nth0(_i, _l, _e).

processString(_e, _e) :- atom(_e).
processString(_e, _s) :- term_to_atom(_e, _s).

true(_x) :- recorded(1, _x, _).

does(_p, _m) :- recorded(2, does(_p, _m), _).



perform_depth_charge(_lr, [_s|_l]) :- perform_depth(_lr, _n, _l), atom_number(_s, _n).
perform_depth(_lr, 0, _ll) :- is_terminal, !, bagof(_x, true(_x), _l), remove_duplicates(_l, _ll).
perform_depth(_lr, _n, _l) :- get_random_joint_move(_lr, _ll), get_next_state(_lr, _ll, _lll), perform_depth(_lr, _n2, _l), _n is _n2+1.

get_random_next_state(_l, _r, _m, _ll) :- get_random_joint_moveg(_l, _r, _m, _lll), get_next_state(_l, _lll, _ll).

get_random_joint_moveg([], _, _, []).
get_random_joint_moveg([_p|_l], _p, _m, [_m|_lll]) :- get_random_joint_moveg(_l, _p, _m, _lll).
get_random_joint_moveg([_p|_l], _r, _m, [_e|_lll]) :- get_random_joint_moveg(_l, _r, _m, _lll), get_legal_moves(_p, _ll), choose(_ll, _e).

get_random_joint_move([], []).
get_random_joint_move([_p|_l], [_e|_lll]) :- get_random_joint_move(_l, _lll), get_legal_moves(_p, _ll), choose(_ll, _e).

get_random_move(_p, _s) :- get_legal_moves(_p, _l), choose(_l, _e), processString(_e, _s).

get_next_state(_l, _lll, _llll) :- add_does_clauses(_l, _lll), bagof(_x, next(_x), _ll), remove_duplicates(_ll, _llll), eraseall(1), eraseall(2), add_true_clauses(_llll).

get_legal_moves(_p, _ll) :- bagof(_x, _p^legal(_p, _x), _l), remove_duplicates(_l, _ll).

compute_state(_l, _s) :- computel(_l), atom_concat('d', '', _s).

computel(_l) :- eraseall(1), add_true_clauses(_l).

is_terminal :- terminal.

get_goal(_p, _s) :- goal(_p, _n), atom_number(_s, _n).
get_goal(_p, '').

initialize_state(_ll) :- bagof(_x, init(_x), _l), remove_duplicates(_l, _ll), computel(_ll).

get_roles(_l) :- bagof(_x, role(_x), _l), remove_duplicates(_l, _ll).

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