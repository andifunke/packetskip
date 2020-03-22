#------------------------------------------------------------
# NetLayer: Traffic Per Peer (comparison to aggregated traffic)
#------------------------------------------------------------
reset
set terminal pdf monochrome dashed dl 1 lw 2 font 'Helvetica,16'
set key font ",14"
# set key out hor
# set key at screen -1,-2 top center
set key tmargin center hor
set key width -3.5
set key samplen 2.0
set yrange [0.1:10000]
set logscale y
set xlabel 'Peer'
set ylabel 'Bandwidth out [Bytes/sec]'
set tics font ",14"
set mytics 10
set border lw 0.75
set grid y lw 0.25

## A_10
#set output 'Top5_Traffic_Peers_A_10.pdf'
#plot '../sim/A_10_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/A_10_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/A_10_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/A_10_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/A_10_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines lt 0 axis x1y1 smooth unique, \
#     '../sim/A_10_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
##     '../sim/A_10_ks/simulation/NetPeersSort.dat' using 1:($107/8) title 'PredecessorMsg (top5)' with lines axis x1y1 smooth unique, \
#
## A_10c
#set output 'Top5_Traffic_Peers_A_10c.pdf'
#plot '../sim/A_10c_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/A_10c_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/A_10c_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/A_10c_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/A_10c_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'PredecessorMsg (top4)' with lines lt 0 axis x1y1 smooth unique, \
#     '../sim/A_10c_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
#
## A_11
#set output 'Top5_Traffic_Peers_A_11.pdf'
#plot '../sim/A_11_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/A_11_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/A_11_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/A_11_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/A_11_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines linetype 0 axis x1y1 smooth unique, \
#     '../sim/A_11_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
#
## A_12
#set output 'Top5_Traffic_Peers_A_12.pdf'
#plot '../sim/A_12_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/A_12_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/A_12_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/A_12_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/A_12_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines lt 0 axis x1y1 smooth unique, \
#     '../sim/A_12_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
#
## A_12c
#set output 'Top5_Traffic_Peers_A_12c.pdf'
#plot '../sim/A_12c_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/A_12c_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/A_12c_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/A_12c_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/A_12c_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'PredecessorMsg (top4)' with lines linetype 0 axis x1y1 smooth unique, \
#     '../sim/A_12c_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique


## B_10
#set output 'Top5_Traffic_Peers_B_10.pdf'
#plot '../sim/B_10_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/B_10_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/B_10_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/B_10_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/B_10_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines lt 0 axis x1y1 smooth unique, \
#     '../sim/B_10_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
##     '../sim/B_10_ks/simulation/NetPeersSort.dat' using 1:($107/8) title 'PredecessorMsg (top5)' with lines axis x1y1 smooth unique, \
#
## B_10c
#set output 'Top5_Traffic_Peers_B_10c.pdf'
#plot '../sim/B_10c_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/B_10c_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/B_10c_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/B_10c_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/B_10c_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines lt 0 axis x1y1 smooth unique, \
#     '../sim/B_10c_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
#
## B_11
#set output 'Top5_Traffic_Peers_B_11.pdf'
#plot '../sim/B_11_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/B_11_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/B_11_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/B_11_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/B_11_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines linetype 0 axis x1y1 smooth unique, \
#     '../sim/B_11_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
#
## B_12
#set output 'Top5_Traffic_Peers_B_12.pdf'
#plot '../sim/B_12_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/B_12_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/B_12_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/B_12_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/B_12_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines lt 0 axis x1y1 smooth unique, \
#     '../sim/B_12_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
#
## B_12c
#set output 'Top5_Traffic_Peers_B_12c.pdf'
#plot '../sim/B_12c_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/B_12c_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/B_12c_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/B_12c_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/B_12c_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines linetype 0 axis x1y1 smooth unique, \
#     '../sim/B_12c_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique


# C_10
set output 'Top5_Traffic_Peers_C_10.pdf'
plot '../sim/C_10_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
     '../sim/C_10_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
     '../sim/C_10_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
     '../sim/C_10_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
     '../sim/C_10_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines linetype 0 axis x1y1 smooth unique, \
     '../sim/C_10_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
#     '../sim/C_10_ks/simulation/NetPeersSort.dat' using 1:($107/8) title 'PredecessorMsg (top5)' with lines axis x1y1 smooth unique, \

## C_11
#set output 'Top5_Traffic_Peers_C_11.pdf'
#plot '../sim/C_11_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
#     '../sim/C_11_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
#     '../sim/C_11_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
#     '../sim/C_11_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
#     '../sim/C_11_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines linetype 0 axis x1y1 smooth unique, \
#     '../sim/C_11_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique

# C_12
set output 'Top5_Traffic_Peers_C_12.pdf'
plot '../sim/C_12_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
     '../sim/C_12_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
     '../sim/C_12_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
     '../sim/C_12_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
     '../sim/C_12_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'LookupReply (top4)' with lines lt 0 axis x1y1 smooth unique, \
     '../sim/C_12_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique

# C_12c
set output 'Top5_Traffic_Peers_C_12c.pdf'
plot '../sim/C_12c_ks/simulation/NetPeersSort.dat' using 1:($102/8) title 'Aggregated' with lines axis x1y1 smooth unique, \
     '../sim/C_12c_ks/simulation/NetPeersSort.dat' using 1:($103/8) title 'Lookup (top1)' with lines axis x1y1 smooth unique, \
     '../sim/C_12c_ks/simulation/NetPeersSort.dat' using 1:($104/8) title 'Ack (top2)' with lines axis x1y1 smooth unique, \
     '../sim/C_12c_ks/simulation/NetPeersSort.dat' using 1:($105/8) title 'Predec.Reply (top3)' with lines axis x1y1 smooth unique, \
     '../sim/C_12c_ks/simulation/NetPeersSort.dat' using 1:($106/8) title 'Predec.Msg (top4)' with lines linetype 0 axis x1y1 smooth unique, \
     '../sim/C_12c_ks/simulation/NetPeersSort.dat' using 1:(($87+$88+$89+$90+$91+$92+$93+$94+$95+$96+$97+$98+$99+$100+$101)/8) title 'PacketSkip' with lines axis x1y1 smooth unique
