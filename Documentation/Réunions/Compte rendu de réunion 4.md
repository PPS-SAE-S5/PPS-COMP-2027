# Compte rendu de la réunion 4 - PPS

## CR n°4 le 10/11/2026 à 10h30 (durée d'environ 22 min)

</br>

- **Contexte**
	- 4ème réunion en visioconférence pour faire une démonstration de l’existant (versions ordinateur, mobile et page web) et obtenir des retours
	- Lien vers l'ordre du jour : [ODJ 4 du 10/11/2026](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/main/Documentation/R%C3%A9unions/ODJ%204%20-%20PPS%20Rolando.pdf)


- **Objectif de la réunion**
    - Faire un point sur l’avancement du projet de manière générale (depuis la réunion 3, lors du sprint 2 du 05/10/2026 au 23/10/2026)
    - Démonstration de la version ordinateur (fonctionnalités, modifications, téléchargement de l'application)
    - Démonstration de la version mobile (fonctionnalités, modifications, téléchargement de l'application)
    - Démonstration rapide de la page web avec le classement
    - Définir la suite (sprint 3 du 02/11/2026 au 27/11/2026)

</br>

---

</br>

- **Présentation de l'application ordinateur**

    - Téléchargement et installation
        - L'application se télécharge depuis la dernière Release du dépôt GitHub (fichier `.jar`), avec une installation rapide
        - Elle fonctionne sur Windows et sur Mac
        - Le dépôt actuel est celui du projet scolaire : un dépôt technique dédié à la maintenance sera créé plus tard, et le lien de téléchargement définitif sera communiqué à la fin du projet
        - L'application n'a pas encore d'icône, mais elle est utilisable sans passer par l'environnement de développement
    - Page de connexion
        - ![Page de connexion](../../Documentation/Sprint%203/Captures/interface_accueil.png)
        - Possibilité de se connecter en tant que visiteur, pour consulter uniquement le classement
    - Tableau de bord
        - ![Tableau de bord](../../Documentation/Sprint%202/Captures/interface_dashboard.png)
        - Interface réorganisée en trois parties : **préparer la compétition**, **pendant la compétition** et **administration**
        - L'administrateur a accès à toutes les pages, les bénévoles n'ont accès qu'à l'attribution des points pendant la compétition.
    - Planning des journées (nouveau)
        - ![Planning des journées](../../Documentation/Sprint%202/Captures/interface_planning.png)
        - Une journée contient plusieurs ateliers (matin / après-midi), chaque atelier contenant des épreuves (ex : vol rapide, session d'endurance)
        - Création d'une nouvelle journée avec sa date, modification d'un atelier et ajout d'une épreuve à un atelier
    - Gestion des épreuves et gestion des pilotes
        - Pas de changement, hormis le remplacement du champ « caserne » par « département »
    - Suivi des participants (nouveau)
        - ![Suivi des participants](../../Documentation/Sprint%202/Captures/interface_suivi.png)
        - Permet de suivre un pilote en particulier (total de points, rang au classement général, nombre d'épreuves saisies, score par épreuve, statut disqualifié ou non encore saisi)
    - Classements
        - Classement général et classement par épreuve, avec le choix entre classement mixte, hommes ou femmes
        - Le classement est toujours accessible via la carte "Classement général" :
        - ![Classement](../../Documentation/Sprint%203/Captures/interface_classement_viewer.png)
        - Mais il est aussi possible d'y acceder sur la page de connexion via le bouton "Non, je veux uniquement suivre le classement"
        - ![Classement](../../Documentation/Sprint%203/Captures/interface_accueil_viewer.png)

</br>

---

</br>

- **Présentation de l'application mobile**

    - Téléchargement et installation
        - L'application s'installe à partir du fichier `.apk` de la Release du dépôt GitHub (mise à jour d'une version déjà installée présentée en direct)
        - ![Release Github](../../Documentation/Sprint%203/Captures/github_release.jpg)
        - Une fois téléchargée, le .apk se trouve dans le téléphone : 
        - ![APK dans le téléphone](../../Documentation/Sprint%203/Captures/release_download.jpg)
        - ![Confirmation d'installation](../../Documentation/Sprint%203/Captures/release_confirmation.jpg)
        - ![Installation](../../Documentation/Sprint%203/Captures/release_installation.jpg)
        - Android affiche un avertissement lors de l'installation, car l'application n'a pas encore été vérifiée par Google Play Protect
        - Testée sur un téléphone de 2021 et sur un téléphone de 2017
    - Page de connexion
        - ![Page de connexion mobile](../../Documentation/Sprint%203/Captures/interface_accueil_mobile.jpg)
        - Bouton pour suivre uniquement le classement, sans se connecter
    - Tableau de bord
        - ![Tableau de bord mobile](../../Documentation/Sprint%203/Captures/interface_dashboard_mobile.jpg)
        - Gestion des épreuves, planning des journées, gestion des pilotes, saisie des résultats, classement général, suivi des participants, gestion des comptes et synchronisation
    - Classements
        - ![Classements mobile](../../Documentation/Sprint%203/Captures/interface_classement_mobile.jpg)
        - Classement général, masculin, féminin et détail des épreuves
        - Bouton « Actualiser » pour mettre à jour les données
    - Suivi des participants
        - ![Suivi des participants mobile](../../Documentation/Sprint%203/Captures/interface_suivi_detail_mobile.jpg)
        - Informations du pilote avec total de points, classement général, nombre d'épreuves et points par épreuve

</br>

*PS : L'application mobile fait la même chose que l'application ordinateur, les fonctionnalités communes aux deux versions n'ont donc pas été présentées une seconde fois*

</br>

---

</br>

- **Présentation de la page web du classement**

    - ![Page web du classement](../../Documentation/Sprint%203/Captures/webpage_classement.png)
    - Page internet publique, accessible à tout le monde, disponible en permanence (24h/24, 7j/7)
    - Mêmes classements que sur les applications : général (mixte, hommes, femmes) et par épreuve, avec un filtre par jour
    - Actualisation automatique toutes les 60 secondes, bouton « Actualiser » pour forcer la mise à jour, et possibilité de mettre le rafraîchissement en pause (pour faire une capture du classement)
    - Mode sombre disponible
    - Les données viennent de la base de données : un bénévole doit d'abord synchroniser son application avec la base pour que ses points apparaissent sur la page

</br>

---

</br>

- **Prise de note**
    - *Retour très positif du client sur les trois versions (ordinateur, mobile et page web)*
    - *L'organisation en journées et ateliers convient : le matin ce sera plutôt la marche-vol ou l'atterrissage de précision, et l'après-midi le vol cross avec balises*
    - *Le remplacement de « caserne » par « département » est apprécié*
    - *Nouveau besoin : un classement par équipe (par département), car plusieurs pilotes d'un même département participent. Les équipes comptent entre 2 et 6 pilotes. Le classement se ferait sur les 2 meilleurs pilotes de chaque équipe (leurs points du classement final), afin de désigner l'équipe championne de France*
    - *Si le classement par équipe n'est pas possible, le client le fera à la main*
    - *Le logo n'est pas encore défini (logo provisoire dans l'application), le client le transmettra à l'équipe dès qu'il sera choisi*

- **À faire / n'a pas encore été implémenté dans cette version**
    - Classement par équipe (pilotes d'un même département), activable par une option : tant qu'elle n'est pas cochée, le classement reste inchangé
    - Corriger le planning : un atelier ne doit pas pouvoir être enregistré sans créneau, horaires, lieu et responsable (bug constaté pendant la démonstration)
    - Intégrer le logo définitif lorsqu'il sera communiqué
    - Poursuivre la mise à jour et l'amélioration des applications jusqu'à la fin du sprint 3 (27/11/2026)
    - Fixer une prochaine réunion pour montrer les modifications (date à définir avec le client)
    - Afficher les sponsors


</br>

</br>

## Participants :

**Jean-Marie Rolando**\
Ali Adam LABADI\
Jinhai-Andrei FENG\
Erwan PAULOIN\
Alexis PIQUEPÉ
