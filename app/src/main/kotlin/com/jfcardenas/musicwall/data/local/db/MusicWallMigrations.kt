package com.jfcardenas.musicwall.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MusicWallMigrations {

    /** v4 → v5: tabla de portadas vetadas en discovery. */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `vetoed_albums` (
                    `id` TEXT NOT NULL,
                    `albumName` TEXT NOT NULL,
                    `artistName` TEXT NOT NULL,
                    `vetoedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
        }
    }

    /** v5 → v6: scrobbler local + caché de match Last.fm. */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `local_scrobbles` (
                    `id` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `artistName` TEXT NOT NULL,
                    `trackName` TEXT NOT NULL,
                    `albumName` TEXT NOT NULL,
                    `imageUrl` TEXT,
                    `artistMbid` TEXT,
                    `trackMbid` TEXT,
                    `albumMbid` TEXT,
                    `matchConfidence` REAL NOT NULL,
                    `rawArtist` TEXT NOT NULL,
                    `rawTrack` TEXT NOT NULL,
                    `rawAlbum` TEXT NOT NULL,
                    `sourceApp` TEXT,
                    `playedAt` INTEGER NOT NULL,
                    `durationMs` INTEGER NOT NULL,
                    `isNowPlaying` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_local_scrobbles_userId_playedAt` " +
                    "ON `local_scrobbles` (`userId`, `playedAt`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_local_scrobbles_userId_artistName_trackName` " +
                    "ON `local_scrobbles` (`userId`, `artistName`, `trackName`)",
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `lf_match_cache` (
                    `cacheKey` TEXT NOT NULL,
                    `artistName` TEXT NOT NULL,
                    `trackName` TEXT NOT NULL,
                    `albumName` TEXT NOT NULL,
                    `imageUrl` TEXT,
                    `artistMbid` TEXT,
                    `trackMbid` TEXT,
                    `albumMbid` TEXT,
                    `matchConfidence` REAL NOT NULL,
                    `cachedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`cacheKey`)
                )
                """.trimIndent(),
            )
        }
    }

    val ALL = arrayOf(MIGRATION_4_5, MIGRATION_5_6)
}
