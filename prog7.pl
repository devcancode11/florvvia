List_Concat([],L,L). 
List_Concat([X1|L1],L2,[X1|L3]) :- List_Concat(L1,L2,L3). 
List_Rev([],[]). 
List_Rev([Head|Tail],Reversed) :- 
List_Rev(Tail, Revtail),List_Concat(Revtail, [Head],Reversed). 
