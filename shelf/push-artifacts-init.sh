#!/bin/bash

######
# Run this script from the root directory of the git working copy
######

######
ROOT=$PWD

# If you use SSH in your config, you will need to manually set this to the HTTPS path
REMOTE_ORIGIN_FULL=$(git config --get remote.origin.url)

# The temporary folder where the orphan branch will be cloned
TEMP_CLONE_PATH=/tmp/artifacts
######

echo "✓ Create temp clone and checkout artifacts branch..."
rm -rf $TEMP_CLONE_PATH
mkdir $TEMP_CLONE_PATH
cd $TEMP_CLONE_PATH
git clone $REMOTE_ORIGIN_FULL $TEMP_CLONE_PATH --depth 1
git checkout --orphan artifacts

echo "✓ Remove old files from temp clone..."
cd $TEMP_CLONE_PATH
git rm -rf .
git clean -d -f

echo "✓ Create sample README.md..."
cat > $TEMP_CLONE_PATH/README.md <<'EOF'
Download, unzip, and open index.html to view reports.
EOF

echo "✓ Create sample index.html..."
cat > $TEMP_CLONE_PATH/index.html <<'EOF'
<html>
  <head><title>Reports</title></head>
  <style>body {font-family: Helvetica, Arial, sans-serif;line-height:160%;padding:20px}</style>
  <body>
    <h1>Shelf Reports</h1>

    <a href="files/coverage/html/index.html">JaCoCo Coverage Report</a><br/>
    JaCoCo helps you visually analyze test coverage.
        Lines coverage reflects the amount of code that has been exercised based on the number of Java byte code instructions called by the tests.
        Branches coverage shows the percent of exercised branches in the code – typically related to if/else and switch statements.
        Cyclomatic complexity reflects the complexity of code by giving the number of paths needed to cover all the possible paths in a code through linear combination.
        See the current configuration (included and excluded file patterns) in the repository at this path: ./shelf/checks.gradle
        <br/>More info on JaCoCo <a href="https://www.jacoco.org/jacoco/trunk/doc/counters.html">here</a>.
    <br/><br/>

    <a href="files/tests/jvmTest/index.html">Test Report</a><br/>
    Browse all executed tests and their results
    <br/><br/>

    <a href="files/detekt-report.html">Detekt Report</a><br/>
    Code smell analysis for Kotlin.
    Complexity report based on logical lines of code, McCabe complexity and amount of code smells
    Suppress findings with Kotlin’s @Suppress and Java’s @SuppressWarnings annotations
    See the current rule set and threshold configurations in the repository at this path: ./shelf/detekt.yml
    <br/>More info on Detekt <a href="https://arturbosch.github.io/detekt/">here</a>.
    <br/><br/>

    <a href="files/cpd/cpdCheck.text">CPD Report</a><br/>
    Flags similar or identical blocks of code.
    If the report file is empty, no blocks were found.
    See the current configuration in the repository at this path: ./shelf/checks.gradle
    <br/>More info on CPD <a href="https://pmd.github.io/latest/pmd_userdocs_cpd.html">here</a>.
    <p>
  </body>
</html>
EOF

echo "✓ Commit and push new files..."
ROOT_HASH=$(git rev-parse HEAD)
cd $TEMP_CLONE_PATH
git add .
#git commit -m "from push-artifacts-init.sh $ROOT_HASH"
#git push --set-upstream origin artifacts

echo "✓ Echo links to commit..."
REMOTE_ORIGIN=${REMOTE_ORIGIN_FULL%.git*}
NEW_HASH=$(git rev-parse HEAD)
echo "Web:" $REMOTE_ORIGIN/tree/$NEW_HASH
echo "Download:" $REMOTE_ORIGIN/archive/$NEW_HASH.zip
