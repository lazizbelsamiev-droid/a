XRAY MOD - Minecraft 1.21.4 (Fabric)

QURISH:
1) JDK 21 va Gradle o'rnating (yoki Fabric template'dan gradle wrapper nusxalang).
2) Shu papkada:  gradle build
3) build/libs/xray-mod-1.0.0.jar ni .minecraft/mods ga tashlang
   (Fabric Loader 1.21.4 va Fabric API ham kerak).

ISHLATISH:
Z - xrayni yoqish/o'chirish   |  X - sozlamalar oynasi
Boshida AUTO: faqat rudalar ko'rinadi (+ ESP ramkalar, to'la yorug'lik).
Sozlamada: "Hammasi" (loydan gulgacha), CUSTOM rejim, blok qidirish va
har bir blokni +/- bilan yoqish. Pastda holat paneli chiqadi.
Sodium/Optifine bilan mos emas.

ESLATMA: agar X bosilganda blok yashirinmasa, logda "BlockMixin" xatosini qidiring
va logni menga yuboring.

JARNI KOMPYUTERSIZ OLISH (GitHub orqali):
1) github.com da bepul akkaunt oching, yangi repository yarating.
2) Shu papkadagi hamma fayllarni (.github papkasi bilan) yuklang.
3) Actions bo'limida "build" ishga tushadi (2-5 daqiqa).
4) Tugagach Artifacts > xray-mod-jar ni yuklab oling, ichidagi
   xray-mod-1.0.0.jar (sources emas) ni mods papkasiga tashlang.
