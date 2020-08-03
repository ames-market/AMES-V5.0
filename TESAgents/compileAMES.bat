set ITDdir=%PathToDirectory%
set amesdir=%ITDdir%\AMES-V5.0\
set tesdir=%amesdir%\TESAgents

cd %amesdir%
call ant clean
call ant jar
cd %tesdir%