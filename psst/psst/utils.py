#This PSST file, originally due to Dheepak Krishnamurthy, has been modified by Swathi Battula to read Price Sensitive Load and Zonal data.

import os
import click

import pandas as pd

generator_data_str_format = '{bus}\t{Pg}\t{Qg}\t{Qmax}\t{Qmin}\t{Vg}\t{mBase}\t{status}\t{Pmax}\t{Pmin}\t{Pc1}\t{Pc2}\t{Qc1min}\t{Qc1max}\t{Qc2min}\t{Qc2max}\t{ramp_agc}\t{ramp_10}\t{ramp_30}\t{ramp_q}\t{apf}'.format

current_directory = os.path.realpath(os.path.dirname(__file__))


def int_else_float_except_string(s):
    try:
        f = float(s.replace(',', '.'))
        i = int(f)
        return i if i==f else f
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
        return range_min + (value-domain_min)*scale_factor

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
    for l in data.splitlines():
        
        l = l.strip()

        if l == '1' or l=='0':
            uc.append(l)
        else:
            uc = []
            uc_dict[l] = uc
    #print('uc_dict:', uc_dict)
    for item in uc_dict:
        NumEntry = len(uc_dict[item])
    return pd.DataFrame(uc_dict, index=range(1,NumEntry+1))


def find_generators(data):
    DIRECTIVE = r'set ThermalGenerators :='
    for l in data.splitlines():
        if l.startswith(DIRECTIVE):
            return l.split('=')[1].strip('; ').split()

def find_buses(data):
    for l in data.splitlines():
        DIRECTIVE = 'set Buses := '
        if l.startswith(DIRECTIVE):
            return l.split(DIRECTIVE)[1].strip(';').split()

def read_model(model_data):

    with open(model_data) as f:
        data = f.read()

    from .case import PSSTCase
    case = PSSTCase(os.path.join(current_directory, '../cases/case.m'))

    ag = find_generators(data)
    for g in ag:
        case.gen.loc[g] = case.gen.loc['GenCo0']
        case.gencost.loc[g] = case.gencost.loc['GenCo0']

    if 'GenCo0' not in ag:
        case.gen.drop('GenCo0', inplace=True)
        case.gencost.drop('GenCo0', inplace=True)

    DIRECTIVE = 'set ThermalGeneratorsAtBus'
    for l in data.splitlines():
        if l.startswith(DIRECTIVE):
            bus, gen = l.split(DIRECTIVE)[1].split(':=')
            bus = bus.replace(']', '').replace('[', '').strip()
            gen = gen.replace(';', '').strip()
            #case.gen.loc[gen, 'GEN_BUS'] = bus
            gen_ary =gen.split(' ')
            for gen_i in gen_ary:
                case.gen.loc[gen_i, 'GEN_BUS'] = bus

    case.PriceSenLoadFlag = 0.0
    for l in data.splitlines():
        if l.startswith('param StorageFlag'):
            case.StorageFlag = float(l.split(':=')[1].split(';')[0].replace(' ', ''))
        if l.startswith('param NDGFlag'):
            case.NDGFlag = float(l.split(':=')[1].split(';')[0].replace(' ', ''))
        if l.startswith('param PriceSenLoadFlag'):
            case.PriceSenLoadFlag = float(l.split(':=')[1].split(';')[0].replace(' ', ''))

    for l in data.splitlines():
        if l.startswith('param DownReservePercent'):
            case.DownReservePercent = float(l.split(':=')[1].split(';')[0].replace(' ', ''))
        if l.startswith('param UpReservePercent'):
            case.UpReservePercent = float(l.split(':=')[1].split(';')[0].replace(' ', ''))

    zonalData = {'NumberOfZones': 0, 'Zones': '', 'HasZonalReserves': False}

    for l in data.splitlines():
        if l.startswith('param NumberOfZones'):
            zonalData['NumberOfZones'] = float(l.split(':=')[1].split(';')[0].replace(' ', ''))

    for l in data.splitlines():
        if l.startswith('param HasZonalReserves'):
            flag_str = l.split(':=')[1].split(';')[0].replace(' ', '')
            if flag_str == 'True' or flag_str == 'true':
                zonalData['HasZonalReserves'] = True


    #print('HasZonalReserves: ', zonalData['HasZonalReserves'])

    for l in data.splitlines():
        DIRECTIVE = 'set Zones := '
        if l.startswith(DIRECTIVE):
            zonalData['Zones'] = l.split(DIRECTIVE)[1].strip(';').split()

    ZonalDownReservePercent = {}
    ZonalUpReservePercent = {}
    zonalBusData = {}

    READ = False
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False
        if l.startswith('param: Buses ZonalDownReservePercent ZonalUpReservePercent'):
            READ = True
            continue
        if READ is True:
            z, Buses, RDZP, RUZP = l.split(" ")
            ZonalDownReservePercent[z] = float(RDZP)
            ZonalUpReservePercent[z] = float(RUZP)
            BusTrim = Buses[:-1]
            BusSplit = BusTrim.split(',')
            zonalBusData[z] = BusSplit

    READ = False
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False

        if l == 'param: PowerGeneratedT0 ScaledUnitOnT0State InitialTimeON InitialTimeOFF MinimumPowerOutput MaximumPowerOutput ScaledMinimumUpTime ScaledMinimumDownTime ScaledRampUpLimit ScaledRampDownLimit ScaledStartupRampLimit ScaledShutdownRampLimit ScaledColdStartTime ColdStartCost HotStartCost ShutdownCost :=':
            READ = True
            continue

        if READ is True:
            g, pg, status, ITO_g, ITF_g, min_g, max_g, scaled_min_up_time, scaled_min_down_time, scaled_ramp_up_rate, scaled_ramp_down_rate, scaled_startup_ramp_rate, scaled_shutdown_ramp_rate, scaled_cold_start_time, coldstartcost, hotstartcost, shutdowncost = l.split()

            case.gen.loc[g, 'PG'] = float(pg.replace(',', '.'))
            case.gen.loc[g, 'UnitOnT0State'] = int(status.replace(',','.'))
            case.gen.loc[g, 'InitialTimeON'] = int(ITO_g)
            case.gen.loc[g, 'InitialTimeOFF'] = int(ITF_g)
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
            cold_start_cost = float(coldstartcost.replace(',', '.'))
            case.gencost.loc[g, "STARTUP_COLD"] = cold_start_cost
            hot_start_cost = float(hotstartcost.replace(',', '.'))
            case.gencost.loc[g, "STARTUP_HOT"] = hot_start_cost
            shutdown_cost = float(shutdowncost.replace(',', '.'))
            case.gencost.loc[g, "SHUTDOWN_COST"] = shutdown_cost


    branch_number = 1
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False

        if l == 'param: BusFrom BusTo ThermalLimit Reactance :=':
            READ = True
            continue

        if READ is True:
            _, b1, b2, tl, r = l.split()
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

    READ = False
    DIRECTIVE = 'param: NetFixedLoadForecast :='
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False

        if l.strip() == '':
            continue

        if l == 'param: NetFixedLoadForecast :=':
            READ = True
            continue

        if READ is True:
            b, t, v = l.split()
            case.load.loc[t, b] = float(v.replace(',', '.'))

    case.load = case.load.fillna(0)
    case.load.drop(0, inplace=True)
    case.load.index = range(1, len(case.load.index)+1)

    # Make Bus1 slack
    case.bus.loc['Bus1', 'TYPE'] = 3.0

    READ = False
    DIRECTIVE = 'param: a b c NS :='
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False

        if l.strip() == '':
            continue

        if l == DIRECTIVE:
            READ = True
            continue

        if READ is True:
            g, a, b, c, NS = l.split()
            case.gencost.loc[g, "c"] = float(c.replace(',', '.'))
            case.gencost.loc[g, "b"] = float(b.replace(',', '.'))
            case.gencost.loc[g, "a"] = float(a.replace(',', '.'))
            case.gencost.loc[g, "NS"] = int(NS.replace(',', '.'))

    READ = False
    priceSenLoadData = {}
    DIRECTIVE = 'set PriceSensitiveLoadNames :='
    for l in data.splitlines():
        if l.startswith(DIRECTIVE):
            continue

    READ = False
    DIRECTIVE ='param: Name ID atBus hourIndex d e f SLMax NS :='

    #Name	   ID	  atBus	 hourIndex	   d   e     f	   SLMax	   NS
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False

        if l.strip() == '':
            continue

        if l == DIRECTIVE:
            READ = True
            continue

        if READ is True:
            Name,ID,atBus,hourIndex,d,e,f,SLMax, NS = l.split()
            priceSenLoadData[Name,(int(hourIndex))] = {'ID':ID,'atBus': atBus,'hourIndex':int(hourIndex),'d':float(d),'e':float(e),'f':float(f),'Pmax':float(SLMax), 'NS':int(NS)}

    case.PositiveMismatchPenalty = 1e6
    case.NegativeMismatchPenalty = 1e6

    for l in data.splitlines():
        if l.startswith('param BalPenPos'):
            case.PositiveMismatchPenalty = float(l.split(':=')[1].split(';')[0].replace(' ', ''))

    for l in data.splitlines():
        if l.startswith('param BalPenNeg'):
            case.NegativeMismatchPenalty = float(l.split(':=')[1].split(';')[0].replace(' ', ''))

    case.TimePeriodLength = 1

    for l in data.splitlines():
        if l.startswith('param TimePeriodLength'):
            case.TimePeriodLength = float(l.split(':=')[1].split(';')[0].replace(' ', ''))

    for l in data.splitlines():
        if l.startswith('param NumTimePeriods'):
            case.NumTimePeriods = int(l.split(':=')[1].split(';')[0].replace(' ', ''))


    ZonalDataComplete = {'zonalData': zonalData, 'zonalBusData': zonalBusData, 'ZonalDownReservePercent': ZonalDownReservePercent, 'ZonalUpReservePercent': ZonalUpReservePercent}

    return case, ZonalDataComplete, priceSenLoadData  #, NDGData
