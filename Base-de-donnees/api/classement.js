// Fonction serverless Vercel : GET /api/classement
//
// La clé Supabase reste côté serveur (variables d'environnement Vercel) et n'apparaît jamais dans le HTML. 
//
//  - Classement général : pour chaque épreuve ACTIVE qui COMPTE DANS LE GÉNÉRAL, un pilote inscrit sans résultat obtient 0 point. Total = somme des points. Tri par total décroissant.
//  - Détail par épreuve : épreuves dont "afficher_classement" est vrai, résultats triés par points décroissants.
//
// Les points sont déjà calculés et stockés dans resultats.points par l'appli et dans la BD : donc on ne fait que les lire.
// Les rangs et le filtre Mixte / Hommes / Femmes sont appliqués côté navigateur.

const TAILLE_PAGE = 1000; // limite par défaut de l'API REST Supabase

function entetes() {
  const cle = process.env.SUPABASE_KEY;
  const h = { apikey: cle };
  // Les nouvelles clés (sb_publishable_... / sb_secret_...) ne sont pas des JWT :
  // on ne les envoie dans Authorization que si c'est une ancienne clé JWT (anon / service_role).
  if (cle.startsWith("eyJ")) h.Authorization = `Bearer ${cle}`;
  return h;
}

// Lit une table complète (avec pagination) en ne demandant que les colonnes utiles
async function lire(table, colonnes, ordre) {
  const base = process.env.SUPABASE_URL.replace(/\/+$/, "");
  const lignes = [];
  for (let debut = 0; ; debut += TAILLE_PAGE) {
    const url = `${base}/rest/v1/${table}?select=${colonnes}&order=${ordre}&limit=${TAILLE_PAGE}&offset=${debut}`;
    const r = await fetch(url, { headers: entetes() });
    if (!r.ok) throw new Error(`${table} : ${r.status} ${await r.text()}`);
    const page = await r.json();
    lignes.push(...page);
    if (page.length < TAILLE_PAGE) break;
  }
  return lignes;
}

const arrondir = (n) => Math.round(n * 100) / 100;

// Calcul pur
function calculer({ pilotes, epreuves, inscriptions, resultats }) {
  const piloteParId = new Map(pilotes.map((p) => [p.id, p]));
  const nomDe = (p) => [p.prenom, p.nom].filter(Boolean).join(" ");

  // Points et résultats indexés par épreuve
  const resultatsParEpreuve = new Map();
  for (const r of resultats) {
    if (!resultatsParEpreuve.has(r.epreuve_id)) resultatsParEpreuve.set(r.epreuve_id, []);
    resultatsParEpreuve.get(r.epreuve_id).push(r);
  }
  const inscritsParEpreuve = new Map();
  for (const i of inscriptions) {
    if (!inscritsParEpreuve.has(i.epreuve_id)) inscritsParEpreuve.set(i.epreuve_id, []);
    inscritsParEpreuve.get(i.epreuve_id).push(i.pilote_id);
  }

  // Épreuves comptées dans le général / épreuves au détail
  const comptees = epreuves.filter((e) => e.actif && e.compte_dans_general);
  const affichables = epreuves.filter((e) => e.afficher_classement);

  // Une ligne par pilote
  const lignes = new Map(
    pilotes.map((p) => [
      p.id,
      { nom: nomDe(p), departement: p.departement || "", genre: p.genre || "", total: 0 },
    ])
  );

  for (const e of comptees) {
    const pointsParPilote = new Map(
      (resultatsParEpreuve.get(e.id) || []).map((r) => [r.pilote_id, Number(r.points) || 0])
    );
    for (const piloteId of inscritsParEpreuve.get(e.id) || []) {
      const ligne = lignes.get(piloteId);
      if (!ligne) continue;
      ligne.total += pointsParPilote.get(piloteId) || 0; // pas de résultat = 0 pt
    }
  }

  // Tri stable par total décroissant (les ex aequo gardent l'ordre alphabétique, comme dans l'appli)
  const general = [...lignes.values()]
    .sort((a, b) => b.total - a.total)
    .map((l) => ({ ...l, total: arrondir(l.total) }));

  const detail = affichables.map((e) => ({
    id: e.id,
    nom: e.nom,
    resultats: (resultatsParEpreuve.get(e.id) || [])
      .map((r) => {
        const p = piloteParId.get(r.pilote_id);
        return {
          nom: p ? nomDe(p) : "?",
          departement: p ? p.departement || "" : "",
          genre: p ? p.genre || "" : "",
          points: arrondir(Number(r.points) || 0),
          disqualifie: !!r.disqualifie,
        };
      })
      .sort((a, b) => b.points - a.points),
  }));

  return { general, epreuves: detail };
}

async function handler(req, res) {
  try {
    if (!process.env.SUPABASE_URL || !process.env.SUPABASE_KEY) {
      throw new Error("Variables SUPABASE_URL / SUPABASE_KEY manquantes sur Vercel");
    }

    // Seulement les colonnes nécessaires : pas d'e-mail, de poids, de n° de licence, etc.
    const [pilotes, epreuves, inscriptions, resultats] = await Promise.all([
      lire("pilotes", "id,nom,prenom,departement,genre", "nom.asc,prenom.asc"),
      lire("epreuves", "id,nom,actif,compte_dans_general,afficher_classement,ordre", "ordre.asc,id.asc"),
      lire("inscriptions_epreuve", "pilote_id,epreuve_id", "id.asc"),
      lire("resultats", "epreuve_id,pilote_id,points,disqualifie", "id.asc"),
    ]);

    const corps = calculer({ pilotes, epreuves, inscriptions, resultats });

    // Cache CDN court : protège Supabase si beaucoup de visiteurs
    res.setHeader("Cache-Control", "s-maxage=20, stale-while-revalidate=20");
    res.status(200).json({ ...corps, maj: new Date().toISOString() });
  } catch (e) {
    res.status(500).json({ erreur: e.message });
  }
}

module.exports = handler;
module.exports.calculer = calculer; // exposé pour les tests
