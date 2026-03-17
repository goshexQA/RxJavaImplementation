Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "    RXJAVA PROJECT COMPILATION       " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Set encoding to UTF-8 for PowerShell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Compile all files
Write-Host "Compiling project..." -ForegroundColor Yellow
 = javac -d out -sourcepath src/main/java 
    src/main/java/rx/core/*.java 
    src/main/java/rx/schedulers/*.java 
    src/main/java/rx/operators/*.java 
    src/main/java/rx/subscription/*.java 
    src/main/java/rx/Demo.java 2>&1

if (1 -eq 0) {
    Write-Host "? Compilation successful!" -ForegroundColor Green
    
    # Run Demo
    Write-Host ""
    Write-Host "Running Demo..." -ForegroundColor Yellow
    Write-Host "-------------------------------------"
    java -cp out rx.Demo
    Write-Host "-------------------------------------"
    
    # Ask if user wants to run interactive tests
    Write-Host ""
     = Read-Host "Run interactive tests? (y/n)"
    if ( -eq 'y') {
        Write-Host ""
        Write-Host "Compiling InteractiveTest..." -ForegroundColor Yellow
        javac -d out -cp out InteractiveTest.java
        
        Write-Host "Running InteractiveTest..." -ForegroundColor Yellow
        Write-Host "-------------------------------------"
        java -cp out InteractiveTest
        Write-Host "-------------------------------------"
    }
} else {
    Write-Host "? Compilation failed!" -ForegroundColor Red
    Write-Host 
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
