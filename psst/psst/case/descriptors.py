# Copyright (c) 2020, Battelle Memorial Institute
# Copyright 2007 - present: numerous others credited in AUTHORS.rst

from __future__ import print_function

import logging
import traceback
from builtins import super

import pandas as pd

logger = logging.getLogger(__name__)
logging.basicConfig()


class Descriptor(object):
    """ Descriptor Base class for psst case """
    name = None
    ty = None

    def __get__(self, instance, cls):
        try:
            return instance.__dict__[self.name]
        except KeyError:
            raise AttributeError("'{}' object has no attribute {}".format(instance.__class__.__name__, self.name))

    def __set__(self, instance, value):
        if self.ty is not None and not isinstance(value, self.ty):
            value = self.ty(value)
        if self._is_valid(instance, value):
            instance.__dict__[self.name] = value
        else:
            raise AttributeError('Validation for {} failed. Please check {}'.format(self.name, value))

    def __delete__(self, instance):
        raise AttributeError("Cannot delete attribute {}".format(self.name))

    @staticmethod
    def _is_valid(instance, value):
        return True


class IndexDescriptor(Descriptor):
    """ IndexDescriptor Base class for psst case """

    def __get__(self, instance, cls):
        try:
            index = self.get_attribute_index(instance)
            return index
        except AttributeError:
            super().__get__(instance, cls)

    def __set__(self, instance, value):
        if isinstance(value, pd.Series) or isinstance(value, list):
            value = pd.Index(value)
        elif isinstance(value, pd.DataFrame):
            # Assume the first column in the dataframe as index.
            value = pd.Index(value.iloc[:, 0].rename(self.name))

        try:
            self.set_attribute_index(instance, value)
        except AttributeError:
            logger.debug('AttributeError on instance.{} when setting index as {}'
                         .format(self.name.replace('_name', ''), self.name))
            logger.debug(traceback.format_exc())

        super().__set__(instance, value)

    def get_attribute_index(self, instance):
        raise AttributeError('IndexDescriptor does not have attribute')

    def set_attribute_index(self, instance, value):
        raise AttributeError('IndexDescriptor does not have attribute')


class Name(Descriptor):
    """ Name Descriptor for a case """
    name = 'name'
    ty = str


class Version(Descriptor):
    """ Version Descriptor for a case """
    name = 'version'
    ty = str


class BaseMVA(Descriptor):
    """ BaseMVA Descriptor for a case """
    name = 'baseMVA'
    ty = float


class Bus(Descriptor):
    """ Bus Descriptor for a case """
    name = 'bus'
    ty = pd.DataFrame


class BusName(IndexDescriptor):
    """ Bus Name Descriptor for a case

    Bus Name is used to set the index for bus dataframe
    Bus Name is also used to set the 'from bus', 'to bus' and 'gen bus' for the remaining data

    """
    name = 'bus_name'
    ty = pd.Index

    def get_attribute_index(self, instance):
        return instance.bus.index

    def set_attribute_index(self, instance, value):
        instance.branch['F_BUS'] = instance.branch['F_BUS'].apply(lambda x: value[value.get_loc(x)])
        instance.branch['T_BUS'] = instance.branch['T_BUS'].apply(lambda x: value[value.get_loc(x)])
        instance.gen['GEN_BUS'] = instance.gen['GEN_BUS'].apply(lambda x: value[value.get_loc(x)])

        bus_name = instance.bus.index
        if isinstance(bus_name, pd.RangeIndex) or isinstance(bus_name, pd.Index):
            logger.debug('Forcing string types for all bus names')
            bus_name = ['Bus{}'.format(b) for b in bus_name]
            instance.bus.index = bus_name

        try:
            instance.load.columns = [v for b, v in zip(instance.bus_name.isin(instance.load.columns), bus_name) if b == True]
        except ValueError:
            instance.load.columns = bus_name
        except AttributeError:
            instance.load = pd.DataFrame(0, index=range(0, 1), columns=bus_name, dtype='float')


class Branch(Descriptor):
    """ Branch Descriptor for a case """
    name = 'branch'
    ty = pd.DataFrame


class BranchName(IndexDescriptor):
    """ Branch Name Descriptor for a case

    Branch Name is used to set the index for the branch dataframe

    """
    name = 'branch_name'
    ty = pd.Index

    def get_attribute_index(self, instance):
        return instance.branch.index

    def set_attribute_index(self, instance, value):
        instance.branch.index = value


class Gen(Descriptor):
    """ Gen Descriptor for a case """
    name = 'gen'
    ty = pd.DataFrame


class GenCost(Descriptor):
    """ GenCost Descriptor for a case """
    name = 'gencost'
    ty = pd.DataFrame


class GenName(IndexDescriptor):
    """ Gen Name for a case """
    name = 'gen_name'
    ty = pd.Index

    def get_attribute_index(self, instance):
        try:
            if not all(instance.gen.index == instance.gencost.index):
                logger.warning('Indices for attributes `gen` and `gencost` do not match.'
                               + '`gen` index will be mapped to `gencost` index')
                instance.gencost.index = instance.gen.index
        except AttributeError:
            logger.debug('Unable to map `gen` indices to `gencost`')
        except ValueError:
            logger.debug('Unable to compare `gen` indices to `gencost`')
        return instance.gen.index

    def set_attribute_index(self, instance, value):
        instance.gen.index = value
        instance.gencost.index = value

        gen_name = instance.gen.index
        if isinstance(gen_name, pd.RangeIndex) or isinstance(gen_name, pd.Index):
            logger.debug('Forcing string types for all gen names')
            gen_name = ['GenCo{}'.format(g) for g in gen_name]
            instance.gen.index = gen_name
            instance.gencost.index = gen_name


class Load(Descriptor):
    name = 'load'
    ty = pd.DataFrame

    def __set__(self, instance, value):
        try:
            matching_indices = set(instance.bus_name).intersection(set(value.columns)) == set(value.columns)
        except:
            raise AttributeError("Unable to set load. Please check that columns in load match bus names")

        if matching_indices:
            super().__set__(instance, value)
        else:
            raise AttributeError("Unable to set load. Please check that columns in load match bus names")


class Period(IndexDescriptor):
    name = 'period'
    ty = pd.Index

    def get_attribute_index(self, instance):
        return instance.load.index

    def set_attribute_index(self, instance, value):
        instance.bus.index = value


class _Attributes(Descriptor):
    name = '_attributes'
    ty = list
