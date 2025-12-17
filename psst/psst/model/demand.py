from pyomo.environ import Param


def initialize_demand(model, net_fixed_load=None):
    model.NetFixedLoad = Param(model.Buses, model.TimePeriods, initialize=net_fixed_load, default=0.0, mutable=True)
