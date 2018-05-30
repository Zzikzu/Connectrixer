Connectrixer
============

This application should be used for creation of port documentation from Brocade SAN switches in form of excel workbook.
The second usage is to set and set portnames on Brocade SAN switches based on aliases. The portname setting is currently implemented only for F-Port (host or storage) connections

The application performs below commands on selected Brocade SAN switches:
	switchshow
	lscfg --show -n
	alishow *
	portshow -i <port index>
	portname -i <port index> -n
	fosexec --fid -cmd <with above commands>

Requirements:
-------------
-	Connectrixer is powered by Java using JavaFX GUI, so it is an platform independent application. It is recommended to update workstation to use Java 8 - 1.8.0_151 for full compatibility.
-	Brocade FOS 7.3 as minimal version needed for full compatibility
-	Connectivity to production environment via AdminLAN enabled
-	Single account (whit same password or predefined special cases) for all SAN switches

Ussage:
-------
-	Start Connectrixer
-	Choose the mode "Create documentation" or "Create portnames"
-	For "Create documentation" mode load excel work book by: File => Open => navigate to file (Template which can be used stored in /files directory)
-	Insert IP addresses of switches by: Edit => Host list (# character can be used as comment)
-	Set user settings and credentials by: Edit => User settings (Num of sessions means the count of parallel processes, max 15)
-	Run
-	Once finished (for "Create documentation" mode) save your excel workbook