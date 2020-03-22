#------------------------------------------------------------
# ComManager: Traffic Time
#------------------------------------------------------------
reset
set terminal pdf monochrome dashed dl 1 lw 3 font 'Helvetica,16'
set xlabel 'Time [minutes]'
set ylabel 'Traffic per Peer [Bytes/sec]'
set xrange [100:329]
set yrange [0:27]
set grid y lw 0.25

set key top right

# A
#set output 'Maintenance_Traffic_Time_A_11.pdf'
#plot '../sim/A_10_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'A – v1.0: Maintenance out' with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/A_11_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'A – v1.1: Maintenance out' with lines lt 1 lw 1.0 axis x1y1 smooth unique

#set output 'Maintenance_Traffic_Time_A_12.pdf'
#plot '../sim/A_10_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'A – v1.0: Maintenance out' with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/A_12_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'A – v1.2: Maintenance out' with lines lt 1 lw 1.0 axis x1y1 smooth unique

set output 'Maintenance_Traffic_Time_A_11+12.pdf'
plot '../sim/A_10_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'A – v1.0: Maintenance out' with lines lt 1 lw 0.50 axis x1y1 smooth unique, \
     '../sim/A_11_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'A – v1.1: Maintenance out' with lines lt 7 lw 0.75 axis x1y1 smooth unique, \
     '../sim/A_12_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'A – v1.2: Maintenance out' with lines lt 1 lw 1.00 axis x1y1 smooth unique

set key bottom right

# C
#set output 'Maintenance_Traffic_Time_C_11.pdf'
#plot '../sim/C_10_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'C – v1.0: Maintenance out' with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/C_11_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'C – v1.1: Maintenance out' with lines lt 1 lw 1.0 axis x1y1 smooth unique

#set output 'Maintenance_Traffic_Time_C_12.pdf'
#plot '../sim/C_10_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'C – v1.0: Maintenance out' with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/C_12_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'C – v1.2: Maintenance out' with lines lt 1 lw 1.0 axis x1y1 smooth unique

set output 'Maintenance_Traffic_Time_C_11+12.pdf'
plot '../sim/C_10_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'C – v1.0: Maintenance out' with lines lt 1 lw 0.50 axis x1y1 smooth unique, \
     '../sim/C_11_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'C – v1.1: Maintenance out' with lines lt 7 lw 0.75 axis x1y1 smooth unique, \
     '../sim/C_12_ks/simulation/CommunicationManagerTraffic.dat' using 1:(($9+$5)/$2/60) title 'C – v1.2: Maintenance out' with lines lt 1 lw 1.00 axis x1y1 smooth unique


#------------------------------------------------------------
# ComManager: Traffic Peer
#------------------------------------------------------------
reset
set terminal pdf monochrome dashed dl 1 lw 3 font 'Helvetica,16'
set key top right
set xlabel 'Peer'
set ylabel 'Traffic per Peer in Total [Bytes]'
set logscale y
#set logscale x
set yrange [100:10000000]
set mytics 10
set border lw 0.75
set grid y

# A
#set output 'Maintenance_Traffic_Peers_A_11.pdf'
#plot '../sim/A_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'A – v1.0: Maintenance out' with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/A_11_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'A – v1.1: Maintenance out' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     '../sim/A_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'A – v1.0: Maintenance in' with lines lt 2 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/A_11_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'A – v1.1: Maintenance in' with lines lt 2 lw 1.0 axis x1y1 smooth unique

#set output 'Maintenance_Traffic_Peers_A_12.pdf'
#plot '../sim/A_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'A – v1.0: Maintenance out' with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/A_12_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'A – v1.2: Maintenance out' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     '../sim/A_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'A – v1.0: Maintenance in' with lines lt 2 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/A_12_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'A – v1.2: Maintenance in' with lines lt 2 lw 1.0 axis x1y1 smooth unique

set output 'Maintenance_Traffic_Peers_A_out.pdf'
plot '../sim/A_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'A – v1.0: Maintenance out' with lines lt 1 lw 0.50 axis x1y1 smooth unique, \
     '../sim/A_11_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'A – v1.1: Maintenance out' with lines lt 7 lw 0.75 axis x1y1 smooth unique, \
     '../sim/A_12_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'A – v1.2: Maintenance out' with lines lt 1 lw 1.00 axis x1y1 smooth unique, \

set output 'Maintenance_Traffic_Peers_A_in.pdf'
plot '../sim/A_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'A – v1.0: Maintenance in' with lines lt 1 lw 0.50 axis x1y1 smooth unique, \
     '../sim/A_11_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'A – v1.1: Maintenance in' with lines lt 7 lw 0.75 axis x1y1 smooth unique, \
     '../sim/A_12_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'A – v1.2: Maintenance in' with lines lt 1 lw 1.00 axis x1y1 smooth unique, \

# C
#set output 'Maintenance_Traffic_Peers_C_11.pdf'
#plot '../sim/C_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'C – v1.0: Maintenance out' with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/C_11_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'C – v1.1: Maintenance out' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     '../sim/C_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'C – v1.0: Maintenance in' with lines lt 2 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/C_11_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'C – v1.1: Maintenance in' with lines lt 2 lw 1.0 axis x1y1 smooth unique

#set output 'Maintenance_Traffic_Peers_C_12.pdf'
#plot '../sim/C_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'C – v1.0: Maintenance out' with lines lt 1 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/C_12_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'C – v1.2: Maintenance out' with lines lt 1 lw 1.0 axis x1y1 smooth unique, \
#     '../sim/C_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'C – v1.0: Maintenance in' with lines lt 2 lw 0.5 axis x1y1 smooth unique, \
#     '../sim/C_12_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'C – v1.2: Maintenance in' with lines lt 2 lw 1.0 axis x1y1 smooth unique

set output 'Maintenance_Traffic_Peers_C_out.pdf'
plot '../sim/C_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'C – v1.0: Maintenance out' with lines lt 1 lw 0.50 axis x1y1 smooth unique, \
     '../sim/C_11_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'C – v1.1: Maintenance out' with lines lt 7 lw 0.75 axis x1y1 smooth unique, \
     '../sim/C_12_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($5+$9) title 'C – v1.2: Maintenance out' with lines lt 1 lw 1.00 axis x1y1 smooth unique, \

set output 'Maintenance_Traffic_Peers_C_in.pdf'
plot '../sim/C_10_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'C – v1.0: Maintenance in' with lines lt 1 lw 0.50 axis x1y1 smooth unique, \
     '../sim/C_11_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'C – v1.1: Maintenance in' with lines lt 7 lw 0.75 axis x1y1 smooth unique, \
     '../sim/C_12_ks/simulation/CommunicationManagerTrafficPeersSort.dat' using 1:($6+$10) title 'C – v1.2: Maintenance in' with lines lt 1 lw 1.00 axis x1y1 smooth unique, \

