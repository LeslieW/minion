# building Translator without rebuiliding the Parser
cd src
echo "### Compiling conjureEssenceSpecification, parser and translation-part"
javac -classpath translator/essencePrimeParser/java-cup-11a.jar:. -d ../ translator/essencePrimeParser/*.java translator/expression/*.java translator/normaliser/*.java  translator/*.java
cd ..
