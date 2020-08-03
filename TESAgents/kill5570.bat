for /f "tokens=5" %%a in ('netstat -aon ^| find ":5570" ') do taskkill /f /pid %%a

