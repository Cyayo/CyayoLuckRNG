# 🍀 CyayoLuckRNG - RNG System with Luck Multiplier

"Kendalikan keberuntungan, kuasai setiap putaran RNG. Sistem luck dinamis yang terintegrasi penuh untuk pengalaman RPG yang lebih adil dan menantang."

```text
=========================================
             QUICK TUTORIAL
=========================================
```

### 1. Cara Kerja Sistem Luck
- Statistik **Luck** dihitung dari: `Default (5)` + `Permission` + `Potion` + `Multiplier` + `Extra Placeholders`.
- Hasil akhir akan dibatasi oleh **Luck Cap** (default 0.75) agar tidak terlalu OP.

### 2. Cara Mengatur Bonus Permission
Kamu bisa menambah atau mengubah bonus luck dari permission di `config.yml`:
```yaml
permission-luck:
  luckrng.luck.special: 75  # Player dengan izin ini dapat +75 Luck
```

### 3. Integrasi PAPI (Extra Luck)
Plugin ini bisa mengambil nilai luck dari plugin lain secara otomatis:
- Menarik luck dari **AuraSkills** (`%auraskills_luck%`).
- Menarik luck dari Custom Stat **MMOItems**.

```text
=========================================
      CYAYOLUCKRNG - COMMANDS & PERMS
=========================================

USER COMMAND:
/lrng info                             > Cek detail statistik luck pribadimu.

ADMIN COMMAND:
/lrng run <table> <bonus> <player>     > Jalankan RNG table untuk player.
/lrng resetcount <table|*> <player>    : Reset counter run player.
/lrng reload                           : Reload config & messages.
/lrng event <mult> <sec> <org>         : Mulai Global Luck Event.
/lrng eventend                         : Stop Global Luck Event.
/lrng abuse <luck> <sec> <cap> <org>   : Admin Abuse Luck spesifik.
/lrng abuseend                         : Stop Admin Abuse.
/lrng drop <mult> <sec> <org>          : Mulai Event Double Drop.
/lrng dropend                          : Stop Event Double Drop.
/lrng vp                               : Jalankan VoteParty Luck.
/lrng vpend                            : Stop VoteParty Luck.

PERMISSIONS:
luckrng.use                            > Izin /lrng info.
luckrng.run                            > Izin /lrng run (NPC/Console).
luckrng.event                          > Izin /lrng event.
luckrng.abuse                          > Izin /lrng abuse.
luckrng.drop                           > Izin /lrng drop.
luckrng.vp                             > Izin /lrng vp.
luckrng.reload                         > Izin /lrng reload & resetcount.
luckrng.admin                          > Master permission (Akses Semua).

[!] CUSTOM LUCK PERMISSION:
Kamu bisa menambah permission luck baru sesukamu.

CARA MENGATUR:
Buka 'config.yml' di bagian 'permission-luck'. Tambahkan baris baru
dengan format 'node.permission: nilai_luck'.
Contoh: 'luckrng.luck.vip: 50'

FUNGSINYA:
Memberikan bonus statistik Luck permanen kepada player berdasarkan
Rank atau Permission yang mereka miliki (misal: Rank MVP +20 Luck).
```

```text
=========================================
             PLACEHOLDERS
=========================================

%luckrng_luck_total%          > Total luck saat ini.
%luckrng_luck_total_capped%   > Total luck setelah dibatasi Cap.
%luckrng_count_<table>%       > Total berapa kali player run table.
%luckrng_luck_multiplier%     > Multiplier event yang aktif.
%luckrng_luck_multiplier_time%> Sisa waktu event multiplier.
%luckrng_aaluck%              > Nilai Admin Abuse luck aktif.
%luckrng_ddrop%               > Nilai Double Drop aktif.
%luckrng_vpluck%              > Nilai VoteParty luck aktif.
```

### 🚀 Advanced Features
- 📊 **BossBar HUD** - Menampilkan durasi event (Luck, Drop, VP) secara visual di atas layar.
- 🔗 **Bridge Integration** - Mengambil data luck dari AuraSkills, MMOItems, dan plugin lainnya.
- 🗳️ **VoteParty Synergy** - Bonus luck otomatis saat VoteParty mencapai goal.
- 🛡️ **Anti-Abuse System** - Batasi keberuntungan player dengan sistem Capping yang cerdas.
- 🎵 **Notification Sounds** - Suara notifikasi saat event dimulai atau berakhir.
