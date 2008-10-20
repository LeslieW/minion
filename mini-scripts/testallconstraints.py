#!/usr/bin/python
# Generate two minion input files, run them then compare dumptree outputs to 
# detect bugs in constraint propagators.

import sys, os, getopt
from constraint_test_common import *
import random
from sendemail import *

(optargs, other)=getopt.gnu_getopt(sys.argv, "", ["minion=", "numtests=", "email", "fullprop", "64bit", "procs="])

if len(other)>1:
    print "Usage: testallconstraints.py [--minion=<location of minion binary>] [--numtests=...] [--email] [--procs=...]"
    sys.exit(1)

# This one tests all the constraints in the following list.
conslist=[]


# equality constraints
conslist+=["diseq", "reifydiseq", "reifyimplydiseq", "eq", "reifyeq", "reifyimplyeq"]

# alldiffs
conslist+=["alldiff", "reifyalldiff",  "reifyimplyalldiff"]
conslist+=["gacalldiff", "reifygacalldiff", "reifyimplygacalldiff"]

# capacity constraints
conslist+=["gcc", "reifygcc", "reifyimplygcc"]

conslist+=["occurrence", "reifyoccurrence", "reifyimplyoccurrence"]
conslist+=["occurrenceleq", "reifyoccurrenceleq", "reifyimplyoccurrenceleq"]
conslist+=["occurrencegeq", "reifyoccurrencegeq", "reifyimplyoccurrencegeq"]

#element constraints
conslist+=["gacelement-deprecated", "reifyimplygacelement-deprecated"]
conslist+=["element", "reifyimplyelement", "watchelement", "reifyimplywatchelement"]

#non-reifiable arithmetic constraints
conslist+=["modulo", "reifyimplymodulo", "pow", "reifyimplypow", "minuseq", "reifyimplyminuseq"]
conslist+=["product", "reifyimplyproduct"]
conslist+=["div", "reifyimplydiv"]
conslist+=["abs", "reifyimplyabs"]


conslist+=["watchsumleq", "watchsumgeq", "watchvecneq", "watchvecexists_less", "watchvecexists_and", "hamming"]
conslist+=["reifyimplywatchsumleq", "reifyimplywatchsumgeq", "reifyimplywatchvecneq", "reifyimplywatchvecexists_less", "reifyimplywatchvecexists_and", "reifyimplyhamming"]

conslist+=["sumgeq", "reifysumgeq", "reifyimplysumgeq"]
conslist+=["sumleq", "reifysumleq", "reifyimplysumleq"]

conslist+=["weightedsumgeq", "reifyweightedsumgeq", "reifyimplyweightedsumgeq"]
conslist+=["weightedsumleq", "reifyweightedsumleq", "reifyimplyweightedsumleq"]

conslist+=["ineq", "reifyineq", "reifyimplyineq"]

# symmetry-breaking constraints

conslist+=["lexleq", "lexless", "reifylexleq", "reifylexless", "reifyimplylexleq", "reifyimplylexless"]

conslist+=["max", "min", "reifyimplymax", "reifyimplymin"]


conslist+=["watchneq", "watchless"]

conslist+=["difference"]

#todo
#conslist+=["weightedsumleq"...

def run_in_proc(numtests):
    testobj=eval("test"+consname+"()")
    testobj.solver=minionbin

    for testnum in range(numtests):
        options = {'reify': reify, 'reifyimply': reifyimply, 'fullprop': fullprop}
        if not testobj.runtest(options):
            sys.exit(1)
    sys.exit(0)

numtests=100
minionbin="bin/minion"
email=False
fullprop=False   # compare the constraint against itself with fullprop. Needs DEBUG=1.
bit64=False
procs=1
for i in optargs:
    (a1, a2)=i
    if a1=="--minion":
        minionbin=a2
    elif a1=="--numtests":
        numtests=int(a2)
    elif a1=="--email":
        email=True
    elif a1=="--fullprop":
        fullprop=True
    elif a1=="--64bit":
        bit64=True
    elif a1=="--procs":
        procs=int(a2)

for consname1index, consname1 in enumerate(conslist):
    print "Testing %s (%d/%d)"%(consname1, consname1index + 1, len(conslist))
    random.seed(12345)   # stupid seed but at least it makes the test repeatable.
    
    reify=False
    reifyimply=False
    consname=consname1
    if consname[0:10]=="reifyimply":
        reifyimply=True
        consname=consname[10:]
    
    if consname[0:5]=="reify":
        reify=True
        consname=consname[5:]
    consname=consname.replace("-", "__minus__")
    
    workers = []
    for procNum in range(procs):
        if procNum == procs - 1:
            num = (numtests // procs) + (numtests % procs)
        else:
            num = (numtests // procs)
        pid = os.fork()
        if pid:
            workers.append(pid)
        else:
            run_in_proc(num)
    
    for worker in workers:
        (pid, exitcode) = os.waitpid(worker, 0)
        if exitcode != 0:
            if email:
                mailstring="Mail from testallconstraints.py.\n"
                mailstring+="Problem with constraint %s. Run testconstraint.py %s on current SVN to replicate the test.\n"%(consname1, consname1)
                if fullprop:
                    mailstring+="Testing equivalence of -fullprop and normal propagation.\n"
                else:
                    mailstring+="Testing correctness against table representation.\n"
                if bit64:
                    mailstring+="Testing 64bit variant.\n"
                mailstring+="Using binary %s\n"%minionbin
                mail(mailstring)
            sys.exit(1)

# if we got here, send an email indicating success.
if email:
    mailstring="Mail from testallconstraints.py.\n"
    mailstring+="Using binary %s\n"%minionbin
    mailstring+="Tested the following constraints with no errors.\n"
    mailstring+=str(conslist)
    if bit64:
        mailstring+="Testing 64bit variant.\n"
    
    mail(mailstring, subject="Minion test successful.")
    

