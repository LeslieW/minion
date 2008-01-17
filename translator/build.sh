# bash
# build whole Translator (including parser)
# 
# !! Please note, that you will need both jflex and java_cup 
#    installed on your machine in order to build the parser and lexer

cd translator/essencePrimeParser/
echo "### Generating EssencePrimeParser"
./build_parser.sh
cd ..
echo "### Compiling conjureEssenceSpecification, parser and Minion-part"
javac -classpath essencePrimeParser/java-cup-11a.jar:. -d ../bin/ conjureEssenceSpecification/*.java essencePrimeParser/*.java preprocessor/*.java minionModel/*.java minionExpressionTranslator/*.java  *.java
cd ..
