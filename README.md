# OCR Application

A Java-based Optical Character Recognition (OCR) desktop application built with the Tesseract OCR engine.

## Features

- Extract text from images (JPG, PNG, BMP, TIFF)
- English language support
- User-friendly GUI
- Save extracted text to files
- Image preprocessing for better accuracy

## Technologies Used

- **Java 21**
- **Tesseract OCR** (via Tess4J)
- **Maven** - Dependency management
- **Swing** - GUI framework

## Project Structure

OCRApplication/  
├── src/main/java/  
│   ├── com.ocrapp.main/          # Application entry point  
│   ├── com.ocrapp.controller/    # Business logic coordination  
│   ├── com.ocrapp.view/          # UI components  
│   ├── com.ocrapp.model/         # Data models  
│   ├── com.ocrapp.service/       # Core OCR services  
│   └── com.ocrapp.util/          # Utility classes  
├── src/main/resources/           # Configuration files  
└── pom.xml                       # Maven configuration  

## Prerequisites

- Java JDK 11 or higher  
- Maven 3.6+  
- Tesseract OCR 5.x installed on system  
- Eclipse IDE (or any Java IDE)

## Installation

1. Clone the repository:
	```bash
	git clone https://github.com/Tobiiiee/OCR-Application.git
	```


2. Navigate to project directory:
	```bash
	cd OCR-Application
	```

3. Build with Maven:
	```bash
	mvn clean install
	```

4. Run the application:
	```bash
	mvn exec:java
	```

## Usage

- Launch the application
- Click **"Load Image"** to select an image file
- Click **"Extract Text"** to perform OCR
- View extracted text in the text area
- Click **"Save Text"** to export results

## Development Status

🚧 In Active Development 🚧
This project is currently being developed following proper SDLC methodology.

## Contributing

This is a learning project, but suggestions and feedback are welcome!

## License

(to be added later)

## Author

Ayaan Qazi
GitHub: [@Tobiiiee](https://github.com/Tobiiiee)

## Acknowledgments

- Tesseract OCR by Google
- Tess4J wrapper library

