```
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
luckrng.run                            > Izin untuk /lrng run (biasanya console/NPC).
luckrng.reload                         > Izin untuk /lrng reload & /lrng resetcount.
luckrng.event                          > Izin untuk /lrng event.
luckrng.abuse                          > Izin untuk /lrng abuse.
luckrng.drop                           > Izin untuk /lrng drop.
luckrng.vp                             > Izin untuk /lrng vp.
luckrng.admin                          > Master permission (mencakup semua akses).
luckrng.luck.<amount>                  > Bonus luck permanen dari permission.
list yang sudah ada:
luckrng.luck.10                        > 10 permanent luck
luckrng.luck.20                        > 20 permanent luck
luckrng.luck.30                        > 30 permanent luck
luckrng.luck.40                        > 40 permanent luck
luckrng.luck.50                        > 50 permanent luck

PLACEHOLDERS:
%luckrng_luck_total%                    > Total luck visual (termasuk multiplier).
%luckrng_luck_total_capped%             > Total luck setelah dibatasi cap.
%luckrng_count_<table>%                 > Total berapa kali player run table itu.
%luckrng_luckbonus%                     > Bonus normalized (0.0 - 1.0) untuk debug.
%luckrng_drop_multiplier%               > Menampilkan multiplier drop yang aktif.
%luckrng_top_<table>_<rank>_name%       > Nama player di rank leaderboard.
%luckrng_top_<table>_<rank>_count%      > Skor player di rank leaderboard.
%luckrng_luck_global%                   > Nilai default-luck dari config.
%luckrng_luck_potion%                   > Nilai luck dari potion effect.
%luckrng_luck_permission%               > Total luck dari permission player.
%luckrng_luck_extra%                    > Luck dari extra-luck-placeholders.
%luckrng_luck_multiplier%               > Nilai multiplier luck aktif.
%luckrng_luck_multiplier_time%          > Sisa waktu event multiplier.
%luckrng_aaluck%                        > Nilai Admin Abuse luck aktif.
%luckrng_ddrop%                         > Nilai Double Drop aktif.
%luckrng_vpluck%                        > Nilai VoteParty luck aktif.

```
