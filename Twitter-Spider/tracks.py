import xml.etree.ElementTree as ET
import sqlite3

conn = sqlite3.connect('trackdb.sqlite')
cur = conn.cursor()

# Make some fresh tables using executescript()
cur.executescript('''
DROP TABLE IF EXISTS Artist;
DROP TABLE IF EXISTS Genre;
DROP TABLE IF EXISTS Album;
DROP TABLE IF EXISTS Track;

CREATE TABLE Artist (
    id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    musician    TEXT UNIQUE
);

CREATE TABLE Genre (
    ID  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    motif    TEXT UNIQUE
);

CREATE TABLE Album (
    id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    artist_id  INTEGER,
    vinyl   TEXT UNIQUE
);

CREATE TABLE Track (
    id  INTEGER NOT NULL PRIMARY KEY 
        AUTOINCREMENT UNIQUE,
    tune TEXT  UNIQUE,
    album_id  INTEGER,
    genre_id  INTEGER,
    len INTEGER, rating INTEGER, count INTEGER
);
''')


fname = raw_input('Enter file name: ')
if ( len(fname) < 1 ) : fname = 'Library.xml'

# <key>Track ID</key><integer>369</integer>
# <key>Name</key><string>Another One Bites The Dust</string>
# <key>Artist</key><string>Queen</string>
def lookup(d, key):
    found = False
    for child in d:
        if found : return child.text
        if child.tag == 'key' and child.text == key :
            found = True
    return None

stuff = ET.parse(fname)
all = stuff.findall('dict/dict/dict')
print 'Dict count:', len(all)
for entry in all:
    if ( lookup(entry, 'Track ID') is None ) : continue

    song_name = lookup(entry, 'Name')
    artist = lookup(entry, 'Artist')
    album = lookup(entry, 'Album')
    genre = lookup(entry, 'Genre')
    count = lookup(entry, 'Play Count')
    rating = lookup(entry, 'Rating')
    length = lookup(entry, 'Total Time')

    if song_name is None or artist is None or genre is None or album is None : 
        continue

    print song_name, artist, album, genre, length, rating, count

    cur.execute('''INSERT OR IGNORE INTO Artist (musician) 
        VALUES ( ? )''', ( artist, ) )
    cur.execute('SELECT id FROM Artist WHERE musician = ? ', (artist, ))
    artist_id = cur.fetchone()[0]
    
    cur.execute('''INSERT OR IGNORE INTO Genre (motif) 
        VALUES ( ? ) ''', ( genre, ))
    cur.execute('SELECT ID FROM Genre WHERE motif = ? ', (genre, ))
    genre_ID = cur.fetchone()[0]

    cur.execute('''INSERT OR IGNORE INTO Album (artist_id, vinyl) 
        VALUES ( ?, ? ) ''', ( artist_id, album ))
    cur.execute('SELECT id from Album where vinyl = ? ', (album, ))
    album_id = cur.fetchone()[0]

    cur.execute('''INSERT OR REPLACE INTO Track
        (title, album_id, genre_id, len, rating, count) 
        VALUES ( ?, ?, ?, ?, ? , ?)''', 
        ( song_name, album_id, genre_ID, length, rating, count ) )
    conn.commit()
    
    #cur.execute('''SELECT Album.title, Artist.name FROM Album JOIN Artist ON Album.artist_id = Artist.id''')
    #cur.execute('''select Album.title, Album.artist_id, Artist.id,Artist.name from Album join Artist on Album.artist_id = Artist.id''')
    #cur.execute('''select Track.title, Genre.name from Track join Genre on Track.genre_id = Genre.id''')

    
