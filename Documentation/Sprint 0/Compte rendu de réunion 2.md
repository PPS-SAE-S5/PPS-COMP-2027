# Compte rendu de la réunion 2 - PPS


</br>

- **Contexte**
	- Nous devons mettre en place une application de gestion de compétition (potentiellement réutilisable) pour la PPS

- **Problématique**
	- Date limite à fin décembre 2026
 	- Projet en groupe
  	- Championnat de France de parapente été 2027

- **Objectif de la réunion**
    - Affinage des informations de la première réunion
    - Première interaction avec Jean Marie Rolando (hors email)

</br>

---

</br>

- **Questions pour reunion 2**

    - Quels seront les différents rôles dans l’application ? (Par exemple : administrateur, participant, visiteur)

    - Quelles informations seront nécessaires pour qu’un participant puisse s’inscrire ?

    - Comment se déroule une série d’épreuves ? / Comment les points sont-ils calculés pour chaque épreuve ?

    - Quelles informations seront nécessaires pour créer une épreuve ?

    - Existe-t-il plusieurs types d’épreuves ?
        - Si oui, quelles sont leurs différences et leurs méthodes de calcul ?

    - Lors de l’affichage du classement, souhaitez-vous afficher également la note finale de chaque participant ?

    - Un visiteur peut-il consulter le classement sans être connecté ?

    - Qui doit inscrire un participant à une épreuve ?
        - Est-ce l’administrateur qui ajoute les participants à une épreuve, ou les participants peuvent-ils s’inscrire eux-mêmes ?

    - Préférez-vous une seule application accessible à la fois sur ordinateur et sur téléphone (site web responsive), ou deux applications distinctes à installer sur ordinateur et sur téléphone, accompagnées d’un site web ?

    - Est-il important pour vous que l’application puisse fonctionner sans connexion Internet ?

    - Pourrons-nous vous poser des questions par e-mail ou via Discord pendant le développement du projet ? (Communication rapide)

    - Avez-vous des préférences concernant le design de l’application ? (Par exemple : style, couleurs, logo, sponsors, etc...)

</br>

---

</br>

- **Informations nécessaires à l'inscription**
	- Numéro de licence (assurance/ fédération)
    - Nom
    - Prénom
    - Caserne
    - Poids
    - Email
    - Année de naissance
    - catégorie

</br>

- **Liste des épreuves (exemples)**
	- Atterrissage de précision
    - marche & vol
    - cross
    - checkpoint à balise en temps donnée

</br>

- **Liste de rôles**
	- Administrateur
    - Responsable de l'épreuve (paramétrage)
    - Bénévoles
    - Pilotes
    - Comité des pilotes

</br>

- **Calcul des points**
    - Marche & vol ± age, poids sac, nbr de balises
    - Chronométrage
    - Sécurité

</br>

- **À prendre en compte/ À faire**
    - Cocher une case : afficher le classement ou non
    - Stockage des données en local (en cas d'absence d'internet)
    - Toute personne inscrite doit passer les épreuves (sinon 0 pt)
    - L'administrateur coche qui a payé/ n'a pas payé les frais d'inscription (possible d'envoyer un mail de rappel)
    - Logo Ariège Tarne + sponsors **Niviuk**
    - Saisie des scores par les commissaires (départ/ arrivée)
    - Tableau facile à trier (option de tri accessibles à tous sur la page de classement)