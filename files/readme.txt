Connectrixer
============

This application should be used for creation of port documentation from Brocade SAN switches in form of excel workbook.
I performs below commands and process the output.
	switchshow
	alishow *
	portshow -i

Requirements:
-------------
-	Connectrixer is powered by Java using JavaFX GUI, so it is an platform independent application. It is recommended to update workstation to use Java 8 - 1.8.0_151 for full compatibility.
-   FOS 7.3 minimal version for full compatibility
-	Connectivity to production environment via AdminLAN enabled
-	Single account (whit same password) for all SAN switches

Ussage:
-------
-	Start connectrixer
-	Open excel work book by: File => Open => navigate to file (Template which can be used stored in /files directory)
-	Insert IP addresses of switches by: Edit => Host list (# character can be used as comment)
-	Set user settings and credentials by: Edit => User settings (Num of sessions means the count of parallel processes, max 15)
-	Run
-	Once finished save your excel workbook