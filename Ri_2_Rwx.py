# -*- coding: utf-8 -*-
"""
Created on Tue May  9 21:47:22 2017

@author: W Wei
"""
import numpy as np

def sum_unfav_deviation(Ri, RwRef):
    deviation = np.asarray(RwRef) - np.asarray(Ri)
#    print ("deviation - > ", deviation)
    sumUnfavDevi = sum([max(0, dev) for dev in deviation])
#    print("sum of unfav -> ", sumUnfavDevi)
    return sumUnfavDevi

def Ri_to_Rw_x(Ri):
    ''' calculate Rw(C;Ctr) from Ri 
        Ri = [n1,n2,..] 5 octave from 125 to 2k or 16 1/3 octave from 100 to 3150 Hz
    '''
    Ri = list(Ri)
    if len(Ri)==5:
        RwRef = np.array([36., 45., 52., 55., 56.]) # 125,250,500,1000,2000
        LOC500HZ = 2
        UNFAVLIMIT = 10
        refNO1 = np.array([-21., -14., -8., -5., -4.]) # for C correction
        refNO2 = np.array([-14., -10., -7., -4., -6.]) # for Ctr correction
    elif len(Ri)==16:
        RwRef = np.array([33., 36., 39., 42., 45., 48., 51., 52., 53., 54., 55., 56., 56., 56., 56., 56.])
        LOC500HZ = 7
        UNFAVLIMIT = 32
        refNO1 = np.array([-29., -26., -23., -21., -19., -17., -15., -13., -12., -11., -10., -9., -9., -9., -9., -9.])
        refNO2 = np.array([-20., -20., -18., -16., -15., -14., -13., -12., -11., -9., -8., -9., -10., -11., -13., -15.])
    else:
        assert False # "the Ri needs to be either 5 octave (125 to 2kHz)or 16 1/3 octave (100 to 3150Hz)"
    
    # Calculate Rw    
    shift = 0
    sumUnfavDevi = sum_unfav_deviation(Ri, RwRef)
    
    if sumUnfavDevi >UNFAVLIMIT:
        while sumUnfavDevi >UNFAVLIMIT:
#            print ("shift-> ", shift)
            shift = shift - 1
            RwRefT = RwRef + shift
            sumUnfavDevi = sum_unfav_deviation(Ri, RwRefT)
        Rw = RwRefT[LOC500HZ]
    elif sumUnfavDevi<=10:
        while sumUnfavDevi <= UNFAVLIMIT:
#            print ("shift-> ", shift)
            shift  = shift + 1
            RwRefT = RwRef + shift
            sumUnfavDevi = sum_unfav_deviation(Ri, RwRefT)
        RwRefT = RwRefT - 1
        Rw = RwRefT[LOC500HZ]
#    print ("\nRw -> ", Rw)
    
    # calculate (C;Ctr)
    XA1 = -10.*np.log10(sum(10.**((refNO1-np.asarray(Ri))/10.)))
    XA2 = -10.*np.log10(sum(10.**((refNO2-np.asarray(Ri))/10.)))
    
    C = np.round(XA1 - Rw)
    Ctr = np.round(XA2 - Rw)
#    print("(C; Ctr) - > ", (C, Ctr))
    
    return [Rw, Rw+C, Rw+Ctr]

if __name__=="__main__":
    Ri = [28, 37, 43, 48, 50, 55, 59, 62, 64, 65, 69, 70, 72, 73, 69, 65]
    [Rw, RwPlusC, RwPlusCtr] = Ri_to_Rw_x(Ri)
    print("\nRw (C;Ctr) -> %d (%d;%d) dB" %(Rw, RwPlusC, RwPlusCtr))
