# !! Please note, that you will need both jflex and java_cup 
#    installed on your machine in order to build the parser and lexer
cd src/translator/essencePrimeParser/
echo "### Generating EssencePrime Parser"
./build_parser.sh
cd ..
cd xcsp2ep
cd mapper
cd functionalsParser
echo "### Generating Functionals Parser for XCSP format"
./build_parser.sh
cd ..
cd ..
cd ..
cd ..
echo "### Compiling TAILOR"
javac -classpath translator/essencePrimeParser/java-cup-11a.jar:. -d ../ translator/conjureEssenceSpecification/*.java translator/essencePrimeParser/*.java translator/expression/*.java translator/normaliser/*.java translator/tailor/*.java  translator/gui/*.java translator/solver/*.java translator/tailor/minion/*.java  translator/xcsp2ep/parser/*.java translator/xcsp2ep/mapper/functionalsParser/*.java translator/xcsp2ep/mapper/*.java translator/xcsp2ep/*.java translator/*.java
cd ..
