# This PSST file, originally due to Dheepak Krishnamurthy,
# has been modified by Swathi Battula to return solver termination condition.

import os
import warnings

from pyomo.common.tempfiles import TempfileManager
from pyomo.environ import SolverFactory

from .results import PSSTResults

PSST_WARNING = os.getenv('PSST_WARNING', 'ignore')


def solve_model(model, solver='glpk', solver_io=None, keep_files=True, verbose=True, symbolic_solver_labels=True,
                is_mip=True, mipgap=0.01):
    if solver == 'xpress':
        solver_instance = SolverFactory(solver, solver_io=solver_io, is_mip=is_mip)
    else:
        solver_instance = SolverFactory(solver, solver_io=solver_io)

    if is_mip and solver != 'ipopt':
        solver_instance.options['mipgap'] = mipgap

    with warnings.catch_warnings():
        warnings.simplefilter(PSST_WARNING)
        TempfileManager.tempdir = os.path.join(os.getcwd(), 'PyomoTempFiles')
        result = solver_instance.solve(model, suffixes=['dual'], tee=verbose, keepfiles=keep_files,
                                       symbolic_solver_labels=symbolic_solver_labels)
        termination_condition = str(result.solver.termination_condition)

    return model, termination_condition
