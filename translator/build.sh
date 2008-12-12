#! /bin/sh
# !! Please note, that you will need both jflex and java_cup 
#    installed on your machine in order to build the parser and lexer
cd src/translator/essencePrimeParser/
echo "### Generating EssencePrimeParser"
./build_parser.sh || exit 1 
cd ../../..
echo "### Generating XCSP functionals parser"
cd src/translator/xcsp2ep/mapper/functionalsParser/
./build_parser.sh || exit 1 
cd ../../../..
echo "### Compiling conjureEssenceSpecification, parser and Minion-part"
javac -classpath translator/essencePrimeParser/java-cup-11a.jar:. -d ../ translator/essencePrimeParser/*.java translator/expression/*.java translator/normaliser/*.java translator/tailor/*.java  translator/gui/*.java translator/solver/*.java translator/tailor/minion/*.java translator/tailor/gecode/*.java translator/xcsp2ep/parser/*.java translator/xcsp2ep/mapper/functionalsParser/*.java translator/xcsp2ep/mapper/*.java translator/xcsp2ep/*.java  translator/*.java || exit 1  
cd ../..
