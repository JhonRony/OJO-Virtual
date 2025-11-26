# OJO Virtual - Asistente de Visión para Personas con Discapacidad Visual 👁️🦯

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-Android-purple?style=for-the-badge&logo=kotlin)
![ML Kit](https://img.shields.io/badge/Google-ML%20Kit-orange?style=for-the-badge&logo=google)
![TensorFlow](https://img.shields.io/badge/TensorFlow-Lite-FF6F00?style=for-the-badge&logo=tensorflow)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)
![API](https://img.shields.io/badge/API-21%2B-brightgreen?style=for-the-badge)

</div>

Una aplicación Android inteligente que actúa como asistente virtual para personas con discapacidad visual, combinando tecnologías avanzadas de Machine Learning, reconocimiento de voz y síntesis de voz para proporcionar autonomía e independencia.

## ✨ Características Principales

### 🎤 **Asistente de Voz Completo**
- **Reconocimiento de Voz en Tiempo Real** usando Android Speech Recognizer
- **Síntesis de Voz Avanzada** con Text-to-Speech nativo
- **Comandos por Voz Naturales** en español
- **Configuración Personalizable** de velocidad y tono de voz

### 👁️ **Sistema de Visión Artificial**
- **Detección de Objetos en Tiempo Real** con EfficientDet-Lite0
- **Reconocimiento de Texto** usando ML Kit Text Recognition
- **Cálculo de Distancias** aproximadas a objetos detectados
- **Detección de Múltiples Objetos** simultáneamente

### 📱 **Interfaz Accesible**
- **Navegación por Comandos de Voz**
- **Retroalimentación Auditiva** constante
- **Detección de Movimiento** (agitación para emergencias)
- **Diseño Universal** para máxima accesibilidad

## 🎯 Comandos de Voz Disponibles

| Comando | Función | Ejemplo |
|---------|---------|---------|
| **"ayuda a ver"** | Activa cámara y detección de objetos | "ayuda a ver" |
| **"lee esto"** | Reconocimiento de texto en imágenes | "Lee esto por favor" |
| **"busca [objeto]"** | Búsqueda específica de objetos | "Busca silla", "Encuentra puerta" |
| **"qué hora es"** | Hora actual del sistema | "¿Qué hora es?" |
| **"qué fecha es"** | Fecha actual | "¿Qué fecha es hoy?" |
| **"batería"** | Nivel de batería del dispositivo | "¿Cuánta batería tengo?" |
| **"día o noche"** | Detecta si es de día o noche | "¿Es de día o de noche?" |
| **"configuración"** | Abre ajustes de voz | "Abre configuración" |
| **"salir" / "cerrar"** | Cierra la aplicación | "Salir de la aplicación" |
| **"ayuda"** | Lista todos los comandos | "¿Qué puedo hacer?" |

## 🛠️ Tecnologías y Arquitectura

### **🤖 Machine Learning & AI**
- **Google ML Kit** - Para reconocimiento de texto y framework de ML
- **TensorFlow Lite** - Runtime para modelos de ML
- **EfficientDet-Lite0** - Modelo de detección de objetos (90 clases COCO)
- **Android CameraX** - Para captura y procesamiento de imágenes

### **📱 Plataforma Android**
- **Kotlin** - Lenguaje principal
- **Android Jetpack**:
    - **ViewModel** - Manejo de estado y ciclo de vida
    - **LiveData/StateFlow** - Reactividad y observación de datos
    - **Navigation Component** - Navegación entre fragments
- **Material Design 3** - UI/UX moderna y accesible

### **🎤 Sistema de Voz**
- **Android SpeechRecognizer** - Reconocimiento de voz nativo
- **TextToSpeech Engine** - Síntesis de voz en español
- **Audio Focus Management** - Manejo inteligente de audio

### **🏗️ Arquitectura de Software**
```
📱 Presentation Layer
├── 🎭 Fragments (UI Components)
├── 🧠 ViewModels (Business Logic)
└── 🎯 Use Cases (Coordinators)

📦 Domain Layer
├── 🔄 Use Cases
├── 📊 Entities
└── 📝 Repositories Interfaces

💾 Data Layer
├── 🗃️ Repositories Implementations
├── 🌐 API Clients
└── 💽 Local Storage
```

## 🧠 Especificaciones del Modelo ML

### **EfficientDet-Lite0 v1**
| Parámetro | Especificación |
|-----------|----------------|
| **Framework** | TensorFlow Lite |
| **Tamaño del Modelo** | 4.6 MB |
| **Input Size** | 320x320 píxeles |
| **mAP (COCO)** | 25.69% |
| **Latencia** | ~37 ms (Pixel 4) |
| **Clases** | 90 objetos COCO |

### **Categorías de Objetos Detectables**
- **👥 Personas y partes del cuerpo**
- **🚗 Vehículos** (coches, bicicletas, motos, buses)
- **🐾 Animales** (gatos, perros, pájaros, caballos)
- **🍽️ Objetos Domésticos** (sillas, mesas, teléfonos, laptops)
- **🛒 Productos de Consumo** (botellas, tazas, platos, libros)
- **🚦 Señales y Semáforos**


## **Autor**
- **JHON RONY VARGAS MUÑOZ**

## 🚀 Instalación y Configuración

### **Prerrequisitos**
- Android Studio Arctic Fox o superior
- Android SDK API 21+ (Android 5.0)
- Dispositivo Android con cámara y micrófono

### **Pasos de Instalación**

1. **Clonar el repositorio**
```bash
git clone https://github.com/JhonRony/ojovirtual.git
cd ojovirtual
```



