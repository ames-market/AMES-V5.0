from pyomo.environ import Set, Param, Any


def initialize_network(model,
                       transmission_lines=None,
                       bus_from=None,
                       bus_to=None):
    model.TransmissionLines = Set(initialize=transmission_lines)

    model.BusFrom = Param(model.TransmissionLines, within=Any, initialize=bus_from)
    model.BusTo = Param(model.TransmissionLines, within=Any, initialize=bus_to)


# Alternative to lines_to
def _derive_connections_to(m, b):
    return (ln for ln in m.TransmissionLines if m.BusTo[ln] == b)


# Alternative to lines_from
def _derive_connections_from(m, b):
    return (ln for ln in m.TransmissionLines if m.BusFrom[ln] == b)


def derive_network(model,
                   lines_from=_derive_connections_from,
                   lines_to=_derive_connections_to):
    model.LinesTo = Set(model.Buses, initialize=lines_to)
    model.LinesFrom = Set(model.Buses, initialize=lines_from)


def _get_b_from_reactance(m, ln):
    if m.Reactance[ln] < 0:
        return 0
    if m.Reactance[ln] == 0:
        return 99999999
    else:
        return 1 / float(m.Reactance[ln])


def calculate_network_parameters(model,
                                 reactance=None,
                                 susceptance=_get_b_from_reactance):
    model.Reactance = Param(model.TransmissionLines, initialize=reactance)
    model.B = Param(model.TransmissionLines, initialize=susceptance)


def enforce_thermal_limits(model,
                           thermal_limit=None,
                           enforce_line=True):
    model.ThermalLimit = Param(model.TransmissionLines, initialize=thermal_limit)
    model.EnforceLine = Param(model.TransmissionLines, initialize=enforce_line)
