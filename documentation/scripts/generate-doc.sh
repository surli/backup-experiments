#!/bin/bash

#Define some variables

BASEDIR=`pwd`
NEXT_VERSION=`cat ${BASEDIR}/../pom.xml | grep '<version>.*SNAPSHOT' | sed 's/.*<version>\(.*\)-SNAPSHOT<\/version>/\1/'`

#first, clean up all existing files
rm $BASEDIR/html/*.html 2>/dev/null
rm -rf $BASEDIR/target/

echo ""
echo ""
echo "****************************************"
echo "*      Create target directory         *"
echo "****************************************"
echo ""
echo ""
#create target directory
mkdir target

echo ""
echo ""
echo "****************************************"
echo "*  Cloning Achilles.wiki/NextRealease  *"
echo "****************************************"
echo ""
echo ""
#checkout the wiki source
cd target
mkdir html
mkdir html/assets
mkdir pdf
cp $BASEDIR/html/achilles.css html/

git clone -b NextRelease https://github.com/doanduyhai/Achilles.wiki.git

#copy css to Achilles.wiki directory
cp $BASEDIR/html/achilles.css Achilles.wiki/

#copy assets to target html assets directory
cp $BASEDIR/target/Achilles.wiki/assets/* $BASEDIR/target/html/assets/



cd Achilles.wiki

#rename Home to Presentation
mv Home.md Presentation.md

#add titles to each file
for i in `ls *.md`
do
title=`echo $i | sed 's/\.md//'`
echo "*** " > temp.md
echo "# $title" >> temp.md
echo "" >> temp.md
cat $i >> temp.md
mv temp.md $i
done

#add title to the index file
mv _Sidebar.md index.md
echo "# Achilles Documentation" >> temp.md
echo " " >> temp.md
echo "### Table of contents" >> temp.md
echo " " >> temp.md
echo "<br/>" >> temp.md
tail -n +3 index.md >> temp.md
mv temp.md index.md

#change initial PDF title
echo "# Achilles" > temp.md
tail -n +3 Presentation.md >> temp.md
mv temp.md Presentation.md



#transform absolute URLs into relative
sed -i -r 's/https:\/\/github.com\/doanduyhai\/Achilles\/wiki\/([^#)]+)/\.\/\1\.html/g' *.md
sed -i -r 's/https:\/\/github.com\/doanduyhai\/Achilles\/wiki/\.\/Presentation\.html/' index.md

#replace all URL from Home.html to Presentation.html
sed -i -r 's/Home\.html/Presentation\.html/g' *.md

echo ""
echo ""
echo "****************************************"
echo "*    Generating HTML documentation     *"
echo "****************************************"
echo ""
echo ""
#execute conversion using pandoc
find . -name \*.md -type f -exec pandoc -R -c achilles.css -f markdown_github+raw_html -t html5 -o $BASEDIR/target/html/{}.html {} \;

#remove .md extension
cd $BASEDIR/target/html
rename "s/\.md\.html/\.html/" *.html

#replace all resource URL to point to local assets
sed -i -r 's/https:\/\/raw.github.com\/wiki\/doanduyhai\/Achilles\/assets\/(.+)/\.\/assets\/\1/' *.html


echo ""
echo ""
echo "****************************************"
echo "*    Generating PDF documentation      *"
echo "****************************************"
echo ""
echo ""
cd $BASEDIR/target/html
pandoc -f html -o $BASEDIR/target/pdf/Achilles-documentation.pdf `grep -Po 'a href="\./[^.]+\.html' index.html | sed 's/a href="\.\///' | uniq`

cd $BASEDIR/target

echo ""
echo ""
echo "***************************************************"
echo "* Creating zipped documentation for version $NEXT_VERSION *"
echo "***************************************************"
echo ""
echo ""


zip -9 -r achilles-"$NEXT_VERSION"-documentation.zip html/ pdf/ 1>/dev/null

mv achilles-"$NEXT_VERSION"-documentation.zip $BASEDIR/versions

