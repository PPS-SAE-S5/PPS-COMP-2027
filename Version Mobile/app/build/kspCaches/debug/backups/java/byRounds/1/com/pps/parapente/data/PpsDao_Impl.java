package com.pps.parapente.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PpsDao_Impl implements PpsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UtilisateurEntity> __insertionAdapterOfUtilisateurEntity;

  private final EntityInsertionAdapter<PiloteEntity> __insertionAdapterOfPiloteEntity;

  private final EntityInsertionAdapter<EpreuveEntity> __insertionAdapterOfEpreuveEntity;

  private final EntityInsertionAdapter<ParametreEntity> __insertionAdapterOfParametreEntity;

  private final EntityInsertionAdapter<BaremeEntity> __insertionAdapterOfBaremeEntity;

  private final EntityInsertionAdapter<ResultatEntity> __insertionAdapterOfResultatEntity;

  private final EntityDeletionOrUpdateAdapter<PiloteEntity> __updateAdapterOfPiloteEntity;

  private final EntityDeletionOrUpdateAdapter<EpreuveEntity> __updateAdapterOfEpreuveEntity;

  private final SharedSQLiteStatement __preparedStmtOfSupprimerUtilisateur;

  private final SharedSQLiteStatement __preparedStmtOfSupprimerPilote;

  private final SharedSQLiteStatement __preparedStmtOfSupprimerEpreuve;

  private final SharedSQLiteStatement __preparedStmtOfSupprimerParametres;

  private final SharedSQLiteStatement __preparedStmtOfSupprimerBareme;

  private final SharedSQLiteStatement __preparedStmtOfInscrireTousLesPilotes;

  private final SharedSQLiteStatement __preparedStmtOfInscrirePiloteAuxEpreuvesActives;

  public PpsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUtilisateurEntity = new EntityInsertionAdapter<UtilisateurEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `utilisateurs` (`id`,`identifiant`,`motDePasse`,`role`,`piloteId`,`actif`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UtilisateurEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getIdentifiant());
        statement.bindString(3, entity.getMotDePasse());
        statement.bindString(4, entity.getRole());
        if (entity.getPiloteId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getPiloteId());
        }
        final int _tmp = entity.getActif() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__insertionAdapterOfPiloteEntity = new EntityInsertionAdapter<PiloteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pilotes` (`id`,`numeroLicence`,`nom`,`prenom`,`caserne`,`poids`,`email`,`anneeNaissance`,`categorie`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PiloteEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNumeroLicence());
        statement.bindString(3, entity.getNom());
        statement.bindString(4, entity.getPrenom());
        if (entity.getCaserne() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCaserne());
        }
        if (entity.getPoids() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getPoids());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getEmail());
        }
        if (entity.getAnneeNaissance() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getAnneeNaissance());
        }
        if (entity.getCategorie() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCategorie());
        }
      }
    };
    this.__insertionAdapterOfEpreuveEntity = new EntityInsertionAdapter<EpreuveEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `epreuves` (`id`,`nom`,`description`,`modeCalcul`,`formule`,`valeurCle`,`sensClassement`,`afficherClassement`,`compteDansGeneral`,`actif`,`ordre`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EpreuveEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNom());
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescription());
        }
        statement.bindString(4, entity.getModeCalcul());
        if (entity.getFormule() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getFormule());
        }
        if (entity.getValeurCle() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getValeurCle());
        }
        statement.bindString(7, entity.getSensClassement());
        final int _tmp = entity.getAfficherClassement() ? 1 : 0;
        statement.bindLong(8, _tmp);
        final int _tmp_1 = entity.getCompteDansGeneral() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        final int _tmp_2 = entity.getActif() ? 1 : 0;
        statement.bindLong(10, _tmp_2);
        statement.bindLong(11, entity.getOrdre());
      }
    };
    this.__insertionAdapterOfParametreEntity = new EntityInsertionAdapter<ParametreEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `epreuve_parametres` (`id`,`epreuveId`,`nomVariable`,`label`,`unite`,`obligatoire`,`ordre`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ParametreEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEpreuveId());
        statement.bindString(3, entity.getNomVariable());
        statement.bindString(4, entity.getLabel());
        if (entity.getUnite() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getUnite());
        }
        final int _tmp = entity.getObligatoire() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getOrdre());
      }
    };
    this.__insertionAdapterOfBaremeEntity = new EntityInsertionAdapter<BaremeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `bareme_points` (`id`,`epreuveId`,`rang`,`points`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BaremeEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEpreuveId());
        statement.bindLong(3, entity.getRang());
        statement.bindDouble(4, entity.getPoints());
      }
    };
    this.__insertionAdapterOfResultatEntity = new EntityInsertionAdapter<ResultatEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `resultats` (`id`,`epreuveId`,`piloteId`,`valeursJson`,`points`,`disqualifie`,`saisiPar`,`dateSaisie`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ResultatEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEpreuveId());
        statement.bindLong(3, entity.getPiloteId());
        statement.bindString(4, entity.getValeursJson());
        statement.bindDouble(5, entity.getPoints());
        final int _tmp = entity.getDisqualifie() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getSaisiPar() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSaisiPar());
        }
        statement.bindLong(8, entity.getDateSaisie());
      }
    };
    this.__updateAdapterOfPiloteEntity = new EntityDeletionOrUpdateAdapter<PiloteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `pilotes` SET `id` = ?,`numeroLicence` = ?,`nom` = ?,`prenom` = ?,`caserne` = ?,`poids` = ?,`email` = ?,`anneeNaissance` = ?,`categorie` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PiloteEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNumeroLicence());
        statement.bindString(3, entity.getNom());
        statement.bindString(4, entity.getPrenom());
        if (entity.getCaserne() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCaserne());
        }
        if (entity.getPoids() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getPoids());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getEmail());
        }
        if (entity.getAnneeNaissance() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getAnneeNaissance());
        }
        if (entity.getCategorie() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCategorie());
        }
        statement.bindLong(10, entity.getId());
      }
    };
    this.__updateAdapterOfEpreuveEntity = new EntityDeletionOrUpdateAdapter<EpreuveEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `epreuves` SET `id` = ?,`nom` = ?,`description` = ?,`modeCalcul` = ?,`formule` = ?,`valeurCle` = ?,`sensClassement` = ?,`afficherClassement` = ?,`compteDansGeneral` = ?,`actif` = ?,`ordre` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EpreuveEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNom());
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescription());
        }
        statement.bindString(4, entity.getModeCalcul());
        if (entity.getFormule() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getFormule());
        }
        if (entity.getValeurCle() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getValeurCle());
        }
        statement.bindString(7, entity.getSensClassement());
        final int _tmp = entity.getAfficherClassement() ? 1 : 0;
        statement.bindLong(8, _tmp);
        final int _tmp_1 = entity.getCompteDansGeneral() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        final int _tmp_2 = entity.getActif() ? 1 : 0;
        statement.bindLong(10, _tmp_2);
        statement.bindLong(11, entity.getOrdre());
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfSupprimerUtilisateur = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM utilisateurs WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSupprimerPilote = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pilotes WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSupprimerEpreuve = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM epreuves WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSupprimerParametres = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM epreuve_parametres WHERE epreuveId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSupprimerBareme = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM bareme_points WHERE epreuveId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfInscrireTousLesPilotes = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "INSERT OR IGNORE INTO inscriptions (piloteId, epreuveId) SELECT id, ? FROM pilotes";
        return _query;
      }
    };
    this.__preparedStmtOfInscrirePiloteAuxEpreuvesActives = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "INSERT OR IGNORE INTO inscriptions (piloteId, epreuveId) SELECT ?, id FROM epreuves WHERE actif = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insererUtilisateur(final UtilisateurEntity u,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfUtilisateurEntity.insertAndReturnId(u);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insererPilote(final PiloteEntity p, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPiloteEntity.insertAndReturnId(p);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insererEpreuve(final EpreuveEntity e,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfEpreuveEntity.insertAndReturnId(e);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insererParametres(final List<ParametreEntity> liste,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfParametreEntity.insert(liste);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insererBareme(final List<BaremeEntity> liste,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBaremeEntity.insert(liste);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object enregistrerResultat(final ResultatEntity r,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfResultatEntity.insert(r);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object majPilote(final PiloteEntity p, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPiloteEntity.handle(p);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object majEpreuve(final EpreuveEntity e, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfEpreuveEntity.handle(e);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object supprimerUtilisateur(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSupprimerUtilisateur.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSupprimerUtilisateur.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object supprimerPilote(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSupprimerPilote.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSupprimerPilote.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object supprimerEpreuve(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSupprimerEpreuve.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSupprimerEpreuve.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object supprimerParametres(final int epreuveId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSupprimerParametres.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, epreuveId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSupprimerParametres.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object supprimerBareme(final int epreuveId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSupprimerBareme.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, epreuveId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSupprimerBareme.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object inscrireTousLesPilotes(final int epreuveId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfInscrireTousLesPilotes.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, epreuveId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeInsert();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfInscrireTousLesPilotes.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object inscrirePiloteAuxEpreuvesActives(final int piloteId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfInscrirePiloteAuxEpreuvesActives.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, piloteId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeInsert();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfInscrirePiloteAuxEpreuvesActives.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object utilisateurParIdentifiant(final String identifiant,
      final Continuation<? super UtilisateurEntity> $completion) {
    final String _sql = "SELECT * FROM utilisateurs WHERE identifiant = ? AND actif = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, identifiant);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UtilisateurEntity>() {
      @Override
      @Nullable
      public UtilisateurEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIdentifiant = CursorUtil.getColumnIndexOrThrow(_cursor, "identifiant");
          final int _cursorIndexOfMotDePasse = CursorUtil.getColumnIndexOrThrow(_cursor, "motDePasse");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfPiloteId = CursorUtil.getColumnIndexOrThrow(_cursor, "piloteId");
          final int _cursorIndexOfActif = CursorUtil.getColumnIndexOrThrow(_cursor, "actif");
          final UtilisateurEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpIdentifiant;
            _tmpIdentifiant = _cursor.getString(_cursorIndexOfIdentifiant);
            final String _tmpMotDePasse;
            _tmpMotDePasse = _cursor.getString(_cursorIndexOfMotDePasse);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final Integer _tmpPiloteId;
            if (_cursor.isNull(_cursorIndexOfPiloteId)) {
              _tmpPiloteId = null;
            } else {
              _tmpPiloteId = _cursor.getInt(_cursorIndexOfPiloteId);
            }
            final boolean _tmpActif;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfActif);
            _tmpActif = _tmp != 0;
            _result = new UtilisateurEntity(_tmpId,_tmpIdentifiant,_tmpMotDePasse,_tmpRole,_tmpPiloteId,_tmpActif);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object tousUtilisateurs(final Continuation<? super List<UtilisateurEntity>> $completion) {
    final String _sql = "SELECT * FROM utilisateurs ORDER BY identifiant";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<UtilisateurEntity>>() {
      @Override
      @NonNull
      public List<UtilisateurEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIdentifiant = CursorUtil.getColumnIndexOrThrow(_cursor, "identifiant");
          final int _cursorIndexOfMotDePasse = CursorUtil.getColumnIndexOrThrow(_cursor, "motDePasse");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfPiloteId = CursorUtil.getColumnIndexOrThrow(_cursor, "piloteId");
          final int _cursorIndexOfActif = CursorUtil.getColumnIndexOrThrow(_cursor, "actif");
          final List<UtilisateurEntity> _result = new ArrayList<UtilisateurEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UtilisateurEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpIdentifiant;
            _tmpIdentifiant = _cursor.getString(_cursorIndexOfIdentifiant);
            final String _tmpMotDePasse;
            _tmpMotDePasse = _cursor.getString(_cursorIndexOfMotDePasse);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final Integer _tmpPiloteId;
            if (_cursor.isNull(_cursorIndexOfPiloteId)) {
              _tmpPiloteId = null;
            } else {
              _tmpPiloteId = _cursor.getInt(_cursorIndexOfPiloteId);
            }
            final boolean _tmpActif;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfActif);
            _tmpActif = _tmp != 0;
            _item = new UtilisateurEntity(_tmpId,_tmpIdentifiant,_tmpMotDePasse,_tmpRole,_tmpPiloteId,_tmpActif);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object tousPilotes(final Continuation<? super List<PiloteEntity>> $completion) {
    final String _sql = "SELECT * FROM pilotes ORDER BY nom, prenom";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PiloteEntity>>() {
      @Override
      @NonNull
      public List<PiloteEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumeroLicence = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroLicence");
          final int _cursorIndexOfNom = CursorUtil.getColumnIndexOrThrow(_cursor, "nom");
          final int _cursorIndexOfPrenom = CursorUtil.getColumnIndexOrThrow(_cursor, "prenom");
          final int _cursorIndexOfCaserne = CursorUtil.getColumnIndexOrThrow(_cursor, "caserne");
          final int _cursorIndexOfPoids = CursorUtil.getColumnIndexOrThrow(_cursor, "poids");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfAnneeNaissance = CursorUtil.getColumnIndexOrThrow(_cursor, "anneeNaissance");
          final int _cursorIndexOfCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "categorie");
          final List<PiloteEntity> _result = new ArrayList<PiloteEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PiloteEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNumeroLicence;
            _tmpNumeroLicence = _cursor.getString(_cursorIndexOfNumeroLicence);
            final String _tmpNom;
            _tmpNom = _cursor.getString(_cursorIndexOfNom);
            final String _tmpPrenom;
            _tmpPrenom = _cursor.getString(_cursorIndexOfPrenom);
            final String _tmpCaserne;
            if (_cursor.isNull(_cursorIndexOfCaserne)) {
              _tmpCaserne = null;
            } else {
              _tmpCaserne = _cursor.getString(_cursorIndexOfCaserne);
            }
            final Double _tmpPoids;
            if (_cursor.isNull(_cursorIndexOfPoids)) {
              _tmpPoids = null;
            } else {
              _tmpPoids = _cursor.getDouble(_cursorIndexOfPoids);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final Integer _tmpAnneeNaissance;
            if (_cursor.isNull(_cursorIndexOfAnneeNaissance)) {
              _tmpAnneeNaissance = null;
            } else {
              _tmpAnneeNaissance = _cursor.getInt(_cursorIndexOfAnneeNaissance);
            }
            final String _tmpCategorie;
            if (_cursor.isNull(_cursorIndexOfCategorie)) {
              _tmpCategorie = null;
            } else {
              _tmpCategorie = _cursor.getString(_cursorIndexOfCategorie);
            }
            _item = new PiloteEntity(_tmpId,_tmpNumeroLicence,_tmpNom,_tmpPrenom,_tmpCaserne,_tmpPoids,_tmpEmail,_tmpAnneeNaissance,_tmpCategorie);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object toutesEpreuves(final Continuation<? super List<EpreuveEntity>> $completion) {
    final String _sql = "SELECT * FROM epreuves ORDER BY ordre, id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EpreuveEntity>>() {
      @Override
      @NonNull
      public List<EpreuveEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNom = CursorUtil.getColumnIndexOrThrow(_cursor, "nom");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfModeCalcul = CursorUtil.getColumnIndexOrThrow(_cursor, "modeCalcul");
          final int _cursorIndexOfFormule = CursorUtil.getColumnIndexOrThrow(_cursor, "formule");
          final int _cursorIndexOfValeurCle = CursorUtil.getColumnIndexOrThrow(_cursor, "valeurCle");
          final int _cursorIndexOfSensClassement = CursorUtil.getColumnIndexOrThrow(_cursor, "sensClassement");
          final int _cursorIndexOfAfficherClassement = CursorUtil.getColumnIndexOrThrow(_cursor, "afficherClassement");
          final int _cursorIndexOfCompteDansGeneral = CursorUtil.getColumnIndexOrThrow(_cursor, "compteDansGeneral");
          final int _cursorIndexOfActif = CursorUtil.getColumnIndexOrThrow(_cursor, "actif");
          final int _cursorIndexOfOrdre = CursorUtil.getColumnIndexOrThrow(_cursor, "ordre");
          final List<EpreuveEntity> _result = new ArrayList<EpreuveEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EpreuveEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNom;
            _tmpNom = _cursor.getString(_cursorIndexOfNom);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpModeCalcul;
            _tmpModeCalcul = _cursor.getString(_cursorIndexOfModeCalcul);
            final String _tmpFormule;
            if (_cursor.isNull(_cursorIndexOfFormule)) {
              _tmpFormule = null;
            } else {
              _tmpFormule = _cursor.getString(_cursorIndexOfFormule);
            }
            final String _tmpValeurCle;
            if (_cursor.isNull(_cursorIndexOfValeurCle)) {
              _tmpValeurCle = null;
            } else {
              _tmpValeurCle = _cursor.getString(_cursorIndexOfValeurCle);
            }
            final String _tmpSensClassement;
            _tmpSensClassement = _cursor.getString(_cursorIndexOfSensClassement);
            final boolean _tmpAfficherClassement;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAfficherClassement);
            _tmpAfficherClassement = _tmp != 0;
            final boolean _tmpCompteDansGeneral;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfCompteDansGeneral);
            _tmpCompteDansGeneral = _tmp_1 != 0;
            final boolean _tmpActif;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfActif);
            _tmpActif = _tmp_2 != 0;
            final int _tmpOrdre;
            _tmpOrdre = _cursor.getInt(_cursorIndexOfOrdre);
            _item = new EpreuveEntity(_tmpId,_tmpNom,_tmpDescription,_tmpModeCalcul,_tmpFormule,_tmpValeurCle,_tmpSensClassement,_tmpAfficherClassement,_tmpCompteDansGeneral,_tmpActif,_tmpOrdre);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object parametres(final int epreuveId,
      final Continuation<? super List<ParametreEntity>> $completion) {
    final String _sql = "SELECT * FROM epreuve_parametres WHERE epreuveId = ? ORDER BY ordre, id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, epreuveId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ParametreEntity>>() {
      @Override
      @NonNull
      public List<ParametreEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpreuveId = CursorUtil.getColumnIndexOrThrow(_cursor, "epreuveId");
          final int _cursorIndexOfNomVariable = CursorUtil.getColumnIndexOrThrow(_cursor, "nomVariable");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfUnite = CursorUtil.getColumnIndexOrThrow(_cursor, "unite");
          final int _cursorIndexOfObligatoire = CursorUtil.getColumnIndexOrThrow(_cursor, "obligatoire");
          final int _cursorIndexOfOrdre = CursorUtil.getColumnIndexOrThrow(_cursor, "ordre");
          final List<ParametreEntity> _result = new ArrayList<ParametreEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ParametreEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpEpreuveId;
            _tmpEpreuveId = _cursor.getInt(_cursorIndexOfEpreuveId);
            final String _tmpNomVariable;
            _tmpNomVariable = _cursor.getString(_cursorIndexOfNomVariable);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final String _tmpUnite;
            if (_cursor.isNull(_cursorIndexOfUnite)) {
              _tmpUnite = null;
            } else {
              _tmpUnite = _cursor.getString(_cursorIndexOfUnite);
            }
            final boolean _tmpObligatoire;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfObligatoire);
            _tmpObligatoire = _tmp != 0;
            final int _tmpOrdre;
            _tmpOrdre = _cursor.getInt(_cursorIndexOfOrdre);
            _item = new ParametreEntity(_tmpId,_tmpEpreuveId,_tmpNomVariable,_tmpLabel,_tmpUnite,_tmpObligatoire,_tmpOrdre);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object bareme(final int epreuveId,
      final Continuation<? super List<BaremeEntity>> $completion) {
    final String _sql = "SELECT * FROM bareme_points WHERE epreuveId = ? ORDER BY rang";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, epreuveId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BaremeEntity>>() {
      @Override
      @NonNull
      public List<BaremeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpreuveId = CursorUtil.getColumnIndexOrThrow(_cursor, "epreuveId");
          final int _cursorIndexOfRang = CursorUtil.getColumnIndexOrThrow(_cursor, "rang");
          final int _cursorIndexOfPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "points");
          final List<BaremeEntity> _result = new ArrayList<BaremeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BaremeEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpEpreuveId;
            _tmpEpreuveId = _cursor.getInt(_cursorIndexOfEpreuveId);
            final int _tmpRang;
            _tmpRang = _cursor.getInt(_cursorIndexOfRang);
            final double _tmpPoints;
            _tmpPoints = _cursor.getDouble(_cursorIndexOfPoints);
            _item = new BaremeEntity(_tmpId,_tmpEpreuveId,_tmpRang,_tmpPoints);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object pilotesInscrits(final int epreuveId,
      final Continuation<? super List<Integer>> $completion) {
    final String _sql = "SELECT piloteId FROM inscriptions WHERE epreuveId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, epreuveId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Integer>>() {
      @Override
      @NonNull
      public List<Integer> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Integer> _result = new ArrayList<Integer>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Integer _item;
            _item = _cursor.getInt(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object resultatsEpreuve(final int epreuveId,
      final Continuation<? super List<ResultatEntity>> $completion) {
    final String _sql = "SELECT * FROM resultats WHERE epreuveId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, epreuveId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ResultatEntity>>() {
      @Override
      @NonNull
      public List<ResultatEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpreuveId = CursorUtil.getColumnIndexOrThrow(_cursor, "epreuveId");
          final int _cursorIndexOfPiloteId = CursorUtil.getColumnIndexOrThrow(_cursor, "piloteId");
          final int _cursorIndexOfValeursJson = CursorUtil.getColumnIndexOrThrow(_cursor, "valeursJson");
          final int _cursorIndexOfPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "points");
          final int _cursorIndexOfDisqualifie = CursorUtil.getColumnIndexOrThrow(_cursor, "disqualifie");
          final int _cursorIndexOfSaisiPar = CursorUtil.getColumnIndexOrThrow(_cursor, "saisiPar");
          final int _cursorIndexOfDateSaisie = CursorUtil.getColumnIndexOrThrow(_cursor, "dateSaisie");
          final List<ResultatEntity> _result = new ArrayList<ResultatEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ResultatEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpEpreuveId;
            _tmpEpreuveId = _cursor.getInt(_cursorIndexOfEpreuveId);
            final int _tmpPiloteId;
            _tmpPiloteId = _cursor.getInt(_cursorIndexOfPiloteId);
            final String _tmpValeursJson;
            _tmpValeursJson = _cursor.getString(_cursorIndexOfValeursJson);
            final double _tmpPoints;
            _tmpPoints = _cursor.getDouble(_cursorIndexOfPoints);
            final boolean _tmpDisqualifie;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfDisqualifie);
            _tmpDisqualifie = _tmp != 0;
            final String _tmpSaisiPar;
            if (_cursor.isNull(_cursorIndexOfSaisiPar)) {
              _tmpSaisiPar = null;
            } else {
              _tmpSaisiPar = _cursor.getString(_cursorIndexOfSaisiPar);
            }
            final long _tmpDateSaisie;
            _tmpDateSaisie = _cursor.getLong(_cursorIndexOfDateSaisie);
            _item = new ResultatEntity(_tmpId,_tmpEpreuveId,_tmpPiloteId,_tmpValeursJson,_tmpPoints,_tmpDisqualifie,_tmpSaisiPar,_tmpDateSaisie);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object resultat(final int epreuveId, final int piloteId,
      final Continuation<? super ResultatEntity> $completion) {
    final String _sql = "SELECT * FROM resultats WHERE epreuveId = ? AND piloteId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, epreuveId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, piloteId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ResultatEntity>() {
      @Override
      @Nullable
      public ResultatEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpreuveId = CursorUtil.getColumnIndexOrThrow(_cursor, "epreuveId");
          final int _cursorIndexOfPiloteId = CursorUtil.getColumnIndexOrThrow(_cursor, "piloteId");
          final int _cursorIndexOfValeursJson = CursorUtil.getColumnIndexOrThrow(_cursor, "valeursJson");
          final int _cursorIndexOfPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "points");
          final int _cursorIndexOfDisqualifie = CursorUtil.getColumnIndexOrThrow(_cursor, "disqualifie");
          final int _cursorIndexOfSaisiPar = CursorUtil.getColumnIndexOrThrow(_cursor, "saisiPar");
          final int _cursorIndexOfDateSaisie = CursorUtil.getColumnIndexOrThrow(_cursor, "dateSaisie");
          final ResultatEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpEpreuveId;
            _tmpEpreuveId = _cursor.getInt(_cursorIndexOfEpreuveId);
            final int _tmpPiloteId;
            _tmpPiloteId = _cursor.getInt(_cursorIndexOfPiloteId);
            final String _tmpValeursJson;
            _tmpValeursJson = _cursor.getString(_cursorIndexOfValeursJson);
            final double _tmpPoints;
            _tmpPoints = _cursor.getDouble(_cursorIndexOfPoints);
            final boolean _tmpDisqualifie;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfDisqualifie);
            _tmpDisqualifie = _tmp != 0;
            final String _tmpSaisiPar;
            if (_cursor.isNull(_cursorIndexOfSaisiPar)) {
              _tmpSaisiPar = null;
            } else {
              _tmpSaisiPar = _cursor.getString(_cursorIndexOfSaisiPar);
            }
            final long _tmpDateSaisie;
            _tmpDateSaisie = _cursor.getLong(_cursorIndexOfDateSaisie);
            _result = new ResultatEntity(_tmpId,_tmpEpreuveId,_tmpPiloteId,_tmpValeursJson,_tmpPoints,_tmpDisqualifie,_tmpSaisiPar,_tmpDateSaisie);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
