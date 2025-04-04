;;; -*- Mode: LISP; Syntax: ANSI-Common-Lisp; Base: 10; Package: CYC;  -*-

;;; Cyc system parameters
;;;
;;; Generated 05/13/2002 10:07:16 from CycL 1.2360

(in-package :CYC)

(cvs-id "$Id: parameters.lisp,v 1.32 2008/05/22 19:18:48 daves Exp $")

;;; Possible values: NIL, T.  If NIL, transcript problems will cause error
;;; breaks that make the system stop.  If T, such problems will not cause
;;; breakage.
(csetq *AUTO-CONTINUE-TRANSCRIPT-PROBLEMS* T)

;;; Possible values: NIL, T.  If NIL, agenda errors will cause the system to
;;; halt.  If T, they will be automatically continued.
(csetq *CONTINUE-AGENDA-ON-ERROR* T)

;;; Possible values: T, NIL.  Type checking occurs in SBHL modules iff this
;;; is nil.
(csetq *SUSPEND-SBHL-TYPE-CHECKING?* NIL)

;;; Possible values: T, NIL.  If NIL, the System Info page (accessible to
;;; administrators only) will estimate, rather than actually count, the
;;; number of operations in the transcript.  If T, it will actually count
;;; them, which takes longer but is accurate.
(csetq *REALLY-COUNT-TRANSCRIPT-OPS* NIL)

;;; Possible values: NIL, T.  If NIL, a local transcript will always be
;;; written when operations are done, even if those operations are also being
;;; written to the master transcript.  If T, then the image does not write
;;; to a local transcript file, and will write to the master transcript when
;;; it is set to transmit operations.  This allows an image that is standalone,
;;; and always in :TRANSMIT-AND-RECEIVE, to keep only a single copy of its
;;; operations.
(csetq *DONT-RECORD-OPERATIONS-LOCALLY* NIL)

;;; Possible values -- :TRANSMIT-AND-RECEIVE, :RECEIVE-ONLY,
;;; :ISOLATED, :DEAF, :DEAD.  This is the communication mode the cyc image
;;; should get initialized to at startup.
(csetq *STARTUP-COMMUNICATION-MODE* :DEAF)

;;; Possible values: T, NIL.  If NIL, the Cyc agenda is not started at
;;; startup, but can be enabled later by the user.  If T, the agenda is enabled
;;; at startup.
(csetq *START-AGENDA-AT-STARTUP?* T)

;;; The base port offset for all the TCP services for the Cyc image.
(csetq *BASE-TCP-PORT* 3600)

;;; Possible values: A number.  This parameter specifies the offset of the html port
;;; from *base-tcp-port*.
(csetq *HTML-PORT-OFFSET* 0)

;;; Possible values: A number.  This parameter specifies the offset of the Cyc API
;;; (application program interface) service from *base-tcp-port*.
(csetq *FI-PORT-OFFSET* 1)

;;; Possible values: A number.  This parameter specifies the offset of the http port
;;; from *base-tcp-port*.
(csetq *HTTP-PORT-OFFSET* 2)

;;; Possible values: A number.  This parameter specifies the offset of the Cyc
;;; CFASL-server service from *base-tcp-port*.
(csetq *CFASL-PORT-OFFSET* 14)

;;; Possible values: T, NIL.  IF T, then remote TCP clients can connect to Cyc, otherwise no remote connections are allowed. The most secure configuration leaves this parameter NIL, and uses a separate web server to redirect HTTP requests to Cyc.
(csetq *TCP-LOCALHOST-ONLY?* NIL)

;;; Possible values: T, NIL.  IF T, then API functions can access host services including the file system and outbound tcp connections.  The most secure configuration leaves this parameter NIL.
(csetq *PERMIT-API-HOST-ACCESS* T)

;;; Possible values: T, NIL.  IF T, then writing to the master transcript will be controlled by the Cyc Transcript Server, which will need to be installed at your site.  You only need to set this to T if you are running multiple instances of Cyc.  If NIL, then Cyc will read and write to the master transcript without regard to other processes doing the same.
(csetq *USE-TRANSCRIPT-SERVER* T)

;;; Possible values: NIL or a string.  This parameter is only used if *USE-TRANSCRIPT-SERVER* is T.  If so, then this parameter should be set to the name of the host offering the
;;; cyc-serializer service.
(csetq *MASTER-TRANSCRIPT-LOCK-HOST* "TRANSCRIPT-SERVER.CYC.COM")

;;; Possible values: A number.  This parameter is only used if
;;; *USE-TRANSCRIPT-SERVER* is T.  If so, then this
;;; parameter should be set to the port number of the cyc-serializer
;;; read service.
(csetq *MASTER-TRANSCRIPT-SERVER-PORT* 3608)

;;; Possible values: T, NIL.  If NIL, tools for modifying the knowledge base are not accessible.
(csetq *CB-EDITING-ENABLED?* T)

;;; Possible values: T, NIL.  If NIL, require authentication before allowing
;;; modifications to the knowledge base.  If T, any user is allowed to
;;; modify the knowledge base.
(csetq *ALLOW-GUEST-TO-EDIT?* NIL)

;;; Possible values: The name of a constant representing a Cyclist.  This is the
;;; default Cyclist initially logged into the system.
(csetq *DEFAULT-CYCLIST-NAME* "Guest")

;;; The directory under which documents served by the HTTP server are stored.
(csetq *HTTP-HTDOCS-DIRECTORY* "httpd/htdocs")

;;; The directory under which Cyc images (.gif or otherwise) are stored
(csetq *HTML-IMAGE-DIRECTORY* "/cycdoc/img/")

;;; The directory under which Javascript files used by the browser are stored
(csetq *HTML-JAVASCRIPT-DIRECTORY* "/cycdoc/js/")

;;; The directory under which CSS (Cascading StyleSheet)  files used by the browser are stored
(csetq *HTML-CSS-DIRECTORY* "/cycdoc/css/")

;;; Possible values: T, NIL.  If T, the html tools will correctly display UTF-8 text
;;; derived from Cyc strings.
(csetq *PERMIT-UTF-8-CHARACTER-DISPLAY* NIL)

;;; The URL for the Cyc system documentation directory.
(csetq *CYC-DOCUMENTATION-URL* "/cycdoc/")

;;; Possible values: A string.  The name of the CGI program that acts as the
;;; intermediary between your WWW server and a Cyc processs.  Normally, the
;;; program will be called ``cg''.  However, if your WWW server requires that
;;; CGI program names have a certain form, such as ``cg.exe'', then change
;;; this parameter to conform.
(csetq *CYC-CGI-PROGRAM* "cg")

;;; Possible values: NIL, T.  If T, the HTML browser allows users to  search for
;;; constants via regular expressions.  Use of this facility requires the
;;; constant-name-grep CGI program and the data file constant-shell.text to
;;; be installed on your WWW server.
(csetq *CONSTANT-NAME-GREP-ENABLED* T)

;;; Possible values: A string.  The name of the CGI program that is used for
;;; performing regular expression searches over constant names.  Normally, the
;;; program will be called ``constant-name-grep''.  However, if your WWW server
;;; requires that CGI program names have a certain form, such as
;;; ``constant-name-grep.exe'', then change this parameter to conform.
(csetq *CYC-GREP-CGI-PROGRAM* "constant-name-grep")

;;; Possible values: One of the symbols :CYCORP or :UNKNOWN. If the execution context
;;; is set to :CYCORP, then the CYC image can assume that it is running in
;;; Cycorp's development environment and make strong assumptions about setup
;;; and infrastructure.
(csetq *CYC-EXECUTION-CONTEXT* :CYCORP)

;;; Possible values: T, NIL.   If T, testing features of the thesaurus
;;; interface will be enabled.  There is only one such feature at present: the
;;; ability of Administrator users to switch among user levels, via a link on
;;; the Preferences page.  If NIL, these testing features are not
;;; available.
(csetq *TM-TESTING* NIL)

;;; Possible values:  T, NIL.  If NIL, normal thesaurus editing is allowed.
;;; If T, globally disables all editing, as for a production installation.
(csetq *TM-DISABLE-EDITING-GLOBALLY* NIL)

;;; Possible values: T, NIL.  If T, the interface will prompt users for a
;;; password at logon time.  If NIL, no password is required.  This will be
;;; ignored if *use-http-authentication* is non-NIL.
(csetq *TM-REQUIRE-PASSWORD?* NIL)

;;; Possile values: T, NIL.  If T, the TM will assume users are already
;;; authenticated by an external mechanism.  In that case, the thesaurus
;;; administrator needs to add new users by using the same ID the
;;; external authentication system will provide.
;;; IMPORTANT:  If *USE-HTTP-AUTHENTICATION* is T, you must customize the
;;; Bootrap Administrator Name, and Bootstrap Administrator login.
(csetq *USE-HTTP-AUTHENTICATION* NIL)

;;; Possible values: NIL, or a string.  If *USE-HTTP-AUTHENTICATION* is T,
;;; this must be set to a string which is the user ID for the initial
;;; administrator.  It must be the same user ID the external authentication
;;; system uses.
(csetq *TM-BOOTSTRAP-ADMINISTRATOR-LOGIN* NIL)

;;; Possible values: NIL, or a string.  If *USE-HTTP-AUTHENTICATION* is T,
;;; this should be set to a string which is the full name of the initial
;;; administrator.
(csetq *TM-BOOTSTRAP-ADMINISTRATOR-NAME* NIL)

;;; Possible values: NIL, T.  If NIL, thesaurus operations will not be saved.
;;; If T, they will be saved to the transcript.
(csetq *TM-DEFAULT-SAVE-OPERATIONS-VALUE* T)

;;; Possible values: A string.  Should be the URL for the homepage link on
;;; the welcome page of the interface.
(csetq *CLIENT-HOME-PAGE* "http://www.cyc.com/")

;;; Possible values: A string.  It should be the pathname of an image file,
;;; expressed as relative to the directory where your web server looks for
;;; documents (often htdocs).
(csetq *CLIENT-LOGO* "/cycdoc/img/cyc-logo-75.gif")

;;; Possible values: NIL, or a string.  Should be the URL for the thesaurus homepage
;;; link found on almost every thesaurus page.
(csetq *CLIENT-THES-HOME-PAGE* "http://www.cyc.com/")

;;; Possible values: NIL, or a string.  It should be the pathname of an image file,
;;; expressed as relative to the directory where your web server looks for
;;; documents (often htdocs).
(csetq *CLIENT-MINI-LOGO* "/cycdoc/img/cyc-logo-tiny.gif")

;;; Possible values: A string, giving the name of the client organization.
(csetq *CLIENT-NAME* "Cycorp, Inc.")

;;; Possible values: A string, such as "Joe Smith at 123-4567" or
;;; "Joe Smith at joe@com.com", giving contact information for the person or
;;; office that will be handling user support at your site.
(csetq *SUPPORT-STRING* "your thesaurus administrator")

;;; Possible values: If customized, a list.  Each list element corresponds to
;;; one contact person.  List elements should be lists containing two strings
;;; each -- the contact name, followed by the contact email address.  For example,
;;; '(("John Thomas" "john@com.com") ("Jane Smith" "jane@com.com"))
(csetq *TM-CONTACTS* "your thesaurus administrator")

;;; Possible values: A string, giving the name of the application.
(csetq *APPLICATION-NAME* "Cycorp Thesaurus")

;;; Possible values: A string, giving the name of the thesaurus manager
;;; tool
(csetq *TOOL-NAME* "TM")

;;; Possible values: A string, giving instructions to the user on the login
;;; page.  If *USE-HTTP-AUTHENTICATION* is turned on, this message will not
;;; appear.
(csetq *CLIENT-SPECIAL-LOGIN-INSTRUCTIONS* "Please enter your Thesaurus Cyclist ID in the box below, and hit ENTER or click the
LOGIN button.")

;;; Possible values: An integer.  This is the number of seconds to wait
;;; before writing out information for the readable log of operations.  This
;;; is separate from the transcript.
(csetq *TM-EVENT-LOG-SAVE-QUANTUM* 300)

;;; Possible values: T, NIL.  If T, the application loads the read transcript
;;; completely when the thesaurus init file is loaded.  (If
;;; *tm-load-thesaurus-init-file-at-startup* is set to T, this will be
;;; at startup time.)  In a C image, this means the application won't
;;; become available for HTML requests until it is caught up with the
;;; transcript.  If NIL, the read transcript isn't run at thesaurus
;;; init file load time, but may be processed later by the agenda if
;;; that is enabled.
(csetq *TM-LOAD-TRANSCRIPT-AT-STARTUP* NIL)

;;; Possible values: T, NIL.  If T, the application will load the thesaurus init
;;; file at startup time.  If NIL, it will wait until the first user attempts to
;;; connect via a web server.  This parameter must be set to T for a
;;; thesaurus-only installation.  Note that it must be set to T for
;;; *tm-load-transcript-at-startup* to result in the transcript being
;;; loaded at startup.
(csetq *TM-LOAD-THESAURUS-INIT-FILE-AT-STARTUP* NIL)

;;; Possible values: NIL, or a string which is the pathname to a special directory where
;;; user preferences are kept for the thesaurus application.  If allowed to default
;;; to NIL, preferences will be kept in a directory relative to the directory from
;;; which Cyc is launched: ./data/thesaurus/preferences/ (as a string).  If you have
;;; several instances of Cyc running at one time, and you want them to look in the
;;; same place for user preferences, set this variable to that directory.
(csetq *TM-SPECIAL-PREFERENCES-DIRECTORY* NIL)

;;; Possible values: NIL, or a string which is the pathname to a special directory where
;;; thesaurus data files are kept.  If allowed to default to NIL, the Thesaurus Manager
;;; will look in the relative directory ./data/thesaurus/load-thesaurus/ when it is asked to
;;; load a thesaurus.  If you have several instances of Cyc running at one time, and you
;;; want them to look in a single place for thesaurus data files, set this variable to that
;;; directory.
(csetq *TM-SPECIAL-LOAD-THESAURUS-DIRECTORY* NIL)

;;; Possible values: NIL, or a string which is the pathname to a special directory
;;; where the results of Output Thesaurus commands will be written.  If allowed to
;;; default to NIL, the Thesaurus Manager will write in a directory relative to the
;;; place where the program was launched (./data/thesaurus/output/) when outputting
;;; thesauri.
(csetq *TM-SPECIAL-OUTPUT-DIRECTORY* NIL)

;;; Possible values: NIL, or a string.  If non-NIL, should be a the name of the
;;; default thesaurus users should initially have as their viewing preference.  If NIL,
;;; users will not have active thesauri set until they go to the Preferences page and
;;; do so themselves.
(csetq *TM-DEFAULT-THESAURUS-NAME* NIL)

;;; Possible values: an integer greater than or equal to 2.  This tells the system how
;;; many incremental binary images to keep on hand when the Thesaurus Manager saves a
;;; memory snapshot.  Such saves are done, for example, at the end of a Batch Load Thesaurus
;;; operation and also, at the discretion of the Thesaurus Administrator, from the Utilities
;;; page.  When a memory snapshot is saved, if there are already this number of incremental
;;; saves in the ./world/ directory, the oldest one is deleted to make room for the new one
;;; that will be saved.  So, with this value set to 2, for example, there are never any more
;;; than 2 incremental saves in the ./world/ directory.  Incremental saves are recognized
;;; only by their filenames, so to make an incremental save immune from deletion, rename it
;;; to something not starting with "inc-".
(csetq *TM-NUMBER-OF-INCREMENTAL-SAVES-TO-KEEP* 2)

;;; Possible values: T or NIL.  When T, the TMAP (Thesaurus Manager Access Protocol) server will be started when ./init/port-init.lisp is loaded.  When NIL, it will not be started.
(csetq *TM-START-TMAP-ON-STARTUP?* NIL)

;;; Possible values: A number.  This parameter specifies the offset of the port number
;;; for the Thesaurus Manager Access Protocol.
(csetq *TMAP-PORT-OFFSET* 13)

(check-system-parameters)





