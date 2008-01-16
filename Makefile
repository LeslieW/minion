all: quick-debug dynamic optimise debug generate 

mingwrelease: clean
	g++ --static minion/*.cpp -o bin/minion-debug.exe -DNO_PRINT
	g++ --static minion/*.cpp -o bin/minion.exe -DNO_DEBUG -O2 -finline-functions
	strip bin/*
	upx bin/*

quick-debug:
	g++ minion/*.cpp -o bin/minion-quick-debug.exe -g -D_GLIBCXX_DEBUG -DNO_PRINT -DQUICK_COMPILE -DWATCHEDLITERALS
debug:
	g++ minion/*.cpp -o bin/minion-debug.exe -g -D_GLIBCXX_DEBUG -DNO_PRINT -DWATCHEDLITERALS
optimise:
	g++ minion/*.cpp -o bin/minion.exe -DNO_DEBUG -O3 -ffast-math -finline-functions -fomit-frame-pointer -DWATCHEDLITERALS 
#replace above with lines like the following for particular machines:
#	g++ minion/*.cpp -o bin/minion.exe -DNO_DEBUG -O3 -ffast-math -finline-functions -fomit-frame-pointer -DWATCHEDLITERALS -march=pentium-m
#	g++ minion/*.cpp -o bin/minion.exe -DNO_DEBUG -O3 -ffast-math -finline-functions -fomit-frame-pointer -DWATCHEDLITERALS -march=pentium4

dodge:
	g++ minion/*.cpp -o bin/minion-dodge.exe -DNO_DEBUG -O2 -finline-functions -funit-at-a-time -DWATCHEDLITERALS -DQUICK_COMPILE

dynamic:
	g++ minion/*.cpp -o bin/minion-dynamic.exe -DNO_DEBUG -O2 -finline-functions -funit-at-a-time -DDYNAMICTRIGGERS


generate: bibd golomb solitaire steelmill sports

bibd:
	g++ generators/Bibd/MinionBIBDInstanceGenerator.cpp -O2 -o bin/bibd.exe
golomb:
	g++ generators/Golomb/GolombMinionGenerator.cpp -O2 -o bin/golomb.exe
solitaire:
	g++ generators/Solitaire/solitaire-solver.cpp -O2 -o bin/solitaire.exe
steelmill:
	g++ generators/Steelmill/steelmill-solver.cpp -O2 -o  bin/steelmill.exe
sports:
	g++ generators/SportsSchedule/MinionSportsInstanceGenerator.cpp -O2 -o bin/sports.exe

lisp-generate: minion-helper minion-sat minion-quasigroup

minion-helper: 
	clisp -x "(clisp-make-executable \"bin/minion-helper\")" -i generators/MinionHelper.lsp
minion-sat: 
	clisp -C -x "(clisp-make-executable \"bin/minion-sat\" (function clisp-toplevel-sat))" -i generators/MinionHelper.lsp -i generators/SAT/MinionDimacsSAT.lsp  
minion-quasigroup: 
	clisp -C -x "(clisp-make-executable \"bin/minion-quasigroup\" (function clisp-toplevel-quasigroup))" -i generators/MinionHelper.lsp -i generators/Quasigroup/MinionQuasigroup.lsp  

clean:
	touch bin/foo
	rm bin/*
