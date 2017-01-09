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
        self.CRange = np.asarray([6,5,5,11,9])/2. # 125 to 2k 
        self.CtrRange = np.asarray([6,4,6,11,12])/2.# 125 to 2k 


class L2i():
    def __init__(self):
        self.medGlazSpec = np.array([-24., -19., -10., -4., -3.]) # normalised to 0 dB
        
    
def room_conditioni(V, S, T):
    condi = 10.*np.log10(T) + 10.*np.log10(S/V) + 11
    return condi

def room_condition2(V, T, n):
    cond2 = 10.*np.log10(T) + 10.*np.log10(n/V) + 21
 

class CalcSNRRandom(C_Ctr, L2i):
    def __init__(self, sourceSpec,L2Limit, V, S, T, n):
        C_Ctr.__init__(self)
        L2i.__init__(self)
        self.sp = sourceSpec
        self.L2Limit = L2Limit
        self.V = V
        self.S = S
        self.T = T
        self.n = n
        self.NUM = 10000
        self.Deltai_Ctr = self.sp - self.Ctr
        self.Deltai_C = self.sp - self.C
        self.refSpecs = [self.sp + self.medGlazSpec, self.sp + self.medGlazSpec]  #assume the initial L2,i spectrum
        self.refSpecs2 = [self.Deltai_Ctr, self.Deltai_C]
        self.refSpecs3 = [[0,0,0,0,0], [0,0,0,0,0]]
        self.variation = (self.CtrRange+self.CRange)/2
        
    def _generate_L2_spec(self, refSpec, variation):   
        specs = []
        for num in range(self.NUM):
            spec = []
            for s,v in zip(refSpec, variation):
                spec.append(round(random.uniform(s-v, s+v))) # use the closest integer
            
            specA = np.asarray(spec)
            total = 10.*np.log10(np.sum(10**(specA/10)))
            specA = specA - total + self.L2Limit
#            print('specA - > ', np.round(specA))
            specs.append(specA)
#            print(aa)
        return specs
    
    def _compare_L2_spec(self):
        refSpecsTest = [self.refSpecs2, self.refSpecs3]
        specNames = ['Src minus 717ref', 'Flat']
        Rwx = []
        for refSpecs, namestr in zip(refSpecsTest, specNames):
            [RwCtr,RwC] = self._run_Rwx(refSpecs, namestr)
            Rwx.append([RwCtr,RwC])
        for n in range(len(refSpecsTest)):
            plt.subplot(1,2,n+1)
            [RwCtr,RwC] = Rwx[n]
            bt = min(RwCtr+RwC)
            top = max(RwCtr+RwC)
            plt.boxplot([np.round(RwCtr), np.round(RwC)])
            plt.xticks([1,2],['Rw+Ctr','Rw+C'])
            plt.ylim([bt-5,top+5])
            plt.ylabel('dB')
            plt.title(specNames[n])
            plt.grid()
        plt.savefig('Statistics.png')
        plt.show()
          
    def _run_Rwx(self, refSpecsInput, namestr):
        condi = room_conditioni(self.V,self.S, self.T)
        L2is = []
        RwCtr, RwC = [], []
        # for Rw+Ctr calculation
        for refSpec in refSpecsInput:
            L2is += self._generate_L2_spec(refSpec, self.variation)
        for L2i in L2is:
#            print("L2Ctr -> ", L2Ctr)
#            print("L2C -> ", L2C)
            vari  = 10.*np.log10(np.sum(10**((L2i - self.Deltai_Ctr)/10)))
            var2 = 10.*np.log10(np.sum(10**((L2i - self.Deltai_C)/10)))
            RwCtr += [condi - vari]
            RwC += [condi - var2]
        
        outputRwCtr = pd.Series(RwCtr)
        print("\nRw+Ctr statistics: ")
        RwCtrStat = outputRwCtr.describe()
        RwCtrStat.to_csv('RwCtr-Statistics'+namestr+'.txt', sep = ' ')
        print(RwCtrStat)
        outputRwC = pd.Series(RwC)
        print("\nRw+C statistics: ")
        RwCStat = outputRwC.describe()
        RwCStat.to_csv('RwC-Statistics'+namestr+'.txt', sep = ' ')
        print(RwCStat)
        
        return [RwCtr, RwC]
        

def case_studies():
    # 4743 VITA 6-16-6.8 (Laminated) (38,34)
    sspeci = np.array([47, 53, 56, 67, 66])
    V, S, T, n = 38, 3.6, 0.5, 1
    L2limit = 30 # 29
    
    # 4574 Spanish city 6/12/4/12/8.8 (44.2) guardian triple (39, 35)
    sspec2 = np.array([57, 59, 64, 85, 79])
    V2, S2, T2, n2 = 40.5, 4.0, 0.5, 0
    L2limit2 = 42 # 38
    
    # Traffic envelope upper limits
    sspec3 = np.array([60.4, 63.1, 65.4, 64.1, 59.2])
    V3, S3, T3, n3 = 45, 2.4, 0.5, 0
    L2limit3 = 35 
    
    # Traffic envelope lower limits
    sspec4 = np.array([49.0, 56.9, 61.0, 67.6, 63.9])
    V4, S4, T4, n4 = 45, 2.4, 0.5, 0
    L2limit4 = 35
    
    casei = [sspeci, V, S, T, n, L2limit]
    case2 = [sspec2, V2, S2, T2, n2, L2limit2]
    case3 = [sspec3, V3, S3, T3, n3, L2limit3]
    case4 = [sspec4, V4, S4, T4, n4, L2limit4]

    return case4

if __name__=='__main__':
    [sourceSpec, V, S, T, n, L2Limit]=case_studies() 
    objtest = CalcSNRRandom(sourceSpec, L2Limit, V, S, T, n)
    objtest._compare_L2_spec()