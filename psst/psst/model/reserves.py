# This PSST file, originally due to Dheepak Krishnamurthy,
# has been modified by Swathi Battula to handle reserve zones.

from pyomo.environ import Set, Param, Reals, NonNegativeReals, value


def _build_price_sen_load_buses_at_each_zone(m, rz):
    price_sen_load_at_each_zone = []
    for b in m.Buses:
        if b in m.BusesAtEachReserveZone[rz]:
            for ld in m.PriceSensitiveLoadsAtBus[b]:
                price_sen_load_at_each_zone.append(ld)
    return price_sen_load_at_each_zone


def _zone_generator_map(m, g):
    bus_found_name = None
    for bus in m.GeneratorsAtBus:
        gen_array = m.GeneratorsAtBus[bus]
        for gen in gen_array:
            if g == gen:
                bus_found_name = bus
    if bus_found_name is not None:
        for zone in m.BusesAtEachReserveZone:
            bus_array = m.BusesAtEachReserveZone[zone]
            for bus in bus_array:
                if bus_found_name == bus:
                    return zone
    return None


def _form_generator_reserve_zones(m, rz):
    list_of_gen_cos = []
    for g in m.Generators:
        if m.GenReserveZoneLocation[g] == rz:
            list_of_gen_cos.append(g)
    return list_of_gen_cos


def _reserve_up_requirement_rule(m, t):
    return m.UpReservePercent * sum(value(m.NetFixedLoad[b, t]) for b in m.Buses)


def _reserve_down_requirement_rule(m, t):
    return m.DownReservePercent * sum(value(m.NetFixedLoad[b, t]) for b in m.Buses)


def initialize_global_reserves(model, down_reserve_percent=None, up_reserve_percent=None,
                               reserve_up_requirement=_reserve_up_requirement_rule,
                               reserve_down_requirement=_reserve_down_requirement_rule):
    model.DownReservePercent = Param(within=Reals, initialize=down_reserve_percent, mutable=True)
    model.UpReservePercent = Param(within=Reals, initialize=up_reserve_percent, mutable=True)
    model.ReserveUpRequirement = Param(model.TimePeriods, initialize=reserve_up_requirement,
                                       within=NonNegativeReals, default=0.0, mutable=True)
    model.ReserveDownRequirement = Param(model.TimePeriods, initialize=reserve_down_requirement,
                                         within=NonNegativeReals, default=0.0, mutable=True)


def initialize_zonal_reserves(model, price_sen_load_flag=False, zone_names=None, buses_at_each_zone=None,
                              zonal_down_reserve_percent=None, zonal_up_reserve_percent=None,
                              price_sen_load_reserve_zones=_build_price_sen_load_buses_at_each_zone,
                              generator_reserve_zones=_form_generator_reserve_zones,
                              zone_generator_map=_zone_generator_map):
    model.ReserveZones = Set(initialize=zone_names)
    model.BusesAtEachReserveZone = Set(model.ReserveZones, initialize=buses_at_each_zone)

    model.GenReserveZoneLocation = Param(model.Generators, initialize=zone_generator_map)
    model.GeneratorsInReserveZone = Set(model.ReserveZones, initialize=generator_reserve_zones)
    model.DemandInReserveZone = Set(model.ReserveZones, initialize=buses_at_each_zone)

    if price_sen_load_flag:
        model.PriceSenLoadInReserveZone = Set(model.ReserveZones, initialize=price_sen_load_reserve_zones)

    model.ZonalDownReservePercent = Param(model.ReserveZones, initialize=zonal_down_reserve_percent,
                                          within=NonNegativeReals, default=0.0, mutable=True)
    model.ZonalUpReservePercent = Param(model.ReserveZones, initialize=zonal_up_reserve_percent,
                                        within=NonNegativeReals,
                                        default=0.0, mutable=True)
