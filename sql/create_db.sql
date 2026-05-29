-- Creazione tabella Utenti
CREATE TABLE Utenti (
    id              SERIAL PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    cognome         VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,  -- usata come username
    password_hash   VARCHAR(255) NOT NULL,          -- bcrypt hash
    data_nascita    DATE,                           
    domicilio       VARCHAR(255),
    ruolo           VARCHAR(20) NOT NULL CHECK (ruolo IN ('cliente', 'gestore'))
);

-- Creazione tabella RistorantiTheKnife
CREATE TABLE RistorantiTheKnife (
    id              SERIAL PRIMARY KEY,
    nome            VARCHAR(255) NOT NULL,
    nazione         VARCHAR(100) NOT NULL,
    citta           VARCHAR(100) NOT NULL,
    indirizzo       VARCHAR(255) NOT NULL,
    latitudine      DECIMAL(10, 7) NOT NULL,
    longitudine     DECIMAL(10, 7) NOT NULL,
    prezzo_medio    DECIMAL(8, 2),
    delivery        BOOLEAN NOT NULL DEFAULT FALSE,
    prenotazione    BOOLEAN NOT NULL DEFAULT FALSE,
    tipo_cucina     VARCHAR(100),
    descrizione     TEXT,
    id_gestore      INTEGER REFERENCES Utenti(id) ON DELETE SET NULL
);

-- Creazione tabella Recensioni
CREATE TABLE Recensioni (
    id              SERIAL PRIMARY KEY,
    id_ristorante   INTEGER NOT NULL REFERENCES RistorantiTheKnife(id) ON DELETE CASCADE,
    id_utente       INTEGER NOT NULL REFERENCES Utenti(id) ON DELETE CASCADE,
    stelle          INTEGER NOT NULL CHECK (stelle BETWEEN 1 AND 5),
    testo           TEXT,
    data_inserimento TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (id_ristorante, id_utente)  -- l'utente puo lasciare una sola recensione per ristorante
);

-- Creazione tabella RisposteRecensioni
CREATE TABLE RisposteRecensioni (
    id              SERIAL PRIMARY KEY,
    id_recensione   INTEGER NOT NULL UNIQUE REFERENCES Recensioni(id) ON DELETE CASCADE,
    id_gestore      INTEGER NOT NULL REFERENCES Utenti(id) ON DELETE CASCADE,
    testo           TEXT NOT NULL,
    data_risposta   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Creazione tabella Preferiti
CREATE TABLE Preferiti (
    id_utente       INTEGER NOT NULL REFERENCES Utenti(id) ON DELETE CASCADE,
    id_ristorante   INTEGER NOT NULL REFERENCES RistorantiTheKnife(id) ON DELETE CASCADE,
    PRIMARY KEY (id_utente, id_ristorante)
);

-- Creazione tabella Prenotazioni
CREATE TABLE Prenotazioni (
    id                SERIAL PRIMARY KEY,
    id_utente         INTEGER NOT NULL REFERENCES Utenti(id) ON DELETE CASCADE,
    id_ristorante     INTEGER NOT NULL REFERENCES RistorantiTheKnife(id) ON DELETE CASCADE,
    data_prenotazione TIMESTAMP, -- data e ora della prenotazione
    posti             INTEGER NOT NULL DEFAULT 1,
    stato             VARCHAR(30) NOT NULL DEFAULT 'CONFERMATA'
);