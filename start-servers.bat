@echo off
echo ========================================
echo Starting Scenario-Based Pixel Game Engine
echo ========================================
echo.

echo Starting Backend Server...
start "Backend Server" cmd /k "cd backend && mvnw.cmd spring-boot:run"

echo Waiting 5 seconds for backend to initialize...
timeout /t 5 /nobreak > nul

echo Starting Frontend Server...
start "Frontend Server" cmd /k "cd frontend && npm run dev"

echo.
echo ========================================
echo Both servers are starting!
echo ========================================
echo Backend: http://localhost:8080
echo Frontend: http://localhost:5173
echo GraphiQL: http://localhost:8080/graphiql
echo.
echo Press any key to close this window...
pause > nul

