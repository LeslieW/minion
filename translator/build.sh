# bash
# build whole Translator (including parser)
# 
# !! Please note, that you will need both jflex and java_cup 
#    installed on your machine in order to build the parser and lexer

cd src/translator/essencePrimeParser/
echo "### Generating EssencePrimeParser"
./build_parser.sh
cd ..
cd ..
echo "### Compiling conjureEssenceSpecification, parser and Minion-part"
javac -classpath translator/essencePrimeParser/java-cup-11a.jar:. -d ../ translator/essencePrimeParser/*.java translator/minionModel/*.java translator/preprocessor/*.java translator/minionExpressionTranslator/*.java translator/*.java
cd ..
