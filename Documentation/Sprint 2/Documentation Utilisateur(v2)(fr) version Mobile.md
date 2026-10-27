# Documentation Utilisateur (fr) v2 — version Mobile (Android)

## Sommaire

- [1. Présentation générale](#1-présentation-générale)
- [2. Installation (version de test)](#2-installation)
- [3. Connexion et rôles](#3-connexion-et-rôles)
- [4. Tableau de bord](#4-tableau-de-bord)
- [5. Gestion des pilotes](#5-gestion-des-pilotes)
- [6. Gestion des épreuves](#6-gestion-des-épreuves)
- [7. Saisie des résultats](#7-saisie-des-résultats)
- [8. Classements](#8-classements)
- [9. Gestion des comptes](#9-gestion-des-comptes-administrateur)
- [10. Synchronisation en ligne](#10-synchronisation-en-ligne)
- [11. Stockage des données](#11-stockage-des-données)
- [12. Questions fréquentes](#12-questions-fréquentes)

---

## 1. Présentation générale

L'application mobile du championnat est faite pour une utilisation **sur le terrain**, depuis un
téléphone Android. Elle est **en cours de développement** et reprend pour l'instant une partie des
fonctionnalités de la version PC :

- inscrire les pilotes,
- créer librement les épreuves du championnat et choisir comment les points sont calculés,
- saisir les résultats au fur et à mesure des épreuves,
- consulter le **classement** général ou par épreuve, mixte ou séparé hommes / femmes,
- gérer les comptes (Administrateur),
- **synchroniser** les données avec les autres appareils.

L'application fonctionne **sans internet** : les données sont d'abord enregistrées sur le téléphone,
puis partagées en ligne dès qu'une connexion est disponible (voir [section 10](#10-synchronisation-en-ligne)).

> L'application mobile est **en retard sur la version PC**.\
> Les fonctionnalités suivantes **n'existent pas encore** sur mobile (elles sont disponibles uniquement dans la version PC) :
>
> - le **planning des journées** (journées, ateliers, épreuves) ;
> - le **suivi des participants** (détail des scores d'un pilote).

---

## 2. Installation

> **Aucune release installable (APK) de l'application mobile n'est encore disponible.**
> L'application est en phase de développement et de test : elle n'est pas encore distribuée aux utilisateurs.
> Cette section sera complétée lorsque la première release mobile sera publiée.

Pour l'instant, l'application est installée uniquement par l'équipe technique, sur des téléphones de test
**Android 8.0 ou plus récent**, directement depuis le code source (Android Studio).

> Pendant la phase de test, une **mise à jour de l'application peut effacer les données locales** du téléphone (la base est recréée). 
> Synchronisez vos données avant toute mise à jour (voir [section 10](#10-synchronisation-en-ligne)).

---

## 3. Connexion et rôles

Au démarrage, l'application affiche un écran de connexion : saisissez votre
**identifiant** et votre **mot de passe**, puis connectez-vous.

![Écran de connexion](./../Sprint%201/Captures/interface_mobile_login.png)

Chaque compte a un **rôle**, qui détermine ce qu'il peut faire dans l'application :

| Rôle | Peut faire |
|---|---|
| **Administrateur** | Tout, y compris créer les comptes des autres utilisateurs |
| **Responsable de l'épreuve** | Créer/modifier les épreuves, gérer les pilotes, saisir des résultats, consulter les classements, synchroniser |
| **Bénévole** | Gérer les pilotes, saisir des résultats, consulter les classements |
| **Pilote** | Consulter les classements uniquement |
| **Comité des pilotes** | Consulter les classements uniquement |

Un compte administrateur existe par défaut à la première installation.
**Changez son mot de passe dès la première utilisation** (voir [section 9](#9-gestion-des-comptes-administrateur)).

---

## 4. Tableau de bord

Après connexion, le tableau de bord affiche, sous forme de cartes à toucher, uniquement les
fonctionnalités auxquelles votre rôle donne accès. Votre identifiant et votre rôle s'affichent
en haut, avec le bouton **Déconnexion**.

![Tableau de bord](./../Sprint%201/Captures/interface_mobile_accueil.png)

| Carte | Rôles concernés |
|---|---|
| Gestion des épreuves | Administrateur, Responsable de l'épreuve |
| Gestion des pilotes | Administrateur, Responsable de l'épreuve, Bénévole |
| Saisie des résultats | Administrateur, Responsable de l'épreuve, Bénévole |
| Classements | Tous les rôles |
| Gestion des comptes | Administrateur |
| Synchronisation en ligne | Administrateur, Responsable de l'épreuve |

---

## 5. Gestion des pilotes

Cet écran sert à inscrire les pilotes au championnat. La liste affiche, pour chaque pilote, son nom,
son numéro de licence, sa caserne, son genre, son année de naissance et son poids.

![Liste des pilotes](./../Sprint%201/Captures/pilotes_mobile_gerer.png)

**Inscrire un pilote :**

1. Touchez **+ Nouveau pilote**.
2. Remplissez la **fiche pilote** :
   - Numéro de licence (obligatoire, unique, sert d'identifiant)
   - Nom, prénom (obligatoires)
   - Caserne
   - Poids
   - Email
   - Année de naissance
   - Genre (Homme ou Femme)
3. Touchez **Ajouter le pilote**.

![Fiche pilote](#)
<!-- Image à fournir : Sprint 2/Captures/mobile_pilote_fiche.png -->

**Modifier ou supprimer un pilote :** touchez sa fiche dans la liste, modifiez les champs puis touchez
**Enregistrer les modifications**, ou touchez **Supprimer ce pilote** (cela supprime aussi tous ses résultats déjà saisis ; une confirmation est demandée).

> Dès qu'un pilote est ajouté, il est automatiquement inscrit à toutes les épreuves déjà
> actives. S'il ne se présente pas à une épreuve, il aura automatiquement 0 point à celle-ci
> dans le classement général.

> Le **genre** sert aux classements séparés hommes / femmes ([section 8](#8-classements)).
> Un pilote dont le genre n'est pas renseigné n'apparaît que dans le classement mixte.

---

## 6. Gestion des épreuves

**Aucune épreuve n'est prédéfinie** : le club crée librement chaque épreuve du championnat
(atterrissage de précision, marche & vol, cross, checkpoint... ou toute autre épreuve),
avec ses propres règles de calcul des points.

![Liste des épreuves](#)
<!-- Image à fournir : Sprint 2/Captures/mobile_epreuves_liste.png -->

La liste affiche chaque épreuve avec sa règle (formule, ou barème avec la variable de classement et son sens).
Touchez **Nouvelle épreuve** pour en créer une, ou touchez une épreuve existante pour la modifier
(**Modifier l'épreuve**).

![Création d'une épreuve](#)
<!-- Image à fournir : Sprint 2/Captures/mobile_epreuve_form.png -->

### 6.1 Informations générales

- **Nom** et **description** de l'épreuve.

### 6.2 Variables saisissables

Définissez les valeurs qui devront être mesurées/saisies (ex : `temps`, `nbrBalises`, `poidsSac`, `distance`...).
Chaque variable a :
- un **nom technique** (sans espace, utilisé dans le calcul),
- un **libellé affiché**,
- une **unité** (facultative).

Touchez **+ Ajouter une variable** (le bouton ✕ retire une variable).

### 6.3 Mode de calcul des points

Deux modes au choix :

**a) Formule mathématique** — vous écrivez une formule avec les noms de variables, par exemple :

```
100 - temps*2 + nbrBalises*10 - poidsSac*0.5
```

Deux variables sont automatiquement disponibles : `age` et `poidsPilote` (issues de la fiche du pilote).

**b) Barème par classement (rang)** — les pilotes sont classés selon une **variable de classement**
(ex : `temps`, `distance`), dans le sens choisi (la plus petite valeur gagne, ou la plus grande),
puis reçoivent des points selon le **barème de points par rang** (ex : 1er = 100 pts, 2e = 90 pts...).
Touchez **+ Ajouter un rang** pour compléter le barème. Au-delà du dernier rang défini,
les points du dernier palier sont reconduits.

> Le barème se recalcule automatiquement pour **tous** les pilotes à chaque nouvelle saisie.

### 6.4 Options de l'épreuve

- **Afficher le classement de cette épreuve** : rend le détail de l'épreuve consultable et affiche sa colonne dans le classement général.
- **Compte dans le classement général** : les points sont additionnés au total du championnat.
- **Épreuve active (proposée à la saisie)** : l'épreuve apparaît (ou non) dans l'écran de saisie.

Terminez avec **Enregistrer l'épreuve**. Pour supprimer une épreuve, touchez **Supprimer cette épreuve**
(cela supprime aussi ses résultats ; une confirmation est demandée).

---

## 7. Saisie des résultats

![Saisie des résultats](#)
<!-- Image à fournir : Sprint 2/Captures/mobile_saisie.png -->

1. Choisissez l'**épreuve** concernée.
2. Choisissez le **pilote** (seuls les pilotes inscrits à cette épreuve apparaissent).
3. Le formulaire s'adapte **automatiquement** : un champ par variable définie pour l'épreuve.
4. Renseignez les valeurs mesurées.
5. Si le pilote doit être disqualifié (problème de sécurité, etc.), cochez
   **Disqualifié / problème de sécurité (0 point automatique)**.
6. Touchez **Enregistrer le résultat**.

La liste des résultats déjà saisis pour l'épreuve s'affiche en dessous, avec les points calculés.

![Saisie avec disqualification](#)
<!-- Image à fournir : Sprint 2/Captures/mobile_saisie_disq.png -->

---

## 8. Classements

L'écran **Classements** propose deux onglets, **Général** et **Détail par épreuve**, et un bouton
**Actualiser** pour recalculer après de nouvelles saisies.

![Classement général](#)
<!-- Image à fournir : Sprint 2/Captures/mobile_classement_general.png -->

- **Général** : rang, points sur chaque épreuve comptant au général, et total de chaque pilote.
  La liste **Classement** permet de choisir **Mixte**, **Hommes** ou **Femmes** ; les rangs sont
  recalculés parmi les pilotes retenus. Un pilote sans genre n'apparaît que dans le classement mixte.
- **Détail par épreuve** : classement complet d'une seule épreuve, avec les valeurs saisies.
  Seules les épreuves dont « Afficher le classement » est coché sont proposées.

![Détail par épreuve](#)
<!-- Image à fournir : Sprint 2/Captures/mobile_classement_epreuve.png -->

> Un pilote inscrit sans résultat saisi obtient automatiquement 0 point à l'épreuve.

Cet écran est accessible à **tous les rôles**.

---

## 9. Gestion des comptes (Administrateur)

Réservé au rôle Administrateur.

![Gestion des comptes](./../Sprint%201/Captures/compte_mobile_gestion.png)

**Créer un compte :** dans **Nouveau compte**, saisissez l'**identifiant**, le **mot de passe** et choisissez le **rôle**
dans la liste, puis touchez **Créer le compte**. Les identifiants sont uniques, sans tenir compte des majuscules.

**Gérer un compte existant :** touchez le compte dans la liste, puis :
- **Désactiver ce compte** / **Réactiver ce compte** : un compte désactivé ne peut plus se connecter, sans perdre son historique ;
- **Nouveau mot de passe** puis **Changer le mot de passe** ;
- **Supprimer ce compte** (une confirmation est demandée).

> Le compte `admin` par défaut ne peut ni être supprimé, ni désactivé. Vous ne pouvez pas non plus supprimer ou désactiver le compte avec lequel vous êtes connecté.

---

## 10. Synchronisation en ligne

Quand le téléphone retrouve internet, cet écran permet d'envoyer ou de recevoir les données avec la base
en ligne (Supabase), **la même que celle des ordinateurs**. Il est réservé à l'Administrateur et au
Responsable de l'épreuve.

![Synchronisation en ligne](#)
<!-- Image à fournir : Sprint 2/Captures/mobile_sync.png -->

### 10.1 Première configuration

1. Saisissez l'**URL du projet** et la **clé API publique** (les mêmes que sur la version PC, fournies par l'équipe technique).
2. Touchez **Enregistrer la configuration**, puis **Tester la connexion**.
3. (Facultatif) Activez la **synchronisation automatique au démarrage** : l'application récupère
   discrètement les nouveautés au lancement, sans rien envoyer ni supprimer, et sans bloquer
   l'ouverture si vous êtes hors-ligne.

L'écran indique la **dernière synchronisation réussie** de l'appareil.

### 10.2 Choisir le sens de la synchronisation

| Bouton | Effet |
|---|---|
| **↓ Base en ligne → application** | Le téléphone devient **identique** à la base en ligne (ce qui a été supprimé en ligne est supprimé ici) |
| **↑ Application → base en ligne** | La base en ligne devient **identique** au téléphone (ce qui a été supprimé ici est supprimé en ligne) |
| **Fusionner les deux bases** | Échange dans les deux sens, **sans jamais rien supprimer** |

Un résumé s'affiche à la fin. Les deux boutons « sens unique » demandent une confirmation (**Continuer** / **Annuler**).

> Le compte `admin` et le compte connecté ne sont jamais supprimés. Une base vide ne peut pas écraser une base remplie.

> Si la même donnée a été modifiée sur deux appareils, la modification la plus récente est conservée.

---

## 11. Stockage des données

Toutes les données (pilotes, épreuves, résultats, comptes) sont stockées **sur le téléphone**.
Aucune connexion internet n'est nécessaire au quotidien.

**Pensez à synchroniser régulièrement** (section 10) : c'est ce qui sauvegarde vos données en ligne
et les partage avec les autres appareils. **Désinstaller l'application efface les données du téléphone**.

---

## 12. Questions fréquentes

**Je ne vois pas certaines cartes sur mon tableau de bord.**
Les cartes dépendent du rôle de votre compte (voir [section 4](#4-tableau-de-bord)). Demandez à l'Administrateur si votre rôle doit être modifié.

**Un pilote n'apparaît pas dans la liste de saisie des résultats.**
Vérifiez que l'épreuve est bien « active » et que le pilote a été inscrit.

**Un pilote n'apparaît pas dans le classement Hommes ou Femmes.**
Vérifiez que son genre est renseigné sur sa fiche.

**Où est le planning des journées ? Où est le suivi des participants ?**
Ces deux fonctionnalités n'existent pas encore sur mobile : l'application mobile est en retard sur la version PC.
Elles sont disponibles uniquement dans la version PC.

**La synchronisation affiche une erreur.**
Vérifiez la connexion internet, l'URL et la clé API, puis touchez **Tester la connexion**.

**J'ai perdu mes données après une mise à jour.**
Pendant la phase de test, une mise à jour peut recréer la base locale. Faites **Base en ligne → application**
pour récupérer vos données (si elles avaient été synchronisées auparavant).

**Un message d'erreur apparaît, que faire ?**
Les messages sont rédigés en français simple. S'ils persistent, contactez la personne en charge technique.
