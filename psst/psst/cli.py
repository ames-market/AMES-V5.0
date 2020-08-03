# -*- coding: utf-8 -*-

import os
import click
import pandas as pd
import random

from .utils import read_unit_commitment, read_model
from .model import build_model

import numpy as np

np.seterr(all='raise')

SOLVER = os.getenv('PSST_SOLVER')


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
    click.echo("Solver : " + str(solver))

    c, ZonalDataComplete, priceSenLoadData = read_model(data.strip("'"))
    model = build_model(c, ZonalDataComplete=ZonalDataComplete, PriceSenLoadData=priceSenLoadData, Op='scuc')

    SolverOutcomes = model.solve(solver=solver)
    Status= str(SolverOutcomes[1])
    click.echo("Model for DAM combined SCUC/SCED is solved. Status: "+ Status)

    if (Status is 'optimal'):

        with open(uc.strip("'"), 'w') as outfile:
            instance = model._model
            results = {}
            resultsPowerGen = {}
            for g in instance.Generators.value:
                for t in instance.TimePeriods:
                    results[(g, t)] = instance.UnitOn[g, t]

            for g in sorted(instance.Generators.value):
                outfile.write("%s\n" % str(g).ljust(8))
                for t in sorted(instance.TimePeriods):
                    outfile.write("% 1d \n" % (int(results[(g, t)].value + 0.5)))

        uc_df = read_unit_commitment(uc.strip("'"))
        c.gen_status = uc_df.astype(int)

        model = build_model(c, ZonalDataComplete=ZonalDataComplete, PriceSenLoadData=priceSenLoadData)

        model.solve(solver=solver)

        with open(output.strip("'"), 'w') as outfile:
            instance = model._model
            results = {}
            resultsPowerGen = {}
            for g in instance.Generators.value:
                for t in instance.TimePeriods:
                    results[(g, t)] = instance.UnitOn[g, t]
                    resultsPowerGen[(g, t)] = instance.PowerGenerated[g, t]

            outfile.write("SOLUTION_STATUS\n")
            outfile.write("optimal \t")
            outfile.write("\nEND_SOLUTION_STATUS\n")

            for g in sorted(instance.Generators.value):
                outfile.write("%s\n" % str(g).ljust(8))
                for t in sorted(instance.TimePeriods):
                    outfile.write("% 1d %6.4f\n" % (int(results[(g, t)].value + 0.5), resultsPowerGen[(g, t)].value))
            outfile.write("DAMLMP\n")
            for h, r in model.results.lmp.iterrows():
                bn = 1
                for _, lmp in r.iteritems():
                    if lmp is None:
                        lmp = 0
                    outfile.write(str(bn) + ' : ' + str(h + 1) + ' : ' + str(round(lmp,2)) +"\n")
                    bn = bn + 1
            outfile.write("END_LMP\n")

            if len(priceSenLoadData) is not 0:
                outfile.write("PSLResults\n")
                instance = model._model
                PriceSenLoadDemand = {}
                for l in instance.PriceSensitiveLoads.value:
                    for t in instance.TimePeriods:
                        PriceSenLoadDemand[(l, t)] = instance.PSLoadDemand[l, t].value

                for l in sorted(instance.PriceSensitiveLoads.value):
                    outfile.write("%s\n" % str(l).ljust(8))
                    for t in sorted(instance.TimePeriods):
                        outfile.write(" %d %6.4f \n" % ( t, PriceSenLoadDemand[(l, t)]))
                #print ('PriceSenLoadDemand = \n',PriceSenLoadDemand)
                outfile.write("END_PSLResults\n")

    elif (Status is 'infeasible'):
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
    click.echo("Solver : " + str(solver))

    uc_df = read_unit_commitment(uc.strip("'"))

    c, ZonalDataComplete, priceSenLoadData = read_model(data.strip("'"))
    c.gen_status = uc_df.astype(int)

    model = build_model(c, ZonalDataComplete=ZonalDataComplete, PriceSenLoadData=priceSenLoadData, Op='sced')
    SolverOutcomes = model.solve(solver=solver)
    Status= str(SolverOutcomes[1])
    #click.echo("Model for RTM SCED is solved. Status: " + Status)
    # click.echo("LMP Outcomes: ")
    # click.echo("" + str(round(model.results.lmp, 4)))

    if (Status is 'optimal'):
        with open(output.strip("'"), 'w') as f:

            f.write("SOLUTION_STATUS\n")
            f.write("optimal \t")
            f.write("\nEND_SOLUTION_STATUS\n")

            f.write("LMP\n")
            for h, r in model.results.lmp.iterrows():
                bn = 1
                for _, lmp in r.iteritems():
                    if lmp is None:
                        lmp = 0
                    f.write(str(bn) + ' : ' + str(h + 1) +' : ' + str(round(lmp,2)) +"\n")
                    bn = bn + 1
            f.write("END_LMP\n")

            f.write("GenCoResults\n")
            instance = model._model

            for g in instance.Generators.value:
                f.write("%s\n" % str(g).ljust(8))
                for t in instance.TimePeriods:
                    f.write("Interval: {}\n".format(str(t)))
                    f.write("\tPowerGenerated: {}\n".format(round(instance.PowerGenerated[g, t].value,4)))
            f.write("END_GenCoResults\n")

            if len(priceSenLoadData) is not 0:
                f.write("PSLResults\n")
                instance = model._model
                PriceSenLoadDemand = {}
                for l in instance.PriceSensitiveLoads.value:
                    for t in instance.TimePeriods:
                        PriceSenLoadDemand[(l, t)] = instance.PSLoadDemand[l, t].value

                for l in sorted(instance.PriceSensitiveLoads.value):
                    f.write("%s\n" % str(l).ljust(8))
                    for t in sorted(instance.TimePeriods):
                        f.write(" %d %6.4f \n" % ( t, PriceSenLoadDemand[(l, t)]))
                #print ('PriceSenLoadDemand = \n',PriceSenLoadDemand)
                f.write("END_PSLResults\n")

            f.write("VOLTAGE_ANGLES\n")
            for bus in sorted(instance.Buses):
                for t in instance.TimePeriods:
                    f.write('{} {} : {}\n'.format(str(bus), str(t), str(round(instance.Angle[bus, t].value,3))))
            f.write("END_VOLTAGE_ANGLES\n")

    elif (Status is 'infeasible'):
        with open(output.strip("'"), 'w') as f:
            f.write("SOLUTION_STATUS\n")
            f.write("infeasible \t")
            f.write("\nEND_SOLUTION_STATUS\n")


if __name__ == "__main__":
    cli()
