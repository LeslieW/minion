# building Translator without rebuiliding the Parser
cd src
echo "### Compiling conjureEssenceSpecification, parser and translation-part"
javac -classpath essencePrimeParser/java-cup-11a.jar:. -d ../bin/ essencePrimeParser/*.java minionModel/*.java preprocessor/*.java minionExpressionTranslator/*.java *.java
cd ..
