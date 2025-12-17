# This PSST file, originally due to Dheepak Krishnamurthy,
# has been modified by Swathi Battula to include Price Sensitive Load and Zonal data constraints.

import logging

import numpy as np
import pandas as pd
from matplotlib.sphinxext.plot_directive import exception_template

from .constraints import (constraint_line, constraint_total_demand, constraint_net_power,
                          constraint_load_generation_mismatch,
                          constraint_power_balance,
                          constraint_reserves,
                          constraint_generator_power,
                          constraint_up_down_time,
                          constraint_for_cost,
                          constraint_for_benefit,
                          objective_function)
from .demand import (initialize_demand)
from .generators import (initialize_generators, initial_state, maximum_minimum_power_output_generators,
                         ramp_up_ramp_down_limits, start_up_shut_down_ramp_limits, minimum_up_minimum_down_time,
                         piece_wise_linear_cost,
                         production_cost, minimum_production_cost,
                         hot_start_cold_start_costs,
                         forced_outage,
                         generator_bus_contribution_factor)
from .model import (create_model, initialize_buses,
                    initialize_time_periods, initialize_model, Suffix
                    )
from .network import (initialize_network, derive_network, calculate_network_parameters, enforce_thermal_limits)
from .price_sensitive_load import (initialize_price_sensitive_load, maximum_power_demand_loads,
                                   piece_wise_linear_benefit, initialize_load_demand,
                                   load_benefit)
from .reserves import initialize_global_reserves, initialize_zonal_reserves
from ..case.utils import calculate_PTDF
from ..solver import solve_model, PSSTResults

logger = logging.getLogger(__file__)


def build_model(case,
                generator_df=None,
                load_df=None,
                branch_df=None,
                bus_df=None,
                ZonalDataComplete=None,
                PriceSenLoadData=None,
                Op=None,
                previous_unit_commitment_df=None,
                config=None):

    # Configuration
    if config is None:
        config = dict()

    zonal_data = ZonalDataComplete['zonal_data']
    zonal_bus_data = ZonalDataComplete['zonal_bus_data']
    zonal_up_reserve_percent = ZonalDataComplete['zonal_up_reserve_percent']
    zonal_down_reserve_percent = ZonalDataComplete['zonal_down_reserve_percent']

    # Get configuration parameters from dictionary
    use_ptdf = config.pop('use_ptdf', False)

    generator_df = generator_df or pd.merge(case.gen, case.gencost, left_index=True, right_index=True)
    load_df = load_df or case.load
    branch_df = branch_df or case.branch
    bus_df = bus_df or case.bus
    down_reserve_percent = case.DownReservePercent
    up_reserve_percent = case.UpReservePercent

    branch_df.index = branch_df.index.astype(object)
    generator_df.index = generator_df.index.astype(object)
    bus_df.index = bus_df.index.astype(object)
    load_df.index = load_df.index.astype(object)

    branch_df = branch_df.astype(object)
    generator_df = generator_df.astype(object)
    bus_df = bus_df.astype(object)
    load_df = load_df.astype(object)

    # Build model information

    mdl = create_model()

    initialize_buses(mdl, bus_names=bus_df.index)
    initialize_time_periods(mdl, time_periods=list(load_df.index), time_period_length=case.TimePeriodLength)

    # Build network data
    initialize_network(mdl, transmission_lines=list(branch_df.index), bus_from=branch_df['F_BUS'].to_dict(),
                       bus_to=branch_df['T_BUS'].to_dict())

    lines_to = {b: list() for b in bus_df.index.unique()}
    lines_from = {b: list() for b in bus_df.index.unique()}

    for i, l in branch_df.iterrows():
        lines_from[l['F_BUS']].append(i)
        lines_to[l['T_BUS']].append(i)

    derive_network(mdl, lines_from=lines_from, lines_to=lines_to)
    calculate_network_parameters(mdl, reactance=(branch_df['BR_X']).to_dict())
    enforce_thermal_limits(mdl, thermal_limit=branch_df['RATE_A'].to_dict())

    # Build generator data
    generator_at_bus = {b: list() for b in generator_df['GEN_BUS'].unique()}

    for i, g in generator_df.iterrows():
        generator_at_bus[g['GEN_BUS']].append(i)

    initialize_generators(mdl,
                          generator_names=generator_df.index,
                          generator_at_bus=generator_at_bus)

    maximum_minimum_power_output_generators(mdl,
                                            minimum_power_output=generator_df['PMIN'].to_dict(),
                                            maximum_power_output=generator_df['PMAX'].to_dict())

    # print('SCALED_RAMP_UP: ', str(generator_df['SCALED_RAMP_UP'].to_dict()))
    # print('SCALED_RAMP_DOWN: ', str(generator_df['SCALED_RAMP_DOWN'].to_dict()))
    ramp_up_ramp_down_limits(mdl, scaled_ramp_up_limits=generator_df['SCALED_RAMP_UP'].to_dict(),
                             scaled_ramp_down_limits=generator_df['SCALED_RAMP_DOWN'].to_dict())

    start_up_shut_down_ramp_limits(mdl, scaled_start_up_ramp_limits=generator_df['SCALED_STARTUP_RAMP'].to_dict(),
                                   scaled_shut_down_ramp_limits=generator_df['SCALED_SHUTDOWN_RAMP'].to_dict())

    minimum_up_minimum_down_time(mdl, scaled_minimum_up_time=generator_df['SCALED_MINIMUM_UP_TIME'].to_dict(),
                                 scaled_minimum_down_time=generator_df['SCALED_MINIMUM_DOWN_TIME'].to_dict())

    forced_outage(mdl)

    generator_bus_contribution_factor(mdl)

    if previous_unit_commitment_df is None:
        previous_unit_commitment = dict()
        for g in generator_df.index:
            previous_unit_commitment[g] = [0] * len(load_df)
        previous_unit_commitment_df = pd.DataFrame(previous_unit_commitment)
        previous_unit_commitment_df.index = load_df.index

    diff = previous_unit_commitment_df.diff()

    initial_state_dict = dict()
    for col in diff.columns:
        s = diff[col].dropna()
        diff_s = s[s != 0]
        if diff_s.empty:
            check_row = previous_unit_commitment_df[col].head(1)
        else:
            check_row = diff_s.tail(1)

        if check_row.values == -1 or check_row.values == 0:
            initial_state_dict[col] = -1 * (len(load_df) - int(check_row.index.values))
        else:
            initial_state_dict[col] = len(load_df) - int(check_row.index.values)

    logger.debug("Initial State of generators is {}".format(initial_state_dict))
    initial_state_dict = generator_df['UnitOnT0State'].to_dict()
    initial_power_generated_dict = generator_df['PG'].to_dict()
    initial_time_periods_online_dict = generator_df['InitialTimeON'].to_dict()
    initial_time_periods_offline_dict = generator_df['InitialTimeOFF'].to_dict()

    initial_state(mdl, init_state=initial_state_dict, initial_power_generated=initial_power_generated_dict,
                  initial_time_periods_online=initial_time_periods_online_dict,
                  initial_time_periods_offline=initial_time_periods_offline_dict)

    # setup production cost for generators

    points = dict()
    values = dict()

    for i, g in generator_df.iterrows():
        points[i] = np.linspace(g['PMIN'], g['PMAX'], num=int(g['NS'] + 1))
        values[i] = g['a'] + g['b'] * points[i] + g['c'] * points[i] ** 2

    for k, v in points.items():
        points[k] = [float(i) for i in v]
    for k, v in values.items():
        values[k] = [float(i) for i in v]

    piece_wise_linear_cost(mdl, points, values)

    minimum_production_cost(mdl)
    production_cost(mdl)

    # setup start up and shut down costs for generators
    scaled_cold_start_time = case.gencost['SCALED_COLD_START_TIME'].to_dict()
    hot_start_costs = case.gencost['STARTUP_HOT'].to_dict()
    cold_start_costs = case.gencost['STARTUP_COLD'].to_dict()
    shutdown_cost = case.gencost['SHUTDOWN_COST'].to_dict()

    hot_start_cold_start_costs(mdl, hot_start_costs=hot_start_costs, cold_start_costs=cold_start_costs,
                               scaled_cold_start_time=scaled_cold_start_time, shutdown_cost=shutdown_cost)

    # Build load data
    load_dict = dict()
    columns = load_df.columns
    for i, t in load_df.iterrows():
        for col in columns:
            load_dict[(col, i)] = t[col]

    initialize_demand(mdl, net_fixed_load=load_dict)

    # Initialize Pyomo Variables
    initialize_model(mdl, positive_mismatch_penalty=case.PositiveMismatchPenalty,
                     negative_mismatch_penalty=case.NegativeMismatchPenalty)

    # price sensitive load
    if case.PriceSenLoadFlag == 0:
        price_sen_load_flag = False
    else:
        price_sen_load_flag = True

    # adding segments for price sensitive loads
    segments = config.pop('segments', 10)
    psl_points = dict()
    psl_values = dict()
    pmax_values = dict()

    # print('segments=',segments)
    if price_sen_load_flag:
        if PriceSenLoadData is not None:
            psl_names = []
            psl_at_buses = {}
            for name, hour in PriceSenLoadData:
                if name not in psl_names:
                    psl_names.append(name)
                psl_record = PriceSenLoadData[name, hour]
                if psl_record['atBus'] not in psl_at_buses.keys():
                    psl_at_buses[psl_record['atBus']] = []
                if name not in psl_at_buses[psl_record['atBus']]:
                    psl_at_buses[psl_record['atBus']].append(name)

                psl_points[name, hour] = np.linspace(0, psl_record['Pmax'], num=psl_record['NS'])
                psl_values[name, hour] = psl_record['d'] + psl_record['e'] * psl_points[name, hour] - psl_record['f'] * \
                                         psl_points[name, hour] ** 2
                pmax_values[name, hour] = psl_record['Pmax']

            for k, v in psl_points.items():
                psl_points[k] = [float(i) for i in v]
            for k, v in psl_values.items():
                psl_values[k] = [float(i) for i in v]
            initialize_price_sensitive_load(mdl,
                                           price_sensitive_load_names=psl_names,
                                           price_sensitive_load_at_bus=psl_at_buses)

            maximum_power_demand_loads(mdl, maximum_power_demand=pmax_values)

            initialize_load_demand(mdl)

            piece_wise_linear_benefit(mdl, psl_points, psl_values)
            load_benefit(mdl)
            constraint_for_benefit(mdl)
        else:
            raise RuntimeError('PriceSenLoadFlag is True, but no Price Sensitive Load Data is correctly loaded')

    initialize_global_reserves(mdl, down_reserve_percent=down_reserve_percent, up_reserve_percent=up_reserve_percent)

    if zonal_data['HasZonalReserves'] is True:
        initialize_zonal_reserves(mdl, price_sen_load_flag=price_sen_load_flag, zone_names=zonal_data['Zones'],
                                  buses_at_each_zone=zonal_bus_data,
                                  zonal_down_reserve_percent=zonal_down_reserve_percent,
                                  zonal_up_reserve_percent=zonal_up_reserve_percent)

    constraint_net_power(mdl, price_sen_load_flag=price_sen_load_flag)

    if use_ptdf:
        ptdf = calculate_PTDF(case, precision=config.pop('ptdf_precision', None),
                              tolerance=config.pop('ptdf_tolerance', None))
        constraint_line(mdl, ptdf=ptdf)
    else:
        constraint_line(mdl, slack_bus=bus_df.index.get_loc(bus_df[bus_df['TYPE'] == 3].index[0]) + 1)
        # Pyomo is 1-indexed for sets, and MATPOWER type of bus should be used to get the slack bus

    constraint_power_balance(mdl)

    constraint_total_demand(mdl, price_sen_load_flag=price_sen_load_flag)
    constraint_load_generation_mismatch(mdl)
    constraint_reserves(mdl, price_sen_load_flag=price_sen_load_flag, has_global_reserves=True,
                        has_zonal_reserves=zonal_data['HasZonalReserves'])
    constraint_generator_power(mdl)
    if Op == 'scuc':
        constraint_up_down_time(mdl)
    constraint_for_cost(mdl)

    # Add objective function
    objective_function(mdl, price_sen_load_flag=price_sen_load_flag)

    for t, row in case.gen_status.iterrows():
        for g, v in row.items():
            if not pd.isnull(v):
                try:
                    mdl.UnitOn[g, t].fixed = True
                    mdl.UnitOn[g, t].value = v
                except KeyError:
                    pass

    mdl.dual = Suffix(direction=Suffix.IMPORT)

    return PSSTModel(mdl)


class PSSTModel(object):

    def __init__(self, _model, is_solved=False):
        self._model = _model
        self._is_solved = is_solved
        self._status = None
        self._results = None

    def __repr__(self):
        repr_string = 'status={}'.format(self._status)
        string = '<{}.{}({})>'.format(self.__class__.__module__, self.__class__.__name__, repr_string)
        return string

    def solve(self, solver='glpk', verbose=False, keep_files=True, **kwargs):
        termination_condition = solve_model(self._model, solver=solver, verbose=verbose, keep_files=keep_files, **kwargs)
        self._results = PSSTResults(self._model)
        return termination_condition

    @property
    def results(self):
        return self._results
