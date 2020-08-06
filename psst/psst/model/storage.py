#This PSST file, originally due to Dheepak Krishnamurthy, has been modified by Swathi Battula to make the code consistent with AMES V5.0 documentation dated 7-2-2020. 


from pyomo.environ import *


def initialize_storage(model, storage_names=None, storage_at_bus=None):


    model.Storage = Set(initialize=storage_names)
    model.StorageAtBus = Set(model.Buses, initialize=storage_at_bus)


def maximum_minimum_storage_power_output(model, 
    minimum_power_output=None, 
    maximum_power_output=None):

    ####################################################################################
    # minimum and maximum power ratings, for each storage unit. units are MW.          #
    ####################################################################################

    # Storage power output >0 when discharging

    #\underbar{POS}_s
    model.MinimumPowerOutputStorage = Param(model.Storage, within=NonNegativeReals, default=0.0)

    def maximum_power_output_validator_storage(m, v, s):
       return v >= value(m.MinimumPowerOutputStorage[s])

    #\overbar{POS}_s
    model.MaximumPowerOutputStorage = Param(model.Storage, within=NonNegativeReals, validate=maximum_power_output_validator_storage, default=0.0)

    #Storage power input >0 when charging

    #\underbar{PIS}_s
    model.MinimumPowerInputStorage = Param(model.Storage, within=NonNegativeReals, default=0.0)

    def maximum_power_input_validator_storage(m, v, s):
       return v >= value(m.MinimumPowerInputStorage[s])

    #\overbar{PIS}_s
    model.MaximumPowerInputStorage = Param(model.Storage, within=NonNegativeReals, validate=maximum_power_input_validator_storage, default=0.0)

    ###############################################
    # storage ramp up/down rates (MW) during each time-step k #
    ###############################################

    # ramp rate limits when discharging
    #SORU_s
    model.ScaledRampUpLimitStorageOutput    = Param(model.Storage, within=NonNegativeReals)
    
	#SORD_s
    model.ScaledRampDownLimitStorageOutput  = Param(model.Storage, within=NonNegativeReals)

    # ramp rate limits when charging
    #SIRU_s
    model.ScaledRampUpLimitStorageInput     = Param(model.Storage, within=NonNegativeReals)
    
	#SIRD_s
    model.ScaledRampDownLimitStorageInput   = Param(model.Storage, within=NonNegativeReals)

    ####################################################################################
    # minimum state of charge (SOC) and maximum energy ratings, for each storage unit. #
    # units are MWh for energy rating and p.u. (i.e. [0,1]) for SOC     #
    ####################################################################################

    # you enter storage energy ratings once for each storage unit

    #\overbar{ES}_s
    model.MaximumEnergyStorage = Param(model.Storage, within=NonNegativeReals, default=0.0)
    #\underbar{SOC}_s
    model.MinimumSocStorage = Param(model.Storage, within=PercentFraction, default=0.0)

    ################################################################################
    # round trip efficiency for each storage unit given as a fraction (i.e. [0,1]) #
    ################################################################################

    #\eta_s
    model.EfficiencyEnergyStorage = Param(model.Storage, within=PercentFraction, default=1.0)

    ########################################################################
    # end-point SOC for each storage unit. units are in p.u. (i.e. [0,1])  #
    ########################################################################

    # end-point values are the SOC targets at the final time period. 

    #EPSOC_s
    model.EndPointSocStorage = Param(model.Storage, within=PercentFraction, default=0.5)

    ############################################################
    # storage initial conditions: SOC, power output and input  #
    ############################################################

    def t0_storage_power_input_validator(m, v, s):
        return (v >= value(m.MinimumPowerInputStorage[s])) and (v <= value(m.MaximumPowerInputStorage[s]))

    def t0_storage_power_output_validator(m, v, s):
        return (v >= value(m.MinimumPowerInputStorage[s])) and (v <= value(m.MaximumPowerInputStorage[s]))

    #\overbar{x}_s(0)
    model.StoragePowerOutputOnT0 = Param(model.Storage, within=NonNegativeIntegers, validate=t0_storage_power_output_validator, default=0)
    #\underbar{x}_s(0)
    model.StoragePowerInputOnT0  = Param(model.Storage, within=NonNegativeIntegers, validate=t0_storage_power_input_validator, default=0)
    #SOC_S(0)
    model.StorageSocOnT0         = Param(model.Storage, within=PercentFraction, default=0.5)
