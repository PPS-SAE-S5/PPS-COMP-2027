# Backlog de sprint - Sprint 3

**Durée :** semaines 45 à 48 (lundi 2 nov. - vendredi 27 nov. 2026)\
**Objectif du sprint :** Release 3 avec démo et installation, reprise complète de la documentation\
**Milestone :** [Sprint 3](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/4)

## Tâches

| ID | Tâche | Description | Priorité | Statut |
|---|---|---|---|---|
| [#11](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/11) | [US #03] Organisation des journées | En tant que gestionnaire, je veux organiser une journée contenant des ateliers (matin, après-midi, 2e journée) afin de répartir les épreuves dans le temps | must | Terminé |
| [#12](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/12) | [US #04] Score total d'un participant | En tant qu'utilisateur, je veux voir le score total d'un participant (« Participant n°X points ») afin de suivre sa performance globale | must | Terminé |
| [#13](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/13) | [US #05] Classement du jour | En tant qu'utilisateur, je veux consulter le classement du jour afin de suivre l'évolution de la compétition | must | Terminé |
| [#15](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/15) | [US #07] Rafraîchissement automatique du classement | En tant qu'utilisateur, je veux que le classement se rafraîchisse automatiquement afin d'avoir des données à jour sans recharger la page | must | Terminé |
| [#16](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/16) | [US #08] Classements sans compte | En tant qu'utilisateur, je veux accéder aux classements sans compte (lecture seule) afin de suivre la compétition facilement | must | Terminé |
| [#21](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/21) | [US #13] Historique des modifications de points | En tant que membre du comité, je veux consulter l'historique des modifications de points afin de garder une traçabilité des litiges | could | Ouvert |
| [#30](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/30) | Tâche : recherche | Ajouter une fonction de recherche (recherche d'un pilote dans le suivi des participants) | could | Terminé |
| [#33](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/33) | Tâche : classement en lecture seule | Mettre en place la page du classement du tournoi actuel en lecture seule (liée à l'US #08) | should | Terminé |
| [#39](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/39) | Tâche : journal des points | Mettre en place un historique (logs) de l'ajout et du retrait de points (liée à l'US #13) | must | Ouvert |
| [#45](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/45) | [Fix #1] Champs obligatoires d'un atelier | Un atelier pouvait être enregistré sans créneau, horaires, lieu ni responsable | should | Terminé |
| [#46](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/46) | [Fix #2] Catégories pompier / open | Distinguer les pompiers du championnat des participants « open » (invités) grâce au numéro de pilote, et renommer « mixte » en « général » | should | Terminé |
| [#50](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/50) | [Fix #6] Export PDF des journées | Exporter le planning d'une journée (ateliers, créneaux, horaires, lieux, responsables, épreuves) en PDF pour l'imprimer ou le diffuser | must | Terminé |
| [#52](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/52) | [Fix #8] Logo définitif | Remplacer le logo provisoire par le logo définitif sur la page de connexion des applications et sur la page web | must | Terminé |

---

</br>

### Demandes de la réunion 4bis, hors milestone Sprint 3

Ces demandes du client ont été enregistrées pendant le sprint mais ne font pas partie du milestone 4.\
Elles sont prévues pour le sprint 4.

| ID | Tâche | Statut |
|---|---|---|
| [#48](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/48) | [Fix #4] Import des pilotes depuis le fichier Excel des réponses au Google Form | À faire |
| [#49](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/49) | [Fix #5] Export des classements en Excel (pompier / open, hommes / femmes, général et par épreuve) | À faire |
| [#51](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/51) | [Fix #7] Limites des ateliers réglées dans un fichier de configuration plutôt que dans le code | À faire |
| [#53](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/53) | [Fix #9] Affichage des sponsors (fichier CSV) sur la page de connexion | À faire |

</br>

## Résumé du fait (pour le Sprint 3)

- [x] Planning des journées : journées, ateliers du matin et de l'après-midi, épreuves ([US #03](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/11))
- [x] Suivi des participants avec score total, rang et recherche d'un pilote ([US #04](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/12), [tâche #30](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/30))
- [x] Classement du jour et rafraîchissement automatique ([US #05](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/13), [US #07](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/15))
- [x] Classement en lecture seule sans compte : bouton "Non, je veux uniquement suivre le classement" et page web ([US #08](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/16), [tâche #33](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/33))
- [x] Champs obligatoires d'un atelier ([#45](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/45), version PC)
- [x] Catégories pompier / open et classement "général" à la place de "mixte" ([#46](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/46), version PC et page web)
- [x] Export PDF du planning d'une journée ([#50](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/50), version PC)
- [x] Logo définitif ([#52](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/52))
- [ ] Historique des modifications de points ([US #13](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/21), [tâche #39](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/39)) : _reporté pour l'instant_
- [ ] [Milestone Sprint 3](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/4) : 11/13 issues fermées (84 %) au 23/11/2026

</br>

## Notes de réunion à archiver

[Compte rendu de la réunion 4](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/main/Documentation/R%C3%A9unions/Compte%20rendu%20de%20r%C3%A9union%204.md)\
[Compte rendu de la réunion 4 - version PDF](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/main/Documentation/R%C3%A9unions/Compte%20rendu%20de%20r%C3%A9union%204.pdf)\
[Compte rendu de la réunion 4bis](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/main/Documentation/R%C3%A9unions/Compte%20rendu%20de%20r%C3%A9union%204bis.md)
