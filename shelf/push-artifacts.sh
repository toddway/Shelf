#!/bin/bash

######
# Run this script from the root directory of the git working copy
######

######
ROOT=$PWD

# The folder where your artifacts are
ARTIFACTS_PATH=$ROOT/shelf/build/reports

# If you use SSH in your config, you will need to manually set this to the HTTPS path
REMOTE_ORIGIN_FULL=$(git config --get remote.origin.url)

# The temporary folder where the orphan branch will be cloned
TEMP_CLONE_PATH=/tmp/artifacts
######


echo "✓ Create temp clone of the artifacts branch..."
rm -rf $TEMP_CLONE_PATH
git clone $REMOTE_ORIGIN_FULL $TEMP_CLONE_PATH --branch artifacts --single-branch

echo "✓ remove old files from temp clone..."
cd $TEMP_CLONE_PATH
git rm -rf ./files
git clean -d -f ./files

echo "✓ Copy new files into temp clone..."
cp -a $ARTIFACTS_PATH/. $TEMP_CLONE_PATH/files

echo "✓ Commit and push new files..."
ROOT_HASH=$(git rev-parse HEAD)
cd $TEMP_CLONE_PATH
git add .
git commit -m "from push-artifacts.sh $ROOT_HASH"
git push

echo "✓ Echo links to commit..."
cd $TEMP_CLONE_PATH
REPORT_HASH=$(git rev-parse HEAD)
REMOTE_ORIGIN=${REMOTE_ORIGIN_FULL%.git*}
echo "Web:" $REMOTE_ORIGIN/tree/$REPORT_HASH
echo "Download:" $REMOTE_ORIGIN/archive/$REPORT_HASH.zip
