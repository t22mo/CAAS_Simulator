set loopcount=9
:loop
start java -jar caas_hashchain_client.jar
set /a loopcount=loopcount-1
if %loopcount%==0 goto exitloop
goto loop
:exitloop