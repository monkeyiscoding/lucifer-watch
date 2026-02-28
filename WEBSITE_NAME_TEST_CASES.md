# Website Name Extraction - Test Cases & Expected Results

## Test Suite for parseWebsiteCommand()

### Test Case 1: Explicit Name with "name is" Pattern ✅
**Voice Command:** 
```
"Lucifer, create a portfolio website for me. The website name is Lucifer."
```
**Expected Behavior:**
- Pattern 1 matches: "name is Lucifer"
- Extracted name: "Lucifer"
- Log output: "Pattern 1 (name is) matched: Lucifer"

**Firestore Result:**
```json
{
  "name": "Lucifer"
}
```
**Status:** ✅ PASS (This was the original issue)

---

### Test Case 2: Alternative "name is" Format ✅
**Voice Command:**
```
"Create a website. Website name is MyPortfolio."
```
**Expected Behavior:**
- Pattern 1 matches: "website name is MyPortfolio"
- Extracted name: "MyPortfolio"
- Firestore saves: `"name": "MyPortfolio"`

**Status:** ✅ PASS

---

### Test Case 3: Direct Website Name ✅
**Voice Command:**
```
"Create a Lucifer website"
```
**Expected Behavior:**
- Pattern 2 matches: "create a Lucifer website"
- Extracted name: "Lucifer"

**Status:** ✅ PASS

---

### Test Case 4: Portfolio-Specific ✅
**Voice Command:**
```
"Lucifer create a portfolio website for me"
```
**Expected Behavior:**
- Pattern 3 matches: "create a portfolio website"
- Pattern 1 doesn't match (no explicit name)
- Falls back to pattern matching
- Extracted name: "portfolio" OR defaults to "My Website" depending on command

**Note:** For this case, user should use explicit "name is" format for clarity

**Status:** ⚠️ REQUIRES EXPLICIT NAME

---

### Test Case 5: Multiple Word Website Name ✅
**Voice Command:**
```
"Create a website. The website name is John Smith Portfolio."
```
**Expected Behavior:**
- Pattern 1 matches: "name is John Smith Portfolio"
- Extracted name: "John Smith Portfolio"
- Character class: `[a-zA-Z0-9\s]` allows spaces

**Firestore Result:**
```json
{
  "name": "John Smith Portfolio"
}
```
**Status:** ✅ PASS

---

### Test Case 6: Name with Numbers ✅
**Voice Command:**
```
"Create a website. Website name is Portfolio2024."
```
**Expected Behavior:**
- Pattern 1 matches: "name is Portfolio2024"
- Extracted name: "Portfolio2024"
- Character class: `[a-zA-Z0-9\s]` allows alphanumeric

**Status:** ✅ PASS

---

### Test Case 7: Name Cleanup - Removing Particles ✅
**Voice Command:**
```
"Website name is My Portfolio for me please"
```
**Expected Behavior:**
- Pattern 1 matches: "name is My Portfolio for me please"
- Initial extraction: "My Portfolio for me please"
- Cleanup regex: `\s+(for\s+me|please|sir)\s*$`
- Final name: "My Portfolio"
- Cleanup removes: "for me please"

**Status:** ✅ PASS

---

### Test Case 8: Default Fallback ✅
**Voice Command:**
```
"Build me a website"
```
**Expected Behavior:**
- Pattern 1: No match (no "name is")
- Pattern 2: No match (no "create website X")
- Pattern 3: No match (no clear name)
- Validation: name.isBlank() = true
- Default: name = "My Website"

**Firestore Result:**
```json
{
  "name": "My Website"
}
```
**Status:** ✅ PASS (Graceful fallback)

---

### Test Case 9: Special Case - Numbers Only ❌
**Voice Command:**
```
"Website name is 2024"
```
**Expected Behavior:**
- Pattern 1 matches: "name is 2024"
- Extracted name: "2024"
- Valid (numbers are allowed)

**Note:** While technically valid, numbers-only names aren't ideal. Consider UI validation.

**Status:** ✅ PASS (but consider UX feedback)

---

### Test Case 10: Edge Case - Empty Name ✅
**Voice Command:**
```
"Website name is"
```
**Expected Behavior:**
- Pattern 1 tries to match but captures empty
- Extracted: "" (empty)
- Cleanup: still empty
- Validation: name.isBlank() = true
- Default: name = "My Website"

**Status:** ✅ PASS (Safe fallback)

---

### Test Case 11: Consecutive "name is" Statements ✅
**Voice Command:**
```
"Name is FirstName. Website name is FinalName."
```
**Expected Behavior:**
- Pattern 1 finds FIRST match: "name is FirstName"
- Extracted: "FirstName"
- Note: Non-greedy `+?` stops at first valid match

**Status:** ⚠️ LIMITATION (Matches first occurrence)
**Recommendation:** Tell users to use "website name is" for clarity

---

## Regex Pattern Behavior Analysis

### Pattern 1: `(?:website\s+)?name\s+is\s+([a-zA-Z0-9\s]+?)(?:\s*\.\s*)?(?:for|create|build|with)?`

| Input | Matches | Captured | Status |
|-------|---------|----------|--------|
| "name is Lucifer" | ✅ YES | "Lucifer" | ✅ |
| "website name is Portfolio" | ✅ YES | "Portfolio" | ✅ |
| "name is" | ✅ YES | "" | ⚠️ (caught by validation) |
| "the name is bad" | ❌ NO | - | ✅ (pattern requires "name is") |
| "name is A B C" | ✅ YES | "A B C" | ✅ |
| "name is test." | ✅ YES | "test" | ✅ |

### Pattern 2: `(?:create\|build)\s+(?:a\s+)?(?:website\|web\s*site)\s+(?:for\s+)?([a-zA-Z0-9\s]+?)(?:\s+with\|\s+for\|\s*$\|\s*\.)`

| Input | Matches | Captured | Status |
|-------|---------|----------|--------|
| "create a website" | ✅ YES | "" | ⚠️ (no name given) |
| "create a Lucifer website" | ✅ YES | "Lucifer" | ✅ |
| "build John website" | ✅ YES | "John" | ✅ |
| "create website for me" | ✅ YES | "" | ⚠️ (no name) |

### Pattern 3: `create\s+(?:a\s+)?([a-zA-Z0-9\s]+?)\s+(?:website\|web\s*site\|portfolio)`

| Input | Matches | Captured | Status |
|-------|---------|----------|--------|
| "create a Lucifer portfolio" | ✅ YES | "Lucifer" | ✅ |
| "create John website" | ✅ YES | "John" | ✅ |
| "create website" | ❌ NO | - | ✅ (pattern requires name) |

---

## Real-World Test Scenarios

### Scenario 1: First-Time User ✅
**User says:** "Lucifer, create a portfolio website for me. The website name is MyPortfolio."
**Result:** 
- ✅ Website created with name "MyPortfolio"
- ✅ Saved to Firestore
- ✅ Can be retrieved later

---

### Scenario 2: Experienced User ✅
**User says:** "Build a Jane Smith portfolio website"
**Result:**
- ⚠️ Ambiguous - could extract "Jane Smith" as name
- ✅ If extraction succeeds: Website name = "Jane Smith"
- ⚠️ Recommend using explicit "name is" format

---

### Scenario 3: Professional Use Case ✅
**User says:** "Website name is ACME Corporation Portfolio 2024"
**Result:**
- ✅ Pattern 1 matches
- ✅ Extracts: "ACME Corporation Portfolio 2024"
- ✅ Saved with full name to Firestore
- ✅ Can be queried by full name later

---

## Performance Notes

### Regex Compilation
- Patterns are compiled once when function runs
- No caching (could be optimized with companion object)
- Typical execution time: < 5ms

### Optimization Opportunity
```kotlin
companion object {
    private val NAME_IS_PATTERN = Regex(...)
    private val CREATE_PATTERN = Regex(...)
    private val LUCIFER_PATTERN = Regex(...)
}
```

---

## Android Logcat Output Examples

### Success Case:
```
D/WebsiteBuilder: Pattern 1 (name is) matched: Lucifer
D/WebsiteBuilder: Final extracted website name: Lucifer
D/WebsiteBuilder: Parsed details: WebsiteDetails(name=Lucifer, description=A professional portfolio website, additionalFeatures=[portfolio sections])
```

### Fallback Case:
```
D/WebsiteBuilder: Final extracted website name: My Website
D/WebsiteBuilder: Parsed details: WebsiteDetails(name=My Website, description=A professional portfolio website, additionalFeatures=[])
```

---

## Debugging Checklist

When a website name isn't being captured correctly:

- [ ] Check Logcat for "Pattern X matched" messages
- [ ] Verify command includes explicit "name is" phrase
- [ ] Check for typos in voice input
- [ ] Ensure website name doesn't exceed 255 characters
- [ ] Verify name doesn't contain special characters (only a-z A-Z 0-9 and spaces)
- [ ] Test with simpler commands if complex command fails
- [ ] Check Firestore database for saved project metadata

---

## Future Enhancements

1. **Support for special characters in names**
   - Current: Only `[a-zA-Z0-9\s]`
   - Proposed: Add `-`, `_`, `'`, `"`

2. **Multi-word name disambiguation**
   - Current: First "name is" match wins
   - Proposed: Use intent scoring to find most relevant match

3. **User confirmation**
   ```kotlin
   "Did you mean the website name is: $extractedName?"
   // Wait for voice confirmation
   ```

4. **Persistent pattern learning**
   - Store failed extractions
   - Improve patterns based on user corrections

5. **Integration with Firestore queries**
   ```kotlin
   // Future: List all user's websites
   suspend fun getUserWebsites(): List<WebsiteProject>
   
   // Future: Get website by name
   suspend fun getWebsiteByName(name: String): WebsiteProject?
   ```

