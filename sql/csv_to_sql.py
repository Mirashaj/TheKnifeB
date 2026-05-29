import csv
import sys
import os
import random
import math

rnd = random.Random(42)  

def extract_price(price_str):
    if not price_str:
        return "NULL"

    # Contiamo il numero di simboli di valuta
    symbols = ['€', '$', '£', '¥', '₩', '฿']
    count = 0
    for s in symbols:
        if s in price_str:
            count = price_str.count(s)
            break

    if count == 1:
        return "15.00"
    elif count == 2:
        return "35.00"
    elif count == 3:
        return "70.00"
    elif count >= 4:
        return "120.00"
    else:
        return "NULL"

def escape_sql(value):
    if not value:
        return "NULL"
    # Sostituisce il singolo apice con due singoli apici per l'escape in SQL
    return "'" + str(value).replace("'", "''") + "'"

def main():
    if len(sys.argv) < 2:
        print("Uso: python csv_to_sql.py <path_al_file.csv>")
        sys.exit(1)

    csv_file = sys.argv[1]

    # Crea il file nella stessa cartella dello script (sql/)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    sql_file = os.path.join(script_dir, 'insert_data.sql')

    if not os.path.exists(csv_file):
        print(f"Errore: File non trovato al percorso: {csv_file}")
        sys.exit(1)

    imported = 0
    skipped = 0
    visti = set()

    with open(csv_file, 'r', encoding='utf-8', errors='ignore') as f_in, \
         open(sql_file, 'w', encoding='utf-8') as f_out:

        reader = csv.DictReader(f_in)

        f_out.write("-- File generato automaticamente da csv_to_sql.py\n")
        f_out.write("-- Contiene l'importazione dei dati ristoranti (michelin_my_maps.csv)\n\n")
        f_out.write("SET client_encoding = 'UTF8';\n\n")

        f_out.write("-- CREDENZIALI DI TEST (password: password123)\n")
        f_out.write("-- mario.gestore@email.com  -> gestore\n")
        f_out.write("-- luigi.gestore@email.com  -> gestore\n")
        f_out.write("-- giulia.cliente@email.com -> cliente\n")
        f_out.write("-- erik.cliente@email.com   -> cliente\n\n")
        f_out.write("INSERT INTO Utenti (nome, cognome, email, password_hash, domicilio, ruolo) VALUES \n")
        f_out.write("('Mario', 'Rossi', 'mario.gestore@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Milano', 'gestore'),\n")
        f_out.write("('Luigi', 'Verdi', 'luigi.gestore@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Roma', 'gestore'),\n")
        f_out.write("('Giulia', 'Bianchi', 'giulia.cliente@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Firenze', 'cliente'),\n")
        f_out.write("('Erik', 'Monti', 'erik.cliente@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Varese', 'cliente'),\n")
        f_out.write("('Anna', 'Conti', 'anna.cliente@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Como', 'cliente');\n\n")

        f_out.write("-- 2. Importazione RistorantiTheKnife\n")

        for row in reader:
            name = row.get('Name', '').replace('\x00', '').strip()
            location = row.get('Location', '').replace('\x00', '').strip()
            lat_str = row.get('Latitude', '').replace('\x00', '').strip()
            lon_str = row.get('Longitude', '').replace('\x00', '').strip()
            address = row.get('Address', '').replace('\x00', '').strip()
            cuisine = row.get('Cuisine', '').replace('\x00', '').strip()
            price = row.get('Price', '').replace('\x00', '').strip()
            description = row.get('Description', '').replace('\x00', '').strip()

            # 1) Salta le righe con campi obbligatori mancanti
            # AGGIUNTO: scarta anche se manca l'indirizzo, dato che nel DB è NOT NULL
            if not name or not location or not lat_str or not lon_str or not address:
                skipped += 1
                continue

            # 2) Split della location in città e nazione
            parts = [p.strip() for p in location.split(',')]
            citta = parts[0]
            nazione = parts[1] if len(parts) > 1 else parts[0] # Fallback se manca la virgola

            # 3) Validazione coordinate
            try:
                lat = float(lat_str)
                lon = float(lon_str)
                if math.isnan(lat) or math.isnan(lon) or math.isinf(lat) or math.isinf(lon):
                    skipped += 1
                    continue
            except ValueError:
                skipped += 1
                continue

            # Troncamento preventivo dei campi
            name_trunc = name[:250]
            address_trunc = address[:250]

            # Evita duplicati di (nome, indirizzo) per evitare fallimenti in Postgres
            # che causerebbero "buchi" negli ID autoincrementanti
            chiave_univoca = (name_trunc.lower(), address_trunc.lower())
            if chiave_univoca in visti:
                skipped += 1
                continue
            visti.add(chiave_univoca)

            # 4) Calcolo prezzo medio (da stringa simbolica)
            prezzo_medio = extract_price(price)

            # 5) SQL escaping e troncamento per evitare errori in Postgres
            sql_name = escape_sql(name_trunc)
            sql_citta = escape_sql(citta[:95])
            sql_nazione = escape_sql(nazione[:95])
            sql_address = escape_sql(address_trunc)
            sql_cuisine = escape_sql(cuisine[:250])
            sql_description = escape_sql(description)

            delivery = 'TRUE' if rnd.random() < 0.4 else 'FALSE'   # 40% dei ristoranti ha delivery
            prenotazione = 'TRUE' if rnd.random() < 0.6 else 'FALSE'  # 60% ha prenotazione online
            
            restaurant_id = imported + 1

            query = (
                "INSERT INTO RistorantiTheKnife "
                "(id, nome, nazione, citta, indirizzo, latitudine, longitudine, prezzo_medio, delivery, prenotazione, tipo_cucina, descrizione, id_gestore) "
                f"VALUES ({restaurant_id}, {sql_name}, {sql_nazione}, {sql_citta}, {sql_address}, {lat}, {lon}, {prezzo_medio}, {delivery}, {prenotazione}, {sql_cuisine}, {sql_description}, NULL);"
            )
            f_out.write(query + "\n")
            imported += 1
            
        f_out.write("\n-- Allinea il contatore autoincrementante al massimo ID inserito\n")
        f_out.write("SELECT setval('ristorantitheknife_id_seq', (SELECT MAX(id) FROM ristorantitheknife));\n")

    print("=== RISULTATO IMPORTAZIONE ===")
    print(f" - Righe processate correttamente (Ristoranti importati): {imported}")
    print(f" - Righe scartate (Mancano campi obbligatori o coordinate non valide): {skipped}")
    print(f" - File SQL generato: {sql_file}")
    print("===============================")

if __name__ == '__main__':
    main()
