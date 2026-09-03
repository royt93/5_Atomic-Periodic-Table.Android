package name: com.mckimquyen.atomicPeriodicTable
policy: https://loitp.notion.site/Term-Privacy-Policy-Disclaimer-319b1cd8783942fa8923d2a3c9bce60f

⚠️ Giá trị thật (admob app id/banner/inter/open app/native/rewarded, VIP_30D_KEY, VIP_3D_KEY) KHÔNG
được ghi ở đây — file này nằm trong repo public, ghi giá trị thật = leak. Nguồn thật duy nhất:
- local: $ATOMIC_PERIODIC_TABLE_SECRETS_FILE (mặc định
  /Users/loitran/AndroidStudioProjects/@mckimquyen/myKeyStore/com.mckimquyen.atomicPeriodicTable/ads.properties)
- remote: https://github.com/royt93/myKeyStore/tree/main/com.mckimquyen.atomicPeriodicTable (private repo)

chú ý:
- dùng admob provider
- debug mode: dùng bộ admob id test
- release mode: dùng bộ admob id real, lấy từ ads.properties ở trên
- có VIP key 30 days / 3 days — lấy từ ads.properties, KHÔNG copy vào file này hay bất kỳ file nào trong repo public
- native ad id có khai trong ads.properties nhưng chưa dùng — SDK AdmobApplovinWrapper hiện tại (1.8.5) chưa hỗ trợ format native