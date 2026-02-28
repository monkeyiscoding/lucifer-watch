#!/bin/bash

# Script to upload Lucifer2 project to GitHub
# This script will initialize git, commit files, and push to GitHub

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Lucifer2 GitHub Upload Script${NC}"
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

echo -e "${GREEN}✓${NC} Git is installed\n"

# Initialize git repository if not already initialized
if [ ! -d .git ]; then
    echo -e "${YELLOW}→${NC} Initializing git repository..."
    git init
    echo -e "${GREEN}✓${NC} Git repository initialized\n"
else
    echo -e "${GREEN}✓${NC} Git repository already exists\n"
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

# Check if .gitignore exists
if [ ! -f .gitignore ]; then
    echo -e "${YELLOW}→${NC} Creating .gitignore file..."
    cat > .gitignore << 'EOF'
# Android Studio
*.iml
.gradle
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
*.apk
*.ap_
*.dex
*.class
bin/
gen/
out/

# Gradle files
.gradle/
build/

# Local configuration file
local.properties

# Log Files
*.log

# Android Studio Navigation editor temp files
.navigation/

# Android Studio captures folder
captures/

# IntelliJ
*.iml
.idea/workspace.xml
.idea/tasks.xml
.idea/gradle.xml
.idea/assetWizardSettings.xml
.idea/dictionaries
.idea/libraries
.idea/caches
.idea/modules.xml
.idea/.name
.idea/compiler.xml
.idea/copyright/profiles_settings.xml
.idea/encodings.xml
.idea/misc.xml
.idea/vcs.xml
.idea/jsLibraryMappings.xml
.idea/datasources.xml
.idea/dataSources.ids
.idea/sqlDataSources.xml
.idea/dynamic.xml
.idea/uiDesigner.xml
.idea/dbnavigator.xml

# Keystore files
*.jks
*.keystore

# External native build folder generated in Android Studio 2.2 and later
.externalNativeBuild

# Google Services (e.g. APIs or Firebase)
# google-services.json

# Freeline
freeline.py
freeline/
freeline_project_description.json

# fastlane
fastlane/report.xml
fastlane/Preview.html
fastlane/screenshots
fastlane/test_output
fastlane/readme.md

# Version control
vcs.xml

# Misc
*.swp
*~
.DS_Store
EOF
    echo -e "${GREEN}✓${NC} .gitignore file created\n"
fi

# Add all files
echo -e "${YELLOW}→${NC} Adding files to git..."
git add .
echo -e "${GREEN}✓${NC} Files added\n"

# Check if there are any changes to commit
if git diff-index --quiet HEAD -- 2>/dev/null; then
    echo -e "${YELLOW}→${NC} No changes to commit"
else
    # Create commit
    echo -e "${YELLOW}→${NC} Creating commit..."
    read -p "Enter commit message (or press Enter for default): " commit_msg
    if [ -z "$commit_msg" ]; then
        commit_msg="Initial commit - Lucifer2 Voice Recorder App"
    fi
    git commit -m "$commit_msg"
    echo -e "${GREEN}✓${NC} Commit created\n"
fi

# Check if remote already exists
if git remote | grep -q "origin"; then
    echo -e "${GREEN}✓${NC} Remote 'origin' already exists"
    REMOTE_URL=$(git remote get-url origin)
    echo -e "   Current URL: $REMOTE_URL\n"
    read -p "Do you want to use this remote? (y/n): " use_existing
    if [ "$use_existing" != "y" ] && [ "$use_existing" != "Y" ]; then
        read -p "Enter new GitHub repository URL (e.g., https://github.com/username/repo.git): " repo_url
        git remote set-url origin "$repo_url"
        echo -e "${GREEN}✓${NC} Remote URL updated\n"
    fi
else
    # Prompt for GitHub repository URL
    echo -e "${YELLOW}→${NC} GitHub Setup"
    echo "Please create a new repository on GitHub first if you haven't already."
    echo "Visit: https://github.com/new\n"
    read -p "Enter your GitHub repository URL (e.g., https://github.com/username/Lucifer2.git): " repo_url

    if [ -z "$repo_url" ]; then
        echo -e "${RED}✗ Error: Repository URL cannot be empty${NC}"
        exit 1
    fi

    # Add remote
    git remote add origin "$repo_url"
    echo -e "${GREEN}✓${NC} Remote 'origin' added\n"
fi

# Determine the default branch name
BRANCH=$(git branch --show-current)
if [ -z "$BRANCH" ]; then
    BRANCH="main"
    git checkout -b main 2>/dev/null || git branch -M main
fi

echo -e "${YELLOW}→${NC} Current branch: $BRANCH\n"

# Push to GitHub
echo -e "${YELLOW}→${NC} Pushing to GitHub..."
echo "If prompted, enter your GitHub credentials or personal access token"
echo ""

# Capture output and exit code
if git push -u origin "$BRANCH" 2>&1 | tee /tmp/git_push.log; PUSH_STATUS=${PIPESTATUS[0]}; [ $PUSH_STATUS -eq 0 ]; then
    echo -e "\n${GREEN}✓${NC} Successfully pushed to GitHub!\n"
    echo -e "${BLUE}========================================${NC}"
    echo -e "${GREEN}✓ Upload Complete!${NC}"
    echo -e "${BLUE}========================================${NC}\n"

    # Extract repository URL for display
    REMOTE_URL=$(git remote get-url origin)
    WEB_URL=$(echo "$REMOTE_URL" | sed 's/\.git$//' | sed 's|git@github.com:|https://github.com/|')
    echo -e "Your project is now on GitHub:"
    echo -e "${BLUE}$WEB_URL${NC}\n"
else
    PUSH_OUTPUT=$(cat /tmp/git_push.log 2>/dev/null)

    # Check if it's a push protection/secret issue
    if echo "$PUSH_OUTPUT" | grep -q "push declined due to repository rule violations\|GH013"; then
        echo -e "\n${RED}✗ GitHub Push Protection: Secret detected in commit history${NC}\n"
        echo -e "${YELLOW}GitHub detected an exposed secret in the repository history.${NC}"
        echo -e "${YELLOW}This is a security feature to prevent accidental exposure of credentials.${NC}\n"

        # Try to extract the unblock URL from the output
        UNBLOCK_URL=$(echo "$PUSH_OUTPUT" | grep "https://github.com" | grep "secret-scanning" | head -1)

        if [ -n "$UNBLOCK_URL" ]; then
            echo -e "${BLUE}To unblock this push:${NC}"
            echo -e "1. Visit: ${BLUE}${UNBLOCK_URL}${NC}"
            echo -e "2. Click 'Allow' or 'Unblock' button"
            echo -e "3. Re-run this script or: ${BLUE}git push -u origin $BRANCH --force${NC}\n"
        else
            echo -e "${BLUE}To unblock this push:${NC}"
            echo -e "1. Go to: ${BLUE}https://github.com/$(git remote get-url origin | grep -oE '[^/]+/[^/]+' | cut -d'/' -f2)/security/secret-scanning${NC}"
            echo -e "2. Find the blocked push and click 'Allow' or 'Unblock'"
            echo -e "3. Re-run this script\n"
        fi

        echo -e "${YELLOW}Alternative: Create a new clean repository${NC}"
        echo -e "1. Create new repo at: ${BLUE}https://github.com/new${NC}"
        echo -e "2. Update the remote: ${BLUE}git remote set-url origin <new-repo-url>${NC}"
        echo -e "3. Push again: ${BLUE}git push -u origin $BRANCH${NC}\n"
    else
        echo -e "\n${RED}✗ Error: Failed to push to GitHub${NC}"
        echo -e "${YELLOW}Possible reasons:${NC}"
        echo "  1. Authentication failed - you may need to set up a personal access token"
        echo "  2. Repository doesn't exist on GitHub - create it first at https://github.com/new"
        echo "  3. You don't have write access to the repository"
        echo "  4. Network connection issue"
        echo ""
        echo -e "${YELLOW}To use a personal access token:${NC}"
        echo "  1. Generate token at: https://github.com/settings/tokens"
        echo "  2. Use it as password when prompted"
        echo ""
        echo -e "${YELLOW}Debug information:${NC}"
        echo "$PUSH_OUTPUT" | tail -10
        echo ""
    fi
    exit 1
fi

# Optional: Create a README if it doesn't exist
if [ ! -f README.md ]; then
    echo -e "${YELLOW}→${NC} README.md not found. Would you like to create one?"
    read -p "Create README.md? (y/n): " create_readme
    if [ "$create_readme" = "y" ] || [ "$create_readme" = "Y" ]; then
        cat > README.md << 'EOF'
# Lucifer2 Voice Recorder

An advanced Android voice recording application with intelligent features.

## Features

- Voice recording with silence detection
- Auto-start and auto-stop functionality
- File management and organization
- Conversation memory
- GitHub integration
- And many more...

## Setup

1. Clone the repository
2. Open in Android Studio
3. Build and run on your Android device

## Requirements

- Android Studio
- Android SDK
- Minimum Android API level as specified in build.gradle

## License

[Specify your license here]
EOF
        git add README.md
        git commit -m "Add README.md"
        git push origin "$BRANCH"
        echo -e "${GREEN}✓${NC} README.md created and pushed\n"
    fi
fi

echo -e "${GREEN}All done! Your project is now on GitHub.${NC}"


