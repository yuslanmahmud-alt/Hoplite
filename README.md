# HoplitePlugin - Minecraft 1.21.x (Paper/Spigot)

Plugin PvP bergaya Hoplite dengan sistem team, class, safe zone border, dan countdown.

---

## 📦 Cara Install

1. Build plugin dengan Maven:
   ```
   mvn clean package
   ```
2. Salin fail `HoplitePlugin-1.0.0.jar` ke folder `plugins/` server anda
3. Restart server
4. Edit `plugins/HoplitePlugin/config.yml` jika perlu

---

## 🎮 Cara Bermain (Flow)

### Langkah 1 — Pemain pilih team
```
/team
```
- GUI akan terbuka — klik **Wool Merah** atau **Wool Biru**
- Class akan diberikan **secara rawak**: Tank, Fighter, Archer, atau Mage
- Skill juga diberikan rawak mengikut class
- Pemain rakan sepasukan akan terlihat **outline hijau** (glow effect)
- Outline **tidak kelihatan** kepada musuh (fair play!)

### Langkah 2 — Admin teleport semua ke arena
```
/teleport-arena
```
- Semua pemain akan di-teleport ke arena
- Setiap pemain akan berada dalam **sangkar kaca** berasingan
- Team Merah di sebelah utara, Team Biru di selatan

### Langkah 3 — Admin mulakan permainan
```
/start
```
- Kiraan **3... 2... 1... MULA!** akan muncul di **tengah skrin** (bukan chat)
- Sangkar kaca akan **hilang secara automatik** selepas kiraan
- Border safe zone akan **diaktifkan**
- Perlengkapan class akan diberikan kepada semua pemain

---

## ⚔️ Classes

| Class   | HP  | Kelajuan | Perlengkapan         |
|---------|-----|----------|----------------------|
| Tank    | 40  | Normal   | Diamond armor + Shield |
| Fighter | 30  | Laju     | Iron armor + Diamond Sword |
| Archer  | 20  | Sangat Laju | Leather armor + Bow |
| Mage    | 20  | Laju     | Gold armor + Blaze Rod |

### Skills (Rawak 2 daripada 3)
- **Tank**: Shield Wall, Iron Skin, Taunt
- **Fighter**: Battle Cry, Whirlwind, Berserk
- **Archer**: Rapid Fire, Explosive Arrow, Eagle Eye
- **Mage**: Fireball, Frost Bolt, Arcane Shield

**Cara guna skill**: Pegang item skill (slot 1 atau 2) dan klik kanan

---

## 🔴 Safe Zone Border

- Border kelihatan sebagai **partikel oren** di tepi sempadan
- **World Border** Minecraft juga digunakan untuk visual tambahan
- Pemain diluar border kena **damage 0.5 HP setiap saat**
- Notifikasi muncul di **ActionBar** apabila diluar zon selamat
- Amaran diberikan apabila **dalam 5 blok** dari tepi

### Mengecilkan Border
```
/border shrink <saiz_baru> <masa_dalam_saat>
```
Contoh: `/border shrink 30 60` — kecilkan ke saiz 30 dalam 60 saat

Notifikasi pengecilan akan muncul di **tengah skrin** semua pemain.

---

## ⚡ Efek Kematian

Apabila pemain mati:
- **Sambaran kilat** muncul di lokasi kematian (visual sahaja, tiada damage)
- Partikel letupan dan kilat ditambah
- Pengumuman dihantar ke semua pemain

---

## 📋 Senarai Command

### Pemain
| Command | Penerangan |
|---------|------------|
| `/team` | Buka GUI pilihan team |
| `/hoplite` | Lihat info plugin |
| `/hoplite info` | Lihat class dan skill anda |

### Admin (OP sahaja)
| Command | Penerangan |
|---------|------------|
| `/teleport-arena` | Teleport semua pemain ke arena (tetapkan titik tengah di lokasi anda) |
| `/start` | Mulakan kiraan 3,2,1 dan permainan |
| `/border size <saiz>` | Tetapkan saiz border |
| `/border shrink <saiz> <saat>` | Kecilkan border secara beransur |
| `/border stop` | Hentikan pengecilan |
| `/border activate` | Aktifkan damage border |
| `/border deactivate` | Nyahaktifkan border |

---

## ⚙️ config.yml

```yaml
arena:
  center:
    world: world
    x: 0.5
    y: 64
    z: 0.5
  spawn-radius: 10  # Jarak spawn dari tengah

border:
  initial-size: 100   # Saiz awal (radius)
  damage-per-tick: 0.5
  damage-interval-ticks: 20
```

---

## 🔧 Keperluan

- **Server**: Paper atau Spigot 1.21.x
- **Java**: 21+
- **Maven**: Untuk build dari source

---

## 📁 Struktur Fail

```
HoplitePlugin/
├── src/main/java/com/hoplite/
│   ├── HoplitePlugin.java          (Main class)
│   ├── commands/
│   │   ├── TeamCommand.java        (/team GUI)
│   │   ├── TeleportArenaCommand.java (/teleport-arena)
│   │   ├── StartCommand.java       (/start)
│   │   ├── BorderCommand.java      (/border)
│   │   └── HopliteCommand.java     (/hoplite)
│   ├── managers/
│   │   ├── TeamManager.java        (Team + glow outline)
│   │   ├── ClassManager.java       (Class equipment + skills)
│   │   ├── BorderManager.java      (Safe zone + damage + particles)
│   │   ├── GameManager.java        (Countdown + death effects)
│   │   └── ArenaManager.java       (Glass cages + teleport)
│   ├── listeners/
│   │   ├── PlayerListener.java     (Death, PvP, skill use)
│   │   ├── BorderListener.java     (Border warning)
│   │   └── TeamListener.java       (GUI clicks)
│   └── models/
│       ├── PlayerClass.java        (Class enum)
│       └── Skill.java              (Skill enum)
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```
