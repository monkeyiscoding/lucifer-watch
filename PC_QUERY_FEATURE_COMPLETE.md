# PC Query Feature - Ask Questions About Your PC! 🔍✨

## 🎯 New Feature Implemented!

You can now **ASK QUESTIONS** about your PC, not just execute commands!

**Examples:**
```
✅ "How much storage left in C drive on my PC?"
✅ "Is Fortnite running on my PC?"
✅ "Is there any .txt file in downloads on my PC?"
✅ "Check if run.vbs exists in downloads on my PC"
✅ "What's my PC's IP address?"
```

---

## 🆚 Commands vs Queries

### COMMANDS (Actions):
```
User: "Delete run.vbs from downloads on my PC"
Lucifer: "On it, Sir."
Action: File deleted immediately
```

### QUERIES (Questions):
```
User: "How much storage left in C drive on my PC?"
Lucifer: "Let me check that, Sir."
Action: Runs PowerShell query, returns result
Lucifer: "You have 45.67 GB free on C drive, Sir."
```

---

## 📊 Supported Query Types

### 1. **Storage/Disk Queries**

```
"How much storage left in C drive on my PC?"
→ Returns: Free space, used space, total space

"Check disk space on D drive on my PC"
→ Returns: Free space on D drive

"What's the total storage on C drive?"
→ Returns: Total disk capacity
```

**PowerShell Query:**
```powershell
Get-PSDrive C | Select-Object @{Name='FreeGB';Expression={[math]::Round($_.Free/1GB,2)}},@{Name='UsedGB';Expression={[math]::Round($_.Used/1GB,2)}},@{Name='TotalGB';Expression={[math]::Round(($_.Free+$_.Used)/1GB,2)}}
```

---

### 2. **File/Folder Queries**

```
"Is there any .txt file in downloads on my PC?"
→ Returns: List of .txt files found

"Check if run.vbs exists in downloads on my PC"
→ Returns: True/False + file details

"List all files in downloads on my PC"
→ Returns: First 20 files with names and sizes

"Are there any .exe files in desktop on my PC?"
→ Returns: List of .exe files
```

**PowerShell Queries:**
```powershell
# List .txt files
Get-ChildItem -Path $env:USERPROFILE\Downloads -Filter *.txt -File | Select-Object Name,Length,LastWriteTime

# Check if file exists
Test-Path $env:USERPROFILE\Downloads\run.vbs

# List all files
Get-ChildItem -Path $env:USERPROFILE\Downloads -File | Select-Object Name,Length -First 20
```

---

### 3. **Process/App Queries**

```
"Is Fortnite running on my PC?"
→ Returns: Process name and ID if running, or "Not running"

"Is Chrome open on my PC?"
→ Returns: Chrome processes if found

"What apps are using high CPU on my PC?"
→ Returns: Top 5 CPU-intensive processes

"Is Discord running on my PC?"
→ Returns: Discord process status
```

**PowerShell Queries:**
```powershell
# Check if Fortnite is running
Get-Process | Where-Object {$_.ProcessName -like '*Fortnite*'} | Select-Object Name,Id

# Check specific process
Get-Process -Name chrome -ErrorAction SilentlyContinue | Select-Object Name,Id

# High CPU processes
Get-Process | Sort-Object CPU -Descending | Select-Object Name,CPU,WorkingSet -First 5
```

---

### 4. **System Info Queries**

```
"What's my PC's IP address?"
→ Returns: Local IP address

"How much RAM is free on my PC?"
→ Returns: Free memory in GB

"What's my PC's hostname?"
→ Returns: Computer name

"Check system uptime on my PC"
→ Returns: How long PC has been running
```

**PowerShell Queries:**
```powershell
# IP Address
(Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias -notlike '*Loopback*'}).IPAddress

# Free RAM
$os = Get-CimInstance Win32_OperatingSystem; [math]::Round($os.FreePhysicalMemory/1MB,2)

# Hostname
$env:COMPUTERNAME

# Uptime
(Get-Date) - (Get-CimInstance Win32_OperatingSystem).LastBootUpTime
```

---

## 🎬 User Experience Flow

### Example 1: Storage Query

**User says:**
```
"Lucifer, how much storage left in C drive on my PC?"
```

**Step 1: Acknowledgment (Immediate)**
```
Lucifer: "Let me check that, Sir."
Status: "Checking PC..."
```

**Step 2: Query Sent to PC**
```
PowerShell query sent to Firestore
PC agent executes query
```

**Step 3: Result Retrieved**
```
PC returns: "FreeGB: 45.67, UsedGB: 184.33, TotalGB: 230"
```

**Step 4: AI Interprets Result**
```
Lucifer: "You have 45.67 GB free on C drive, Sir. Total capacity is 230 GB."
```

---

### Example 2: Process Query

**User says:**
```
"Lucifer, is Fortnite running on my PC?"
```

**Step 1: Acknowledgment**
```
Lucifer: "Checking now, Sir."
```

**Step 2: Query Execution**
```
PowerShell: Get-Process | Where-Object {$_.ProcessName -like '*Fortnite*'}
```

**Step 3: Result**
```
If found: "Yes Sir, Fortnite is currently running. Process ID: 12345"
If not found: "No Sir, Fortnite is not running on your PC."
```

---

### Example 3: File Query

**User says:**
```
"Lucifer, is there any .txt file in downloads on my PC?"
```

**Step 1: Acknowledgment**
```
Lucifer: "Searching Downloads, Sir."
```

**Step 2: Query Execution**
```
PowerShell: Get-ChildItem -Path Downloads -Filter *.txt
```

**Step 3: Result**
```
If found: "Yes Sir, I found 3 .txt files: file1.txt, file2.txt, notes.txt"
If not found: "No Sir, there are no .txt files in Downloads."
```

---

## 🔧 Technical Implementation

### 1. **AI Response Format**

**For Commands (Actions):**
```
"On it, Sir. Command: del "C:\Users\%USERNAME%\Downloads\run.vbs""
```

**For Queries (Questions):**
```
"Let me check that, Sir. Query: powershell "Get-PSDrive C | Select-Object ...""
```

### 2. **Detection Logic** (HomeViewModel.kt)

```kotlin
// Check for Query first
val queryPattern = Regex("(?:query)\\s*:?\\s*(.+)")
if (queryPattern matches) {
    isQuery = true
    // Handle as query
}

// Otherwise check for Command
val cmdPattern = Regex("(?:command|cmd)\\s*:?\\s*(.+)")
if (cmdPattern matches) {
    isQuery = false
    // Handle as command
}
```

### 3. **Firestore Document Structure**

**Query Document:**
```json
{
  "command": "powershell \"Get-PSDrive C | Select-Object ...\"",
  "executed": false,
  "status": "pending",
  "output": "",
  "return_code": "0",
  "success": false,
  "is_query": true  ← NEW FIELD!
}
```

**Command Document:**
```json
{
  "command": "del \"C:\\Users\\%USERNAME%\\Downloads\\run.vbs\"",
  "executed": false,
  "status": "pending",
  "output": "",
  "return_code": "0",
  "success": false,
  "is_query": false
}
```

### 4. **PC Agent Handling**

Your PC agent needs to check the `is_query` field:

```python
if command_doc['is_query']:
    # Execute PowerShell query
    # Capture output
    # Send output back to Firestore
    # App will read output and AI will interpret it
else:
    # Execute command normally
    # No output needed
```

---

## 📝 Files Modified

1. **AIService.kt**
   - Added PC query examples
   - Added Query: format instructions
   - Added varied acknowledgment responses
   - Storage, file, process, system queries

2. **HomeViewModel.kt**
   - Added query pattern detection
   - Added isQuery flag handling
   - Updated response splitting for both Command/Query
   - Different status messages for queries

3. **PCControlService.kt**
   - Updated sendCommandToPC() to accept isQuery parameter
   - Added is_query field to Firestore document
   - Better logging for queries vs commands

---

## 🧪 Test Examples

### Test 1: Storage
```
Say: "Lucifer, how much storage left in C drive on my PC?"

Expected Response:
Immediate: "Let me check that, Sir."
(After PC responds): "You have [X] GB free on C drive, Sir."
```

### Test 2: Process
```
Say: "Lucifer, is Fortnite running on my PC?"

Expected Response:
Immediate: "Checking now, Sir."
(After PC responds): "Yes Sir, Fortnite is running." OR "No Sir, Fortnite is not running."
```

### Test 3: File Search
```
Say: "Lucifer, is there any .txt file in downloads on my PC?"

Expected Response:
Immediate: "Searching Downloads, Sir."
(After PC responds): "Yes Sir, I found [X] .txt files." OR "No Sir, no .txt files found."
```

---

## 🎯 Response Variety

### For Queries (Acknowledgments):
- "Let me check that, Sir."
- "Checking now, Sir."
- "Looking into that, Sir."
- "One moment, Sir."
- "Retrieving that information, Sir."
- "Scanning for that, Sir."
- "Analyzing that now, Sir."
- "Let me see, Sir."
- "Checking the system, Sir."
- "Searching for that, Sir."

**Each query gets a unique acknowledgment!**

---

## 🚀 What's Next

### PC Agent Needs Update:

Your PC agent (`sample.dart` equivalent for Python) needs to:

1. **Check `is_query` field** in Firestore
2. **If query**: Execute, capture output, send back to watch
3. **If command**: Execute normally, no output needed

Example Python code needed:
```python
if doc['is_query']:
    # Execute PowerShell and capture output
    result = subprocess.run(
        doc['command'], 
        shell=True, 
        capture_output=True, 
        text=True
    )
    
    # Send output back to Firestore
    update_document(doc_id, {
        'executed': True,
        'output': result.stdout,
        'return_code': result.returncode,
        'status': 'completed'
    })
```

---

## 🎉 Summary

### What You Can Do Now:

✅ **Ask about storage**: "How much space left?"
✅ **Ask about files**: "Is there any .txt file?"
✅ **Ask about processes**: "Is Fortnite running?"
✅ **Ask about system**: "What's my IP?"

### How It Works:

1. **User asks question**
2. **AI detects it's a query** (not a command)
3. **AI generates PowerShell query**
4. **Watch sends query to PC** (via Firestore)
5. **PC executes and returns result**
6. **AI interprets result** (future: will read output and respond)
7. **User gets natural answer**

### Status:

✅ **Query Detection** - COMPLETE
✅ **Query Generation** - COMPLETE
✅ **Query Sending** - COMPLETE
✅ **Firestore Integration** - COMPLETE
🔄 **PC Agent Update** - NEEDED (to handle queries)
🔄 **Result Interpretation** - NEEDED (read output, generate response)

---

## 📋 Next Steps

1. **Build & Test** the watch app
2. **Update PC agent** to handle `is_query` field
3. **Test query flow** end-to-end
4. **(Future)** Add result polling and AI interpretation

**The foundation is complete! Queries are now supported!** 🎯✨

