set loopcount=9
:loop
start java -jar ../caas_hashchain_client/build/libs/caas_hashchain_client-3.2.1-fat.jar
set /a loopcount=loopcount-1
if %loopcount%==0 goto exitloop
goto loop
:exitloop