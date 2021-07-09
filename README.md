# Importiert B-Pläne aus WFS-Diensten

Anwendung erweitert [KVWMAP](https://kvwmap.de/).

Main-Class: de.gdiservice.bplan.BPlanImportStarter

Parameter:

        dburl:  [username@host:port/dbname]
        pgpass: [path zur pgpass-Datei]
        cronExpr: [Quartz]
        kvwmap_url:
        kvwmap_username:
        kvwmap_password:
                
Sollen E-Mails versendet werden sind anzugeben:
       
        smtpHost
        emailUser
        emailPwd


Docker image can be created:

        git clone https://github.com/rtrier/bplan.git
        cd bplan
        docker build -t bplan_importer .

Example-Script see: [sample.sh](https://raw.githubusercontent.com/rtrier/bplan/master/de.gdiservice.bplan/sample.sh).
