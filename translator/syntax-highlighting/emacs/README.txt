===============================================
SYNTAX HIGHLIGHTING IN EMACS
===============================================
support: andrea@cs.st-and.ac.uk

Peter Gregory (peter.gregory@cis.strath.ac.uk)
provided an Emacs mode that produces syntax
highlighting for Essence' files that end with 
suffix .eprime in editor Emacs. 

Files
-----
eprime-mode.el		Emacs mode for Essence'


Installation
------------

You need to edit Emacs' config file in your 
home-directory at: ~/.emacs. If the files does 
not exist, create it. Copy the following lines 
into that file and change the path to wherever 
you put the file eprime-mode.el:

(setq auto-mode-alist (cons '("\\.eprime" . eprime-mode) auto-mode-alist))
(autoload 'eprime-mode "/path/to/where/you/put/eprime-mode" "Major mode for Essence'" t)
