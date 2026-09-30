package com.pps.parapente.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile PpsDao _ppsDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `utilisateurs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `identifiant` TEXT NOT NULL, `motDePasse` TEXT NOT NULL, `role` TEXT NOT NULL, `piloteId` INTEGER, `actif` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_utilisateurs_identifiant` ON `utilisateurs` (`identifiant`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pilotes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `numeroLicence` TEXT NOT NULL, `nom` TEXT NOT NULL, `prenom` TEXT NOT NULL, `caserne` TEXT, `poids` REAL, `email` TEXT, `anneeNaissance` INTEGER, `categorie` TEXT)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_pilotes_numeroLicence` ON `pilotes` (`numeroLicence`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `epreuves` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nom` TEXT NOT NULL, `description` TEXT, `modeCalcul` TEXT NOT NULL, `formule` TEXT, `valeurCle` TEXT, `sensClassement` TEXT NOT NULL, `afficherClassement` INTEGER NOT NULL, `compteDansGeneral` INTEGER NOT NULL, `actif` INTEGER NOT NULL, `ordre` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `epreuve_parametres` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epreuveId` INTEGER NOT NULL, `nomVariable` TEXT NOT NULL, `label` TEXT NOT NULL, `unite` TEXT, `obligatoire` INTEGER NOT NULL, `ordre` INTEGER NOT NULL, FOREIGN KEY(`epreuveId`) REFERENCES `epreuves`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_epreuve_parametres_epreuveId` ON `epreuve_parametres` (`epreuveId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `bareme_points` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epreuveId` INTEGER NOT NULL, `rang` INTEGER NOT NULL, `points` REAL NOT NULL, FOREIGN KEY(`epreuveId`) REFERENCES `epreuves`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_bareme_points_epreuveId_rang` ON `bareme_points` (`epreuveId`, `rang`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `inscriptions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `piloteId` INTEGER NOT NULL, `epreuveId` INTEGER NOT NULL, FOREIGN KEY(`piloteId`) REFERENCES `pilotes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`epreuveId`) REFERENCES `epreuves`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_inscriptions_piloteId_epreuveId` ON `inscriptions` (`piloteId`, `epreuveId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inscriptions_epreuveId` ON `inscriptions` (`epreuveId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `resultats` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epreuveId` INTEGER NOT NULL, `piloteId` INTEGER NOT NULL, `valeursJson` TEXT NOT NULL, `points` REAL NOT NULL, `disqualifie` INTEGER NOT NULL, `saisiPar` TEXT, `dateSaisie` INTEGER NOT NULL, FOREIGN KEY(`piloteId`) REFERENCES `pilotes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`epreuveId`) REFERENCES `epreuves`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_resultats_epreuveId_piloteId` ON `resultats` (`epreuveId`, `piloteId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_resultats_piloteId` ON `resultats` (`piloteId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'adeaeba7068ebc71801df1f54b9dd911')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `utilisateurs`");
        db.execSQL("DROP TABLE IF EXISTS `pilotes`");
        db.execSQL("DROP TABLE IF EXISTS `epreuves`");
        db.execSQL("DROP TABLE IF EXISTS `epreuve_parametres`");
        db.execSQL("DROP TABLE IF EXISTS `bareme_points`");
        db.execSQL("DROP TABLE IF EXISTS `inscriptions`");
        db.execSQL("DROP TABLE IF EXISTS `resultats`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUtilisateurs = new HashMap<String, TableInfo.Column>(6);
        _columnsUtilisateurs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurs.put("identifiant", new TableInfo.Column("identifiant", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurs.put("motDePasse", new TableInfo.Column("motDePasse", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurs.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurs.put("piloteId", new TableInfo.Column("piloteId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurs.put("actif", new TableInfo.Column("actif", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUtilisateurs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUtilisateurs = new HashSet<TableInfo.Index>(1);
        _indicesUtilisateurs.add(new TableInfo.Index("index_utilisateurs_identifiant", true, Arrays.asList("identifiant"), Arrays.asList("ASC")));
        final TableInfo _infoUtilisateurs = new TableInfo("utilisateurs", _columnsUtilisateurs, _foreignKeysUtilisateurs, _indicesUtilisateurs);
        final TableInfo _existingUtilisateurs = TableInfo.read(db, "utilisateurs");
        if (!_infoUtilisateurs.equals(_existingUtilisateurs)) {
          return new RoomOpenHelper.ValidationResult(false, "utilisateurs(com.pps.parapente.data.UtilisateurEntity).\n"
                  + " Expected:\n" + _infoUtilisateurs + "\n"
                  + " Found:\n" + _existingUtilisateurs);
        }
        final HashMap<String, TableInfo.Column> _columnsPilotes = new HashMap<String, TableInfo.Column>(9);
        _columnsPilotes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPilotes.put("numeroLicence", new TableInfo.Column("numeroLicence", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPilotes.put("nom", new TableInfo.Column("nom", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPilotes.put("prenom", new TableInfo.Column("prenom", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPilotes.put("caserne", new TableInfo.Column("caserne", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPilotes.put("poids", new TableInfo.Column("poids", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPilotes.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPilotes.put("anneeNaissance", new TableInfo.Column("anneeNaissance", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPilotes.put("categorie", new TableInfo.Column("categorie", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPilotes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPilotes = new HashSet<TableInfo.Index>(1);
        _indicesPilotes.add(new TableInfo.Index("index_pilotes_numeroLicence", true, Arrays.asList("numeroLicence"), Arrays.asList("ASC")));
        final TableInfo _infoPilotes = new TableInfo("pilotes", _columnsPilotes, _foreignKeysPilotes, _indicesPilotes);
        final TableInfo _existingPilotes = TableInfo.read(db, "pilotes");
        if (!_infoPilotes.equals(_existingPilotes)) {
          return new RoomOpenHelper.ValidationResult(false, "pilotes(com.pps.parapente.data.PiloteEntity).\n"
                  + " Expected:\n" + _infoPilotes + "\n"
                  + " Found:\n" + _existingPilotes);
        }
        final HashMap<String, TableInfo.Column> _columnsEpreuves = new HashMap<String, TableInfo.Column>(11);
        _columnsEpreuves.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("nom", new TableInfo.Column("nom", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("modeCalcul", new TableInfo.Column("modeCalcul", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("formule", new TableInfo.Column("formule", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("valeurCle", new TableInfo.Column("valeurCle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("sensClassement", new TableInfo.Column("sensClassement", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("afficherClassement", new TableInfo.Column("afficherClassement", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("compteDansGeneral", new TableInfo.Column("compteDansGeneral", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("actif", new TableInfo.Column("actif", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuves.put("ordre", new TableInfo.Column("ordre", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEpreuves = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEpreuves = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEpreuves = new TableInfo("epreuves", _columnsEpreuves, _foreignKeysEpreuves, _indicesEpreuves);
        final TableInfo _existingEpreuves = TableInfo.read(db, "epreuves");
        if (!_infoEpreuves.equals(_existingEpreuves)) {
          return new RoomOpenHelper.ValidationResult(false, "epreuves(com.pps.parapente.data.EpreuveEntity).\n"
                  + " Expected:\n" + _infoEpreuves + "\n"
                  + " Found:\n" + _existingEpreuves);
        }
        final HashMap<String, TableInfo.Column> _columnsEpreuveParametres = new HashMap<String, TableInfo.Column>(7);
        _columnsEpreuveParametres.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuveParametres.put("epreuveId", new TableInfo.Column("epreuveId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuveParametres.put("nomVariable", new TableInfo.Column("nomVariable", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuveParametres.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuveParametres.put("unite", new TableInfo.Column("unite", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuveParametres.put("obligatoire", new TableInfo.Column("obligatoire", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpreuveParametres.put("ordre", new TableInfo.Column("ordre", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEpreuveParametres = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysEpreuveParametres.add(new TableInfo.ForeignKey("epreuves", "CASCADE", "NO ACTION", Arrays.asList("epreuveId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesEpreuveParametres = new HashSet<TableInfo.Index>(1);
        _indicesEpreuveParametres.add(new TableInfo.Index("index_epreuve_parametres_epreuveId", false, Arrays.asList("epreuveId"), Arrays.asList("ASC")));
        final TableInfo _infoEpreuveParametres = new TableInfo("epreuve_parametres", _columnsEpreuveParametres, _foreignKeysEpreuveParametres, _indicesEpreuveParametres);
        final TableInfo _existingEpreuveParametres = TableInfo.read(db, "epreuve_parametres");
        if (!_infoEpreuveParametres.equals(_existingEpreuveParametres)) {
          return new RoomOpenHelper.ValidationResult(false, "epreuve_parametres(com.pps.parapente.data.ParametreEntity).\n"
                  + " Expected:\n" + _infoEpreuveParametres + "\n"
                  + " Found:\n" + _existingEpreuveParametres);
        }
        final HashMap<String, TableInfo.Column> _columnsBaremePoints = new HashMap<String, TableInfo.Column>(4);
        _columnsBaremePoints.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBaremePoints.put("epreuveId", new TableInfo.Column("epreuveId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBaremePoints.put("rang", new TableInfo.Column("rang", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBaremePoints.put("points", new TableInfo.Column("points", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBaremePoints = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysBaremePoints.add(new TableInfo.ForeignKey("epreuves", "CASCADE", "NO ACTION", Arrays.asList("epreuveId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesBaremePoints = new HashSet<TableInfo.Index>(1);
        _indicesBaremePoints.add(new TableInfo.Index("index_bareme_points_epreuveId_rang", true, Arrays.asList("epreuveId", "rang"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoBaremePoints = new TableInfo("bareme_points", _columnsBaremePoints, _foreignKeysBaremePoints, _indicesBaremePoints);
        final TableInfo _existingBaremePoints = TableInfo.read(db, "bareme_points");
        if (!_infoBaremePoints.equals(_existingBaremePoints)) {
          return new RoomOpenHelper.ValidationResult(false, "bareme_points(com.pps.parapente.data.BaremeEntity).\n"
                  + " Expected:\n" + _infoBaremePoints + "\n"
                  + " Found:\n" + _existingBaremePoints);
        }
        final HashMap<String, TableInfo.Column> _columnsInscriptions = new HashMap<String, TableInfo.Column>(3);
        _columnsInscriptions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInscriptions.put("piloteId", new TableInfo.Column("piloteId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInscriptions.put("epreuveId", new TableInfo.Column("epreuveId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInscriptions = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysInscriptions.add(new TableInfo.ForeignKey("pilotes", "CASCADE", "NO ACTION", Arrays.asList("piloteId"), Arrays.asList("id")));
        _foreignKeysInscriptions.add(new TableInfo.ForeignKey("epreuves", "CASCADE", "NO ACTION", Arrays.asList("epreuveId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesInscriptions = new HashSet<TableInfo.Index>(2);
        _indicesInscriptions.add(new TableInfo.Index("index_inscriptions_piloteId_epreuveId", true, Arrays.asList("piloteId", "epreuveId"), Arrays.asList("ASC", "ASC")));
        _indicesInscriptions.add(new TableInfo.Index("index_inscriptions_epreuveId", false, Arrays.asList("epreuveId"), Arrays.asList("ASC")));
        final TableInfo _infoInscriptions = new TableInfo("inscriptions", _columnsInscriptions, _foreignKeysInscriptions, _indicesInscriptions);
        final TableInfo _existingInscriptions = TableInfo.read(db, "inscriptions");
        if (!_infoInscriptions.equals(_existingInscriptions)) {
          return new RoomOpenHelper.ValidationResult(false, "inscriptions(com.pps.parapente.data.InscriptionEntity).\n"
                  + " Expected:\n" + _infoInscriptions + "\n"
                  + " Found:\n" + _existingInscriptions);
        }
        final HashMap<String, TableInfo.Column> _columnsResultats = new HashMap<String, TableInfo.Column>(8);
        _columnsResultats.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResultats.put("epreuveId", new TableInfo.Column("epreuveId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResultats.put("piloteId", new TableInfo.Column("piloteId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResultats.put("valeursJson", new TableInfo.Column("valeursJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResultats.put("points", new TableInfo.Column("points", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResultats.put("disqualifie", new TableInfo.Column("disqualifie", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResultats.put("saisiPar", new TableInfo.Column("saisiPar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResultats.put("dateSaisie", new TableInfo.Column("dateSaisie", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysResultats = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysResultats.add(new TableInfo.ForeignKey("pilotes", "CASCADE", "NO ACTION", Arrays.asList("piloteId"), Arrays.asList("id")));
        _foreignKeysResultats.add(new TableInfo.ForeignKey("epreuves", "CASCADE", "NO ACTION", Arrays.asList("epreuveId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesResultats = new HashSet<TableInfo.Index>(2);
        _indicesResultats.add(new TableInfo.Index("index_resultats_epreuveId_piloteId", true, Arrays.asList("epreuveId", "piloteId"), Arrays.asList("ASC", "ASC")));
        _indicesResultats.add(new TableInfo.Index("index_resultats_piloteId", false, Arrays.asList("piloteId"), Arrays.asList("ASC")));
        final TableInfo _infoResultats = new TableInfo("resultats", _columnsResultats, _foreignKeysResultats, _indicesResultats);
        final TableInfo _existingResultats = TableInfo.read(db, "resultats");
        if (!_infoResultats.equals(_existingResultats)) {
          return new RoomOpenHelper.ValidationResult(false, "resultats(com.pps.parapente.data.ResultatEntity).\n"
                  + " Expected:\n" + _infoResultats + "\n"
                  + " Found:\n" + _existingResultats);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "adeaeba7068ebc71801df1f54b9dd911", "53b9df7969bf7094df5cc4d17c4cdde7");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "utilisateurs","pilotes","epreuves","epreuve_parametres","bareme_points","inscriptions","resultats");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `utilisateurs`");
      _db.execSQL("DELETE FROM `pilotes`");
      _db.execSQL("DELETE FROM `epreuves`");
      _db.execSQL("DELETE FROM `epreuve_parametres`");
      _db.execSQL("DELETE FROM `bareme_points`");
      _db.execSQL("DELETE FROM `inscriptions`");
      _db.execSQL("DELETE FROM `resultats`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PpsDao.class, PpsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PpsDao dao() {
    if (_ppsDao != null) {
      return _ppsDao;
    } else {
      synchronized(this) {
        if(_ppsDao == null) {
          _ppsDao = new PpsDao_Impl(this);
        }
        return _ppsDao;
      }
    }
  }
}
