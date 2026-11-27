# Documentation utilisateur (FR) v4 - version mobile (Android)

## Sommaire

- [1. Présentation générale](#1-présentation-générale)
- [2. Installation](#2-installation)
- [3. Connexion et rôles](#3-connexion-et-rôles)
- [4. Tableau de bord](#4-tableau-de-bord)
- [5. Gestion des pilotes](#5-gestion-des-pilotes)
- [6. Gestion des épreuves](#6-gestion-des-épreuves)
- [7. Planning des journées](#7-planning-des-journées)
- [8. Saisie des résultats](#8-saisie-des-résultats)
- [9. Classements](#9-classements)
- [10. Suivi des participants](#10-suivi-des-participants)
- [11. Gestion des comptes (administrateur)](#11-gestion-des-comptes-administrateur)
- [12. Synchronisation en ligne](#12-synchronisation-en-ligne)
- [13. Stockage des données](#13-stockage-des-données)
- [14. Questions fréquentes](#14-questions-fréquentes)

---

## 1. Présentation générale

L'application mobile est faite pour une utilisation **sur le terrain**, depuis un téléphone Android. Elle permet :

- d'inscrire les pilotes ;
- de créer les épreuves du championnat et de choisir comment les points sont calculés ;
- d'organiser la compétition en **journées**, **ateliers** et **épreuves** ;
- de saisir les résultats au fil des épreuves ;
- de consulter le **classement**, général ou par épreuve, avec un filtre hommes / femmes ;
- de suivre le détail des scores de chaque pilote ;
- de gérer les comptes (administrateur) ;
- de **synchroniser** les données avec les autres téléphones et ordinateurs.

L'application fonctionne **sans internet** : les données sont enregistrées sur le téléphone, puis partagées en ligne dès qu'une connexion est disponible (voir [section 12](#12-synchronisation-en-ligne)).

> Certaines fonctions ne sont pour l'instant disponibles que sur la version PC : le numéro de pilote et la catégorie pompier / open, le filtre de classement par catégorie, et l'export PDF du planning.

---

## 2. Installation

L'application s'installe à partir du fichier `.apk` publié dans les [releases du projet](https://github.com/PPS-SAE-S5/PPS-COMP-2027/releases). Elle demande **Android 8.0 ou plus récent**.

1. Depuis le téléphone, ouvrez la dernière release et touchez le fichier `.apk` (par exemple `PPS-COMP-2.1.apk`) pour le télécharger.

![Release sur GitHub](../Sprint%203/Captures/github_release.jpg)

2. Ouvrez le dossier **Téléchargements** (Download) du téléphone et touchez le fichier `.apk`.

![Fichier APK dans le téléphone](../Sprint%203/Captures/release_download.jpg)

3. Confirmez avec **Installer**.

![Confirmation de l'installation](../Sprint%203/Captures/release_confirmation.jpg)

![Installation en cours](../Sprint%203/Captures/release_installation.jpg)

> Android peut afficher un avertissement de sécurité (Play Protect), car l'application n'est pas publiée sur le Play Store. Selon le téléphone, il faut aussi autoriser l'installation d'applications depuis le navigateur ou le gestionnaire de fichiers.

Pour mettre l'application à jour, installez le nouveau fichier `.apk` par-dessus l'ancien : les données du téléphone sont conservées. Par précaution, synchronisez vos données avant toute mise à jour (voir [section 12](#12-synchronisation-en-ligne)).

---

## 3. Connexion et rôles

Au démarrage, l'application affiche l'écran de connexion. Saisissez votre **identifiant** et votre **mot de passe**, puis touchez **Se connecter**.

![Écran de connexion](../Sprint%203/Captures/interface_accueil_mobile.jpg)

Chaque compte a un **rôle**, qui détermine ce qu'il peut faire :

| Rôle | Peut faire |
|---|---|
| **Administrateur** | Tout, y compris créer les comptes des autres utilisateurs |
| **Responsable de l'épreuve** | Gérer les épreuves, le planning et les pilotes, saisir les résultats, suivre les participants, consulter les classements, synchroniser |
| **Bénévole** | Gérer les pilotes, saisir les résultats, suivre les participants, consulter les classements et le planning |
| **Comité des pilotes** | Consulter le planning et les classements |

Un compte administrateur existe par défaut après l'installation. **Changez son mot de passe dès la première utilisation** (voir [section 11](#11-gestion-des-comptes-administrateur)).

### Suivre le classement sans compte

Une personne qui veut seulement voir le classement (pilote, public, famille...) touche **Non, je veux uniquement suivre le classement**. L'application ouvre directement les classements, en lecture seule, sans identifiant.

Le classement est aussi consultable sur internet, à l'adresse [pps-classement.vercel.app](https://pps-classement.vercel.app/).

---

## 4. Tableau de bord

Après la connexion, le tableau de bord affiche, sous forme de cartes à toucher, uniquement les fonctions auxquelles votre rôle donne accès. Votre identifiant et votre rôle s'affichent en haut, et le bouton **Déconnexion** se trouve dans la barre bleue.

![Tableau de bord](../Sprint%203/Captures/interface_dashboard_mobile.jpg)

| Carte | Rôles concernés |
|---|---|
| Gestion des épreuves | Administrateur, responsable de l'épreuve |
| Planning des journées | Tous les rôles (modification : administrateur et responsable de l'épreuve) |
| Gestion des pilotes | Administrateur, responsable de l'épreuve, bénévole |
| Saisie des résultats | Administrateur, responsable de l'épreuve, bénévole |
| Classements | Tous les rôles |
| Suivi des participants | Administrateur, responsable de l'épreuve, bénévole |
| Gestion des comptes | Administrateur |
| Synchronisation en ligne | Administrateur, responsable de l'épreuve |

---

## 5. Gestion des pilotes

Cet écran sert à inscrire les pilotes au championnat. La liste affiche, pour chaque pilote, son nom, son numéro de licence et son département.

![Liste des pilotes](../Sprint%201/Captures/pilotes_mobile_gerer.png)

**Inscrire un pilote :**

1. Touchez **+ Nouveau pilote**.
2. Remplissez la **fiche pilote** :
   - numéro de licence (obligatoire, unique) ;
   - nom et prénom (obligatoires) ;
   - département ;
   - poids ;
   - email ;
   - année de naissance ;
   - genre (Homme ou Femme).
3. Touchez **Ajouter le pilote**.

![Fiche pilote](../Sprint%202/Captures/mobile_pilote_fiche.png)

**Modifier ou supprimer un pilote :** touchez sa fiche dans la liste, modifiez les champs et touchez **Enregistrer les modifications**, ou touchez **Supprimer ce pilote**. La suppression efface aussi ses résultats ; une confirmation est demandée.

> Un nouveau pilote est inscrit automatiquement à toutes les épreuves actives. S'il ne se présente pas à une épreuve, il y obtient 0 point.

> Le **genre** sert aux classements hommes / femmes ([section 9](#9-classements)). Un pilote sans genre renseigné n'apparaît que dans le classement mixte.

---

## 6. Gestion des épreuves

Aucune épreuve n'est prédéfinie : le club crée chaque épreuve du championnat (atterrissage de précision, marche & vol, cross, checkpoint ou toute autre épreuve) avec ses propres règles de calcul des points.

![Liste des épreuves](../Sprint%202/Captures/mobile_epreuves_liste.png)

La liste affiche chaque épreuve avec sa règle : la formule, ou le barème avec sa variable de classement et son sens. Touchez **Nouvelle épreuve** pour en créer une, ou touchez une épreuve existante pour la modifier.

![Création d'une épreuve](../Sprint%202/Captures/mobile_epreuve_form.png)

### 6.1 Informations générales

Renseignez le **nom** et la **description** de l'épreuve.

### 6.2 Variables à saisir

Définissez les valeurs qui seront mesurées (par exemple `temps`, `nbrBalises`, `poidsSac`, `distance`). Chaque variable a :
- un **nom technique**, sans espace, utilisé dans le calcul ;
- un **libellé affiché** ;
- une **unité**, facultative.

Touchez **+ Ajouter une variable** ; le bouton ✕ retire une variable.

### 6.3 Mode de calcul des points

**a) Formule mathématique** : écrivez une formule avec les noms des variables, par exemple :

```
100 - temps*2 + nbrBalises*10 - poidsSac*0.5
```

Deux variables issues de la fiche du pilote sont disponibles sans saisie : `age` et `poidsPilote`.

**b) Barème par classement (rang)** : les pilotes sont classés selon une **variable de classement** (par exemple `temps` ou `distance`), dans le sens choisi : la plus petite valeur gagne, ou la plus grande. Ils reçoivent ensuite les points du **barème par rang** (par exemple 1er = 100 pts, 2e = 90 pts). Touchez **+ Ajouter un rang** pour compléter le barème. Au-delà du dernier rang défini, les points du dernier rang s'appliquent à tous les suivants.

> Le barème est recalculé pour tous les pilotes de l'épreuve à chaque nouvelle saisie.

### 6.4 Options de l'épreuve

- **Afficher le classement de cette épreuve** : le détail de l'épreuve est consultable et sa colonne apparaît dans le classement général.
- **Compte dans le classement général** : les points s'ajoutent au total du championnat.
- **Épreuve active (proposée à la saisie)** : l'épreuve apparaît dans l'écran de saisie et dans le planning.

Terminez avec **Enregistrer l'épreuve**. Pour supprimer une épreuve, touchez **Supprimer cette épreuve** : ses résultats sont supprimés aussi, après confirmation.

---

## 7. Planning des journées

Cet écran organise la compétition dans le temps :

**Journée → ateliers (matin / après-midi) → épreuves**

- **+ Nouvelle journée** crée une journée : nom obligatoire, date facultative (jj/mm/aaaa). **Renommer / date** et **Supprimer la journée** agissent sur la journée affichée. La suppression d'une journée supprime ses ateliers, mais pas les épreuves, qui redeviennent « à placer ».
- **+ Nouvel atelier** ouvre le formulaire d'un atelier : nom, journée et créneau (Matin ou Après-midi), puis heures de début et de fin (`HH:mm`), lieu et responsable.
- Dans **Épreuves de l'atelier**, ajoutez les épreuves. L'ordre de la liste est l'ordre de passage : ▲ et ▼ le modifient, ✕ retire l'épreuve.
- La zone **Épreuves à placer** liste les épreuves actives qui ne sont dans aucun atelier.

> Une épreuve ne peut appartenir qu'à **un seul** atelier.

**Droits :** seuls l'administrateur et le responsable de l'épreuve modifient le planning. Les autres rôles le consultent.

---

## 8. Saisie des résultats

![Saisie des résultats](../Sprint%202/Captures/mobile_saisie.png)

1. Choisissez l'**épreuve**.
2. Choisissez le **pilote** : seuls les pilotes inscrits à cette épreuve apparaissent.
3. Le formulaire affiche un champ pour chaque variable de l'épreuve.
4. Renseignez les valeurs mesurées.
5. Si le pilote doit être disqualifié (problème de sécurité...), cochez **Disqualifié / problème de sécurité (0 point automatique)**.
6. Touchez **Enregistrer le résultat**.

La liste des résultats déjà saisis pour l'épreuve s'affiche en dessous, avec les points calculés.

![Saisie avec disqualification](../Sprint%202/Captures/mobile_saisie_disq.png)

Pour corriger un résultat, choisissez à nouveau l'épreuve et le pilote, puis enregistrez les nouvelles valeurs : elles remplacent les anciennes.

---

## 9. Classements

L'écran **Classements** propose deux onglets, **Général** et **Détail par épreuve**, et un bouton **Actualiser** qui recalcule le classement après de nouvelles saisies.

![Classement général](../Sprint%203/Captures/interface_classement_mobile.jpg)

- **Général** : rang, département, points sur chaque épreuve qui compte au général, et total de chaque pilote. La liste **Classement** propose **Mixte (hommes + femmes)**, **Hommes** ou **Femmes** ; les rangs sont recalculés parmi les pilotes retenus. Un pilote sans genre n'apparaît que dans le classement mixte.
- **Détail par épreuve** : classement d'une seule épreuve, avec les valeurs saisies. Seules les épreuves dont « Afficher le classement » est coché sont proposées.

![Détail par épreuve](../Sprint%202/Captures/mobile_classement_epreuve.png)

> Un pilote inscrit sans résultat saisi obtient 0 point à l'épreuve.

Les classements sont accessibles à tous les rôles et sans compte.

---

## 10. Suivi des participants

Cet écran affiche le détail des scores d'un pilote.

![Liste du suivi des participants](../Sprint%203/Captures/interface_suivi_mobile.jpg)

1. Cherchez un pilote dans la barre de recherche (nom, prénom, licence ou département).
2. Touchez son nom dans la liste.
3. L'écran affiche sa licence, son département, son genre et son âge, puis son **total de points**, son **rang au classement général** et le nombre d'**épreuves saisies**.
4. La partie **Scores par épreuve** donne, pour chaque épreuve, les valeurs saisies, les points, le statut (saisi, disqualifié, pas encore saisi) et si l'épreuve compte au général.

![Détail d'un participant](../Sprint%203/Captures/interface_suivi_detail_mobile.jpg)

Touchez **Actualiser** pour recharger les données. Cet écran est accessible à l'administrateur, au responsable de l'épreuve et au bénévole.

---

## 11. Gestion des comptes (administrateur)

Cet écran est réservé à l'administrateur.

![Gestion des comptes](../Sprint%201/Captures/compte_mobile_gestion.png)

**Créer un compte :** dans **Nouveau compte**, saisissez l'**identifiant** et le **mot de passe**, choisissez le **rôle**, puis touchez **Créer le compte**. Les identifiants sont uniques, sans tenir compte des majuscules.

> Le rôle « Pilote » de la liste correspond à l'accès en lecture seule au classement. Il est inutile de créer des comptes pour les pilotes : ils utilisent le bouton « Non, je veux uniquement suivre le classement ».

**Gérer un compte existant :** touchez le compte dans la liste, puis :
- **Désactiver ce compte** / **Réactiver ce compte** : un compte désactivé ne peut plus se connecter, et son historique est conservé ;
- **Nouveau mot de passe**, puis **Changer le mot de passe** ;
- **Supprimer ce compte** : une confirmation est demandée.

> Le compte `admin` par défaut ne peut être ni supprimé ni désactivé. Vous ne pouvez pas non plus supprimer ou désactiver le compte avec lequel vous êtes connecté.

---

## 12. Synchronisation en ligne

Quand le téléphone a accès à internet, cet écran envoie ou reçoit les données de la base en ligne (Supabase), **la même que celle des ordinateurs**. Il est réservé à l'administrateur et au responsable de l'épreuve.

### 12.1 Première configuration

1. Saisissez l'**URL du projet** et la **clé API publique**, les mêmes que sur la version PC, fournies par la personne en charge technique.
2. Touchez **Enregistrer la configuration**, puis **Tester la connexion**.
3. (Facultatif) Activez la **récupération automatique au démarrage** : à chaque lancement, l'application récupère les nouveautés en ligne, sans rien envoyer ni supprimer, et sans bloquer l'ouverture si vous êtes hors-ligne.

L'écran indique la date de la **dernière synchronisation réussie** du téléphone.

### 12.2 Choisir le sens de la synchronisation

| Bouton | Effet |
|---|---|
| **↓ Base en ligne → application** | Le téléphone devient **identique** à la base en ligne : ce qui a été supprimé en ligne est supprimé ici |
| **↑ Application → base en ligne** | La base en ligne devient **identique** au téléphone : ce qui a été supprimé ici est supprimé en ligne |
| **Fusionner les deux bases** | Échange dans les deux sens, **sans rien supprimer** |

Un résumé s'affiche à la fin. Les deux boutons à sens unique demandent une confirmation (**Continuer** / **Annuler**).

> Le compte `admin` et le compte connecté ne sont jamais supprimés. Une base vide ne peut pas écraser une base remplie.

> Si la même donnée a été modifiée sur deux appareils, la modification la plus récente est conservée.

---

## 13. Stockage des données

Toutes les données (pilotes, épreuves, planning, résultats, comptes) sont stockées **sur le téléphone**. L'application n'a pas besoin d'internet pour fonctionner.

**Synchronisez régulièrement** ([section 12](#12-synchronisation-en-ligne)) : c'est ce qui sauvegarde vos données en ligne et les partage avec les autres appareils. **Désinstaller l'application efface les données du téléphone.**

---

## 14. Questions fréquentes

**Je ne vois pas certaines cartes sur mon tableau de bord.**
Les cartes dépendent du rôle de votre compte (voir [section 4](#4-tableau-de-bord)). Demandez à l'administrateur si votre rôle doit changer.

**Un pilote n'apparaît pas dans la liste de saisie des résultats.**
Vérifiez que l'épreuve est cochée « active ». Un pilote est inscrit automatiquement aux épreuves actives au moment de son ajout.

**Un pilote n'apparaît pas dans le classement Hommes ou Femmes.**
Vérifiez que son genre est renseigné sur sa fiche.

**Je ne trouve pas la catégorie pompier / open ni l'export PDF du planning.**
Ces fonctions n'existent pour l'instant que sur la version PC.

**Android refuse d'installer l'application.**
Autorisez l'installation d'applications depuis la source utilisée (navigateur ou gestionnaire de fichiers), puis touchez à nouveau le fichier `.apk`.

**La synchronisation affiche une erreur.**
Vérifiez la connexion internet, l'URL et la clé API, puis touchez **Tester la connexion**.

**J'ai perdu mes données après une réinstallation.**
Si elles avaient été synchronisées, touchez **Base en ligne → application** pour les récupérer.

**Un message d'erreur apparaît, que faire ?**
Les messages sont rédigés en français simple. Si l'erreur persiste, contactez la personne en charge technique.
