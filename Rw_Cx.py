"""
Calcualte acoustically required single number rating for glaizng and ventilation strategy. 
Author: Weigang Wei
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
        
        
def Deltai(sourceSpec, refCurve):
    ''' calculated the normalised Delta, where Delta is the difference 
        between source and the Ctr or C curve. ref Wei's study
            sourceSpec: array of source spec
            refCurve: array of Ctr or C cuver
            length of sourceSpec and refCurve should match
    '''
    return sourceSpec - refCurve
    
def norm_Delta(Deltai):
    v = 10*np.log10(np.sum(10**(Deltai/10.)))
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

def required_RwC(L2i, DeltaiC, condi):
    RwC = condi - 10*np.log10(np.sum(10**((L2i - DeltaiC)/10.)))
    return RwC

def calc_singleNo_rating_Wei(sourceSpec, L2Limit, V, S, T, n):    
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
    print('10*lg(T)+10*lg(S/V)+11 -> ', np.round(condi))
    RwC = required_RwC(L2i, DeltaiC, condi)
    
    print('\nRequired Rw+Ctr ->', int(round(RwCtr)))
    print('Required Rw+C   -> ', int(round(RwC)))
    print("\n\n")
    
    return [np.round(RwCtr), np.round(RwC)]

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
        self.refSpec = [0, 0, 0, 0, 0]
        self.variation = (self.CtrRange + self.CRange)/2.
        
    def _generate_L2_spec(self, refSpec, variation):   
        print("L2,i variation: ", variation)
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
        return specs
    
    def _run_test(self):
        condi = room_conditioni(self.V,self.S, self.T)
#        cond2 = room_condition2(self.V, self.T, self.n)
        L2is = []
        RwCtr, RwC = [], []
        L2is = self._generate_L2_spec(self.refSpec, self.variation)
#            
        for L2i in L2is:
            vari  = 10.*np.log10(np.sum(10**((L2i - self.Deltai_Ctr)/10)))
            var2 = 10.*np.log10(np.sum(10**((L2i - self.Deltai_C)/10)))
            RwCtr += [condi - vari]
            RwC += [condi - var2]
#        
        outputRwCtr = pd.Series(RwCtr)
        print("\nRw+Ctr statistics: ")
        RwCtrStat = outputRwCtr.describe()
        RwCtrStat.to_csv('RwCtr-Statistics.txt', sep = ' ')
        print(RwCtrStat)
        outputRwC = pd.Series(RwC)
        print("\nRw+C statistics: ")
        RwCStat = outputRwC.describe()
        RwCStat.to_csv('RwC-Statistics.txt', sep = ' ')
        print(RwCStat)
        
        
        # print required Rw+Ctr, Rw+C
        plt.figure()
        bt = min(RwCtr+RwC)
        top = max(RwCtr+RwC)
        plt.boxplot([np.round(RwCtr), np.round(RwC)])
        plt.xticks([1,2],['Rw+Ctr','Rw+C'])
        plt.ylim([bt-5,top+5])
        plt.ylabel('dB')
        plt.grid()
        plt.savefig('Statistics.png')
        
        plt.figure()
        plt.subplot(1,2,1)
        plt.hist(RwCtr, bins=2*int(max(RwCtr)-min(RwCtr)))
        plt.subplot(1,2,2)
        plt.hist(RwC, bins=2*int(max(RwC)-min(RwC)))
        plt.savefig('density-function.png')
        plt.show()

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
    
    return case3
    
if __name__=='__main__':
    [sourceSpec, V, S, T, n, L2Limit]=case_studies() 
    [RwCtr, RwC] = calc_singleNo_rating_Wei(sourceSpec, L2Limit, V, S, T, n)
    objtest = CalcSNRRandom(sourceSpec, L2Limit, V, S, T, n)
    objtest._run_test()
