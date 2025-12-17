# This PSST file, originally due to Dheepak Krishnamurthy,
# has been modified by Swathi Battula to read Price Sensitive Load and Zonal data.

import os

import pandas as pd

generator_data_str_format = '{bus}\t{Pg}\t{Qg}\t{Qmax}\t{Qmin}\t{Vg}\t{mBase}\t{status}\t{Pmax}\t{Pmin}\t{Pc1}\t{Pc2}\t{Qc1min}\t{Qc1max}\t{Qc2min}\t{Qc2max}\t{ramp_agc}\t{ramp_10}\t{ramp_30}\t{ramp_q}\t{apf}'.format

current_directory = os.path.realpath(os.path.dirname(__file__))


def int_else_float_except_string(s):
    try:
        f = float(s.replace(',', '.'))
        i = int(f)
        return i if i == f else f
    except ValueError:
        return s


def has_number(string):
    return any(case.isdigit() for case in string)


def dict_to_repr(d):
    string = ''
    for i, (k, v) in enumerate(d.items()):
        if i == 0:
            string = string + '{}={}'.format(k, v)
        else:
            string = string + ', {}={}'.format(k, v)
    return string


def make_interpolater(domain_min, domain_max, range_min, range_max):
    # Figure out how 'wide' each range is
    domain_span = domain_max - domain_min
    range_span = range_max - range_min

    try:
        # Compute the scale factor between left and right values
        scale_factor = float(range_span) / float(domain_span)
    except ZeroDivisionError:
        scale_factor = 0

    # create interpolation function using pre-calculated scaleFactor
    def interp_fn(value):
        return range_min + (value - domain_min) * scale_factor

    return interp_fn


def create_gen_data(**kwargs):
    gen_data = dict()
    gen_data['bus'] = kwargs.pop('bus', 0)
    gen_data['Pg'] = kwargs.pop('Pg', 0)
    gen_data['Qg'] = kwargs.pop('Qg', 0)
    gen_data['Qmax'] = kwargs.pop('Qmax', 0)
    gen_data['Qmin'] = kwargs.pop('Qmin', 0)
    gen_data['Vg'] = kwargs.pop('Vg', 0)
    gen_data['mBase'] = kwargs.pop('mBase', 0)
    gen_data['status'] = kwargs.pop('status', 0)
    gen_data['Pmax'] = kwargs.pop('Pmax', 0)
    gen_data['Pmin'] = kwargs.pop('Pmin', 0)
    gen_data['Pc1'] = kwargs.pop('Pc1', 0)
    gen_data['Pc2'] = kwargs.pop('Pc2', 0)
    gen_data['Qc1min'] = kwargs.pop('Qc1min', 0)
    gen_data['Qc1max'] = kwargs.pop('Qc1max', 0)
    gen_data['Qc2min'] = kwargs.pop('Qc2min', 0)
    gen_data['Qc2max'] = kwargs.pop('Qc2max', 0)
    gen_data['ramp_agc'] = kwargs.pop('ramp_agc', 0)
    gen_data['ramp_10'] = kwargs.pop('ramp_10', 0)
    gen_data['ramp_30'] = kwargs.pop('ramp_30', 0)
    gen_data['ramp_q'] = kwargs.pop('ramp_q', 0)
    gen_data['apf'] = kwargs.pop('apf', 0)
    return gen_data


def read_unit_commitment(ucfile):
    with open(ucfile) as f:
        data = f.read()

    uc_dict = dict()
    uc = []
    for ln in data.splitlines():

        ln = ln.strip()

        if ln == '1' or ln == '0':
            uc.append(ln)
        else:
            uc = []
            uc_dict[ln] = uc
    num_entry = len(uc) + 1
    return pd.DataFrame(uc_dict, index=range(1, num_entry))


def find_generators(data):
    directive = r'set ThermalGenerators :='
    for ln in data.splitlines():
        if ln.startswith(directive):
            return ln.split('=')[1].strip('; ').split()
    return None


def find_buses(data):
    for ln in data.splitlines():
        directive = 'set Buses := '
        if ln.startswith(directive):
            return ln.split(directive)[1].strip(';').split()
    return None


def read_model(model_data):
    with open(model_data) as f:
        data = f.read()

    from .case import PSSTCase
    case = PSSTCase(os.path.join(current_directory, '../cases/case.m'))

    ag = find_generators(data)
    for g in ag:
        case.gen.loc[g] = case.gen.loc['GenCo0']
        case.gencost.loc[g] = case.gencost.loc['GenCo0']
        case.gen_status.loc[g] = case.gen_status.loc['GenCo0']

    if 'GenCo0' not in ag:
        case.gen.drop('GenCo0', inplace=True)
        case.gencost.drop('GenCo0', inplace=True)
        case.gen_status.drop('GenCo0', inplace=True)

    directive = 'set ThermalGeneratorsAtBus'
    case.gen['GEN_BUS'] = ""
    for ln in data.splitlines():
        if ln.startswith(directive):
            bus, gen = ln.split(directive)[1].split(':=')
            bus = bus.replace(']', '').replace('[', '').strip()
            gen = gen.replace(';', '').strip()
            gen_ary = gen.split(' ')
            for gen_i in gen_ary:
                case.gen.loc[gen_i, 'GEN_BUS'] = bus

    case.PriceSenLoadFlag = 0.0
    for ln in data.splitlines():
        if ln.startswith('param StorageFlag'):
            case.StorageFlag = float(ln.split(':=')[1].split(';')[0].replace(' ', ''))
        if ln.startswith('param NDGFlag'):
            case.NDGFlag = float(ln.split(':=')[1].split(';')[0].replace(' ', ''))
        if ln.startswith('param PriceSenLoadFlag'):
            case.PriceSenLoadFlag = float(ln.split(':=')[1].split(';')[0].replace(' ', ''))

    for ln in data.splitlines():
        if ln.startswith('param DownReservePercent'):
            case.DownReservePercent = float(ln.split(':=')[1].split(';')[0].replace(' ', ''))
        if ln.startswith('param UpReservePercent'):
            case.UpReservePercent = float(ln.split(':=')[1].split(';')[0].replace(' ', ''))

    zonal_data = {'NumberOfZones': 0, 'Zones': [], 'HasZonalReserves': False}

    for ln in data.splitlines():
        if ln.startswith('param NumberOfZones'):
            zonal_data['NumberOfZones'] = int(ln.split(':=')[1].split(';')[0].replace(' ', ''))

    for ln in data.splitlines():
        if ln.startswith('param HasZonalReserves'):
            flag_str = ln.split(':=')[1].split(';')[0].replace(' ', '')
            if flag_str == 'True' or flag_str == 'true':
                zonal_data['HasZonalReserves'] = True

    # print('HasZonalReserves: ', zonal_data['HasZonalReserves'])

    for ln in data.splitlines():
        directive = 'set Zones := '
        if ln.startswith(directive):
            zonal_data['Zones'] = ln.split(directive)[1].strip(';').split()

    zonal_down_reserve_percent = {}
    zonal_up_reserve_percent = {}
    zonal_bus_data = {}

    is_read = False
    for ln in data.splitlines():
        if ln.strip() == ';':
            is_read = False
        if ln.startswith('param: Buses ZonalDownReservePercent ZonalUpReservePercent'):
            is_read = True
            continue
        if is_read:
            z, buses, rdzp, ruzp = ln.split(" ")
            zonal_down_reserve_percent[z] = float(rdzp)
            zonal_up_reserve_percent[z] = float(ruzp)
            bus_trim = buses[:-1]
            bus_split = bus_trim.split(',')
            zonal_bus_data[z] = bus_split

    is_read = False
    for ln in data.splitlines():
        if ln.strip() == ';':
            is_read = False

        if ln == 'param: PowerGeneratedT0 ScaledUnitOnT0State InitialTimeON InitialTimeOFF MinimumPowerOutput MaximumPowerOutput ScaledMinimumUpTime ScaledMinimumDownTime ScaledRampUpLimit ScaledRampDownLimit ScaledStartupRampLimit ScaledShutdownRampLimit ScaledColdStartTime ColdStartCost HotStartCost ShutdownCost :=':
            is_read = True
            continue

        if is_read:
            g, pg, status, ito_g, itf_g, min_g, max_g, \
                scaled_min_up_time, scaled_min_down_time, \
                scaled_ramp_up_rate, scaled_ramp_down_rate, \
                scaled_startup_ramp_rate, scaled_shutdown_ramp_rate, \
                scaled_cold_start_time, cold_start_cost, hot_start_cost, shut_down_cost = ln.split()

            case.gen.loc[g, 'PG'] = float(pg.replace(',', '.'))
            case.gen.loc[g, 'UnitOnT0State'] = int(status.replace(',', '.'))
            case.gen.loc[g, 'InitialTimeON'] = int(ito_g)
            case.gen.loc[g, 'InitialTimeOFF'] = int(itf_g)
            case.gen.loc[g, 'PMIN'] = float(min_g.replace(',', '.'))
            case.gen.loc[g, 'PMAX'] = float(max_g.replace(',', '.'))
            case.gen.loc[g, 'SCALED_MINIMUM_UP_TIME'] = int(scaled_min_up_time)
            case.gen.loc[g, 'SCALED_MINIMUM_DOWN_TIME'] = int(scaled_min_down_time)
            ramp_up = float(scaled_ramp_up_rate.replace(',', '.'))
            case.gen.loc[g, 'SCALED_RAMP_UP'] = 999999 if ramp_up == 0 else ramp_up
            # print('SCALED_RAMP_UP:',case.gen.loc[g, 'SCALED_RAMP_UP'])
            ramp_down = float(scaled_ramp_down_rate.replace(',', '.'))
            case.gen.loc[g, 'SCALED_RAMP_DOWN'] = 999999 if ramp_down == 0 else ramp_down
            # print('SCALED_RAMP_DOWN:',case.gen.loc[g, 'SCALED_RAMP_DOWN'])
            startup_ramp = float(scaled_startup_ramp_rate.replace(',', '.'))
            case.gen.loc[g, 'SCALED_STARTUP_RAMP'] = 999999 if startup_ramp == 0 else startup_ramp
            shutdown_ramp = float(scaled_shutdown_ramp_rate.replace(',', '.'))
            case.gen.loc[g, 'SCALED_SHUTDOWN_RAMP'] = 999999 if shutdown_ramp == 0 else shutdown_ramp
            scaled_cold_start_time = int(scaled_cold_start_time.replace(',', '.'))
            case.gencost.loc[g, 'SCALED_COLD_START_TIME'] = scaled_cold_start_time
            cold_start_cost = float(cold_start_cost.replace(',', '.'))
            case.gencost.loc[g, "STARTUP_COLD"] = cold_start_cost
            hot_start_cost = float(hot_start_cost.replace(',', '.'))
            case.gencost.loc[g, "STARTUP_HOT"] = hot_start_cost
            shutdown_cost = float(shut_down_cost.replace(',', '.'))
            case.gencost.loc[g, "SHUTDOWN_COST"] = shutdown_cost

    branch_number = 1
    for ln in data.splitlines():
        if ln.strip() == ';':
            is_read = False

        if ln == 'param: BusFrom BusTo ThermalLimit Reactance :=':
            is_read = True
            # Set data type for columns
            case.branch['F_BUS'] = ""
            case.branch['T_BUS'] = ""
            case.branch['BR_X'] = float(1.0)
            case.branch['RATE_A'] = float(1.0)
            continue

        if is_read:
            _, b1, b2, tl, r = ln.split()
            case.branch.loc[branch_number] = case.branch.loc[0]
            case.branch.loc[branch_number, 'F_BUS'] = b1
            case.branch.loc[branch_number, 'T_BUS'] = b2
            case.branch.loc[branch_number, 'BR_X'] = float(r.replace(',', '.'))
            case.branch.loc[branch_number, 'RATE_A'] = float(tl.replace(',', '.'))
            branch_number = branch_number + 1

    case.branch.drop(0, inplace=True)

    ag = find_buses(data)
    for b in ag:
        case.bus.loc[b] = case.bus.loc['Bus1']

    if 'Bus1' not in ag:
        case.bus.drop('Bus1', inplace=True)

    is_read = False
    directive = 'param: NetFixedLoadForecast :='
    for ln in data.splitlines():
        if ln.strip() == ';':
            is_read = False

        if ln.strip() == '':
            continue

        if ln == directive:
            is_read = True
            continue

        if is_read:
            b, t, v = ln.split()
            case.load.loc[t, b] = float(v.replace(',', '.'))

    case.load = case.load.fillna(0)
    case.load.drop(0, inplace=True)
    case.load.index = range(1, len(case.load.index) + 1)

    # Make Bus1 slack
    case.bus.loc['Bus1', 'TYPE'] = 3.0

    is_read = False
    directive = 'param: a b c NS :='
    for ln in data.splitlines():
        if ln.strip() == ';':
            is_read = False

        if ln.strip() == '':
            continue

        if ln == directive:
            is_read = True
            continue

        if is_read:
            g, a, b, c, ns = ln.split()
            case.gencost.loc[g, "c"] = float(c.replace(',', '.'))
            case.gencost.loc[g, "b"] = float(b.replace(',', '.'))
            case.gencost.loc[g, "a"] = float(a.replace(',', '.'))
            case.gencost.loc[g, "NS"] = int(ns.replace(',', '.'))

    price_sen_load_data = {}
    directive = 'set PriceSensitiveLoadNames :='
    for ln in data.splitlines():
        if ln.startswith(directive):
            continue

    is_read = False
    directive = 'param: Name ID atBus hourIndex d e f SLMax NS :='
    # Name ID atBus hourIndex d e f SLMax NS
    for ln in data.splitlines():
        if ln.strip() == ';':
            is_read = False

        if ln.strip() == '':
            continue

        if ln == directive:
            is_read = True
            continue

        if is_read:
            name, idx, at_bus, hour_index, d, e, f, sl_max, ns = ln.split()
            price_sen_load_data[name, (int(hour_index))] = {'ID': idx, 'atBus': at_bus, 'hourIndex': int(hour_index),
                                                            'd': float(d), 'e': float(e), 'f': float(f),
                                                            'Pmax': float(sl_max), 'NS': int(ns)}

    case.PositiveMismatchPenalty = 1e6
    case.NegativeMismatchPenalty = 1e6

    for ln in data.splitlines():
        if ln.startswith('param BalPenPos'):
            case.PositiveMismatchPenalty = float(ln.split(':=')[1].split(';')[0].replace(' ', ''))

    for ln in data.splitlines():
        if ln.startswith('param BalPenNeg'):
            case.NegativeMismatchPenalty = float(ln.split(':=')[1].split(';')[0].replace(' ', ''))

    case.TimePeriodLength = 1

    for ln in data.splitlines():
        if ln.startswith('param TimePeriodLength'):
            case.TimePeriodLength = float(ln.split(':=')[1].split(';')[0].replace(' ', ''))

    for ln in data.splitlines():
        if ln.startswith('param NumTimePeriods'):
            case.NumTimePeriods = int(ln.split(':=')[1].split(';')[0].replace(' ', ''))

    zonal_data_complete = {'zonal_data': zonal_data, 'zonal_bus_data': zonal_bus_data,
                           'zonal_down_reserve_percent': zonal_down_reserve_percent,
                           'zonal_up_reserve_percent': zonal_up_reserve_percent}

    return case, zonal_data_complete, price_sen_load_data  # , NDGData
