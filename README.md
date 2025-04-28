# 🎮 Vision X: Application Android de Détection d'Objets & Quiz

Expérience ludique et pédagogique de reconnaissance d'objets, développée en Kotlin et Jetpack Compose.

---

## 📖 Table des matières

1. [🚀 Présentation](#-présentation)
2. [✨ Fonctionnalités clés](#-fonctionnalités-clés)
3. [🛠️ Tech Stack](#️-tech-stack)
4. [⚙️ Installation](#️-installation)
5. [▶️ Utilisation](#️-utilisation)

---

## 🚀 Présentation

`App Mobile` est une application Android native développée en **Kotlin** avec **Jetpack Compose**. Elle permet :

- La détection d'objets via la caméra (ML Kit Image Labeling).
- La traduction automatique des labels en français.
- Le stockage des captures dans `Pictures/ObjectDetection`.
- La création d’un quiz interactif généré à partir de vos captures.

---

## ✨ Fonctionnalités clés

- 🎠 **Accueil** : Carrousel animé, catégories et galerie de vos dernières images.
- 📸 **Détection** : Prise de photo, analyse ML Kit, retour vocal (TTS) et enregistrement sur l’appareil.
- ❓ **Quiz** : Questions à choix multiples générées automatiquement, suivi de progression, feedback visuel et vocal.
- 🔄 **Navigation** : Barre inférieure animée (Accueil / Détecter / Quiz).

---

## 🛠️ Tech Stack

- **Langage** : Kotlin
- **UI** : Jetpack Compose (Material3, Animation, Navigation)
- **ML** : ML Kit Image Labeling
- **Images** : Coil, Accompanist Pager
- **Architecture** : MVVM adapté à Compose
- **Stockage** : MediaStore, FileProvider
- **TTS** : Android TextToSpeech

> Voir `Gradle Scripts/build.gradle.kts` pour les versions exactes.

---

## ⚙️ Installation

1. **Cloner le dépôt**
   ```bash
   git clone https://github.com/diawara9412/app_mobile.git
   cd app_mobile
