#------------------------------------------------------------
# Duration
#------------------------------------------------------------

reset
set term pdf monochrome dashed dl 1 lw 3 font 'Helvetica,16'
set key top center horizontal
set xlabel 'Time [Minutes]'
set ylabel 'Operation lasted [seconds]'
set xrange [200:329]
set grid y lw 0.25
set mytics 2

set yrange [0:1]

set output 'Search_Duration_A_10c_fs.pdf'
set table 'smoothed1'
plot '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed2'
plot '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set table 'smoothed3'
plot '../sim/A_10c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed4'
plot '../sim/A_10c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set style fill transparent solid 0.4 noborder
plot "< paste smoothed1 smoothed2 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.0: full-Search (Avg)'         with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
     "< paste smoothed3 smoothed4 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/A_10c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.0 + cache: full-Search (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique

set output 'Search_Duration_A_12c_fs.pdf'
set table 'smoothed1'
plot '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed2'
plot '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set table 'smoothed3'
plot '../sim/A_12c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed4'
plot '../sim/A_12c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set style fill transparent solid 0.4 noborder
plot "< paste smoothed1 smoothed2 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.2: full-Search (Avg)'         with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
     "< paste smoothed3 smoothed4 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/A_12c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.2 + cache: full-Search (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique

set yrange [0:0.5]

set output 'Search_Duration_A_10c_ks.pdf'
set table 'smoothed1'
plot '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed2'
plot '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set table 'smoothed3'
plot '../sim/A_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed4'
plot '../sim/A_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set style fill transparent solid 0.4 noborder
plot "< paste smoothed1 smoothed2 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.0: k-Search (Avg)'         with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
     "< paste smoothed3 smoothed4 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/A_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.0 + cache: k-Search (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique

set output 'Search_Duration_A_12c_ks.pdf'
set table 'smoothed1'
plot '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed2'
plot '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set table 'smoothed3'
plot '../sim/A_12c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed4'
plot '../sim/A_12c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set style fill transparent solid 0.4 noborder
plot "< paste smoothed1 smoothed2 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.2: k-Search (Avg)'         with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
     "< paste smoothed3 smoothed4 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/A_12c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.2 + cache: k-Search (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique







##########################

# A

# full search

set yrange [0:1]
set mytics 2

## A_10
#set output 'Search_Duration_A_10.pdf'
#set table 'smoothed1ks'
#plot '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2ks'
#plot '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set table 'smoothed1fs'
#plot '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2fs'
#plot '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set style fill transparent solid 0.4 noborder
#plot "< paste smoothed1fs smoothed2fs | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_10_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.0: full-Search (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.0: k-Search (Avg)'    with lines lt 7 lw 1.0 axis x1y1 smooth unique
#
## A_12
#set output 'Search_Duration_A_12.pdf'
#set table 'smoothed1ks'
#plot '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2ks'
#plot '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set table 'smoothed1fs'
#plot '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2fs'
#plot '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set style fill transparent solid 0.4 noborder
#plot "< paste smoothed1fs smoothed2fs | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_12_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.2: full-Search (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.2: k-Search (Avg)'    with lines lt 7 lw 1.0 axis x1y1 smooth unique
#
## A_12c
#set output 'Search_Duration_A_12c.pdf'
#set table 'smoothed1ks'
#plot '../sim/A_12c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2ks'
#plot '../sim/A_12c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set table 'smoothed1fs'
#plot '../sim/A_12c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2fs'
#plot '../sim/A_12c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set style fill transparent solid 0.4 noborder
#plot "< paste smoothed1fs smoothed2fs | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_12c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.2 + cache: full-Search (Avg)'  with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_12c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.2 + cache: k-Search (Avg)'     with lines lt 7 lw 1.0 axis x1y1 smooth unique


# update

set output 'Update_Duration_A_10+10c.pdf'
set table 'smoothed1'
plot '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($13-$15)/1000) smooth unique
unset table
set table 'smoothed2'
plot '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($13+$16)/1000) smooth unique
unset table
set table 'smoothed3'
plot '../sim/A_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($13-$15)/1000) smooth unique
unset table
set table 'smoothed4'
plot '../sim/A_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($13+$16)/1000) smooth unique
unset table
set style fill transparent solid 0.4 noborder
plot \
    "< paste smoothed1 smoothed2 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
    '../sim/A_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($13/1000) title 'A – v1.0: - Update (Avg)'        with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
    "< paste smoothed3 smoothed4 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
    '../sim/A_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($13/1000) title 'A – v1.0 + cache: Update (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique



# k-search
#set yrange [0:0.5]
#
#
## A_10c
#set output 'Search_Duration_A_10c.pdf'
#set table 'smoothed1ks'
#plot '../sim/A_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2ks'
#plot '../sim/A_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set table 'smoothed1fs'
#plot '../sim/A_10c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2fs'
#plot '../sim/A_10c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set style fill transparent solid 0.4 noborder
#plot "< paste smoothed1fs smoothed2fs | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_10c_fs/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.0 + cache: full-Search (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/A_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'A – v1.0 + cache: k-Search (Avg)'    with lines lt 7 lw 1.0 axis x1y1 smooth unique





########################################################

# B


set yrange [0:6]
set mytics 2

# search

# B_10
set output 'Search_Duration_B_10.pdf'
set table 'smoothed1ks'
plot '../sim/B_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed2ks'
plot '../sim/B_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set style fill transparent solid 0.4 noborder
plot "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/B_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'B – v1.0: k-Search (Avg)'         with lines lt 1 lw 0.5 axis x1y1 smooth unique

# B_10c
set output 'Search_Duration_B_10c.pdf'
set table 'smoothed1ks'
plot '../sim/B_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
unset table
set table 'smoothed2ks'
plot '../sim/B_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
unset table
set style fill transparent solid 0.4 noborder
plot "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
     '../sim/B_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'B – v1.0 + cache: k-Search (Avg)' with lines lt 1 lw 0.5 axis x1y1 smooth unique

## B_12
#set output 'Search_Duration_B_12.pdf'
#set table 'smoothed1ks'
#plot '../sim/B_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2ks'
#plot '../sim/B_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set style fill transparent solid 0.4 noborder
#plot "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/B_12_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'B – v1.2: k-Search (Avg)'          with lines lt 1 lw 0.5 axis x1y1 smooth unique
#
## B_12c
#set output 'Search_Duration_B_12c.pdf'
#set table 'smoothed1ks'
#plot '../sim/B_12c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24-$26)/1000) smooth unique
#unset table
#set table 'smoothed2ks'
#plot '../sim/B_12c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($24+$27)/1000) smooth unique
#unset table
#set style fill transparent solid 0.4 noborder
#plot "< paste smoothed1ks smoothed2ks | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
#     '../sim/B_12c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($24/1000) title 'B – v1.2 + cache: k-Search (Avg)'              with lines lt 1 lw 0.5 axis x1y1 smooth unique

# update

set output 'Update_Duration_B_10+10c.pdf'
set table 'smoothed1'
plot '../sim/B_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($13-$15)/1000) smooth unique
unset table
set table 'smoothed2'
plot '../sim/B_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($13)/1000) smooth unique
unset table
set table 'smoothed3'
plot '../sim/B_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($13-$15)/1000) smooth unique
unset table
set table 'smoothed4'
plot '../sim/B_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:(($13)/1000) smooth unique
unset table
set style fill transparent solid 0.4 noborder
plot \
    "< paste smoothed1 smoothed2 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
    '../sim/B_10_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($13/1000) title 'B – v1.0: Update (Avg)'          with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
    "< paste smoothed3 smoothed4 | grep -v 'u' | awk '{print $1,$2,$5}'" using 1:2:3 with filledcurves lc rgb "#CCCCCC" notitle, \
    '../sim/B_10c_ks/simulation/AAF_SKIPGRAPH.dat' using 1:($13/1000) title 'B – v1.0 + cache: Update (Avg)' with lines lt 1 lw 1.0 axis x1y1 smooth unique
