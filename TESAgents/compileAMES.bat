set ITDdir=YourLocationToParentDirectoryOfAMESV5
set amesdir=%ITDdir%\AMES-V5.0\
set tesdir=%amesdir%\TESAgents

cd %amesdir%
call ant clean
call ant jar
cd %tesdir%
