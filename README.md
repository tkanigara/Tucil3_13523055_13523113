# Tucil 3 Stima: Rush Hour Puzzle Solver

![Author](https://drive.google.com/uc?export=view&id=189Gkd8fKeMKwIaAQMvNTX5NpZsbA-JCL)

**Rush Hour Puzzle Solver** adalah program yang menyelesaikan puzzle Rush Hour dengan menggunakan berbagai algoritma pencarian. Puzzle Rush Hour adalah permainan papan di mana pemain harus memindahkan mobil-mobil pada papan permainan untuk mencapai pintu keluar. Program ini mengimplementasikan beberapa algoritma pencarian untuk menemukan urutan gerakan optimal yang mengarahkan mobil target (biasanya dilambangkan dengan 'P') ke pintu keluar.

## Fitur
- Implementasi 4 algoritma pencarian:
  - **Uniform Cost Search (UCS)**
  - **Greedy Best-First Search (GBFS)**
  - **A* Search**
  - **Iterative Deepening A* (IDA*) Search**
- 3 fungsi heuristik untuk algoritma informed search:
  - **Blocking Pieces**: Menghitung jumlah kendaraan yang menghalangi mobil target
  - **Manhattan Distance**: Menghitung jarak Manhattan dari mobil target ke pintu keluar
  - **Combined**: Kombinasi dari Blocking Pieces dan Manhattan Distance
- Visualisasi dalam bentuk GUI untuk melihat langkah-langkah solusi
- Editor papan untuk membuat puzzle custom
- Menyimpan solusi ke file teks

## Struktur Proyek
```
Tucil3_13523055_13523113/
├── bin/                    # Berisi file-file .class hasil kompilasi
├── docs/                   # Dokumentasi tambahan
├── src/                    # Source code
│   ├── algorithm/          # Implementasi algoritma pencarian (UCS, GBFS, A*, IDA*)
│   ├── gui/                # Implementasi GUI
│   ├── model/              # Model data (Board, Piece, Move)
│   ├── util/               # Utilitas (BoardPrinter, FileParser)
│   └── Main.java           # Entry point program
├── test/                   # Berisi test cases
│   ├── input/              # File input puzzle
│   └── output/             # File output solusi
└── README.md
```

## Kebutuhan Sistem
- Java Development Kit (JDK) 8 atau lebih tinggi
- Sistem operasi yang mendukung Java (Windows, Linux, MacOS)

## Cara Mengompilasi Program
1. Pastikan JDK sudah terpasang pada sistem Anda
2. Buka terminal/command prompt di direktori utama proyek
3. Jalankan perintah berikut untuk mengompilasi seluruh program:

```bash
javac -d bin src/Main.java src/algorithm/*.java src/gui/*.java src/model/*.java src/util/*.java
```

## Cara Menjalankan Program
### CLI Mode
1. Buka terminal/command prompt di direktori utama proyek
2. Jalankan perintah berikut:

```bash
java -cp bin Main
```

3. Program akan meminta path file input, algoritma yang digunakan, dan heuristik (jika menggunakan algoritma informed search)
4. Solusi akan ditampilkan di terminal
5. Dalam menginput path, gunakan relative path, contoh : test/input/right.txt
6. Untuk menyimpan hasil, hanya bisa dalam GUI mode

### GUI Mode
1. Buka terminal/command prompt di direktori utama proyek
2. Jalankan perintah berikut:

```bash
java -cp bin Main --gui
```

3. Gunakan antarmuka grafis untuk memuat puzzle, memilih algoritma dan heuristik, serta menjalankan solusi
4. Anda juga dapat membuat puzzle kustom dengan mengklik tombol "Create Puzzle"


## Format File Input
File input menggunakan format teks dengan struktur sebagai berikut:
1. Baris pertama: dimensi papan (baris dan kolom)
2. Baris kedua: jumlah kendaraan reguler (tidak termasuk mobil target)
3. Sisa baris: representasi papan permainan, dengan:
   - `.` menandakan sel kosong
   - `P` menandakan mobil target
   - Huruf lain (A-Z, kecuali P) menandakan kendaraan reguler
   - `K` menandakan pintu keluar

Contoh file input:
```
6 6
11
AAB..F
..BCDF
GPPCDFK
GH.III
GHJ...
LLJMM.
```

## Pembuat
| Nama | NIM | Kelas |
|------|-----|-------|
| Muhammad Timur Kanigara | 13523055 | K01 |
| Kefas Kurnia Jonathan | 13523113 | K02 |
