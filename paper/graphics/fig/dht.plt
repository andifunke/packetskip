#------------------------------------------------------------
# Number Of Objects and Local DHT size
#------------------------------------------------------------
reset
set term pdf monochrome dashed dl 1 lw 3 font 'Helvetica,16'
#set key font ",15"
set key top right
set xlabel 'Peer'
set ylabel 'Number Of Objects [#]'
set y2label 'Local DHT size [KByte]'
set xrange [:]
set yrange [0:4]
set y2range [0:20]
set ytics nomirror
set y2tics
set logscale x
set grid y y2 lw 0.25

## A
#set output 'DHTSize_PeersSort_A.pdf'
#plot '../sim/A_10_ks/simulation/DHTOverlayPeersSort.dat' using 1:32 title 'Number Of Objects' with lines axis x1y1, \
#     '../sim/A_10_ks/simulation/DHTOverlayPeersSort.dat' using 1:($33/1000) title 'A – v1.0: Local DHT size' with lines lt 2 lw 0.5 axis x1y2, \
#     '../sim/A_12_ks/simulation/DHTOverlayPeersSort.dat' using 1:($33/1000) title 'A – v1.2: Local DHT size' with lines lt 2 lw 1.0 axis x1y2

# C
set output 'DHTSize_PeersSort_C.pdf'
plot '../sim/C_10_ks/simulation/DHTOverlayPeersSort.dat' using 1:32 title 'Number Of Objects' with lines axis x1y1, \
     '../sim/C_10_ks/simulation/DHTOverlayPeersSort.dat' using 1:($33/1000) title 'C – v1.0: Local DHT size' with lines lt 2 lw 0.5 axis x1y2, \
     '../sim/C_12_ks/simulation/DHTOverlayPeersSort.dat' using 1:($33/1000) title 'C – v1.2: Local DHT size' with lines lt 2 lw 1.0 axis x1y2
