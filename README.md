# 🍀 CyayoLuckRNG - RNG System with Luck Multiplier

"Kendalikan keberuntungan, kuasai setiap putaran RNG. Sistem luck dinamis yang terintegrasi penuh untuk pengalaman RPG yang lebih adil dan menantang."

```text
=========================================
             QUICK TUTORIAL
=========================================
```

### 1. Cara Kerja Sistem Luck
- Setiap player memiliki statistik **Luck** yang mempengaruhi hasil RNG (contoh: Drop Rate, Gacha, Forge).
- Luck bisa didapat dari Permission, Potion Effect, atau Event Global.
- Plugin ini menyediakan API dan Placeholder yang bisa digunakan oleh plugin lain untuk mengambil nilai Luck player.

### 2. Cara Menjalankan Event
- **Luck Event:** Gunakan `/lrng event 2.0 3600 Global` untuk menggandakan Luck semua player selama 1 jam.
- **Double Drop:** Gunakan `/lrng drop 2.0 1800 Global` untuk mengaktifkan event Double Drop.
- **Admin Abuse:** Fitur khusus untuk mengatur Luck player secara spesifik untuk durasi tertentu.

### 3. Integrasi Placeholder
Gunakan PlaceholderAPI untuk menampilkan status luck di Scoreboard atau Chat:
- `%luckrng_luck_total%` untuk melihat total luck saat ini.
- `%luckrng_luck_multiplier%` untuk melihat multiplier yang sedang aktif.

```text
=========================================
      CYAYOLUCKRNG - COMMANDS & PERMS
=========================================

USER COMMAND:
/lrng info                             > Cek detail statistik luck pribadimu.

ADMIN COMMAND:
/lrng run <table> <bonus> <player>     > Menjalankan RNG table untuk player.
/lrng resetcount <table|*> <player>    : Reset jumlah run (counter) player.
/lrng reload                           : Reload konfigurasi & pesan plugin.
/lrng event <mult> <sec> <org>         : Mulai Event Luck Multiplier.
/lrng eventend                         : Hentikan Event Luck Multiplier.
/lrng abuse <luck> <sec> <cap> <org>   : Mulai Admin Abuse Luck Event.
/lrng abuseend                         : Hentikan Admin Abuse Luck.
/lrng drop <mult> <sec> <org>          : Mulai Event Double Drop Multiplier.
/lrng dropend                          : Hentikan Event Double Drop.
/lrng vp                               : Mulai VoteParty Luck tambahan.
/lrng vpend                            : Hentikan VoteParty Luck.

PERMISSIONS:
luckrng.use                            > Izin untuk /lrng info.
luckrng.run                            > Izin untuk /lrng run (NPC/Console).
luckrng.reload                         > Izin untuk /lrng reload & resetcount.
luckrng.event                          > Izin untuk /lrng event.
luckrng.abuse                          > Izin untuk /lrng abuse.
luckrng.drop                           > Izin untuk /lrng drop.
luckrng.vp                             > Izin untuk /lrng vp.
luckrng.admin                          > Master permission (Akses Semua).
luckrng.luck.<amount>                  > Bonus luck permanen dari permission.
(Contoh: luckrng.luck.10, luckrng.luck.50)
```

```text
=========================================
             PLACEHOLDERS
=========================================

%luckrng_luck_total%          > Total luck (termasuk multiplier).
%luckrng_luck_total_capped%   > Total luck setelah dibatasi cap.
%luckrng_count_<table>%       > Total run player di table tersebut.
%luckrng_luck_multiplier%     > Nilai multiplier luck aktif.
%luckrng_luck_multiplier_time%> Sisa waktu event multiplier.
%luckrng_top_<table>_<rank>_name% > Nama player di rank leaderboard.
```

### 🚀 Plugin Features
- ✨ **Dynamic Luck System** - Hitung luck dari berbagai sumber secara akurat.
- 📅 **Global Events** - Mulai event Luck atau Drop Multiplier dengan satu command.
- 🏆 **Leaderboard Support** - Pantau siapa player paling beruntung di servermu.
- 🔌 **API Ready** - Mudah diintegrasikan dengan plugin RPG lainnya.
- 📊 **Capped Luck** - Atur batas maksimal luck agar ekonomi server tetap stabil.
