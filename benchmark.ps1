# benchmark_full.ps1

$runs = 5
$threadsList = @(1, 2, 4, 8)

# Prepare CSV files with headers
"Run,Time_ms" | Out-File sequential_raw.csv
"Threads,Run,Time_ms" | Out-File parallel_raw.csv

# Store times in-memory for averaging
$seqTimes = @()
$parallelTimes = @{}

foreach ($t in $threadsList) { $parallelTimes[$t] = @() }

Write-Host "=== Sequential Sobel Benchmark ==="
for ($i = 1; $i -le $runs; $i++) {
    $output = java SequentialSobel
    if ($output -match 'Sequential Time: ([\d\.]+) ms') {
        $time = [double]$matches[1]
        Write-Host "Run $i`: $time ms"
        $seqTimes += $time
        "$i,$time" | Out-File -Append sequential_raw.csv
    } else {
        Write-Host "Run $i`: Output format not recognized."
    }
}

Write-Host "`n=== Parallel Sobel Benchmark ==="
foreach ($threads in $threadsList) {
    Write-Host "`nThreads: $threads"
    for ($i = 1; $i -le $runs; $i++) {
        $output = java ParallelSobel $threads
        if ($output -match 'Parallel Time: ([\d\.]+) ms') {
            $time = [double]$matches[1]
            Write-Host "Run $i`: $time ms"
            $parallelTimes[$threads] += $time
            "$threads,$i,$time" | Out-File -Append parallel_raw.csv
        } else {
            Write-Host "Run $i`: Output format not recognized."
        }
    }
}

# Calculate averages and speed-ups
function Average($arr) { [math]::Round(($arr | Measure-Object -Average).Average, 2) }

$avgSeq = Average $seqTimes
Write-Host "`n=== Averages and Speed-ups ==="
Write-Host "Sequential average time: $avgSeq ms"

# Prepare summary CSV
"Threads,Avg_Time_ms,Speedup" | Out-File summary.csv

foreach ($threads in ,0 + $threadsList) {
    if ($threads -eq 0) {
        # Sequential baseline
        Write-Host "Threads: Sequential (baseline), Avg Time: $avgSeq ms, Speed-up: 1.00"
        "Sequential,$avgSeq,1.00" | Out-File -Append summary.csv
    } else {
        $avgPar = Average $parallelTimes[$threads]
        $speedup = [math]::Round($avgSeq / $avgPar, 2)
        Write-Host "Threads: $threads, Avg Time: $avgPar ms, Speed-up: $speedup"
        "$threads,$avgPar,$speedup" | Out-File -Append summary.csv
    }
}

