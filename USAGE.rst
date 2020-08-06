

Running AMES V5.0:

After installation instructions 1-6 have been carried out from INSTALLATION.rst file, the AMES V5.0 can be run using the following choices:

Choice 1: Users who intend to run AMES V5.0 without using FNCS can simply execute the command below from command line prompt
java -jar -Djava.library.path=%YourLocationForAmesV5%/fncsDependencies "%YourLocationForAmesV5%/dist/AMES-V5.0.jar"

Choice 2: Users who intend to run AMES V5.0 using FNCS needs to have several TES Agents setup as given in 'TESAgents' subdirectory and 
execute the command below from command line prompt

	C:/YourLocationForAmesV5/TESAgents/runAMES.bat

Note: For developers who intend to make modifications to AMES V5.0 they need to compile, using the command below, before running AMES V5.0.

	C:/YourLocationForAmesV5/TESAgents/compileAMES.bat


