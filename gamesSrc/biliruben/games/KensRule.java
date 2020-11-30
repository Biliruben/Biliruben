package biliruben.games;

import java.util.Date;

public class KensRule {

    static final int tileA = 0;
    static final int tileB = 1;
    static final int tileC = 2;
    static final int tileD = 3;
    static final int tileE = 4;
    static final int tileF = 5;
    static final int tileG = 6;
    static final int tileH = 7;
    static final int tileI = 8;
    static final int tileJ = 9;
    static final int tileK = 10;
    static final int tileL = 11;
    static final int tileM = 12;
    static final int tileN = 13;
    static final int tileO = 14;
    static final int tileP = 15;
    static final int tileQ = 16;
    static final int tileR = 17;
    static final int tileS = 18;

    static long seedCnt = 0L;
    static long solCnt = 0L;

    /**
     * @param args
     */
    public static void main(String[] args) {


        // log4j msgs at various levels
        // log.fatal("this is fatal error msg");
        // log.error("this is error error msg");
        // log.warn("this is warn error msg");
        // log.info("this is info error msg");
        // log.debug("this is debug error msg");
        // log.trace("this is trace error msg");

        // Java print msgs
        // System.out.println("this is stdout msg");
        // System.err.println("this is stderr msg");

        // Java thread dump
        // new Exception().printStackTrace(System.out);

        // IIQ audit event
        // Boolean ret = Auditor.logAs("Source", "run", "Target", "Arg1", "Arg2", "Arg3", "Arg4");
        // context.commitTransaction(); 

        // Java exception
        // throw new IllegalArgumentException("this is Exception msg")
        Date date = new Date();

        int[] tileSetValues = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        for (int loopG = 1; loopG < 20; loopG++) {
            zeroAllValues(tileSetValues);
            tileSetValues[tileG] = loopG;

            System.out.println("G=" +loopG+ " of 19; attempts=" +seedCnt);

            for (int loopK = 1; loopK < 20; loopK++) {
                tileSetValues[tileK] = 0;
                if (isValueInSet(loopK,tileSetValues)) continue;
                tileSetValues[tileK] = loopK;

                for (int loopM = 1; loopM < 20; loopM++) {
                    tileSetValues[tileM] = 0;
                    if (isValueInSet(loopM,tileSetValues)) continue;
                    tileSetValues[tileM] = loopM;

                    for (int loopO = 1; loopO < 20; loopO++) {
                        tileSetValues[tileO] = 0;
                        if (isValueInSet(loopO,tileSetValues)) continue;
                        tileSetValues[tileO] = loopO;

                        for (int loopQ = 1; loopQ < 20; loopQ++) {
                            tileSetValues[tileQ] = 0;
                            if (isValueInSet(loopQ,tileSetValues)) continue;
                            tileSetValues[tileQ] = loopQ;

                            for (int loopR = 1; loopR < 20; loopR++) {
                                tileSetValues[tileR] = 0;
                                if (isValueInSet(loopR,tileSetValues)) continue;
                                tileSetValues[tileR] = loopR;

                                for (int loopS = 1; loopS < 20; loopS++) {
                                    tileSetValues[tileS] = 0;
                                    if (isValueInSet(loopS,tileSetValues)) continue;
                                    tileSetValues[tileS] = loopS;

                                    seedCnt++;
                                    int valueTmp = 0;

                                    valueTmp = computeTileValue(tileL, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileL] = valueTmp;

                                    valueTmp = computeTileValue(tileD, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileD] = valueTmp;

                                    valueTmp = computeTileValue(tileI, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileI] = valueTmp;

                                    valueTmp = computeTileValue(tileJ, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileJ] = valueTmp;

                                    valueTmp = computeTileValue(tileC, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileC] = valueTmp;

                                    valueTmp = computeTileValue(tileF, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileF] = valueTmp;

                                    valueTmp = computeTileValue(tileP, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileP] = valueTmp;

                                    valueTmp = computeTileValue(tileH, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileH] = valueTmp;

                                    valueTmp = computeTileValue(tileB, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileB] = valueTmp;

                                    valueTmp = computeTileValue(tileE, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileE] = valueTmp;

                                    valueTmp = computeTileValue(tileN, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileN] = valueTmp;

                                    valueTmp = computeTileValue(tileA, tileSetValues); 
                                    if ((valueTmp < 1) || (19 < valueTmp) || (isValueInSet(valueTmp,tileSetValues))) { zeroComputedValues(tileSetValues); continue; }
                                    tileSetValues[tileA] = valueTmp;

                                    printSolution(tileSetValues);
                                    solCnt++;

                                    zeroComputedValues(tileSetValues);
                                } // loopS
                                tileSetValues[tileS] = 0;
                            } // loopR
                            tileSetValues[tileR] = 0;
                        } // loopQ
                        tileSetValues[tileQ] = 0;
                    } // loopO
                    tileSetValues[tileO] = 0;
                } // loopM
                tileSetValues[tileM] = 0;
            } // loopK
            tileSetValues[tileK] = 0;
        }
        // seedCnt equals 19*18*17*16*15*14*13 attempts
        System.out.println("attempts: " +seedCnt+ "; solutions: " +solCnt);
        long diff = System.currentTimeMillis();
        long elapsed = diff - date.getTime();
        System.out.println("Elapsed time: " + elapsed);
    }
    static void printSolution(int[] setTmp) {

        //System.out.printf("        H=%-2d", setTmp[tileH]);
        //System.out.printf("    S=%-2d    I=%-2d", setTmp[tileS], setTmp[tileI]);
        //System.out.printf("R=%-2d     B=%-2d     J=%-2d", setTmp[tileR], setTmp[tileB], setTmp[tileJ]);
        //System.out.printf("    G=%-2d     C=%-2d", setTmp[tileG], setTmp[tileC]);
        //System.out.printf("Q=%-2d    A=%-2d     K=%-2d", setTmp[tileQ], setTmp[tileA], setTmp[tileK]);
        //System.out.printf("    F=%-2d     D=%-2d", setTmp[tileF], setTmp[tileD]);
        //System.out.printf("P=%-2d    E=%-2d     L=%-2d", setTmp[tileP], setTmp[tileE], setTmp[tileL]);
        //System.out.printf("    O=%-2d    M=%-2d", setTmp[tileO], setTmp[tileM]);
        //System.out.printf("        N=%-2d", setTmp[tileN]);

        System.out.println("         H="+setTmp[tileH]);
        System.out.println("    S="+setTmp[tileS]+"     I="+setTmp[tileI]);
        System.out.println("R="+setTmp[tileR]+"     B="+setTmp[tileB]+"     J="+setTmp[tileJ]);
        System.out.println("    G="+setTmp[tileG]+"     C="+setTmp[tileC]);
        System.out.println("Q="+setTmp[tileQ]+"     A="+setTmp[tileA]+"     K="+setTmp[tileK]);
        System.out.println("    F="+setTmp[tileF]+"     D="+setTmp[tileD]);
        System.out.println("P="+setTmp[tileP]+"     E="+setTmp[tileE]+"     L="+setTmp[tileL]);
        System.out.println("    O="+setTmp[tileO]+"     M="+setTmp[tileM]);
        System.out.println("         N="+ setTmp[tileN]);

    }


    static boolean isValueInSet(int valueTmp, int[] setTmp) {
        for (int tileTmp = tileA; tileTmp <= tileS; tileTmp++) if (valueTmp == setTmp[tileTmp]) return true;
        return false;
    }

    static void zeroAllValues(int[] setTmp) {
        for (int tileTmp = tileA; tileTmp <= tileS; tileTmp++) setTmp[tileTmp] = 0;
    }

    static void zeroComputedValues(int [] setTmp) {
        setTmp[tileL] = setTmp[tileD] = setTmp[tileI] = setTmp[tileJ] = setTmp[tileC] = setTmp[tileF] = setTmp[tileP] = setTmp[tileH] = setTmp[tileB] = setTmp[tileE] = setTmp[tileN] = setTmp[tileA] = 0;
    }

    static int computeTileValue(int tileTmp, int[] setTmp) {
        int retVal = 0;
        switch (tileTmp) {
        case tileL:
            retVal = 38 - setTmp[tileM] + setTmp[tileO] - setTmp[tileQ] - setTmp[tileR];
            break;
        case tileD:
            retVal = 114 - setTmp[tileG] - setTmp[tileK] - 2*setTmp[tileL] - setTmp[tileM] - setTmp[tileQ] - 2*setTmp[tileR] - setTmp[tileS] ;
            break;
        case tileI:
            retVal = -38 + setTmp[tileK] + setTmp[tileL] + setTmp[tileR] + setTmp[tileS] ;
            break;
        case tileJ:
            retVal = 38 - setTmp[tileK] - setTmp[tileL];
            break;
        case tileC:
            retVal = 76  - setTmp[tileJ] - setTmp[tileI]  - setTmp[tileD] - setTmp[tileK] - setTmp[tileL] - setTmp[tileM];
            break;
        case tileF:
            retVal = 38  - setTmp[tileG] - setTmp[tileO] - setTmp[tileS];
            break;
        case tileP:
            retVal = 38  - setTmp[tileQ] - setTmp[tileR];
            break;
        case tileH:
            retVal = 38  - setTmp[tileJ] - setTmp[tileI];
            break;
        case tileB:
            retVal = 76  - setTmp[tileH] - setTmp[tileC]  - setTmp[tileJ] - setTmp[tileI] - setTmp[tileK] - setTmp[tileS];
            break;
        case tileE:
            retVal = 38  - setTmp[tileF] - setTmp[tileM] - setTmp[tileQ];
            break;
        case tileN:
            retVal = 38  - setTmp[tileP] - setTmp[tileO];
            break;
        case tileA:
            retVal = 190 - setTmp[tileN] - setTmp[tileE] - setTmp[tileB] - setTmp[tileH] - setTmp[tileP] - setTmp[tileF] - setTmp[tileC] - setTmp[tileJ] - setTmp[tileI] - setTmp[tileD] - setTmp[tileG] - setTmp[tileK] - setTmp[tileL] - setTmp[tileM] - setTmp[tileO] - setTmp[tileQ] - setTmp[tileR] - setTmp[tileS];
            break;
        default: retVal = 0; break;
        }
        return retVal;
    }



}
