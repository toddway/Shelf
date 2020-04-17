#!/bin/bash

# run this from the root directory of your git working copy

TEMP_CLONE_PATH=/tmp/artifacts
ROOT=$PWD
ROOT_HASH=$(git rev-parse HEAD)
ROOT_URL_FULL=$(git config --get remote.origin.url)
ROOT_URL=${ROOT_URL_FULL%.git*}
ROOT_REPORTS_PATH=$ROOT/shelf/build/reports

echo "✓ Create temp clone of the artifacts branch..."
rm -rf $TEMP_CLONE_PATH
git clone $ROOT_URL_FULL $TEMP_CLONE_PATH --branch artifacts --single-branch

echo "✓ remove old files from temp clone..."
cd $TEMP_CLONE_PATH
git rm -rf ./files
git clean -d -f ./files

echo "✓ copy new files into temp clone..."
cp -a $ROOT_REPORTS_PATH/. $TEMP_CLONE_PATH/files

echo "✓ Commit and push new files..."
cd $TEMP_CLONE_PATH
git add .
git commit -m "from push-artifacts.sh $ROOT_HASH"
git push

echo "✓ Echo links to commit..."
cd $TEMP_CLONE_PATH
REPORT_HASH=$(git rev-parse HEAD)
echo "Web:" $ROOT_URL/tree/$REPORT_HASH
echo "Download:" $ROOT_URL/archive/$REPORT_HASH.zip
