import logging
import os

import numpy as np

from pypower.makePTDF import makePTDF

logger = logging.getLogger(__name__)

def calculate_PTDF(case, precision=None, tolerance=None):
    bus = case.bus.copy(deep=True)
    branch = case.branch.copy(deep=True)
    value = [i + 1 for i in range(0, len(bus.index))]
    bus_name = bus.index
    bus.index = value
    bus.index = bus.index.astype(int)
    branch['F_BUS'] = branch['F_BUS'].apply(lambda x: value[bus_name.get_loc(x)]).astype(int)
    branch['T_BUS'] = branch['T_BUS'].apply(lambda x: value[bus_name.get_loc(x)]).astype(int)
    bus = np.array(bus.reset_index())
    branch = np.array(branch)
    ptdf = makePTDF(case.baseMVA, bus, branch, )
    if precision is not None:
        ptdf = ptdf.round(precision)
    if tolerance is not None:
        ptdf[abs(ptdf) < tolerance] = 0
    return ptdf

