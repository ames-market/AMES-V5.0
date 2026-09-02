# This PSST file, originally due to Dheepak Krishnamurthy,
# has been modified by Swathi Battula to include Price Sensitive Load and Zonal data.

import os

import click
import numpy as np

from psst.model import build_model
from psst.utils import read_model, read_unit_commitment

np.seterr(all='raise')

SOLVER = os.getenv('PSST_SOLVER')
#SOLVER = "/home/d3j331/grid/tenv/ibm/cplex/bin/x86-64_linux/cplexamp"

@click.group()
@click.version_option('0.1.0', '--version')
def cli():
    pass


@cli.command()
@click.option('--uc', default=None, type=click.Path(), help='Path to unit commitment file')
@click.option('--data', default=None, type=click.Path(), help='Path to model data')
@click.option('--output', default=None, type=click.Path(), help='Path to output file')
@click.option('--solver', default=SOLVER, help='Solver')
def scuc(uc, data, output, solver):
    click.echo("Running combined DAM SCUC/SCED using Modified version of PSST")

    if SOLVER is not None:
        solver = SOLVER
    click.echo("Solver: " + str(solver))

    c, zonal_data_complete, price_sen_load_data = read_model(data.strip("'"))
    model = build_model(c, ZonalDataComplete=zonal_data_complete, PriceSenLoadData=price_sen_load_data, Op='scuc')
    model_name, solver_status = model.solve(solver=solver)
    click.echo(f'DAM combined SCUC/SCED is solved. Model: {model_name}, Status: {solver_status}')

    if solver_status == 'optimal':
        with open(uc.strip("'"), 'w') as outfile:
            instance = model._model
            results = {}
            for g in instance.Generators.data():
                for t in instance.TimePeriods:
                    results[(g, t)] = instance.UnitOn[g, t]

            for g in sorted(instance.Generators.data()):
                outfile.write(f"{str(g).ljust(8)}\n")
                for t in sorted(instance.TimePeriods):
                    outfile.write(f"{int(results[(g, t)].value + 0.5): 1d} \n")

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
                outfile.write(f"{str(g).ljust(8)}\n")
                for t in sorted(instance.TimePeriods):
                    outfile.write(f"{int(results[(g, t)].value + 0.5): 1d} {results_power_gen[(g, t)].value:6.4f}\n")
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
                    outfile.write(f"{str(ld).ljust(8)}\n")
                    for t in sorted(instance.TimePeriods):
                        outfile.write(f" {t:d} {price_sen_load_demand[(ld, t)]:6.4f} \n")
                # print ('PriceSenLoadDemand = \n',price_sen_load_demand)
                outfile.write("END_PSLResults\n")

    elif solver_status == 'infeasible':
        with open(output.strip("'"), 'w') as f:
            f.write("SOLUTION_STATUS\n")
            f.write("infeasible \t")
            f.write("\nEND_SOLUTION_STATUS\n")


@cli.command()
@click.option('--uc', default=None, type=click.Path(), help='Path to unit commitment file')
@click.option('--data', default=None, type=click.Path(), help='Path to model data')
@click.option('--output', default='./output.dat', type=click.Path(), help='Path to output file')
@click.option('--solver', default=SOLVER, help='Solver')
def sced(uc, data, output, solver):
    click.echo("Running RTM SCED using Modified version of PSST")

    if SOLVER is not None:
        solver = SOLVER
    click.echo("Solver: " + str(solver))

    uc_df = read_unit_commitment(uc.strip("'"))

    c, zonal_data_complete, price_sen_load_data = read_model(data.strip("'"))
    c.gen_status = uc_df.astype(int)

    model = build_model(c, ZonalDataComplete=zonal_data_complete, PriceSenLoadData=price_sen_load_data, Op='sced')
    model_name, solver_status = model.solve(solver=solver)
    click.echo(f'RTM SCED is solved. Model: {model_name}, Status: {solver_status}')

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
                f.write(f"{str(g).ljust(8)}\n")
                for t in instance.TimePeriods:
                    f.write(f"Interval: {t!s}\n")
                    f.write(f"\tPowerGenerated: {round(instance.PowerGenerated[g, t].value, 4)}\n")
            f.write("END_GenCoResults\n")

            if len(price_sen_load_data) != 0:
                f.write("PSLResults\n")
                instance = model._model
                price_sen_load_demand = {}
                for ld in instance.PriceSensitiveLoads.data():
                    for t in instance.TimePeriods:
                        price_sen_load_demand[(ld, t)] = instance.PSLoadDemand[ld, t].value

                for ld in sorted(instance.PriceSensitiveLoads.data()):
                    f.write(f"{str(ld).ljust(8)}\n")
                    for t in sorted(instance.TimePeriods):
                        f.write(f" {t:d} {price_sen_load_demand[(ld, t)]:6.4f} \n")
                # print ('PriceSenLoadDemand = \n',price_sen_load_demand)
                f.write("END_PSLResults\n")

            f.write("VOLTAGE_ANGLES\n")
            for bus in sorted(instance.Buses):
                for t in instance.TimePeriods:
                    f.write(f'{bus!s} {t!s} : {round(instance.Angle[bus, t].value, 3)!s}\n')
            f.write("END_VOLTAGE_ANGLES\n")

    elif solver_status == 'infeasible':
        with open(output.strip("'"), 'w') as f:
            f.write("SOLUTION_STATUS\n")
            f.write("infeasible \t")
            f.write("\nEND_SOLUTION_STATUS\n")


if __name__ == "__main__":
    cli()
    # small test cases
    # path = "../../DATA/"
    # path= "/home/d3j331/grid/tesp/examples/analysis/dsot/code/lean_aug_8_pv_fl_ev_f/"
    # scuc(path+"uc.dat",path+"dam.dat",path+"res.out", SOLVER)
    # sced(path+"uc.dat",path+"rtm.dat",path+"res.out", SOLVER)
