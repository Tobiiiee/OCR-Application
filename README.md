# OCR Application

A Java-based Optical Character Recognition (OCR) desktop application built with the Tesseract OCR engine.

![License](https://img.shields.io/badge/license-Apache%202.0-red)

## Features

- Extract text from images (JPG, PNG, BMP, TIFF)
- English language support (more to be added later)
- User-friendly GUI
- Save extracted text to files
- Image preprocessing for better accuracy

## Technologies Used

- **Java 21**
- **Tesseract OCR** (via Tess4J)
- **Maven** - Dependency management
- **Swing** - GUI framework

## License

This project is open-sourced under the **Apache License, Version 2.0**.

The Apache License 2.0 is a permissive license that grants users broad rights to use, modify, and distribute the code for private or commercial purposes. **It requires prominent attribution** and includes an explicit patent license grant from contributors.

For the full text and details, see the [LICENSE](LICENSE) file in the root of the repository.

## Development Status

🚧 In Active Development 🚧
This project is currently being developed following proper SDLC methodology.

## Project Structure

OCRApplication/  
├── src/main/java/  
│ ├── com.ocrapp.main/ # Application entry point  
│ ├── com.ocrapp.controller/ # Business logic coordination  
│ ├── com.ocrapp.view/ # UI components  
│ ├── com.ocrapp.model/ # Data models  
│ ├── com.ocrapp.service/ # Core OCR services  
│ └── com.ocrapp.util/ # Utility classes  
├── src/main/resources/ # Configuration files  
└── pom.xml # Maven configuration

## Prerequisites

- Java JDK 11 or higher
- Maven 3.6+
- Tesseract OCR 5.x installed on system
- Eclipse IDE (or any Java IDE)

## Installation

### Windows Users (.exe)

1. Download `OCRApplication.exe` from [Releases](https://github.com/Tobiiiee/OCR-Application/releases)
2. Install Java 11+ and Tesseract OCR
3. Double-click `OCRApplication.exe`

### All Platforms (.jar)

1. Download `OCRApplication.jar`
2. Run: `java -jar OCRApplication.jar`

## Usage

- Launch the application
- Click **"Load Image"** to select an image file
- Click **"Extract Text"** to perform OCR
- View extracted text in the text area
- Click **"Save Text"** to export results

## Contributing

This is a learning project, but suggestions and feedback are welcome!

## Author

Ayaan Qazi  
GitHub: [@Tobiiiee](https://github.com/Tobiiiee)

## Acknowledgments

- **Tesseract OCR** by Google (Licensed under Apache 2.0)
- **Tess4J** wrapper library
- **Leptonica** library (used by Tesseract for image processing)
