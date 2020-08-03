
from pyomo.environ import *


def initialize_demand(model, NetFixedLoad=None):

    model.NetFixedLoad = Param(model.Buses, model.TimePeriods, initialize=NetFixedLoad, default=0.0, mutable=True)

