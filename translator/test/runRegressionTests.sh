#!/bin/bash
# 
# Run regression tests for Tailor
# Andrea Rendl

TEST_DIR=test/regressionTests
XCSP_DIR=test/regressionTests/xcsp


# test the XCSP instances
cd ..
for X in `ls ${XCSP_DIR}/*.xml.bz2`;
  do	
  java -Xms128m -Xmx512m -jar tailor.jar -tf -dvr -silent -xcsp $X $X.minion
  Base=`echo ${X} | cut -f 1 -d'.'`
  Difference=`bzdiff "${X}.minion" "${Base}.xml.minion.expected.bz2"`  
  if [ "$Difference" != "" ]; then
      echo "ERROR in translating ${X}" && exit 1
  else
      echo "OK: translating ${X}"
      rm ${X}.minion
  fi
done
cd test/

# Start with the .cm files
cd ..
for X in `ls ${TEST_DIR}/*.cm`;
  do
  java -jar tailor.jar  -tf -silent -dvr -out $X.minion $X 
  Difference=`diff "${X}.minion" "${X}.minion.expected"`
  
  if [ "$Difference" != "" ]; then
      echo "ERROR in translating ${X}" && exit 1
  else 
      echo "OK: translating ${X}"
      rm ${X}.minion
  fi
done



# then move on to the .eprime files
for X in `ls ${TEST_DIR}/*.eprime`
  do
# if there is a parameter file
#echo "`ls ${X}*.param`"
parameters=`ls ${X}*.param 2> /dev/null`
if [ "$parameters" != "" ]; then 
    for Y in `ls ${X}*.param` 
      do
      java -jar tailor.jar  -tf -silent -dvr -out $Y.minion $X $Y 
      Difference=`diff "${Y}.minion" "${Y}.minion.expected"`
      if [ "$Difference" != "" ]; then
	  echo "ERROR in translating ${Y}" && exit 1
      else 
	  echo "OK: translating ${Y}"
	  rm ${Y}.minion
      fi
    done
    
# otherwise just run the .eprime file
    
else
    java -jar tailor.jar -tf -dvr -silent -out $X.minion $X 
    
    Difference=`diff "${X}.minion" "${X}.minion.expected"` 
    
    if [ "$Difference" != "" ]; then
	echo "ERROR in translating ${X}" && exit 1
    else 
	echo "OK: translating ${X}"
        rm ${X}.minion
    fi
    
fi
    
done
# go back to the initial directory
cd test



