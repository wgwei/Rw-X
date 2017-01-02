# -*- coding: utf-8 -*-
"""
Created on Thu Jul 28 15:55:16 2016

Calculate the minimum required Rw+Ctr or Rw+C

@author: Weigang Wei
"""

import numpy as np
import random
import pandas as pd
import matplotlib.pylab as plt


class C_Ctr():
    def __init__(self):
        """ the ranges for C and Ctr testing are based on the Pilkinton and Guardian
            test data.
        """
        self.C = np.asarray([-21,-14,-8,-5,-4])
        self.Ctr = np.asarray([-14,-10,-7,-4,-6])
        self.CRange = np.asarray([6,5,5,10,9])/2. # 125 to 2k 
        self.CtrRange = np.asarray([5,4,7,12,12])/2.# 125 to 2k 
    
def Deltai(sourceSpec, refCurve):
    ''' calculated the normalised Delta, where Delta is the difference 
        between source and the Ctr or C curve. ref Wei's study
            sourceSpec: array of source spec
            refCurve: array of Ctr or C cuver
            length of sourceSpec and refCurve should match
    '''
    return sourceSpec - refCurve
    
def norm_Delta(Deltai):
    v = 10*np.log10(sum(10**(Deltai/10.)))
    normalisedDelta = Deltai - v
    return normalisedDelta

def L2_i_spec(L2Limit, normalisedDelta):
    ''' determine the internal noise spectrum based on the difference between 
        the source and the reference Ctr or C curve. 
    '''
    return L2Limit + normalisedDelta
    
def room_conditioni(V, S, T):
    condi = 10.*np.log10(T) + 10.*np.log10(S/V) + 11
    return condi

def room_condition2(V, T, n):
    cond2 = 10.*np.log10(T) + 10.*np.log10(n/V) + 21
    return cond2

def required_RwCtr(L2i, DeltaiCtr, condi):
    RwCtr = condi - 10*np.log10(sum(10**((L2i - DeltaiCtr)/10.)))
    return RwCtr

def required_RwC(L2i, DeltaiC, cond2):
    RwC = cond2 - 10*np.log10(sum(10**((L2i - DeltaiC)/10.)))
    return RwC

def calc_singleNo_rating(sourceSpec, L2Limit, V, S, T, n):    
    ''' sourceSpec = array of source spec
        L2limit = single number in dB(A)
        V = volume in m3
        S = window area in m2
        T = reverberation time in s
        n = number of vent
    '''
    CCtr = C_Ctr()
    CtrOct = CCtr.Ctr
    DeltaiCtr = sourceSpec - CtrOct
    print('Source - Ctr curve -> ', DeltaiCtr)
    normalisedDelta = norm_Delta(DeltaiCtr)
    print('normalised Source - Ctr -> ', np.round(normalisedDelta))
    L2i = L2_i_spec(L2Limit, normalisedDelta)  
    print('L2 in octave bands -> ', np.round(L2i))
    condi = room_conditioni(V,S,T)
    print('10*lg(T)+10*lg(S/V)+11 -> ', np.round(condi))
    RwCtr = required_RwCtr(L2i, DeltaiCtr, condi)
    
    print('\n')
    COct = CCtr.C
    DeltaiC = sourceSpec - COct
    print('Source - C curve -> ', DeltaiC)
    normalisedDelta = norm_Delta(DeltaiC)
    print('normalised Source - C -> ', np.round(normalisedDelta))
    L2i = L2_i_spec(L2Limit, normalisedDelta)  
    print('L2 in octave bands -> ', np.round(L2i))
    cond2 = room_condition2(V,T,n)
    print('10*lg(T)+10*lg(n/V)+21 -> ', np.round(cond2))
    RwC = required_RwC(L2i, DeltaiC, cond2)
    
    print('\nRequired Rw+Ctr -> %d' %RwCtr)
    print('Required Rw+C   -> %d' %RwC)
    print("\n\n")
    
    return [np.round(RwCtr), np.round(RwC)]

class CalcTest(C_Ctr):
    def __init__(self, sourceSpec,L2Limit, V, S, T, n):
        C_Ctr.__init__(self)
        self.sp = sourceSpec
        self.L2Limit = L2Limit
        self.V = V
        self.S = S
        self.T = T
        self.n = n
        self.NUM = 10000
        self.Deltai_Ctr = self.sp - self.Ctr
        self.Deltai_C = self.sp - self.C
        self.refSpecs = [self.Deltai_Ctr, self.Deltai_C]  #assume the initial L2,i spectrum
        self.variations = [self.CtrRange, self.CRange]
        
    def _generate_spec(self, refSpec, variation):   
        print("L2,i variation: ", variation)
        specs = []
        for num in range(self.NUM):
            spec = []
            for s,v in zip(refSpec, variation):
                spec.append(round(random.uniform(s-v, s+v))) # use the closest integer
            
            specA = np.asarray(spec)
            total = 10.*np.log10(sum(10**(specA/10)))
            specA = specA - total + self.L2Limit
#            print('specA - > ', np.round(specA))
            specs.append(specA)
        return specs
    
    def _run_test(self):
        condi = room_conditioni(self.V,self.S, self.T)
        cond2 = room_condition2(self.V, self.T, self.n)
        L2is = []
        RwCtr, RwC = [], []
        for refSpec, variation in zip(self.refSpecs, self.variations):
            L2is += self._generate_spec(refSpec, variation)
        for L2i in L2is:
            vari  = 10.*np.log10(np.sum(10**((L2i - self.Deltai_Ctr)/10)))
            var2 = 10.*np.log10(np.sum(10**((L2i - self.Deltai_C)/10)))
            RwCtr += [condi - vari]
            RwC += [cond2 - var2]
        
        outputRwCtr = pd.Series(RwCtr)
        print("\nRw+Ctr statistics: ")
        print(outputRwCtr.describe())
        outputRwC = pd.Series(RwC)
        print("\nRw+C statistics: ")
        print(outputRwC.describe())
        
        
        # print required Rw+Ctr, Rw+C
        plt.figure()
        bt = min(RwCtr+RwC)
        top = max(RwCtr+RwC)
        plt.boxplot([np.round(RwCtr), np.round(RwC)])
        plt.xticks([1,2],['Rw+Ctr','Rw+C'])
        plt.ylim([bt-5,top+5])
        plt.ylabel('dB')
        plt.grid()
        
        plt.figure()
        plt.subplot(1,2,1)
        plt.hist(RwCtr, bins=2*int(max(RwCtr)-min(RwCtr)))
        plt.subplot(1,2,2)
        plt.hist(RwC, bins=2*int(max(RwC)-min(RwC)))
        plt.show()


if __name__=='__main__':
    sourceSpec = np.asarray([60, 53, 56, 67, 66]) # 4743 vita 70 dB(A) 47
    V, S, T, n = 38, 3.6, 0.5, 1
    L2Limit = 30
    [RwCtr, RwC] = calc_singleNo_rating(sourceSpec, L2Limit, V, S, T, n)
    objtest = CalcTest(sourceSpec, L2Limit, V, S, T, n)
    objtest._run_test()
