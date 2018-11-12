@echo off
REM Hiermit wird der Inhalt der Datei .sqliterc im Benutzer Homeverzeichnis ausgeführt.
REM Das ist beispielsweise der Befehl: PRAGMA journal_mode=WAL

cd\
cd server\sqlite
sqlite3 TileHexMapJndiTest