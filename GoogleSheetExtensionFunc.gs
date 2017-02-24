function log10(x){
    return Math.log(x)/Math.LN10;
}

function sum(range){
    var sumValue = 0;
    for(var i=0; i<range.length; i++){
        sumValue += range[i];
    }
    return sumValue;
}

function array_plus_number(objecti, number){
    var arr = [];
    for (var i=0; i<objecti.length; i++){
        arr.push(objecti[i] + number);
    }
    return arr;
}

function random_int_between(a, b){
    var v = Math.floor(Math.random() * b) + a;
    return v;
}

function extracted_parameters(){
    /* extract the parameters from ISO 717 and Pilkington and Guardian glass
    assume the internal spec is flat + variation 
    5 octave bands considered only */
    var parameters = {L2refspec:[0,0,0,0,0], specVariation:[6, 5, 6, 11, 11], cNormalised:[-21,-14,-8,-5,-4], ctrNormalised:[-14,-10,-7,-4,-6]} ;
   return parameters;
}

function source_minus_C(sourceSpec){
    var parameters = extracted_parameters();
    var Cvalue = parameters.cNormalised;
    var srcMinusC = [];
    
    for (var i=0; i<5; i++){
        srcMinusC.push(sourceSpec[0][i] - Cvalue[i]);
    }
    return srcMinusC;
}

function source_minus_Ctr(sourceSpec){
    var parameters = extracted_parameters();
    var CtrValue = parameters.ctrNormalised;
    var srcMinusCtr = [];
    for (var i=0; i<5; i++){
        srcMinusCtr.push(sourceSpec[0][i] - CtrValue[i]);
    }
    return srcMinusCtr;
}
    
 
function room_condition(V,S,T,n){
     /* constant determined by the room volume, window size and vent number */
     var condi = 10*log10(T) + 10*log10(S/V) + 11; 
     var cond2 = 10*log10(T) + 10*log10(n/V) + 21;     
     return [condi, cond2];
}


function gen_internal_spec(L2spcsVariation, IANL){
     var L2specs = [];
     for (var i=0; i<L2spcsVariation.length; i++){
         var totalEngergy = 0;
         for (var j=0; j<L2spcsVariation[i].length; j++){
              totalEngergy += Math.pow(10,(L2spcsVariation[i][j]/10));
         }
         var totalLevel = 10*log10(totalEngergy);
         L2specs.push(array_plus_number(L2spcsVariation[i], -totalLevel+IANL));
    }
    return L2specs;
}

function gen_random_spec_variations(NumOfSpec){
    var parameters = extracted_parameters();
    var refspec = parameters.L2refspec;
    var variation = parameters.specVariation;
    var L2specsVariation = [];
    for (var i=0; i<NumOfSpec; i++){
         var spec = [];
         for (var f=0; f<refspec.length; f++){
             spec.push(random_int_between(refspec[f]-variation[f]/2, refspec[f]+variation[f]/2));
         }
         L2specsVariation.push(spec);
    }
    return L2specsVariation;
}

function r_x(sourceMinusX, roomCondition, L2specs){
    var RwX = [];
    for (var i=0; i<L2specs.length; i++){
        var L2i = L2specs[i];
        var L2EnergyX = 0;
        for (var j=0; j<5; j++){ // 5 octave
            L2EnergyX += Math.pow(10, (L2i[j] - sourceMinusX[j])/10);
        }
        RwX.push(roomCondition - 10*log10(L2EnergyX));
    }
    return RwX;
}


function get_statistical_value(RwC, RwCtr){
    RwC.sort();
    RwCtr.sort();
        
    // get statistical values
    var Cmax = RwC[RwC.length-1];
    var C10 = RwC[Math.round(0.9*RwC.length)];
    var C25 = RwC[Math.round(0.75*RwC.length)];
    var C75 = RwC[Math.round(0.25*RwC.length)];
    var C50perRange = C25 - C75;
    
    var Ctrmax = RwCtr[RwCtr.length - 1];
    var Ctr10 = RwCtr[Math.round(0.9*RwCtr.length)];
    var Ctr25 = RwCtr[Math.round(0.75*RwCtr.length)];
    var Ctr75 = RwCtr[Math.round(0.25*RwCtr.length)];
    var Ctr50perRange = Ctr25 - Ctr75;
    
    return [[Cmax, C10, C25, C75, C50perRange],[Ctrmax, Ctr10, Ctr25,Ctr75, Ctr50perRange]];
}
 
 
function get_Rwx_samples(VSTn, sourceSpec, IANLwin, IANLvent){
/* used for debugging
function get_Rwx_samples(){
    var V = 38;
    var S = 3.6;
    var T = 0.5;
    var n = 1;
    var sourceSpec = [[47, 53, 56, 67, 66]];
    var IANLwin = 30;
    var IANLvent = 30;
*/
     var V = VSTn[0][0]; 
     var S = VSTn[0][1];
     var T = VSTn[0][2];
     var n = VSTn[0][3];
       
    var NumOfSpec = 1000;
    var L2spcsVariation = gen_random_spec_variations(NumOfSpec);
    var L2specsWin = gen_internal_spec(L2spcsVariation, IANLwin);
    var L2specsVent = gen_internal_spec(L2spcsVariation, IANLvent);
    
    var sourceMinusC = source_minus_C(sourceSpec);
    var sourceMinusCtr = source_minus_Ctr(sourceSpec);
    var roomConditions = room_condition(V,S,T,n);
    
    var RwC = r_x(sourceMinusC, roomConditions[0], L2specsWin);
    var RwCtr = r_x(sourceMinusCtr, roomConditions[0], L2specsWin);
    
    var RwCVent = r_x(sourceMinusC, roomConditions[1], L2specsVent);
    var RwCtrVent = r_x(sourceMinusCtr, roomConditions[1], L2specsVent); 
    
    // sort smallest to largest for glazing spec
    RwC.sort();
    RwCtr.sort();
        
    // get statistical values
    var Cmax = RwC[RwC.length-1];
    var C10 = RwC[Math.round(0.9*RwC.length)];
    var C25 = RwC[Math.round(0.75*RwC.length)];
    var C75 = RwC[Math.round(0.25*RwC.length)];
    var C50perRange = C25 - C75;
    
    var Ctrmax = RwCtr[RwCtr.length - 1];
    var Ctr10 = RwCtr[Math.round(0.9*RwCtr.length)];
    var Ctr25 = RwCtr[Math.round(0.75*RwCtr.length)];
    var Ctr75 = RwCtr[Math.round(0.25*RwCtr.length)];
    var Ctr50perRange = Ctr25 - Ctr75;
    
    // sort smallest to largest for vent spec
    RwCVent.sort();
    RwCtrVent.sort();
    
    // get statistical values
    var CmaxVent = RwCVent[RwCVent.length-1];
    var C10Vent = RwCVent[Math.round(0.9*RwCVent.length)];
    var C25Vent = RwCVent[Math.round(0.75*RwCVent.length)];
    var C75Vent = RwCVent[Math.round(0.25*RwCVent.length)];
    var C50perRangeVent = C25Vent - C75Vent;
    
    var CtrmaxVent = RwCtrVent[RwCtrVent.length - 1];
    var Ctr10Vent = RwCtrVent[Math.round(0.9*RwCtrVent.length)];
    var Ctr25Vent = RwCtrVent[Math.round(0.75*RwCtrVent.length)];
    var Ctr75Vent = RwCtrVent[Math.round(0.25*RwCtrVent.length)];
    var Ctr50perRangeVent = Ctr25Vent - Ctr75Vent
    
    // recommended glazing parameter
    if (C50perRange>Ctr50perRange){
      var recommendCtr = "Rw+Ctr";
      var recommendC = "";
    }
    else{
      var recommendCtr = "";
      var recommendC = "Rw+C";
    }
    
    // recommended vent spec
    if (C50perRangeVent>Ctr50perRangeVent){
      var recommendVentCtr = "Rw+Ctr";
      var recommendVentC = "";
    }
    else{
      var recommendVentCtr = "";
      var recommendVentC = "Rw+C";
    }
    
    var titles = ["Parameter", "Max", "10%", "25%", "75%", "25% to 75%", "Recommended para"];
    var statisticalReturn = [titles,["Glazing Rw+C", Cmax, C10, C25, C75, C50perRange, recommendC], 
                                         ["Glazing Rw+Ctr", Ctrmax, Ctr10, Ctr25,Ctr75, Ctr50perRange, recommendCtr], 
                    ["Vent Rw+C", CmaxVent, C10Vent, C25Vent, C75Vent, C50perRangeVent, recommendVentC], 
                    ["Vent Rw+Ctr", CtrmaxVent, Ctr10Vent, Ctr25Vent,Ctr75Vent, Ctr50perRangeVent, recommendVentCtr]];

                         
     // read glass and vent data from the sheets
     var ss = SpreadsheetApp.getActiveSpreadsheet();
     var Loc125Hz = 5;
     var Loc2kHz = 9;
     
     // process glass
     var glassSheet = ss.getSheetByName("glazing data");
     var glassRange = glassSheet.getDataRange(); // glass data
     var glassSpec = glassRange.getValues(); // object [][] 
     var glassL2 = [];     
     for (var i = 1; i < glassSpec.length; i++) { //loop row
       var L2f = [];
       for (var j = Loc125Hz; j < Loc2kHz; j++) { 
         if (glassSpec[i][j]) {
           L2f.push(sourceSpec[0][j-Loc125Hz] - glassSpec[i][j] + roomConditions[0]); // sourceSpec start from location 0
         }
       }
       var energy = 0.0;
       for (var f=0; f<L2f.length; f++){
         energy = energy + Math.pow(10, L2f[f]/10);
       }
       var L2 = 10*log10(energy);
       if (L2<=IANLwin){
         glassL2.push([glassSpec[i][1], glassSpec[i][3], glassSpec[i][4], L2]);
       }
     }  
     
     // process vent
     var ventSheet = ss.getSheetByName("vent data");     
     var ventRange = ventSheet.getDataRange(); // vent data
     var ventSpec = ventRange.getValues(); //object [][]
     var ventL2 = [];     
     for (var m = 1; m < ventSpec.length; m++) { //loop row
       var L2fVent = [];
       for (var n = Loc125Hz; n < Loc2kHz; n++) { 
         if (ventSpec[m][n]) {
           L2fVent.push(sourceSpec[0][n-Loc125Hz] - ventSpec[m][n] + roomConditions[1]);
         }
       }
       var energy = 0.0;
       for (var f=0; f<L2fVent.length; f++){
         energy = energy + Math.pow(10, L2fVent[f]/10);
       }
       var L2 = 10*log10(energy);
       if (L2<=IANLvent){
         ventL2.push([ventSpec[m][1], ventSpec[m][3], ventSpec[m][4], L2]);
       }
     } 
    
     var titles2 = ["Producer", "Rw + C", "Rw + Ctr", "Internal noise level, dB(A)"];
     statisticalReturn.push(" ");
     statisticalReturn.push(titles2);
     if (glassL2.length<1){
       statisticalReturn.push("No glass data feasible");
     }
     else{
       for (var p=0; p<glassL2.length; p++){
         statisticalReturn.push(glassL2[p]);
       }
     }
     
     var titles3 = ["Producer", "Dne,w + C", "Dne,w + Ctr", "Internal noise level, dB(A)"];
     statisticalReturn.push(" ");
     statisticalReturn.push(titles3);
     if (ventL2.length<1){
       statisticalReturn.push("No vent data feasible");
     }
     else{
       for (var q=0; q<ventL2.length; q++){
         statisticalReturn.push(ventL2[q]);
       }
     }
    
  return statisticalReturn;
}
