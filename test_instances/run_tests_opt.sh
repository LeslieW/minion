e="../bin/minion.exe"
j=0
pass=0
for i in *.minion; do
j=$(($j + 1))
if $e -test $i &> /dev/null;
then pass=$(($pass + 1));
else 
  echo Fail $i;
fi
done
echo $pass of $j tests successful.
