# building Translator without rebuiliding the Parser
cd src
echo "### Compiling conjureEssenceSpecification, parser and translation-part"
javac -classpath translator/essencePrimeParser/java-cup-11a.jar:. -d ../ translator/conjureEssenceSpecification/*.java translator/essencePrimeParser/*.java translator/expression/*.java translator/normaliser/*.java translator/tailor/*.java  translator/gui/*.java translator/solver/*.java translator/*.java
cd ..
