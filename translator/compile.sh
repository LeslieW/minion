# building Translator without rebuiliding the Parser
cd src
echo "### Compiling conjureEssenceSpecification, parser and translation-part"
javac -classpath translator/essencePrimeParser/java-cup-11a.jar:. -d ../bin/ translator/essencePrimeParser/*.java translator/minionModel/*.java translator/preprocessor/*.java translator/minionExpressionTranslator/*.java translator/*.java
cd ..
