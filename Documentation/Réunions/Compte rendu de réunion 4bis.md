# Compte rendu de la réunion 4bis - PPS

## CR n°4bis le 12/11/2026 à 17h00 (durée d'environ 1 heure)

</br>

- **Contexte**
	- Réunion 4bis avec Jean-Michel Bruel (JMB) qui était absent à la [réunion 3](/Documentation/Réunions/Compte%20rendu%20de%20réunion%203.md) et à la [réunion 4](/Documentation/Réunions/Compte%20rendu%20de%20réunion%204.md)
	- Compte rendu de la réunion précédente : [Compte rendu de réunion 4](Compte%20rendu%20de%20réunion%204.md)
    - Lien vers l'ordre du jour : [ODJ 4 du 12/11/2026](../Réunions/ODJ%204%20-%20PPS%20JMB.pdf)


- **Objectif de la réunion**
    - Présenter les nouveautés de la version ordinateur
        - reprise de la démonstration de l'existant,
        - focus sur les nouveautés depuis la fin du [sprint 2](../Réunions/Compte%20rendu%20de%20réunion%202.md)
    - Présenter la version mobile
        - reprise de la démonstration de l'existant,
        - focus sur les nouveautés depuis la fin du [sprint 2](../Réunions/Compte%20rendu%20de%20réunion%202.md)
    - Faire un tour rapide de la page web avec le classement
    - Recueillir les retours et définir la suite (après la fin du sprint 3 au 27/11/2026)

</br>

---

</br>

- **Présentation de l'application ordinateur (version bureau)**

    - Installation
        - Via la Release du dépôt GitHub
    - Planning des journées (nouveau)
        - ![Planning des journées](../../Documentation/Sprint%202/Captures/interface_planning.png)
    - Suivi des participants (nouveau)
        - ![Suivi des participants](../../Documentation/Sprint%202/Captures/interface_suivi.png)
    - Connexion en tant que visiteur, pour voir uniquement le classement
        - ![Classement visiteur](../../Documentation/Sprint%203/Captures/interface_accueil_viewer.png)
    - Synchronisation avec la base de données
        - Possible uniquement en tant qu'administrateur
        - Le message « erreur compte déjà actif » peut apparaître : il provient de la sécurité de Supabase et est courant

</br>

---

</br>

- **Discussion sur la version ordinateur**
    - Logo : à récupérer par mail (prochainement)
    - Limites des ateliers : ne pas les « patcher » dans le code, mais envisager un fichier de configuration
    - Export PDF des journées (à discuter avec l'équipe de développement)
    - Import les "pilotes" à partir d'un fichier Excel (issu d'un Google Form)
    - Export des classements en Excel

- **Filtrage du classement**
    - Distinguer deux catégories de participants :
        - Catégorie pompier
        - Catégorie open (invité)
    - La différenciation se fait avec le numéro de pilote (ID dans la base de données)
    - Renommer le classement « mixte » en « général »

</br>

---

</br>

- **Présentation de l'application mobile**

    - Installation
        - Via la Release du dépôt GitHub
        - ![Installation mobile](../../Documentation/Sprint%203/Captures/github_release.jpg)
        - ![Installation mobile](../../Documentation/Sprint%203/Captures/release_installation.jpg)
    - Si message « Google Play Store doit analyser l'application »
        - Explication : l'application est un `.apk` signé, mais l'équipe n'a pas payé de compte Google Play Console (mode développeur) donc c'est normal
    - Configuration de la base de données pour la version mobile en mode administrateur

</br>

---

</br>

- **Présentation de la page web du classement**

    - ![Page web du classement](../../Documentation/Sprint%203/Captures/webpage_classement.png)
    - Tour rapide des fonctionnalités :
        - Actualiser manuellement les données (actualisation automatique toutes les 60 secondes par défaut)
        - Mettre en pause l'actualisation automatique
        - Voir le classement général
        - Voir les classements par épreuve
        - Filtrer par genre
        - Passer du mode clair au mode sombre

</br>

---

</br>

- **Autres retours au cours de la réunion**
    - Préparer un CSV type pour les inscriptions (pour configurer le futur Google Form)
    - Mettre à jour le README et changer les liens des Releases (mettre la release v2.1)
    - Prévoir un autre dépôt pour la version finale du projet (maintenance future)
    - Si c'est possible d'avoir un même `src` pour l'application ordinateur et mobile (_ce n'est cependant pas la voie choisie pour ce projet_)
    - Équipes par régions : mettre la proposition de mise à jour dans les issues, mais ne pas l'implémenter pour l'instant
    - Le logo arrive bientôt, d'après JMB
    - Les sponsors doivent être mis en CSV(?) et affichés sur la page de connexion (ils arrivent aussi bientôt)
    - Supabase : risque que la base de données passe hors ligne, toruver un intermédiaire

- **Fin de réunion**
    - Retour sur la documentation (les comptes rendus)
    - La suite après le 27/11/2026 ([fin du sprint 3](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/4))

</br>
</br>

- **À faire / n'a pas encore été implémenté dans ce sprint**
    - Filtrage du classement : catégories pompier et open (via le numéro de pilote), renommer « mixte » en « général »
    - Préparer un CSV type pour les inscriptions
    - Mettre à jour le README et les liens de la [Release 2.1](https://github.com/PPS-SAE-S5/PPS-COMP-2027/releases) (la plus récente)
    - Créer les issues : équipes par régions, import des pilotes depuis Excel, export des classements en Excel, export PDF des journées, fichier de configuration des limites d'ateliers
    - Intégrer le logo et les sponsors (CSV) lorsqu'ils seront communiqués
    - Prévoir le dépôt de la version finale pour la maintenance

</br>

</br>

## Participants :

**Jean-Michel Bruel (JMB)**\
Alexis PIQUEPÉ
