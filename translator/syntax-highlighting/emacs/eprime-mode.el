(defvar eprime-mode-hook nil)

(defvar eprime-mode-map
  (let ((eprime-mode-map (make-keymap)))
    (define-key eprime-mode-map "\C-j" 'newline-and-indent)
    (define-key eprime-mode-map "\C-c \C-c" 'comment-region)
    eprime-mode-map)
  "Keymap for EPRIME major mode")

(add-to-list 'auto-mode-alist '("\\.eprime\\'" . eprime-mode))

(defface symbol-face '((t (:foreground "blue"))) "foo face")
(defvar symbol-face 'symbol-face)
(set-face-bold-p symbol-face t)

(defface paren-face '((t (:foreground "darkgreen"))) "paren face")
(defvar paren-face 'paren-face)
(set-face-bold-p paren-face t)

(defvar eprime-font-lock-keywords
   '(("forall" . font-lock-keyword-face)
     ("ESSENCE'" . font-lock-keyword-face)
     ("maximising" . font-lock-keyword-face)
     ("minimising" . font-lock-keyword-face)
     ("such that" . font-lock-keyword-face)
     ("letting" . font-lock-keyword-face)
     ("\\<be\\>" . font-lock-keyword-face)
     ("\\<of\\>" . font-lock-keyword-face)
     ("indexed by" . font-lock-keyword-face)
     ("matrix" . font-lock-type-face)
     ("domain" . font-lock-type-face)
     ("int" . font-lock-type-face)
     ("bool" . font-lock-type-face)
     ("given" . font-lock-keyword-face)
     ("find" . font-lock-keyword-face)
     ("\\\:" . symbol-face)
     ("\\\]" . paren-face)
     ("\\\[" . paren-face)
     ("\)" . paren-face)
     ("\(" . paren-face)
     ("\\\." . symbol-face)
     ("\\\," . symbol-face)
     ("\\\+" . symbol-face)
     ("!=" . symbol-face)
     ("\=" . symbol-face)
     ("\\\*" . symbol-face)
     ("\>" . symbol-face)
     ("\\\\" . symbol-face)
     ("\\\/" . symbol-face)
     )
   "Font-lock keywords for `my-new-mode'.
         See `font-lock-keywords' for a description of the format.")





;; (defconst eprime-font-lock-keywords-1
;;   (list
;;    '((concat "<" (regexp-opt '("forall" "matrix") t) ">") 
;;      . font-lock-keyword-face)
;;    '("\\('\\w*'\\)" . font-lock-builtin-face))
;; ;   '("\\<\\(matrix\\)>" . font-lock-builtin-face))
;;   "Minimal highlighting expressions for EPRIME mode")

;; (defvar eprime-font-lock-keywords eprime-font-lock-keywords-1
;;   "Default highlighting expressions for EPRIME mode")



(defun eprime-indent-line ()
  "Indent current line as WPDL code"
  (interactive)
  (beginning-of-line)

  (indent-line-to 3))




(defvar eprime-mode-syntax-table
  (let ((eprime-mode-syntax-table (make-syntax-table)))

;    (modify-syntax-entry ?_ "w" eprime-mode-syntax-table)
    (modify-syntax-entry ?$ "<" eprime-mode-syntax-table)
    (modify-syntax-entry ?\n ">" eprime-mode-syntax-table)


    eprime-mode-syntax-table)
  "Syntax table for eprime-mode")





;; (defun eprime-mode ()
;;   "Major mode for editing ESSENCE' files"
;;   (interactive)
;;   (kill-all-local-variables)
;;   (set-syntax-table eprime-mode-syntax-table)
;;   (use-local-map eprime-mode-map)

;;   (set (make-local-variable 'font-lock-defaults) '(eprime-font-lock-keywords))

;;   (set (make-local-variable 'indent-line-function) 'eprime-indent-line)  

;;   (setq major-mode 'eprime-mode)
;;   (setq mode-name "Essence'")
;;   (run-hooks 'eprime-mode-hook)
;;   (message "Essence' Mode Enabled!")
;; )


(define-derived-mode eprime-mode fundamental-mode "Essence'"
  "Major mode for editing Essence' files."
  (set (make-local-variable 'font-lock-defaults) '(eprime-font-lock-keywords))
  (set (make-local-variable 'indent-line-function) 'eprime-indent-line))

(provide 'eprime-mode)


