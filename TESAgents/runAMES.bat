set ITDdir=YourLocationToParentDirectoryOfAMESV5
set AmesVersion=AMES-V5.0
set amesdir=%ITDdir%\%AmesVersion%\
set fncslibdir=%amesdir%\fncsDependencies
set apidir=%amesdir%\TESAgents
set logfilesdir=%apidir%\logfiles

set Param=MaxDay

for /f "tokens=1,2" %%a in (%amesdir%/DATA/%1.dat) do ( if %%a==%Param% set MaxDay=%%b )

set /a "tmax=%MaxDay%*86400"

md %logfilesdir% 2> nul

cd %amesdir%

set FNCS_FATAL=no
set FNCS_LOG_STDOUT=yes
set FNCS_LOG_LEVEL=DEBUG4
set FNCS_TRACE=no
set FNCS_CONFIG_FILE=%apidir%/YamlFiles/ames.yaml

start /b cmd /c java -jar -Djava.library.path=%fncslibdir% "%amesdir%/dist/%AmesVersion%.jar"^ > %logfilesdir%/ames.log 2^>^&1

cd %apidir%

set FNCS_FATAL=no
set FNCS_LOG_STDOUT=yes
set FNCS_LOG_LEVEL=
set FNCS_TRACE=no
start /b cmd /c fncs_broker 4 ^>%logfilesdir%/broker.log 2^>^&1

set FNCS_CONFIG_FILE=YamlFiles/tracer.yaml
start /b cmd /c fncs_tracer %tmax%s ^>%logfilesdir%/tracer.log 2^>^&1

set FNCS_CONFIG_FILE=YamlFiles/NetLoadForecastDAM.yaml
set FNCS_LOG_LEVEL=DEBUG4
start /b cmd /c python NetLoadForecastDAM.py %tmax% 1 ^>%logfilesdir%/NetLoadForecastDAM.log 2^>^&1


set FNCS_CONFIG_FILE=YamlFiles/NetLoadForecastRTM.yaml
set FNCS_LOG_LEVEL=
start /b cmd /c python NetLoadForecastRTM.py %tmax% 1 ^>%logfilesdir%/NetLoadForecastRTM.log 2^>^&1
