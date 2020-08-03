/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amesmarket;

/**
 *
 * Default simulation parameters.
 *
 * A struct-type class to group the default parameters together to make it easy
 * to pass them around. Public visibility to make is simpler to hook into
 * existing code.
 *
 *
 * Adapted from the {@link  AMESGUIFrame.AMESFrame}
 *
 */
public class DefaultSimulationParameters {

    public final long Default_RandomSeed;
    public final int Default_iMaxDay;
    public final double Default_dThresholdProbability;
    public final double Default_dDailyNetEarningThreshold;
    public final double Default_dGenPriceCap;
    public final double Default_dLSEPriceCap;
    public final int Default_iStartDay;
    public final int Default_iCheckDayLength;
    public final double Default_dActionProbability;
    public final int Default_iLearningCheckStartDay;
    public final int Default_iLearningCheckDayLength;
    public final double Default_dLearningCheckDifference;
    public final int Default_iDailyNetEarningStartDay;
    public final int Default_iDailyNetEarningDayLength;
    public final boolean Default_bMaximumDay = true;
    public final boolean Default_bThreshold = true;
    public final boolean Default_bDailyNetEarningThreshold = false;
    public final boolean Default_bActionProbabilityCheck = false;
    public final boolean Default_bLearningCheck = false;

    // Learning and action domain parameters
    public final double Default_Cooling;
    public final double Default_Experimentation;
    public final double Default_InitPropensity;
    public final int Default_M1;
    public final int Default_M2;
    public final int Default_M3;
    public final double Default_RI_MAX_Lower;
    public final double Default_RI_MAX_Upper;
    public final double Default_RI_MIN_C;
    public final double Default_Recency;
    public final double Default_SlopeStart;
    public final int Default_iSCostExcludedFlag;

    public DefaultSimulationParameters() {

        Default_Cooling = 1000.0;
        Default_Experimentation = 0.96;
        Default_InitPropensity = 6000.0;
        Default_Recency = 0.04;

        Default_M1 = 1;
        Default_M2 = 1;
        Default_M3 = 1;
        Default_RI_MAX_Lower = 0.75;
        Default_RI_MAX_Upper = 0.75;
        Default_RI_MIN_C = 1.0;
        Default_SlopeStart = 0.001;
        Default_iSCostExcludedFlag = 1;


        Default_RandomSeed = 695672061;
        Default_iMaxDay = 5;
        Default_dThresholdProbability = 0.999;
        Default_dDailyNetEarningThreshold = 10.0;
        Default_dGenPriceCap = 1000.0;
        Default_dLSEPriceCap = 0.0;
        Default_iStartDay = 1;
        Default_iCheckDayLength = 5;
        Default_dActionProbability = 0.001;
        Default_iLearningCheckStartDay = 1;
        Default_iLearningCheckDayLength = 5;
        Default_dLearningCheckDifference = 0.001;
        Default_iDailyNetEarningStartDay = 1;
        Default_iDailyNetEarningDayLength = 5;
    }
}
