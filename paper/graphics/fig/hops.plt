#------------------------------------------------------------
# Hops
#------------------------------------------------------------

reset
set term pdf monochrome dashed dl 1 lw 3 font 'Helvetica,16'
#set key top center horizontal
set key tmargin center hor
set key font ",14"
set key width -3.5
set key samplen 3.0
set xlabel 'Time [Minutes]'
set ylabel 'Search query: Contacted Nodes [#]'
set xrange [200:329]
set yrange [0:60]
set grid y lw 0.25

## A_10
#set output 'Search_Hops_A_10.pdf'
#set table 'smoothed1ks'
#plot '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($79-$81) smooth unique
#unset table
#set table 'smoothed2ks'
#plot '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($79+$82) smooth unique
#unset table
#set table 'smoothed1fs'
#plot '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($79-$81) smooth unique
#unset table
#set table 'smoothed2fs'
#plot '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($79+$82) smooth unique
#unset table
#set style fill transparent solid 0.4 noborder
#plot "< paste smoothed1fs smoothed2fs | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:79 title 'A – v1.0: full-Search Hops (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:79 title 'A – v1.0: k-Search Hops (Avg)'    with lines lt 7 lw 1.0 axis x1y1 smooth unique
#
## A_12
#set output 'Search_Hops_A_12.pdf'
#set table 'smoothed1ks'
#plot '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($79-$81) smooth unique
#unset table
#set table 'smoothed2ks'
#plot '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($79+$82) smooth unique
#unset table
#set table 'smoothed1fs'
#plot '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($79-$81) smooth unique
#unset table
#set table 'smoothed2fs'
#plot '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($79+$82) smooth unique
#unset table
#set style fill transparent solid 0.4 noborder
#plot "< paste smoothed1fs smoothed2fs | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:79 title 'A – v1.2: full-Search Hops (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:79 title 'A – v1.2: k-Search Hops (Avg)'    with lines lt 7 lw 1.0 axis x1y1 smooth unique

# Combined
set output 'Search_Hops.pdf'
set style fill transparent solid 0.4 noborder
plot \
     '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:79 title 'v1.2: full-Search Hops (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
     '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:79 title 'v1.0: full-Search Hops (Avg)' with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
     '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:79 title 'v1.2: k-Search Hops (Avg)'    with lines lt 4 lw 1.0 axis x1y1 smooth unique, \
     '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:79 title 'v1.0: k-Search Hops (Avg)'    with lines lt 2 lw 0.5 axis x1y1 smooth unique, \

=MITTELWERT(CA2012:CA3220)