======================================================
THEKNIFE
LAB B, CORSO DI LAUREA TRIENNALE IN INFORMATICA
UNIVERSITA' DEGLI STUDI DELL'INSUBRIA

PROGETTO REALIZZATO DA:
Erik Mirashaj 760453 VA emirashaj@studenti.uninsubria.it
Igor Gorchynskyi 757184 VA
Kabuka Dan Mumanga 757708 VA
Lorenzo Mujeci 757597 VA

======================================================

CONTENUTI:
    --> bin/: contiene i file .jar: applicazione eseguibile 
    --> src/: codice sorgente del progetto
    --> sql/: file sql e script python utilizzati per creare e popolare il DB
    --> doc/: documentazione e manuali
    --> autori.txt: file con informazioni sugli autori del progetto
    
    --> run-client.bat: file client eseguibile batch per Windows
    --> run-server.bat: file server eseguibile batch per Windows
    --> run-client.sh: file client eseguibile shell per Linux e macOS
    --> run-server.sh: file server eseguibile shell per Linux e macOS

    --> Backup.sql: file dump con tutti i dati del DB
    --> Database_Guide.mp4: video guida per mostrare i passaggi necessari per creare il DB 

AVVIO:
Doppio click su "run-server.bat" (Windows) o "run-server.sh" (Linux/macOS) dalla directory principale del progetto.
Doppio click su "run-client.bat" (Windows) o "run-client.sh".


DATABASE:
Per ripristinare il database bisogna usare il file dump "Backup.sql"
1. Spostare il file dump nella cartella Programmi\PostgreSQL\18\pg Admin4\runtime
2. Eseguire il cdm come amministratore
3. spostarsi nella cartella Programmi\PostgreSQL\18\pg Admin4\runtime
4. Creare il database da pgAdmin
5. Eseguire il comando "pg_restore -U *nomeutente* -d *nomedb* Backup.sql"