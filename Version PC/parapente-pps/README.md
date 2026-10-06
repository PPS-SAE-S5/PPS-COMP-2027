# Championnat de France Pompiers de Parapente — Application de gestion

Projet réalisé pour **Parapente Pays de Sault (PPS)**, en vue du Championnat de France
Pompiers de Parapente (été 2027). Version de **base** (JavaFX, stockage 100% local).

## Nouveautés version 0.5

1. **Les comptes utilisateurs sont maintenant synchronisés** (identifiant, mot de passe
   haché, rôle, actif/inactif), fusionnés par identifiant (comme "admin"). Si vous utilisez
   Supabase, exécutez une fois `supabase_migration_v3_utilisateurs.sql`. Choix assumé pour
   un outil interne au club : le mot de passe haché transite donc, lui aussi, par Supabase.
2. **Correction** : le script `schema.sql` plantait au deuxième lancement de l'application
   (ligne qui essayait encore de lire l'ancienne colonne "catégorie" après sa suppression) —
   supprimée.
3. **Correction fenêtre** : le redimensionnement en changeant d'écran est maintenant appliqué
   après l'affichage (et non juste avant), ce qui le rend fiable y compris en plein écran.
4. **Correction écran de saisie** : appuyer sur Entrée dans un champ de saisie passe
   maintenant au champ suivant au lieu de valider/recharger l'écran à chaque champ (seul le
   dernier champ valide, en une fois) ; le tableau des résultats déjà saisis ne revient plus
   en haut à chaque actualisation.

## Nouveautés version 0.4

1. **"Catégorie" (texte libre) remplacée par "Genre"** (Homme / Femme uniquement, liste
   déroulante non modifiable) sur la fiche pilote. Si vous utilisez la synchronisation
   Supabase, exécutez une fois `supabase_migration_v2_genre.sql` dans l'éditeur SQL de
   Supabase (après avoir déjà exécuté `supabase_migration.sql`).
2. **Gestion des comptes complétée** (écran "Gestion des comptes") : en sélectionnant un
   compte dans le tableau, vous pouvez maintenant :
   - l'**activer / désactiver** (un compte désactivé ne peut plus se connecter, sans avoir
     à le supprimer ni perdre son historique) ;
   - lui **changer son mot de passe**.
   Le compte `admin` par défaut ne peut toujours ni être supprimé, ni désactivé.

## Nouveautés version 0.3 — Synchronisation en ligne (Supabase)

L'application reste **100 % utilisable hors ligne**, exactement comme avant : tout fonctionne
en local par défaut, rien ne change si la synchronisation n'est pas configurée.

Nouveau : un écran **"Synchronisation en ligne"** (visible pour Administrateur et Responsable
de l'épreuve) permet, quand un appareil retrouve internet, d'envoyer/recevoir les données avec
une base Supabase partagée entre plusieurs ordinateurs.

**Avant la première utilisation**, exécutez une fois le script `supabase_migration.sql`
(fourni à la racine du projet) dans l'éditeur SQL de votre projet Supabase (SQL Editor → New
query → coller le contenu → Run). Il ajoute uniquement les colonnes nécessaires à la
synchronisation, sans toucher à vos tables existantes.

Ensuite, dans l'écran "Synchronisation en ligne" :
1. Collez l'URL du projet et la clé API publique (mêmes informations que dans le fichier de
   test `lookerBD.html`).
2. "Tester la connexion" pour vérifier.
3. "Synchroniser maintenant" pour envoyer/recevoir les données. Un résumé indique combien de
   lignes ont été reçues/envoyées par table.
4. Cochez "synchronisation automatique au démarrage" si vous voulez qu'une synchronisation
   soit tentée en arrière-plan à chaque ouverture de l'application (silencieuse, n'empêche
   jamais l'ouverture si l'appareil est hors ligne).

**Comment ça fonctionne (pour information, rien à faire de particulier au quotidien) :**
- Chaque pilote, épreuve, variable, barème, inscription et résultat reçoit un identifiant
  stable (uuid) qui permet de faire correspondre les mêmes données entre plusieurs appareils
  (l'id local, qui recommence à 1 sur chaque ordinateur, ne peut pas servir à ça).
- Si la même donnée a été modifiée sur deux appareils différents avant une synchronisation,
  c'est la modification la plus récente (par date) qui est conservée.
- Les pilotes se fusionnent automatiquement par numéro de licence, les inscriptions et
  résultats par couple pilote+épreuve : pas de doublon si deux bénévoles inscrivent le même
  pilote sur deux ordinateurs différents avant leur première synchronisation.
- **Les comptes de connexion (identifiants/mots de passe) ne sont jamais synchronisés** :
  chaque appareil garde ses propres comptes, pour qu'aucun mot de passe ne transite sur le
  réseau.
- Avertissement de sécurité à connaître : la clé API utilisée est la clé publique ("anon"),
  intégrée dans la configuration de l'application. C'est une simplification volontaire pour
  un outil interne au club ; une personne ayant accès au fichier `data/sync.properties`
  pourrait théoriquement lire/modifier la base en ligne. Pour une sécurité renforcée plus
  tard (accès restreint par rôle côté serveur), il faudra passer par un petit serveur
  intermédiaire plutôt que d'exposer la clé directement — à en reparler si besoin.

## Nouveautés version 0.2

Corrections apportées suite aux premiers tests, sans modification des fonctionnalités déjà validées :

1. **Messages d'erreur en français compréhensible** : plus aucun message technique brut
   (SQL, exceptions Java) n'est affiché à l'écran. Voir `util/MessageErreurUtil.java`.
2. **Liste déroulante des pilotes** (écran "Saisie des résultats") : affiche maintenant
   le nom complet et le numéro de licence du pilote au lieu du nom technique de l'objet Java.
3. **Création d'épreuve en mode Barème** : le nom d'une variable saisi/modifié dans le
   tableau des variables apparaît désormais immédiatement dans la liste "Variable de
   classement", sans avoir besoin d'ajouter une deuxième variable au préalable.
4. **Taille de la fenêtre stabilisée** : elle ne change plus de taille (ni ne "rétrécit")
   en changeant d'écran, y compris quand la fenêtre est agrandie/maximisée.
5. **Emplacement pour le logo du club** : une image "placeholder" (`view/logo.png`) apparaît
   sur l'écran de connexion et en haut du tableau de bord. **Pour mettre le logo du club,
   il suffit de remplacer le fichier `src/main/resources/com/pps/parapente/view/logo.png`
   par votre propre image, en conservant exactement ce nom de fichier** (idéalement une
   image carrée, fond transparent, en PNG) — aucune modification de code n'est nécessaire.

## Ce que fait déjà cette version

- **Connexion par rôle** : Administrateur, Responsable de l'épreuve, Bénévole, Pilote, Comité des pilotes.
  (compte par défaut : `admin` / `admin123`)
- **Gestion des pilotes** : inscription avec tous les champs demandés (n° licence, nom, prénom,
  caserne, poids, email, année de naissance, catégorie).
- **Gestion des épreuves — 100% paramétrable** : la liste donnée par le club
  (atterrissage de précision, marche & vol, cross, checkpoint...) n'est qu'un **exemple**.
  Le Responsable de l'épreuve crée ici librement :
  - le nom et la description de l'épreuve,
  - les **variables à saisir** (ex: temps, nombre de balises, poids du sac...), créées à la volée,
  - le **mode de calcul des points**, au choix :
    - **Formule mathématique libre** (ex: `100 - temps*2 + nbrBalises*10 - poidsSac*0.5`),
      avec accès automatique à `age` et `poidsPilote` du pilote,
    - **Barème par rang** (ex: 1er = 100 pts, 2e = 90 pts...), calculé automatiquement
      en classant les pilotes sur la variable de son choix (temps, distance...).
  - la case **« afficher le classement »** de l'épreuve,
  - si l'épreuve **compte dans le classement général**.
- **Saisie des résultats** : formulaire généré dynamiquement selon les variables définies
  pour l'épreuve sélectionnée + case "disqualifié / sécurité" (0 point automatique).
- **Classement général en temps réel** : recalculé à la demande, une colonne par épreuve.
  Règle du club appliquée : un pilote inscrit qui n'a pas de résultat sur une épreuve
  obtient automatiquement 0 point.
- **Stockage 100% local** : base de données fichier **H2**, dans le dossier `data/` à côté
  de l'application. **Aucune connexion internet n'est nécessaire** pour utiliser l'application
  (répond à l'exigence du club en cas d'absence de réseau sur le terrain).
- **Gestion des comptes** (Administrateur) : créer les comptes des bénévoles, responsables, etc.

## Prérequis

- **Java 17 ou plus récent** (JDK complet, pas juste un JRE)
- **Maven** (pour télécharger les dépendances et lancer l'application)
- Une connexion internet **uniquement la première fois**, pour que Maven télécharge
  JavaFX, H2 et exp4j (ensuite l'application fonctionne hors-ligne).

## Lancer l'application

```bash
cd parapente-pps
mvn clean javafx:run
```

Au premier lancement, un dossier `data/` est créé automatiquement à côté du projet :
c'est là que vit toute la base de données (fichier `pps_championnat.mv.db`).
Il suffit de conserver ce dossier (ou de le sauvegarder/copier sur une clé USB) pour
garder toutes les données du championnat.

## Construire un exécutable (.jar) à distribuer

```bash
mvn clean package
```

## Structure du projet

```
parapente-pps/
├── pom.xml                        # Dépendances Maven (JavaFX, H2, exp4j, json)
├── src/main/java/com/pps/parapente/
│   ├── MainApp.java                # Point d'entrée JavaFX
│   ├── model/                      # Classes métier (Pilote, Epreuve, Resultat...)
│   ├── dao/                        # Accès base de données H2 (SQL brut, JDBC)
│   ├── service/                    # Logique métier (calcul de points, classement, auth)
│   ├── controller/                 # Contrôleurs JavaFX (un par écran)
│   └── util/                       # Utilitaires (navigation, session, sécurité)
└── src/main/resources/
    ├── db/schema.sql                # Schéma SQL de la base locale (créé au 1er lancement)
    └── com/pps/parapente/view/      # Fichiers FXML (écrans) + style.css
```

## Modèle de données (résumé)

- `pilotes` : fiche d'inscription du pilote
- `epreuves` : une épreuve, avec son mode de calcul (`FORMULE` ou `BAREME`)
- `epreuve_parametres` : les variables saisissables définies pour une épreuve (dynamique)
- `bareme_points` : la table rang → points (mode BAREME)
- `inscriptions_epreuve` : qui doit passer quelle épreuve
- `resultats` : les valeurs saisies + points calculés, par pilote et par épreuve
- `utilisateurs` : comptes de connexion et leur rôle

## Pistes d'évolution (hors de ce socle de base)

- Export des classements en PDF / Excel pour affichage papier.
- Mode "hors-ligne puis synchronisation" si un serveur central est mis en place plus tard
  (aujourd'hui tout est local ; le passage à un serveur partagé — pour plusieurs postes de
  saisie en simultané sur le terrain — est la prochaine étape à discuter avec le client).
- Historique/audit des modifications de résultats (qui a changé quoi, et quand).
- Écran dédié "Comité des pilotes" (validation/contestation des résultats).
- Gestion fine des catégories (classements séparés par catégorie).
