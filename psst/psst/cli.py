# This PSST file, originally due to Dheepak Krishnamurthy,
# has been modified by Swathi Battula to include Price Sensitive Load and Zonal data.
# -*- coding: utf-8 -*-

import os

import click
import numpy as np

from psst.model import build_model
from psst.utils import read_unit_commitment, read_model

np.seterr(all='raise')

SOLVER = os.getenv('PSST_SOLVER')


# @click.group()
# @click.version_option('0.1.0', '--version')
def cli():
    pass


# @cli.command()
# @click.option('--uc', default=None, type=click.Path(), help='Path to unit commitment file')
# @click.option('--data', default=None, type=click.Path(), help='Path to model data')
# @click.option('--output', default=None, type=click.Path(), help='Path to output file')
# @click.option('--solver', default=SOLVER, help='Solver')
def scuc(uc, data, output, solver):
    click.echo("Running combined DAM SCUC/SCED using Modified version of PSST")

    if SOLVER is not None:
        solver = SOLVER
    click.echo("Solver : " + str(solver))

    c, zonal_data_complete, price_sen_load_data = read_model(data.strip("'"))
    model = build_model(c, ZonalDataComplete=zonal_data_complete, PriceSenLoadData=price_sen_load_data, Op='scuc')
    model_name, solver_status = model.solve(solver=solver)
    click.echo("Model for DAM combined SCUC/SCED is solved. Status: " + solver_status)

    if solver_status == 'optimal':
        with open(uc.strip("'"), 'w') as outfile:
            instance = model._model
            results = {}
            for g in instance.Generators.data():
                for t in instance.TimePeriods:
                    results[(g, t)] = instance.UnitOn[g, t]

            for g in sorted(instance.Generators.data()):
                outfile.write("%s\n" % str(g).ljust(8))
                for t in sorted(instance.TimePeriods):
                    outfile.write("% 1d \n" % (int(results[(g, t)].value + 0.5)))

        uc_df = read_unit_commitment(uc.strip("'"))
        c.gen_status = uc_df.astype(int)

        model = build_model(c, ZonalDataComplete=zonal_data_complete, PriceSenLoadData=price_sen_load_data)
        model.solve(solver=solver)

        with open(output.strip("'"), 'w') as outfile:
            instance = model._model
            results = {}
            results_power_gen = {}
            for g in instance.Generators.data():
                for t in instance.TimePeriods:
                    results[(g, t)] = instance.UnitOn[g, t]
                    results_power_gen[(g, t)] = instance.PowerGenerated[g, t]

            outfile.write("SOLUTION_STATUS\n")
            outfile.write("optimal \t")
            outfile.write("\nEND_SOLUTION_STATUS\n")

            for g in sorted(instance.Generators.data()):
                outfile.write("%s\n" % str(g).ljust(8))
                for t in sorted(instance.TimePeriods):
                    outfile.write("% 1d %6.4f\n" % (int(results[(g, t)].value + 0.5), results_power_gen[(g, t)].value))
            outfile.write("DAMLMP\n")
            for h, r in model.results.lmp.iterrows():
                bn = 1
                for _, lmp in r.items():
                    if lmp is None:
                        lmp = 0
                    outfile.write(str(bn) + ' : ' + str(h + 1) + ' : ' + str(round(lmp, 2)) + "\n")
                    bn = bn + 1
            outfile.write("END_LMP\n")

            if len(price_sen_load_data) != 0:
                outfile.write("PSLResults\n")
                instance = model._model
                price_sen_load_demand = {}
                for ld in instance.PriceSensitiveLoads.data():
                    for t in instance.TimePeriods:
                        price_sen_load_demand[(ld, t)] = instance.PSLoadDemand[ld, t].value

                for ld in sorted(instance.PriceSensitiveLoads.data()):
                    outfile.write("%s\n" % str(ld).ljust(8))
                    for t in sorted(instance.TimePeriods):
                        outfile.write(" %d %6.4f \n" % (t, price_sen_load_demand[(ld, t)]))
                # print ('PriceSenLoadDemand = \n',price_sen_load_demand)
                outfile.write("END_PSLResults\n")

    elif solver_status == 'infeasible':
        with open(output.strip("'"), 'w') as f:
            f.write("SOLUTION_STATUS\n")
            f.write("infeasible \t")
            f.write("\nEND_SOLUTION_STATUS\n")


# @cli.command()
# @click.option('--uc', default=None, type=click.Path(), help='Path to unit commitment file')
# @click.option('--data', default=None, type=click.Path(), help='Path to model data')
# @click.option('--output', default='./output.dat', type=click.Path(), help='Path to output file')
# @click.option('--solver', default=SOLVER, help='Solver')
def sced(uc, data, output, solver):
    click.echo("Running RTM SCED using Modified version of PSST")

    if SOLVER is not None:
        solver = SOLVER
    click.echo("Solver : " + str(solver))

    uc_df = read_unit_commitment(uc.strip("'"))

    c, zonal_data_complete, price_sen_load_data = read_model(data.strip("'"))
    c.gen_status = uc_df.astype(int)

    model = build_model(c, ZonalDataComplete=zonal_data_complete, PriceSenLoadData=price_sen_load_data, Op='sced')
    model_name, solver_status = model.solve(solver=solver)
    click.echo("Model for RTM SCED is solved. Status: " + solver_status)

    if solver_status == 'optimal':
        with open(output.strip("'"), 'w') as f:

            f.write("SOLUTION_STATUS\n")
            f.write("optimal \t")
            f.write("\nEND_SOLUTION_STATUS\n")

            f.write("LMP\n")
            for h, r in model.results.lmp.iterrows():
                bn = 1
                for _, lmp in r.items():
                    if lmp is None:
                        lmp = 0
                    f.write(str(bn) + ' : ' + str(h + 1) + ' : ' + str(round(lmp, 2)) + "\n")
                    bn = bn + 1
            f.write("END_LMP\n")

            f.write("GenCoResults\n")
            instance = model._model

            for g in instance.Generators.data():
                f.write("%s\n" % str(g).ljust(8))
                for t in instance.TimePeriods:
                    f.write("Interval: {}\n".format(str(t)))
                    f.write("\tPowerGenerated: {}\n".format(round(instance.PowerGenerated[g, t].value, 4)))
            f.write("END_GenCoResults\n")

            if len(price_sen_load_data) != 0:
                f.write("PSLResults\n")
                instance = model._model
                price_sen_load_demand = {}
                for ld in instance.PriceSensitiveLoads.data():
                    for t in instance.TimePeriods:
                        price_sen_load_demand[(ld, t)] = instance.PSLoadDemand[ld, t].value

                for ld in sorted(instance.PriceSensitiveLoads.data()):
                    f.write("%s\n" % str(ld).ljust(8))
                    for t in sorted(instance.TimePeriods):
                        f.write(" %d %6.4f \n" % (t, price_sen_load_demand[(ld, t)]))
                # print ('PriceSenLoadDemand = \n',price_sen_load_demand)
                f.write("END_PSLResults\n")

            f.write("VOLTAGE_ANGLES\n")
            for bus in sorted(instance.Buses):
                for t in instance.TimePeriods:
                    f.write('{} {} : {}\n'.format(str(bus), str(t), str(round(instance.Angle[bus, t].value, 3))))
            f.write("END_VOLTAGE_ANGLES\n")

    elif solver_status == 'infeasible':
        with open(output.strip("'"), 'w') as f:
            f.write("SOLUTION_STATUS\n")
            f.write("infeasible \t")
            f.write("\nEND_SOLUTION_STATUS\n")


if __name__ == "__main__":
    # cli()
    path = "/home/d3j331/grid/repo/AMES-V5.0/DATA/"
    # path = "/home/d3j331/grid/tesp/examples/analysis/glm_dsot/code/gld_feeder_test_pv_bt_fl_ev/"
    # scuc(path+"uc.dat",path+"dam.dat",path+"res.out", SOLVER)
    sced(path+"uc.dat",path+"rtm.dat",path+"res.out", SOLVER)
