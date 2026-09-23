-- ============================================================
-- Schéma de la base locale H2 - Championnat France Pompiers Parapente
-- Ce script est rejoué à chaque démarrage (CREATE TABLE IF NOT EXISTS)
-- ============================================================

-- Utilisateurs de l'application (comptes de connexion)
CREATE TABLE IF NOT EXISTS utilisateurs (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    identifiant     VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe    VARCHAR(256) NOT NULL,   -- haché SHA-256
    role            VARCHAR(40)  NOT NULL,   -- ADMINISTRATEUR, RESPONSABLE_EPREUVE, BENEVOLE, PILOTE, COMITE_PILOTES
    pilote_id       INT NULL,                -- si le compte est lié à un pilote
    actif           BOOLEAN NOT NULL DEFAULT TRUE
);

-- Pilotes (informations d'inscription au championnat)
CREATE TABLE IF NOT EXISTS pilotes (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    numero_licence      VARCHAR(50) NOT NULL UNIQUE,
    nom                 VARCHAR(100) NOT NULL,
    prenom              VARCHAR(100) NOT NULL,
    caserne             VARCHAR(150),
    poids               DOUBLE,
    email               VARCHAR(150),
    annee_naissance     INT,
    categorie           VARCHAR(50),
    date_inscription    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Épreuves (créées librement par le Responsable de l'épreuve)
CREATE TABLE IF NOT EXISTS epreuves (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nom                 VARCHAR(150) NOT NULL,
    description         VARCHAR(1000),
    mode_calcul         VARCHAR(20) NOT NULL DEFAULT 'FORMULE', -- FORMULE ou BAREME
    formule             VARCHAR(500),           -- utilisé si mode_calcul = FORMULE (ex: 100 - temps*2 + nbrBalises*10)
    valeur_cle          VARCHAR(100),           -- utilisé si mode_calcul = BAREME : variable servant à classer les pilotes
    sens_classement     VARCHAR(4) DEFAULT 'ASC', -- ASC (plus petit = meilleur, ex: temps) ou DESC (plus grand = meilleur)
    afficher_classement BOOLEAN NOT NULL DEFAULT TRUE,  -- case à cocher : rendre public le classement de cette épreuve
    compte_dans_general BOOLEAN NOT NULL DEFAULT TRUE,  -- l'épreuve compte-t-elle dans le classement général ?
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    ordre               INT DEFAULT 0,
    date_creation       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Paramètres/variables saisissables pour chaque épreuve (définis dynamiquement par le responsable)
-- Ex: pour "Marche & Vol" -> variables: tempsMarche, poidsSac, nbrBalises
CREATE TABLE IF NOT EXISTS epreuve_parametres (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    epreuve_id      INT NOT NULL,
    nom_variable    VARCHAR(60) NOT NULL,   -- identifiant utilisé dans la formule (sans espace, ex: "temps")
    label           VARCHAR(150) NOT NULL,  -- libellé affiché à la saisie (ex: "Temps de marche (min)")
    unite           VARCHAR(30),
    obligatoire     BOOLEAN DEFAULT TRUE,
    ordre           INT DEFAULT 0,
    FOREIGN KEY (epreuve_id) REFERENCES epreuves(id) ON DELETE CASCADE
);

-- Barème de points par rang (utilisé si mode_calcul = BAREME)
-- Ex: rang 1 -> 100 pts, rang 2 -> 90 pts, ...
CREATE TABLE IF NOT EXISTS bareme_points (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    epreuve_id  INT NOT NULL,
    rang        INT NOT NULL,
    points      DOUBLE NOT NULL,
    FOREIGN KEY (epreuve_id) REFERENCES epreuves(id) ON DELETE CASCADE,
    UNIQUE(epreuve_id, rang)
);

-- Inscription d'un pilote à une épreuve (permet de savoir qui doit passer l'épreuve)
CREATE TABLE IF NOT EXISTS inscriptions_epreuve (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    pilote_id   INT NOT NULL,
    epreuve_id  INT NOT NULL,
    FOREIGN KEY (pilote_id) REFERENCES pilotes(id) ON DELETE CASCADE,
    FOREIGN KEY (epreuve_id) REFERENCES epreuves(id) ON DELETE CASCADE,
    UNIQUE(pilote_id, epreuve_id)
);

-- Résultats saisis (valeurs brutes + points calculés)
CREATE TABLE IF NOT EXISTS resultats (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    epreuve_id      INT NOT NULL,
    pilote_id       INT NOT NULL,
    valeurs_json    VARCHAR(2000),   -- ex: {"temps": 812, "nbrBalises": 6}
    points          DOUBLE DEFAULT 0,
    disqualifie     BOOLEAN DEFAULT FALSE,  -- sécurité : 0 pt si problème de sécurité
    saisi_par       VARCHAR(100),
    date_saisie     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (epreuve_id) REFERENCES epreuves(id) ON DELETE CASCADE,
    FOREIGN KEY (pilote_id) REFERENCES pilotes(id) ON DELETE CASCADE,
    UNIQUE(epreuve_id, pilote_id)
);

-- Compte administrateur par défaut créé au premier lancement (identifiant: admin / mdp: admin123)
MERGE INTO utilisateurs (id, identifiant, mot_de_passe, role, actif) KEY(id)
VALUES (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRATEUR', TRUE);
