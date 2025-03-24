# 7 Days to Die Mod Manager

A lightweight and multilingual desktop mod manager for **7 Days to Die**.  
Quickly install, manage, and view details about your mods with a user-friendly Java Swing interface.

---

## ✨ Features

- 📁 Select your game folder easily
- 🔍 Automatically detects installed mods
- 📝 View detailed information (name, description, author, version)
- 🌐 Supports multiple languages (Spanish and English)
- 🧰 Identify the mods by it's `modinfo.xml`
- 💾 Saves configuration to `config.properties` for persistence

---

## 📦 Folder Structure

The game path should be the folder where your 7 days to die game is installed:
C:\\Program files (x86)\\Steam\\steamapps\\common\\7DaysToDie.

The mods for the mod manager must be on the "mods" folder on the same route as the
".jar" executable. If the folder doesn't exist while the
mod manager is executed, it'll create it.

Each mod must have a `modinfo.xml` file, otherwise it won't be detected.

The `modinfo.xml` structure must be this for being read by the manager:

```xml
<mod>
    <Name value="example_mod_id"/>
    <DisplayName value="Example Mod Name"/>
    <Author value="Your Name"/>
    <Version value="1.0.0"/>
    <Description value="A description of your mod."/>
</mod>
```
---
## 🧪 How to Use
Launch the program

- Click Select Folder and choose your 7 Days to Die game folder

The manager will:

- Display all detected mods
- Let you inspect mod metadata
- Show "No Mods Available" if none are found

---
## 🌍 Language Support
Language strings are loaded from {lang}Labels.properties.

For now, the application just supports English and Spanish.

---
## 🛠️ Requirements
- Java 21 or higher
- A valid modinfo.xml in each mod directory

---
## 📌 Notes
Mod folders must reside under Mods/ in your game directory and in your
".jar" executable directory.

This app does not download or install mods — it only manages existing ones.

---
## 📖 License
MIT License

---
## 👤 Author
Developed by [Luna115-onCode](https://github.com/Luna115-onCode)