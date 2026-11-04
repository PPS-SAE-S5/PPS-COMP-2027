// Fonction serverless Vercel : la clé Supabase reste côté serveur (variables d'environnement)
// et n'apparaît jamais dans le HTML envoyé au navigateur.

async function lire(table, select) {
  const url = `${process.env.SUPABASE_URL}/rest/v1/${table}?select=${select}`;
  const r = await fetch(url, {
    headers: {
      apikey: process.env.SUPABASE_KEY,
      Authorization: `Bearer ${process.env.SUPABASE_KEY}`,
    },
  });
  if (!r.ok) throw new Error(`${table} : ${r.status} ${await r.text()}`);
  return r.json();
}

module.exports = async (req, res) => {
  try {
    if (!process.env.SUPABASE_URL || !process.env.SUPABASE_KEY) {
      throw new Error("Variables SUPABASE_URL / SUPABASE_KEY manquantes sur Vercel");
    }

    // ⚠️ À ADAPTER à ton schéma : colonnes supposées
    //   pilotes   : id, nom, prenom
    //   resultats : pilote_id, points
    const [pilotes, resultats] = await Promise.all([
      lire("pilotes", "id,nom,prenom"),
      lire("resultats", "pilote_id,points"),
    ]);

    const total = {};
    for (const r of resultats) {
      total[r.pilote_id] = (total[r.pilote_id] || 0) + (Number(r.points) || 0);
    }

    const classement = pilotes
      .map((p) => ({
        nom: [p.prenom, p.nom].filter(Boolean).join(" "),
        points: total[p.id] || 0,
      }))
      .sort((a, b) => b.points - a.points)
      .map((p, i) => ({ rang: i + 1, ...p }));

    // Cache CDN court : protège Supabase si beaucoup de visiteurs
    res.setHeader("Cache-Control", "s-maxage=10, stale-while-revalidate=5");
    res.status(200).json({ classement, maj: new Date().toISOString() });
  } catch (e) {
    res.status(500).json({ erreur: e.message });
  }
};
