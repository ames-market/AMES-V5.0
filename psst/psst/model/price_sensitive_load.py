# This PSST file is developed by Swathi Battula

from pyomo.environ import *


def initialize_price_senstive_load(model,
                                   price_sensitive_load_names=None,
                                   price_sensitive_load_at_bus=None):
    model.PriceSensitiveLoads = Set(initialize=price_sensitive_load_names)
    model.PriceSensitiveLoadsAtBus = Set(model.Buses, initialize=price_sensitive_load_at_bus)


def maximum_power_demand_loads(model, maximum_power_demand=None):
    model.MaximumPowerDemand = Param(model.PriceSensitiveLoads, model.TimePeriods, initialize=maximum_power_demand,
                                     within=NonNegativeReals, default=0.0)


def piece_wise_linear_benefit(model, points=None, values=None):
    # benefits associated with each price-sensitive load, for each time period.
    model.BenefitPiecewisePoints = Param(model.PriceSensitiveLoads, model.TimePeriods, initialize=points)
    model.BenefitPiecewiseValues = Param(model.PriceSensitiveLoads, model.TimePeriods, initialize=values)


def initialize_load_demand(model):
    # amount of power consumed by load, at each time period.
    def demand_bounds_rule(m, ld, t):
        return 0, m.MaximumPowerDemand[ld, t]

    model.PSLoadDemand = Var(model.PriceSensitiveLoads, model.TimePeriods, within=NonNegativeReals,
                             bounds=demand_bounds_rule)
    model.LoadBenefit = Var(model.PriceSensitiveLoads, model.TimePeriods, within=NonNegativeReals)


def load_benefit(model):
    model.LoadDemandPiecewisePoints = {}
    model.LoadDemandPiecewiseValues = {}
    for ld in model.PriceSensitiveLoads:
        for t in model.TimePeriods:
            load_demand_piecewise_points_rule(model, ld, t)


def load_demand_piecewise_points_rule(m, ld, t):
    if len(m.BenefitPiecewisePoints[ld, t]) > 0:
        m.LoadDemandPiecewisePoints[ld, t] = list(m.BenefitPiecewisePoints[ld, t])
        temp = list(m.BenefitPiecewiseValues[ld, t])
        m.LoadDemandPiecewiseValues[ld, t] = {}
        for i in range(len(m.BenefitPiecewisePoints[ld, t])):
            m.LoadDemandPiecewiseValues[ld, t][m.LoadDemandPiecewisePoints[ld, t][i]] = temp[i]
