#This PSST file, originally due to Dheepak Krishnamurthy, has been modified by Swathi Battula to make the code consistent with AMES V5.0 documentation dated 7-2-2020. 

from pyomo.environ import *
import click

def initialize_generators(model,
                        generator_names=None,
                        generator_at_bus=None):

    model.Generators = Set(initialize=generator_names)
    model.GeneratorsAtBus = Set(model.Buses, initialize=generator_at_bus)

def forced_outage(model):

    model.GeneratorForcedOutage = Param(model.Generators * model.TimePeriods, within=Binary, default=False)

def generator_bus_contribution_factor(model):
    model.GeneratorBusContributionFactor = Param(model.Generators, model.Buses, within=NonNegativeReals, default=1.0)

def maximum_minimum_power_output_generators(model, minimum_power_output=None, maximum_power_output=None):

    model.MinimumPowerOutput = Param(model.Generators, initialize=minimum_power_output, within=NonNegativeReals, default=0.0)
    model.MaximumPowerOutput = Param(model.Generators, initialize=maximum_power_output, within=NonNegativeReals, default=0.0)


def ramp_up_ramp_down_limits(model, scaled_ramp_up_limits=None, scaled_ramp_down_limits=None):

    #SRU_j
    model.ScaledRampUpLimit = Param(model.Generators, within=NonNegativeReals, initialize=scaled_ramp_up_limits)
    #SRD_j
    model.ScaledRampDownLimit = Param(model.Generators, within=NonNegativeReals, initialize=scaled_ramp_down_limits)


def start_up_shut_down_ramp_limits(model, scaled_start_up_ramp_limits=None, scaled_shut_down_ramp_limits=None):


    #SSU_j
    model.ScaledStartUpRampLimit = Param(model.Generators, within=NonNegativeReals, initialize=scaled_start_up_ramp_limits)
    #SSD_j
    model.ScaledShutDownRampLimit = Param(model.Generators, within=NonNegativeReals, initialize=scaled_shut_down_ramp_limits)


def minimum_up_minimum_down_time(model, scaled_minimum_up_time=None, scaled_minimum_down_time=None):
    #SUT_j
    model.ScaledMinimumUpTime = Param(model.Generators, within=NonNegativeIntegers, default=0, initialize=scaled_minimum_up_time)
    #SDT_j
    model.ScaledMinimumDownTime = Param(model.Generators, within=NonNegativeIntegers, default=0, initialize=scaled_minimum_down_time)

def initial_state(model, initial_state=None,
                initial_power_generated=None,
                initial_time_periods_online=None,
                initial_time_periods_offline=None
                ):

    model.UnitOnT0State = Param(model.Generators, within=Reals, initialize=initial_state, mutable=True)

    def t0_unit_on_rule(m, g):
        return int(value(m.UnitOnT0State[g]) >= 1)

    #v_j(0) --> Value follows immediated from \hat{v}_j value. DON'T SET
    model.UnitOnT0 = Param(model.Generators, within=Binary, initialize=t0_unit_on_rule, mutable=True)

    model.PowerGeneratedT0 = Param(model.Generators, within=NonNegativeReals, default=0, initialize=initial_power_generated)

    
    model.InitialTimePeriodsOnLine = Param(model.Generators, within=NonNegativeIntegers, default=0, initialize=initial_time_periods_online)

    
    model.InitialTimePeriodsOffLine = Param(model.Generators, within=NonNegativeIntegers, default=0, initialize=initial_time_periods_offline)


def hot_start_cold_start_costs(model,
                            hot_start_costs=None,
                            cold_start_costs=None,
                            scaled_cold_start_time=None,
                            shutdown_cost=None):

    ###############################################
    # startup cost parameters for each generator. #
    ###############################################

    #SCST_j
    model.ScaledColdStartTime = Param(model.Generators, within=NonNegativeIntegers, default=0, initialize=scaled_cold_start_time)
    #HSC_j
    model.HotStartCost = Param(model.Generators, within=NonNegativeReals, default=0.0, initialize=hot_start_costs)
    #CSC_j
    model.ColdStartCost = Param(model.Generators, within=NonNegativeReals, default=0.0, initialize=cold_start_costs)

    ##################################################################################
    # shutdown cost for each generator. in the literature, these are often set to 0. #
    ##################################################################################

    model.ShutdownCostCoefficient = Param(model.Generators, within=NonNegativeReals, default=0.0, initialize=shutdown_cost)


def _minimum_production_cost_fn(m, g):
    # Minimum production cost (needed because Piecewise constraint on ProductionCost
    # has to have lower bound of 0, so the unit can cost 0 when off -- this is added
    # back in to the objective if a unit is on
    if len(m.CostPiecewisePoints[g]) > 1:
        return m.CostPiecewiseValues[g].first()
    elif len(m.CostPiecewisePoints[g]) == 1:
        # If there's only one piecewise point given, that point should be (MaxPower, MaxCost) -- i.e. the cost function is linear through (0,0),
        # so we can find the slope of the line and use that to compute the cost of running at minimum generation
        return m.MinimumPowerOutput[g] * (m.CostPiecewiseValues[g].first() / m.MaximumPowerOutput[g])
    else:
        return  m.ProductionCostA0[g] + \
                m.ProductionCostA1[g] * m.MinimumPowerOutput[g] + \
                m.ProductionCostA2[g] * m.MinimumPowerOutput[g]**2


def minimum_production_cost(model, minimum_production_cost=_minimum_production_cost_fn):
    model.MinimumProductionCost = Param(model.Generators, within=NonNegativeReals, initialize=_minimum_production_cost_fn, mutable=True)


def piece_wise_linear_cost(model, points=None, values=None):
    # production cost associated with each generator, for each time period.
    model.CostPiecewisePoints = Set(model.Generators, initialize=points, ordered=True)
    model.CostPiecewiseValues = Set(model.Generators, initialize=values, ordered=True)


def production_cost(model):

    model.PowerGenerationPiecewisePoints = {}
    model.PowerGenerationPiecewiseValues = {}
    for g in model.Generators:
        for t in model.TimePeriods:
            power_generation_piecewise_points_rule(model, g, t)


def power_generation_piecewise_points_rule(m, g, t):
    minimum_production_cost = value(m.MinimumProductionCost[g])
    if len(m.CostPiecewisePoints[g]) > 0:
        m.PowerGenerationPiecewisePoints[g,t] = list(m.CostPiecewisePoints[g])
        temp = list(m.CostPiecewiseValues[g])
        m.PowerGenerationPiecewiseValues[g,t] = {}
        for i in range(len(m.CostPiecewisePoints[g])):
            m.PowerGenerationPiecewiseValues[g,t][m.PowerGenerationPiecewisePoints[g,t][i]] = temp[i] - minimum_production_cost
        # MinimumPowerOutput will be one of our piecewise points, so it is safe to add (0,0)
        if m.PowerGenerationPiecewisePoints[g,t][0] != 0:
            m.PowerGenerationPiecewisePoints[g,t].insert(0,0)
        m.PowerGenerationPiecewiseValues[g,t][0] = 0

