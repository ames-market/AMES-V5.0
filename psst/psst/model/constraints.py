# This PSST file, originally due to Dheepak Krishnamurthy,
# has been modified by Swathi Battula to include
# Price Sensitive Load and Zonal data constraints and modified ramping constraints.

from functools import partial

import logging
import numpy as np

from pyomo.environ import Constraint, Objective, Piecewise, minimize, value, simple_constraint_rule

logger = logging.getLogger(__file__)
eps = 1e-3


def fix_first_angle_rule(m, t, slack_bus=1):
    return m.Angle[m.Buses.at(slack_bus), t] == 0.0


def lower_line_power_bounds_rule(m, l, t):
    if m.EnforceLine[l] and np.any(np.absolute(m.ThermalLimit[l]) > eps):
        return -m.ThermalLimit[l] <= m.LinePower[l, t]
    else:
        return Constraint.Skip


def upper_line_power_bounds_rule(m, l, t):
    if m.EnforceLine[l] and np.any(np.absolute(m.ThermalLimit[l]) > eps):
        return m.ThermalLimit[l] >= m.LinePower[l, t]
    else:
        return Constraint.Skip


def line_power_ptdf_rule(m, l, t):
    return m.LinePower[l, t] == sum(float(m.PTDF[l, i]) * m.NetPowerInjectionAtBus[b, t] for i, b in enumerate(m.Buses))


def line_power_rule(m, l, t):
    if m.B[l] == 99999999:
        logger.debug(
            " Line Power Angle constraint skipped for line between {} and {} ".format(m.BusFrom[l], m.BusTo[l]))
        return Constraint.Skip
    else:
        return m.LinePower[l, t] == m.B[l] * (m.Angle[m.BusFrom[l], t] - m.Angle[m.BusTo[l], t])


def calculate_total_demand(m, t, PriceSenLoadFlag=False):
    _constraint = sum(m.NetFixedLoad[b, t] for b in m.Buses)

    if PriceSenLoadFlag is True:
        _constraint = _constraint + \
                      sum(sum(m.PSLoadDemand[l, t] for l in m.PriceSensitiveLoadsAtBus[b]) for b in m.Buses)

    _constraint = m.TotalNetDemand[t] == _constraint

    return _constraint


def neg_load_generate_mismatch_tolerance_rule(m, b):
    return sum((m.negLoadGenerateMismatch[b, t] for t in m.TimePeriods)) >= 0.0


def pos_load_generate_mismatch_tolerance_rule(m, b):
    return sum((m.posLoadGenerateMismatch[b, t] for t in m.TimePeriods)) >= 0.0


def neg_global_reserve_mismatch_tolerance_rule(m):
    return sum((m.negGlobalReserveMismatch[t] for t in m.TimePeriods)) >= 0.0


def pos_global_reserve_mismatch_tolerance_rule(m):
    return sum((m.posGlobalReserveMismatch[t] for t in m.TimePeriods)) >= 0.0


def power_balance(m, b, t):
    # Power balance at each node (S)
    # bus b, time t (S)

    _constraint = m.NetPowerInjectionAtBus[b, t] + sum(m.LinePower[l, t] for l in m.LinesTo[b]) \
                 - sum(m.LinePower[l, t] for l in m.LinesFrom[b]) \
                 + m.LoadGenerateMismatch[b, t] == 0

    return _constraint


# This function defines m.NetPowerInjectionAtBus[b, t] constraint
def net_power_at_bus_rule(m, b, t, PriceSenLoadFlag=False):
    try:
        _constraint = sum(
            (1 - m.GeneratorForcedOutage[g, t]) * m.GeneratorBusContributionFactor[g, b] * m.PowerGenerated[g, t] for g in
            m.GeneratorsAtBus[b])
    except:
        _constraint = 0

    _constraint = _constraint - m.NetFixedLoad[b, t]

    if PriceSenLoadFlag is True:
        _constraint = _constraint - sum(m.PSLoadDemand[l, t] for l in m.PriceSensitiveLoadsAtBus[b])

    _constraint = m.NetPowerInjectionAtBus[b, t] == _constraint

    return _constraint


def pos_rule(m, b, t):
    return m.posLoadGenerateMismatch[b, t] >= m.LoadGenerateMismatch[b, t]


def neg_rule(m, b, t):
    return m.negLoadGenerateMismatch[b, t] >= - m.LoadGenerateMismatch[b, t]


def pos_global_reserve_rule(m, t):
    return m.posGlobalReserveMismatch[t] >= m.GlobalReserveMismatch[t]


def neg_global_reserve_rule(m, t):
    return m.negGlobalReserveMismatch[t] >= - m.GlobalReserveMismatch[t]


def enforce_reserve_down_requirements_rule(m, t):
    _constraint = sum(m.MinimumPowerAvailable[g, t] for g in m.Generators)

    _constraint = _constraint + m.ReserveDownRequirement[t]

    _constraint = _constraint <= m.TotalNetDemand[t]

    return _constraint


def enforce_reserve_up_requirements_rule(m, t):
    _constraint = sum(m.MaximumPowerAvailable[g, t] for g in m.Generators)

    _constraint = _constraint - m.ReserveUpRequirement[t]

    _constraint = _constraint >= m.TotalNetDemand[t]

    return _constraint


def enforce_zonal_reserve_down_requirement_rule(m, rz, t, PriceSenLoadFlag=False):
    _constraint = sum(m.MinimumPowerAvailable[g, t] for g in m.GeneratorsInReserveZone[rz])

    if PriceSenLoadFlag is False:
        _constraint = _constraint <= (1 - m.ZonalDownReservePercent[rz]) * sum(
            m.NetFixedLoad[d, t] for d in m.DemandInReserveZone[rz])
    else:
        _constraint = _constraint <= (1 - m.ZonalDownReservePercent[rz]) * \
                      (sum(m.NetFixedLoad[d, t] for d in m.DemandInReserveZone[rz]) +
                       sum(m.PSLoadDemand[l, t] for l in m.PriceSenLoadInReserveZone[rz]))

    return _constraint


def enforce_zonal_reserve_up_requirement_rule(m, rz, t, PriceSenLoadFlag=False):
    _constraint = sum(m.MaximumPowerAvailable[g, t] for g in m.GeneratorsInReserveZone[rz])

    if PriceSenLoadFlag is False:
        _constraint = _constraint <= (1 - m.ZonalUpReservePercent[rz]) * sum(
            m.NetFixedLoad[d, t] for d in m.DemandInReserveZone[rz])
    else:
        _constraint = _constraint <= (1 - m.ZonalUpReservePercent[rz]) * \
                      (sum(m.NetFixedLoad[d, t] for d in m.DemandInReserveZone[rz]) +
                       sum(m.PSLoadDemand[l, t] for l in m.PriceSenLoadInReserveZone[rz]))

    return _constraint


def enforce_generator_output_limits_rule_part_a(m, g, t):
    return m.MinimumPowerAvailable[g, t] <= m.PowerGenerated[g, t]


def enforce_generator_output_limits_rule_part_b(m, g, t):
    return m.PowerGenerated[g, t] <= m.MaximumPowerAvailable[g, t]


def enforce_generator_output_limits_rule_part_c(m, g, t):
    return m.MaximumPowerAvailable[g, t] <= m.MaximumPowerOutput[g] * m.UnitOn[g, t]


def enforce_generator_output_limits_rule_part_d(m, g, t):
    return m.MinimumPowerAvailable[g, t] >= m.MinimumPowerOutput[g] * m.UnitOn[g, t]


def enforce_ramping_rule_1(m, g, t):
    if t == 1:
        return m.MaximumPowerAvailable[g, t] <= m.PowerGeneratedT0[g] + \
               m.ScaledRampUpLimit[g] * m.UnitOnT0[g] + \
               m.ScaledStartUpRampLimit[g] * (m.UnitOn[g, t] - m.UnitOnT0[g]) + \
               m.MaximumPowerOutput[g] * (1 - m.UnitOn[g, t])
    else:
        return m.MaximumPowerAvailable[g, t] <= m.PowerGenerated[g, t - 1] + \
               m.ScaledRampUpLimit[g] * m.UnitOn[g, t - 1] + \
               m.ScaledStartUpRampLimit[g] * (m.UnitOn[g, t] - m.UnitOn[g, t - 1]) + \
               m.MaximumPowerOutput[g] * (1 - m.UnitOn[g, t])


def enforce_ramping_rule_2(m, g, t):
    if t >= value(m.NumTimePeriods):
        return Constraint.Skip
    else:
        return m.MaximumPowerAvailable[g, t] <= \
               m.MaximumPowerOutput[g] * m.UnitOn[g, t + 1] + \
               m.ScaledShutDownRampLimit[g] * (m.UnitOn[g, t] - m.UnitOn[g, t + 1])


def enforce_ramping_rule_3(m, g, t):
    if t == 1:
        return m.PowerGeneratedT0[g] - m.MinimumPowerAvailable[g, t] <= \
               m.ScaledRampDownLimit[g] * m.UnitOn[g, t] + \
               m.ScaledShutDownRampLimit[g] * (m.UnitOnT0[g] - m.UnitOn[g, t]) + \
               m.MaximumPowerOutput[g] * (1 - m.UnitOnT0[g])
    else:
        return m.PowerGenerated[g, t - 1] - m.MinimumPowerAvailable[g, t] <= \
               m.ScaledRampDownLimit[g] * m.UnitOn[g, t] + \
               m.ScaledShutDownRampLimit[g] * (m.UnitOn[g, t - 1] - m.UnitOn[g, t]) + \
               m.MaximumPowerOutput[g] * (1 - m.UnitOn[g, t - 1])


def compute_hot_start_rule(m, g, t):
    if (t + 1) <= value(m.ScaledColdStartTime[g]):
        if (t + 1) - value(m.ScaledColdStartTime[g]) <= value(m.UnitOnT0State[g]):
            m.HotStart[g, t] = 1
            m.HotStart[g, t].fixed = True
            return Constraint.Skip
        else:
            return m.HotStart[g, t] <= sum(m.UnitOn[g, i] for i in range(0, t))
    else:
        return m.HotStart[g, t] <= sum(m.UnitOn[g, i] for i in range(t - m.ScaledColdStartTime[g], t))


def compute_startup_costs_rule_minusM(m, g, t):
    if t == 1:
        return m.StartupCost[g, t] >= m.ColdStartCost[g] - (m.ColdStartCost[g] - m.HotStartCost[g]) * m.HotStart[g, t] \
               - m.ColdStartCost[g] * (1 - (m.UnitOn[g, t] - m.UnitOnT0[g]))
    else:
        return m.StartupCost[g, t] >= m.ColdStartCost[g] - (m.ColdStartCost[g] - m.HotStartCost[g]) * m.HotStart[g, t] \
               - m.ColdStartCost[g] * (1 - (m.UnitOn[g, t] - m.UnitOn[g, t - 1]))


def compute_shutdown_costs_rule(m, g, t):
    if t == 1:
        return m.ShutdownCost[g, t] >= m.ShutdownCostCoefficient[g] * (m.UnitOnT0[g] - m.UnitOn[g, t])
    else:
        return m.ShutdownCost[g, t] >= m.ShutdownCostCoefficient[g] * (m.UnitOn[g, t - 1] - m.UnitOn[g, t])


def enforce_up_time_constraints_initial(m, g):
    if value(m.InitialTimePeriodsOnLine[g]) == 0:
        return Constraint.Skip
    return sum((1 - m.UnitOn[g, t]) for t in m.TimePeriods if t <= value(m.InitialTimePeriodsOnLine[g])) == 0.0


@simple_constraint_rule
def enforce_up_time_constraints_subsequent(m, g, t):
    if t <= value(m.InitialTimePeriodsOnLine[g]):
        return Constraint.Skip
    elif t <= (value(m.NumTimePeriods - m.ScaledMinimumUpTime[g]) + 1):
        if t == 1:
            return sum(
                m.UnitOn[g, n] for n in m.TimePeriods if t <= n <= (t + value(m.ScaledMinimumUpTime[g]) - 1)) >= \
                   m.ScaledMinimumUpTime[g] * (m.UnitOn[g, t] - m.UnitOnT0[g])
        else:
            return sum(
                m.UnitOn[g, n] for n in m.TimePeriods if t <= n <= (t + value(m.ScaledMinimumUpTime[g]) - 1)) >= \
                   m.ScaledMinimumUpTime[g] * (m.UnitOn[g, t] - m.UnitOn[g, t - 1])
    else:
        if t == 1:
            return sum((m.UnitOn[g, n] - (m.UnitOn[g, t] - m.UnitOnT0[g])) for n in m.TimePeriods if n >= t) >= 0.0
        else:
            return sum((m.UnitOn[g, n] - (m.UnitOn[g, t] - m.UnitOn[g, t - 1])) for n in m.TimePeriods if n >= t) >= 0.0


def enforce_down_time_constraints_initial(m, g):
    if value(m.InitialTimePeriodsOffLine[g]) == 0:
        return Constraint.Skip
    return sum(m.UnitOn[g, t] for t in m.TimePeriods if (t + 1) <= value(m.InitialTimePeriodsOffLine[g])) == 0.0


@simple_constraint_rule
def enforce_down_time_constraints_subsequent(m, g, t):
    if t <= value(m.InitialTimePeriodsOffLine[g]):
        return Constraint.Skip
    elif t <= (value(m.NumTimePeriods - m.ScaledMinimumDownTime[g]) + 1):
        if t == 1:
            return sum((1 - m.UnitOn[g, n]) for n in m.TimePeriods if
                       t <= n <= (t + value(m.ScaledMinimumDownTime[g]) - 1)) >= \
                   m.ScaledMinimumDownTime[g] * (m.UnitOnT0[g] - m.UnitOn[g, t])
        else:
            return sum((1 - m.UnitOn[g, n]) for n in m.TimePeriods if
                       t <= n <= (t + value(m.ScaledMinimumDownTime[g]) - 1)) >= \
                   m.ScaledMinimumDownTime[g] * (m.UnitOn[g, t - 1] - m.UnitOn[g, t])
    else:
        if t == 1:
            return sum(
                ((1 - m.UnitOn[g, n]) - (m.UnitOnT0[g] - m.UnitOn[g, t])) for n in m.TimePeriods if n >= t) >= 0.0
        else:
            return sum(
                ((1 - m.UnitOn[g, n]) - (m.UnitOn[g, t - 1] - m.UnitOn[g, t])) for n in m.TimePeriods if n >= t) >= 0.0


def commitment_in_stage_st_cost_rule(m, st):
    return m.CommitmentStageCost[st] == (sum(
        m.StartupCost[g, t] + m.ShutdownCost[g, t] for g in m.Generators for t in m.CommitmentTimeInStage[st]) + sum(
        sum(m.UnitOn[g, t] for t in m.CommitmentTimeInStage[st]) * m.MinimumProductionCost[g] * m.TimePeriodLength for g
        in m.Generators))


def generation_in_stage_st_cost_rule(m, st):
    return m.GenerationStageCost[st] == sum(m.ProductionCost[g, t] for g in m.Generators for t in
                                            m.GenerationTimeInStage[
                                                st]) + m.LoadPositiveMismatchPenalty * m.TimePeriodLength * \
           (sum(m.posLoadGenerateMismatch[b, t] for b in m.Buses for t in
                m.GenerationTimeInStage[st])) + m.LoadNegativeMismatchPenalty * m.TimePeriodLength * \
           (sum(m.negLoadGenerateMismatch[b, t] for b in m.Buses for t in m.GenerationTimeInStage[st]))


def StageCost_rule(m, st):
    return m.StageCost[st] == m.GenerationStageCost[st] + m.CommitmentStageCost[st]


def total_cost_objective_rule(m, PriceSenLoadFlag=False):
    if PriceSenLoadFlag:
        return (- sum(m.LoadBenefit[l, t] for l in m.PriceSensitiveLoads for t in m.TimePeriods) + sum(
            m.StageCost[st] for st in m.StageSet))
    return sum(m.StageCost[st] for st in m.StageSet)


def constraint_net_power(model, PriceSenLoadFlag=False):
    partial_net_power_at_bus_rule = partial(net_power_at_bus_rule, PriceSenLoadFlag=PriceSenLoadFlag)
    model.CalculateNetPowerAtBus = Constraint(model.Buses, model.TimePeriods, rule=partial_net_power_at_bus_rule)


################################################

def constraint_line(model, ptdf=None, slack_bus=1):
    model.LinePowerConstraintLower = Constraint(model.TransmissionLines, model.TimePeriods,
                                                rule=lower_line_power_bounds_rule)
    model.LinePowerConstraintHigher = Constraint(model.TransmissionLines, model.TimePeriods,
                                                 rule=upper_line_power_bounds_rule)

    if ptdf is not None:
        model.PTDF = ptdf
        model.CalculateLinePower = Constraint(model.TransmissionLines, model.TimePeriods, rule=line_power_ptdf_rule)
    else:
        partial_fix_first_angle_rule = partial(fix_first_angle_rule, slack_bus=slack_bus)
        model.FixFirstAngle = Constraint(model.TimePeriods, rule=partial_fix_first_angle_rule)
        model.CalculateLinePower = Constraint(model.TransmissionLines, model.TimePeriods, rule=line_power_rule)


def constraint_total_demand(model, PriceSenLoadFlag=False):
    partial_calculate_total_demand = partial(calculate_total_demand, PriceSenLoadFlag=PriceSenLoadFlag)
    model.CalculateTotalDemand = Constraint(model.TimePeriods, rule=partial_calculate_total_demand)


def constraint_load_generation_mismatch(model):
    model.PosLoadGenerateMismatchTolerance = Constraint(model.Buses, rule=pos_load_generate_mismatch_tolerance_rule)
    model.NegLoadGenerateMismatchTolerance = Constraint(model.Buses, rule=neg_load_generate_mismatch_tolerance_rule)
    model.DefinePosMismatch = Constraint(model.Buses, model.TimePeriods, rule=pos_rule)
    model.DefineNegMismatch = Constraint(model.Buses, model.TimePeriods, rule=neg_rule)


def constraint_power_balance(model):
    fn_power_balance = partial(power_balance)
    model.PowerBalance = Constraint(model.Buses, model.TimePeriods, rule=fn_power_balance)


def constraint_reserves(model, PriceSenLoadFlag=False, has_global_reserves=True, has_zonal_reserves=False):
    if has_global_reserves is True:
        fn_enforce_reserve_up_requirements = partial(enforce_reserve_up_requirements_rule)
        fn_enforce_reserve_down_requirements = partial(enforce_reserve_down_requirements_rule)
        model.EnforceReserveUpRequirements = Constraint(model.TimePeriods, rule=fn_enforce_reserve_up_requirements)
        model.EnforceReserveDownRequirements = Constraint(model.TimePeriods, rule=fn_enforce_reserve_down_requirements)

    if has_zonal_reserves is True:
        fn_enforce_zonal_reserve_down_requirement_rule = partial(enforce_zonal_reserve_down_requirement_rule,
                                                                 PriceSenLoadFlag=PriceSenLoadFlag)
        fn_enforce_zonal_reserve_up_requirement_rule = partial(enforce_zonal_reserve_up_requirement_rule,
                                                               PriceSenLoadFlag=PriceSenLoadFlag)
        model.EnforceZonalReserveDownRequirements = Constraint(model.ReserveZones, model.TimePeriods,
                                                               rule=fn_enforce_zonal_reserve_down_requirement_rule)
        model.EnforceZonalReserveUpRequirements = Constraint(model.ReserveZones, model.TimePeriods,
                                                             rule=fn_enforce_zonal_reserve_up_requirement_rule)


def constraint_generator_power(model):
    model.EnforceGeneratorOutputLimitsPartA = Constraint(model.Generators, model.TimePeriods,
                                                         rule=enforce_generator_output_limits_rule_part_a)
    model.EnforceGeneratorOutputLimitsPartB = Constraint(model.Generators, model.TimePeriods,
                                                         rule=enforce_generator_output_limits_rule_part_b)
    model.EnforceGeneratorOutputLimitsPartC = Constraint(model.Generators, model.TimePeriods,
                                                         rule=enforce_generator_output_limits_rule_part_c)
    model.EnforceGeneratorOutputLimitsPartD = Constraint(model.Generators, model.TimePeriods,
                                                         rule=enforce_generator_output_limits_rule_part_d)

    model.EnforceRampingRule1 = Constraint(model.Generators, model.TimePeriods, rule=enforce_ramping_rule_1)
    model.EnforceRampingRule2 = Constraint(model.Generators, model.TimePeriods, rule=enforce_ramping_rule_2)
    model.EnforceRampingRule3 = Constraint(model.Generators, model.TimePeriods, rule=enforce_ramping_rule_3)


def constraint_up_down_time(model):
    model.EnforceUpTimeConstraintsInitial = Constraint(model.Generators, rule=enforce_up_time_constraints_initial)
    model.EnforceUpTimeConstraintsSubsequent = Constraint(model.Generators, model.TimePeriods,
                                                          rule=enforce_up_time_constraints_subsequent)

    model.EnforceDownTimeConstraintsInitial = Constraint(model.Generators, rule=enforce_down_time_constraints_initial)
    model.EnforceDownTimeConstraintsSubsequent = Constraint(model.Generators, model.TimePeriods,
                                                            rule=enforce_down_time_constraints_subsequent)


def production_cost_function(m, g, t, x):
    # a function for use in piecewise linearization of the cost function.
    return m.TimePeriodLength * m.PowerGenerationPiecewiseValues[g, t][x]


def load_benefit_function(m, g, t, x):
    # a function for use in piecewise linearization of the price sensitive load benefit function.
    return m.TimePeriodLength * m.LoadDemandPiecewiseValues[g, t][x]


def constraint_for_cost(model):
    model.ComputeProductionCosts = Piecewise(model.Generators * model.TimePeriods, model.ProductionCost,
                                             model.PowerGenerated, pw_pts=model.PowerGenerationPiecewisePoints,
                                             f_rule=production_cost_function, pw_constr_type='LB', warning_tol=1e-20)

    model.ComputeHotStart = Constraint(model.Generators, model.TimePeriods, rule=compute_hot_start_rule)
    model.ComputeStartupCostsMinusM = Constraint(model.Generators, model.TimePeriods,
                                                 rule=compute_startup_costs_rule_minusM)
    model.ComputeShutdownCosts = Constraint(model.Generators, model.TimePeriods, rule=compute_shutdown_costs_rule)

    model.Compute_commitment_in_stage_st_cost = Constraint(model.StageSet, rule=commitment_in_stage_st_cost_rule)

    model.Compute_generation_in_stage_st_cost = Constraint(model.StageSet, rule=generation_in_stage_st_cost_rule)

    model.Compute_Stage_Cost = Constraint(model.StageSet, rule=StageCost_rule)


def constraint_for_benefit(model):
    model.ComputePSLoadBenefits = Piecewise(model.PriceSensitiveLoads * model.TimePeriods, model.LoadBenefit,
                                            model.PSLoadDemand, pw_pts=model.LoadDemandPiecewisePoints,
                                            f_rule=load_benefit_function, pw_constr_type='UB', warning_tol=1e-20)


def objective_function(model, PriceSenLoadFlag=False):
    if PriceSenLoadFlag is False:
        model.TotalCostObjective = Objective(rule=total_cost_objective_rule, sense=minimize)
    else:
        partial_total_cost_objective_rule = partial(total_cost_objective_rule, PriceSenLoadFlag=PriceSenLoadFlag)
        model.TotalCostObjective = Objective(rule=partial_total_cost_objective_rule, sense=minimize)
