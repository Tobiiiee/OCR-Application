# OCR Application

A Java-based Optical Character Recognition (OCR) desktop application built with the Tesseract OCR engine.

![License](https://img.shields.io/badge/license-Apache%202.0-red)
![Java](https://img.shields.io/badge/Java-21-orange)
![Tesseract](https://img.shields.io/badge/Tesseract-5.5.0-blue)

## 🚀 Features

- **Extract text from images** (JPG, PNG, BMP, TIFF, GIF)
- **Interactive region selection** - Select specific areas for focused OCR
- **Multi-language OCR support** (English, Spanish, French, German, Italian, Portuguese, Arabic, Chinese, Japanese, Korean, Russian)
- **Intelligent dark background detection and inversion** for better accuracy
- **One-click copy to clipboard** for extracted text
- **Real-time progress feedback** with smooth animations
- **Save extracted text** to files
- **Keyboard shortcuts** (Ctrl+O, Ctrl+S, Ctrl+C, Ctrl+L)
- **Advanced image preprocessing**:
  - Automatic color inversion for dark backgrounds
  - Grayscale conversion
  - Contrast enhancement
  - Smart resizing for large images

## 🛠️ Technologies Used

- **Java 21**
- **Tesseract OCR 5.5.0** (via Tess4J)
- **Maven** - Dependency management
- **Swing** - GUI framework
- **Apache License 2.0**

## 📄 License

This project is open-sourced under the **Apache License, Version 2.0**.

The Apache License 2.0 is a permissive license that grants users broad rights to use, modify, and distribute the code for private or commercial purposes. **It requires prominent attribution** and includes an explicit patent license grant from contributors.

For the full text and details, see the [LICENSE](LICENSE) file in the root of the repository.

## 🚧 Development Status

This project is currently in active development following proper SDLC methodology.

## 📁 Project Structure

OCRApplication/  
├── src/main/java/  
│ ├── com.ocrapp.main/ # Application entry point  
│ ├── com.ocrapp.controller/ # Business logic coordination  
│ ├── com.ocrapp.view/ # UI components  
│ ├── com.ocrapp.model/ # Data models  
│ ├── com.ocrapp.service/ # Core OCR services  
│ └── com.ocrapp.util/ # Utility classes  
├── src/main/resources/ # Configuration files  
├── target/ # Compiled output (JAR/EXE)  
├── pom.xml # Maven configuration  
├── launch4j-config.xml # Windows executable config  
└── LICENSE # Apache 2.0 License

## 📋 Prerequisites

- **Java JDK 11 or higher** ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))
- **Tesseract OCR 5.x** installed on system ([Download](https://github.com/UB-Mannheim/tesseract/wiki))
- (Optional for development) Eclipse IDE or any Java IDE

## 📥 Installation

### Option 1: Download Pre-built Release (Easiest)

#### Windows Users (.exe)

1. Download `OCRApplication.exe` from [Releases](https://github.com/Tobiiiee/OCR-Application/releases)
2. Ensure Java 11+ and Tesseract OCR are installed
3. Double-click `OCRApplication.exe` to launch

#### All Platforms (.jar)

1. Download `OCRApplication.jar` from [Releases](https://github.com/Tobiiiee/OCR-Application/releases)
2. Run: `java -jar OCRApplication.jar`

### Option 2: Build from Source

1. **Clone the repository:**

```bash
   git clone https://github.com/Tobiiiee/OCR-Application.git
   cd OCR-Application
```

2. **Build with Maven:**

```bash
   mvn clean package
```

3. **Run the application:**

```bash
   java -jar target/OCRApplication.jar
```

**Or on windows:**

```bash
   target/OCRApplication.exe
```

## 🎯 Usage

1. Launch the application
2. Load Image: Click "Load Image" or press Ctrl+O to select an image file
3. (Optional) Crop Region: Click "Crop Region" to select a specific area for OCR
4. Extract Text: Click "Extract Text" to perform OCR
5. View Results: Extracted text appears in the right panel
6. Copy or Save:
   - Click "Copy to Clipboard" or press Ctrl+C to copy text
   - Click "Save Text" or press Ctrl+S to save to a file

## 🌍 Multi-Language Support

The application supports 11 languages for OCR. However, you need to install the corresponding language data files in Tesseract.

### Installing Additional Languages

### Recommended: Best Accuracy

For best accuracy, use the tessdata_best models:

1. Download language data from: [tessdata_best](https://github.com/tesseract-ocr/tessdata_best)
2. Download the .traineddata file for your desired language(s)
3. Copy files to: C:\Program Files\Tesseract-OCR\tessdata\ (Windows) or your Tesseract data directory
4. Restart the application
5. Select language from the dropdown menu

### Alternative: Fast Models (Lower Accuracy)

For faster processing with lower accuracy, use [tessdata_fast](https://github.com/tesseract-ocr/tessdata_fast)

## Available Languages

🌐 English (eng) - typically pre-installed  
🌐 Spanish (spa)  
🌐 French (fra), German (deu)  
🌐 Italian (ita)  
🌐 Portuguese (por)  
🌐 Arabic (ara)  
🌐 Russian (rus)  
🌐 Chinese Simplified (chi_sim)  
🌐 Japanese (jpn)  
🌐 Korean (kor)

## 🤝 Contributing

This is a learning project, but suggestions and feedback are welcome!

## 👤 Author

Ayaan Qazi  
GitHub: [@Tobiiiee](https://github.com/Tobiiiee)

## 🙏 Acknowledgments

- **Tesseract OCR** by Google - OCR engine (Apache 2.0)
- **tessdata_best** High-accuracy trained models (Apache 2.0)
- **Tess4J** Java wrapper for Tesseract (Apache 2.0)
- **Leptonica** Image processing library used by Tesseract (BSD-2-Clause)
- **Apache Maven** - Build automation (Apache 2.0)
- **Launch4j** - Windows executable wrapper (BSD/MIT)

## 🔗 Useful Links

- [Tesseract Documentation](https://tesseract-ocr.github.io/)
- [Tesseract Language Data (Best Accuracy)](https://github.com/tesseract-ocr/tessdata_best)
- [Tesseract Language Data (Fast)](https://github.com/tesseract-ocr/tessdata_fast)
- [Tess4J Documentation](http://tess4j.sourceforge.net/)
- [Java Download](https://www.oracle.com/java/technologies/downloads/)
- [Apache Maven Download](https://maven.apache.org/download.cgi)
