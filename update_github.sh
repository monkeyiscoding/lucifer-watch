#!/bin/bash

# Script to upload/update Lucifer2 project to GitHub
# Works for both initial upload AND regular updates

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Lucifer2 GitHub Sync Script${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Navigate to project directory
cd "$(dirname "$0")"
PROJECT_DIR=$(pwd)
echo -e "${GREEN}✓${NC} Working in: $PROJECT_DIR\n"

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo -e "${RED}✗ Error: git is not installed${NC}"
    echo "Please install git first: brew install git"
    exit 1
fi

# Check if this is a git repository
if [ ! -d .git ]; then
    echo -e "${YELLOW}→${NC} First-time setup: Initializing git repository..."
    git init
    git branch -M main
    echo -e "${GREEN}✓${NC} Git repository initialized\n"
else
    echo -e "${GREEN}✓${NC} Git repository exists\n"
fi

# Configure git user if not set
if [ -z "$(git config user.name)" ]; then
    echo -e "${YELLOW}→${NC} Git user not configured. Please enter your details:"
    read -p "Enter your name: " git_name
    read -p "Enter your email: " git_email
    git config user.name "$git_name"
    git config user.email "$git_email"
    echo -e "${GREEN}✓${NC} Git user configured\n"
fi

# Check current branch
BRANCH=$(git branch --show-current 2>/dev/null)
if [ -z "$BRANCH" ]; then
    BRANCH="main"
    git checkout -b main 2>/dev/null || true
fi

echo -e "${YELLOW}→${NC} Current branch: $BRANCH\n"

# Check for changes
echo -e "${YELLOW}→${NC} Checking for changes..."
git add .

if git diff --cached --quiet; then
    echo -e "${YELLOW}ℹ${NC} No changes to commit. Your repository is up to date!\n"

    # Check if we need to push anyway
    if git rev-parse --verify origin/$BRANCH &>/dev/null; then
        UNPUSHED=$(git log origin/$BRANCH..$BRANCH --oneline 2>/dev/null | wc -l | tr -d ' ')
        if [ "$UNPUSHED" -gt 0 ]; then
            echo -e "${YELLOW}→${NC} You have $UNPUSHED unpushed commit(s)"
            read -p "Push to GitHub? (y/n): " push_anyway
            if [ "$push_anyway" = "y" ] || [ "$push_anyway" = "Y" ]; then
                git push origin $BRANCH
                echo -e "${GREEN}✓${NC} Changes pushed to GitHub!\n"
            fi
        else
            echo -e "${GREEN}✓${NC} Everything is synced with GitHub!\n"
        fi
    fi

    echo -e "${GREEN}All done!${NC}"
    exit 0
fi

# Show summary of changes
CHANGED_FILES=$(git diff --cached --name-status | wc -l | tr -d ' ')
echo -e "${GREEN}✓${NC} Found $CHANGED_FILES file(s) with changes\n"

# Show a preview of changes (first 10 files)
echo -e "${YELLOW}→${NC} Changed files (showing first 10):"
git diff --cached --name-status | head -10
if [ $CHANGED_FILES -gt 10 ]; then
    echo "   ... and $(($CHANGED_FILES - 10)) more files"
fi
echo ""

# Ask for commit message
echo -e "${YELLOW}→${NC} Commit your changes"
read -p "Enter commit message: " commit_msg

if [ -z "$commit_msg" ]; then
    # Generate smart default message
    if git rev-parse --verify HEAD &>/dev/null; then
        commit_msg="Update project files ($(date '+%Y-%m-%d %H:%M'))"
    else
        commit_msg="Initial commit - Lucifer2 Voice Recorder App"
    fi
    echo -e "${YELLOW}ℹ${NC} Using default message: $commit_msg"
fi

git commit -m "$commit_msg"
echo -e "${GREEN}✓${NC} Commit created\n"

# Check if remote exists
if ! git remote | grep -q "origin"; then
    # First-time setup: add remote
    echo -e "${YELLOW}→${NC} First-time setup: Configure GitHub repository"
    echo "Please create a repository on GitHub if you haven't already."
    echo "Visit: https://github.com/new\n"
    read -p "Enter your GitHub repository URL: " repo_url

    if [ -z "$repo_url" ]; then
        echo -e "${RED}✗ Error: Repository URL cannot be empty${NC}"
        exit 1
    fi

    git remote add origin "$repo_url"
    echo -e "${GREEN}✓${NC} Remote 'origin' added\n"
fi

# Show remote info
REMOTE_URL=$(git remote get-url origin)
echo -e "${GREEN}✓${NC} GitHub remote: $REMOTE_URL\n"

# Determine if this is first push or update
if git ls-remote --exit-code --heads origin $BRANCH &>/dev/null; then
    echo -e "${YELLOW}→${NC} Updating existing repository..."
    PUSH_CMD="git push origin $BRANCH"
    IS_FIRST_PUSH=false
else
    echo -e "${YELLOW}→${NC} First push to GitHub..."
    PUSH_CMD="git push -u origin $BRANCH"
    IS_FIRST_PUSH=true
fi

# Push to GitHub
echo -e "${YELLOW}→${NC} Pushing to GitHub..."
echo ""

if $PUSH_CMD 2>&1 | tee /tmp/git_push.log; PUSH_STATUS=${PIPESTATUS[0]}; [ $PUSH_STATUS -eq 0 ]; then
    echo -e "\n${GREEN}✓${NC} Successfully synced with GitHub!\n"
    echo -e "${BLUE}========================================${NC}"
    echo -e "${GREEN}✓ Sync Complete!${NC}"
    echo -e "${BLUE}========================================${NC}\n"

    # Display repository URL
    WEB_URL=$(echo "$REMOTE_URL" | sed 's/\.git$//' | sed 's|git@github.com:|https://github.com/|')
    echo -e "View your project at:"
    echo -e "${BLUE}$WEB_URL${NC}\n"

    # Show statistics
    TOTAL_COMMITS=$(git rev-list --count HEAD 2>/dev/null || echo "1")
    echo -e "Repository Stats:"
    echo -e "  Commits: $TOTAL_COMMITS"
    echo -e "  Branch: $BRANCH"
    echo -e "  Files: $(git ls-files | wc -l | tr -d ' ')"
    echo ""
else
    PUSH_OUTPUT=$(cat /tmp/git_push.log 2>/dev/null)

    # Check if it's a push protection/secret issue
    if echo "$PUSH_OUTPUT" | grep -q "push declined due to repository rule violations\|GH013"; then
        echo -e "\n${RED}✗ GitHub Push Protection: Secret detected${NC}\n"
        echo -e "${YELLOW}GitHub's security feature detected a potential secret in your code.${NC}\n"

        # Try to extract the unblock URL
        UNBLOCK_URL=$(echo "$PUSH_OUTPUT" | grep -o "https://github.com/[^/]*/[^/]*/security/secret-scanning/unblock-secret/[^[:space:]]*" | head -1)

        if [ -n "$UNBLOCK_URL" ]; then
            echo -e "${BLUE}Option 1: Unblock the push${NC}"
            echo -e "1. Visit: ${BLUE}${UNBLOCK_URL}${NC}"
            echo -e "2. Click 'Allow' button"
            echo -e "3. Run this script again\n"
        fi

        echo -e "${BLUE}Option 2: Review and remove the secret${NC}"
        echo -e "1. Check which file contains the secret (see error above)"
        echo -e "2. Remove or replace the secret"
        echo -e "3. Run this script again\n"

        echo -e "${BLUE}Option 3: Create new repository${NC}"
        echo -e "1. Create new repo at: ${BLUE}https://github.com/new${NC}"
        echo -e "2. Run: ${BLUE}git remote set-url origin <new-repo-url>${NC}"
        echo -e "3. Run this script again\n"

        exit 1
    else
        # Other errors
        echo -e "\n${RED}✗ Failed to push to GitHub${NC}\n"
        echo -e "${YELLOW}Possible issues:${NC}"
        echo "  • Authentication failed - use a personal access token"
        echo "  • Repository doesn't exist - create it at https://github.com/new"
        echo "  • No write permission - check repository access"
        echo "  • Network connection issue"
        echo ""
        echo -e "${YELLOW}Tips:${NC}"
        echo "  • Generate token: https://github.com/settings/tokens"
        echo "  • Use token as password when prompted"
        echo "  • Check repository exists: $WEB_URL"
        echo ""
        exit 1
    fi
fi

# Optional: Remind about workflow for next time
if $IS_FIRST_PUSH; then
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  Next Time${NC}"
    echo -e "${BLUE}========================================${NC}\n"
    echo "To update your code in the future, simply run:"
    echo -e "${GREEN}./upload_to_github.sh${NC}"
    echo ""
    echo "The script will automatically:"
    echo "  • Detect your changes"
    echo "  • Ask for commit message"
    echo "  • Push to GitHub"
    echo ""
    echo "Or use these quick commands:"
    echo -e "${GREEN}git add . && git commit -m \"your message\" && git push${NC}"
    echo ""
fi

echo -e "${GREEN}All done!${NC}"

