# Chiffrage - Webapp de gestion de concours de parapente

**Équipe :** 4 développeurs
**Budget nominal :** 4 × 100h = **400h** (hors heures supplémentaires)
**Date :** Septembre 2026 ~ Décembre 2026

---

## 1. Contexte

Développement d'une application web permettant d'organiser et de suivre des concours de parapente : inscription des pilotes, gestion des épreuves/manches, saisie et calcul des scores (barème classique), classements et diffusion des résultats.

- **Destination actuelle :** un concours organisé pour des pompiers.
- **Projet open source (GitHub)**, pensé pour être **réutilisable ultérieurement pour d'autres types de concours**.
- L'application doit gérer **plusieurs concours** (historique) et être **totalement responsive** (mobile et desktop).

---

## 2. Choix techniques retenus

| Élément | Choix |
|---|---|
| Backend | Node.js (quasi certain) |
| Frontend | Vue |
| Base de données | SQL (PostgreSQL ?) |
| Hébergement | Première observation : Render, Railway, Fly.io pour le back + BDD ; Vercel/Netlify pour le front |
| Dépôt | GitHub public |
| Identité graphique | Carte blanche/ rien de décider pour l'instant |

> 💡 Le code doit être écrit en gardant une **nomenclature générique** ("compétition", "épreuve" plutôt que des termes spécifiques au parapente) afin de ne pas fermer la porte à une réutilisation future pour d'autres types d'épreuves.

---

## 3. Périmètre fonctionnel retenu

| Domaine | Fonctionnalités incluses |
|---|---|
| Authentification | Administrateur / bénévoles / pilotes / Comité des pilotes  |
| Compétitions | Création et gestion de **plusieurs épreuves** (en cours + historique), manches, dates, lieu |
| Pilotes | Pré-inscription en ligne, profil (nom, licence, caserne, catégorie, etc...) |
| Scores | Saisie manuelle des résultats, **barème de calcul classique/standard** |
| Classements | Calcul automatique du classement général/ par manche visible |
| Résultats publics | Page publique consultable sans compte, et responsive évidemment |
| Back-office | Interface d'administration (CRUD concours, manches, pilotes, scores), et responsive (encore) |
| Export | Export PDF/Excel des classements |
| Notifications | Email de confirmation d'inscription/ rappel de paiement des frais d'inscription |
| Responsive | Mobile + desktop sur l'ensemble de l'application |

### Hors périmètre (non chiffré ici)
- Import automatique de traces GPS / intégration trackers - confirmé non nécessaire [mais vu lors de la réunion 2](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/3611c4203475c93ef5d7aedcf57af4cf6bd8197b/Documentation/Sprint%200/Compte%20rendu%20de%20r%C3%A9union%202.md)
- Application mobile native
- Multi-langue (?)

---

## 4. Répartition des lots et estimation

| # | Lot | Description | Estimation (h) |
|---|---|---|---|
| 1 | Cadrage & spécifications | Ateliers, rédaction des specs (barème classique déjà identifié, moins d'exploration) | 14h |
| 2 | Setup projet & architecture | Repo GitHub, CI/CD, environnements, nomenclature générique | 22h |
| 3 | UI/UX Design | Identité graphique minimale + maquettes responsive (mobile & desktop) | 32h |
| 4 | Authentification & rôles | Login, gestion des sessions, rôles admin/bénévoles/comité | 20h |
| 5 | Module Compétitions & Manches | CRUD multi-concours, historique, gestion des manches | 34h |
| 6 | Module Scores & Classements | Saisie des scores, implémentation MVP du barème classique, classements | 40h |
| 7 | Page publique de résultats | Affichage responsive des classements | 26h |
| 8 | Back-office admin | Interface de gestion globale, responsive | 30h |
| 9 | Export & notifications | Export PDF/Excel, emails transactionnels | 16h |
| 10 | Tests | Tests unitaires et d'intégration sur les modules critiques | 28h |
| 11 | Hébergement & déploiement | Mise en place hébergement low-cost (PaaS), domaine, SSL, mise en prod | 16h |
| 12 | Recette & corrections | Tests fonctionnels multi-device, corrections de bugs | 22h |
| 13 | Documentation | Doc technique, guide utilisateur admin, README + licence (repo public) | 12h |
| 14 | Gestion de projet | Réunions d'équipe, points d'avancement, coordination | 18h |
| | **TOTAL** | | **≈ 330h** |

**Marge de sécurité (imprévus) : ~15% → +50h**

**Total estimé avec marge : ≈ 400h**

---

## 5. Analyse par rapport au budget

- Budget nominal : **380h**
- Estimation avec marge : **≈ 400h**
- Écart : **+20h**, soit environ **5h par développeur** - largement absorbable dans une marge d'heures sup raisonnable, sans remettre en cause le planning.

---

## 6. Bonus si le temps le permet (hors budget des 400h)

Le barème étant volontairement livré en **version minimale** au départ, voici les améliorations possibles à caser en fin de projet si de la marge subsiste :

| Amélioration | Estimation (h) |
|---|---|
| Pondérations avancées du barème (coefficients par manche, bonus/malus) | ~10h |
| Interface de configuration du barème par concours (au lieu d'un barème codé en dur qui sera, rappel, fait par l'administrateur) | ~8h |
| Historique/statistiques des participants sur plusieurs compétitions | ~10h |

---

## 7. Répartition indicative par profil

| Profil suggéré | Lots concernés | Charge estimée |
|---|---|---|
| Dev Backend (x1-2) | Auth, Compétitions, Participants, Scores/Classements, Export | ~125h |
| Dev Frontend (x1-2) | UI/UX, Page publique, Back-office | ~115h |
| Full-stack / DevOps | Setup, hébergement/déploiement, tests transverses | ~85h |
| Chef de projet technique (partagé) | Cadrage, gestion de projet, recette | ~65h |

---

## 8. Planning indicatif

| Semaine | Activité |
|---|---|
| S1 | Cadrage, setup GitHub/CI, maquettes |
| S2-S3 | Auth, classement, interfaces, page publique  |
| S4-S5 | correctifs, création d'ateliers |
| S6 | Back-office, exports, notifications (push tel/ email) |
| S7 | Tests, ajouts de dernière minute|
| S8 | Corrections, mise en prod, livraison + bonus si marge disponible |

---

## 9. Estimation tarifaire de l'hébergement (ordre de grandeur, à valider plus tard)

Ces coûts sont **séparés du budget de développement (400h)** - ce sont des frais récurrents une fois l'app en ligne

| Poste | Solution low-cost typique | Estimation basse | Estimation haute |
|---|---|---|---|
| Backend Node.js (API) | Render / Railway / Fly.io (petite instance) | 0 €/mois (tier gratuit, avec limites) | ~10 €/mois |
| Base de données PostgreSQL | Tier gratuit (Render/Railway/Supabase, souvent limité en durée ou en volume) puis payant | 0 €/mois | ~10 €/mois |
| Frontend Vue (statique/SPA) | Vercel / Netlify (tier gratuit largement suffisant) | 0 €/mois | 0 €/mois |
| Emails transactionnels | Brevo / Mailgun (tier gratuit, quelques centaines d'emails/jour) | 0 €/mois | ~10 €/mois |
| Nom de domaine | Registrar classique (.fr, .org...) | ~10 €/an | ~20 €/an |
| **Total estimé** | | **≈ 10 €/an** (si tout reste en tiers gratuits) | **≈ 250-300 €/an** (si passage sur offres payantes) |

**En résumé :** pour un usage léger et saisonnier (concours ponctuel), il est tout à fait réaliste de rester sur des **tiers gratuits** pendant un moment (coût quasi nul, hors nom de domaine), quitte à passer sur une offre payante à quelques euros/mois si le trafic ou le volume de données augmente. Une fourchette réaliste à prévoir : **0 à 25 €/mois**, soit **~120 à 300 €/an** en cas de montée en charge.

PS : il est possible que cela change au cours du projet selon les ressources à notre disposition /!\

---

## 10. Estimation du coût salarial (côté entreprise)


| Profil | Taux horaire chargé (estimation) | Coût pour 400h (estimation finale) |
|---|---|---|
| **Développeur junior (×4)** | **~30 €/h** | **12 000 €** |

**Important :** *(Base de calcul : salaire brut junior ~35 k€/an, coût chargé employeur ~50 k€/an, soit ~31 €/h sur une base de 1 607h/an - arrondi à 30 €/h)*

| Statut | Coût horaire | Coût pour 400h |
|---|---|---|
| Stagiaire (gratification légale minimale) | ~4,35 €/h | ~1 740 € |
| Alternant | ~8-15 €/h (selon âge/année) | ~3 200-6 000 € |
| Salarié junior (CDI/CDD) | ~30 €/h | ~12 000 € |

**Coût total projet (indicatif) :** en additionnant coût salarial (**~12 000 €** si salariés juniors, bien moins si stage/alternance) + hébergement (~10 € à 300 €/an), le projet se situe autour de **12 000€** pour la première année dans l'hypothèse "salarié junior".

---

## 11. Points encore ouverts

- **Choix précis de l'hébergeur low-cost**
- **Statut réel de l'équipe** nos rôles, nos ressources
- **Licence open source** à choisir pour le dépôt GitHub (MIT, AGPL, etc) <-- à discuter avec un représentant pédagogique

---