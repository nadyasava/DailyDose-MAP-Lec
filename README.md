## Anggota Kelompok
1. Nadya Sava Maritza (00000082273)
2. Arya Setiawan (00000083123)
3. Raisya Putri Virnanda (00000081024)
4. Sandya Pradayan Harijanto (00000082436)

## DailyDose
DailyDose adalah aplikasi jurnal yang dirancang untuk memberikan pengalaman menulis keseharian yang lebih personal, ekspresif, dan menyenangkan dibandingkan aplikasi notes biasa. Dengan fitur-fitur unik seperti menambahkan gambar cover pada setiap entri jurnal, mengatur tag suasana hati, dan mencatat tanggal entri, pengguna dapat dengan mudah mengenang dan mencari pengalaman atau momen penting berdasarkan suasana hati atau tanggal tertentu. DailyDose memungkinkan pengguna untuk menyimpan kenangan dengan cara yang lebih hidup dan inspiratif, menjadikannya pilihan ideal untuk yang ingin mencatat dan mempersonalisasi pengalaman mereka sehari-hari.

## Why DailyDose?
- **Kesehatan Mental**: Menulis jurnal membantu meningkatkan kesadaran diri dan mengatur emosi.
- **Perkembangan Pribadi**: Lacak kemajuan Anda dan renungkan pengalaman hidup Anda.
- **Media Ekspresi**: Ekspresikan diri secara bebas dan jelajahi ide-ide Anda.
- **Penyimpan Kenangan**: Dokumentasikan perjalanan hidup Anda dan kenang momen-momen berharga.
  
## Fitur

- **Autentikasi Pengguna**: Mendukung login dan pendaftaran pengguna.
- **Navigasi yang Mudah**: Menggunakan Bottom Navigation untuk akses cepat ke berbagai fitur.
- **Home**: Menampilkan tiga entri jurnal terbaru dan shortcut menuju halaman *journals*.
- **Entri Jurnal**: Tambahkan judul, deskripsi, dan foto untuk setiap  jurnal yang dibuat.
- **Kalender**: Memungkinkan pengguna untuk melihat jurnal yang telah dibuat berdasarkan tanggal yang dipilih.
- **Kategorisasi**: Tandai setiap jurnal dengan kategori yang dikategorikan oleh mood.
- **Mencari Jurnal** : Pengguna dapat mencari jurnal tertentu berdasarkan judul dan menggunakan filter mood.
- **Manajemen Profil Pengguna**: Menyimpan dan mengelola informasi pengguna.

## Teknologi yang Digunakan

- **Android SDK**: Untuk pengembangan antarmuka dan fitur utama aplikasi Android.
- **Kotlin**: Bahasa pemrograman utama yang digunakan untuk menulis seluruh kode aplikasi.
- **Firebase Authentication**: Digunakan untuk mengelola autentikasi pengguna dengan opsi login Email/Password.
- **Firestore Database**: Digunakan sebagai basis data utama untuk menyimpan data jurnal pengguna dan sinkronisasi antar perangkat secara real-time.
- **Firebase Storage**: Menyimpan file gambar yang diunggah pengguna secara aman agar bisa diakses di dalam aplikasi.
- **Shared Preferences**: Digunakan untuk menyimpan data lokal seperti status login dan pengaturan aplikasi.
- **RecyclerView**: Menampilkan daftar jurnal pengguna dengan tampilan yang dapat di-scroll.
- **Glide**: Digunakan untuk memuat dan menampilkan jurnal dengan caching, sehingga meningkatkan efisiensi aplikasi.

## Cara Penggunaan
1. *Aplikasi akan mengarahkan ke halaman login* saat diluncurkan pertama kali.
2. *Pengguna dapat mendaftarkan akun* jika belum memiliki akun, setelah itu **pengguna dapat masuk** menggunakan kredensial mereka.
3. Setelah berhasil masuk, pengguna akan diarahkan ke halaman utama (home) dengan akses ke halaman berikut :
   - **Home**: Menyambut pengguna dan menampilkan 3 entri jurnal terakhir dengan *shortcut* menuju halaman *journals*.
   - **Calendar**: Menampilkan jurnal yang dibuat oleh pengguna berdasarkan tanggal yang dipilih oleh pengguna.
   - **Add Journal**: Menambah entri jurnal baru yang terdiri dari *title, mood, cover photo*, dan juga *content*.
   - **Journals**: Menampilkan semua jurnal yang dibuat oleh pengguna, dimana pengguna dapat mencari jurnal tertentu berdasarkan *title* dan filter berdasarkan kategori mood.
   - **Profile**: Menampilkan dan mengelola informasi pengguna yang terdiri dari foto profil, nama depan, nama belakang, dan email.
