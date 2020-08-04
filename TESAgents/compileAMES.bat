set ITDdir=C:\Users\swathi\Dropbox\AMESLatestVersion
set amesdir=%ITDdir%\AMES-V5.0\
set tesdir=%amesdir%\TESAgents

cd %amesdir%
call ant clean
call ant jar
cd %tesdir%