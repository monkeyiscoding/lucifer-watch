#!/bin/bash
# Quick Command Reference - GitHub Upload for Lucifer2

# ============================================
# STEP-BY-STEP QUICK START
# ============================================

# STEP 1: Make script executable (already done)
chmod +x upload_to_github.sh

# STEP 2: Run the upload script
cd /Users/ayush/StudioProjects/Lucifer2
./upload_to_github.sh

# Then follow the on-screen prompts!

# ============================================
# ALTERNATIVE: MANUAL COMMANDS
# ============================================

# Navigate to project
cd /Users/ayush/StudioProjects/Lucifer2

# Initialize git (if not already done)
git init

# Configure git user (first time only)
git config user.name "Your Name"
git config user.email "your.email@example.com"

# Stage all files
git add .

# Create commit
git commit -m "Initial commit - Lucifer2 Voice Recorder App"

# Add GitHub remote
git remote add origin https://github.com/monkeyiscoding/lucifer2.git

# Push to GitHub
git push -u origin main

# ============================================
# USEFUL GIT COMMANDS
# ============================================

# Check status
git status

# View staged changes
git diff --cached

# View all changes
git diff

# Check remote URLs
git remote -v

# Change remote URL
git remote set-url origin https://github.com/username/repo.git

# View commit history
git log --oneline

# Push with force (for rewriting history)
git push -u origin main --force

# Create a new branch
git checkout -b feature-branch

# Switch branch
git checkout main

# Merge branch
git merge feature-branch

# Delete local branch
git branch -d feature-branch

# ============================================
# TROUBLESHOOTING QUICK COMMANDS
# ============================================

# Check if git is installed
git --version

# Test GitHub authentication
ssh -T git@github.com

# View git configuration
git config --list

# Check remote connection
git remote show origin

# Fix line ending issues
git config core.autocrlf true

# Reset to last commit (discard changes)
git reset --hard HEAD

# Undo last commit (keep changes)
git reset --soft HEAD~1

# ============================================
# CLEANUP COMMANDS
# ============================================

# Remove untracked files (preview)
git clean -fdn

# Remove untracked files (actually delete)
git clean -fd

# Reset working directory
git checkout .

# Reset to specific commit
git reset --hard <commit-hash>

# ============================================
# HELPFUL RESOURCES
# ============================================

# View this file
cat QUICK_UPLOAD_REFERENCE.sh

# View detailed instructions
cat GITHUB_UPLOAD_INSTRUCTIONS.md

# View complete setup guide
cat GITHUB_SETUP_COMPLETE.md

# ============================================
# QUICK STATUS CHECK
# ============================================

echo "=== Git Status ==="
git status

echo "=== Remote Configuration ==="
git remote -v

echo "=== Recent Commits ==="
git log --oneline -5

echo "=== Current Branch ==="
git branch

# ============================================
# ONE-LINER COMMANDS
# ============================================

# Full setup in one command (if git is initialized)
git add . && git commit -m "Initial commit" && git push -u origin main

# Check everything before pushing
git status && git diff --cached

# View what would be pushed
git log origin/main..main

# See all branches
git branch -a

# Count commits
git rev-list --count HEAD

# Show project size
du -sh .git

# List all files in git index
git ls-files | wc -l

# ============================================
# NOTES
# ============================================

# • Always review changes with 'git status' before committing
# • Use meaningful commit messages
# • Never commit secrets or API keys
# • Test the script on a test repository first
# • Use --force only when rewriting history (dangerous!)
# • Backup important work before using --hard reset
# • Contact GitHub support if push protection blocks legitimate code

# ============================================
# RECOMMENDED WORKFLOW
# ============================================

# 1. Make changes to your code
# 2. Check what changed: git status
# 3. Review changes: git diff
# 4. Stage changes: git add .
# 5. Create commit: git commit -m "Clear message"
# 6. Push to GitHub: git push origin main
# 7. Verify on GitHub: https://github.com/username/repo

# ============================================
# For more help: ./upload_to_github.sh
# ============================================

