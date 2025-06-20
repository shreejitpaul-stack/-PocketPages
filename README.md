# PocketPages 📝

PocketPages is a modern, offline-first, and feature-rich note-taking application for Android, inspired by the versatility of Notion. It provides a clean, block-based editor designed for everything from quick notes and to-do lists to structured databases and long-form writing, all while ensuring your data is always available, even without an internet connection.

<img src="https://github.com/user-attachments/assets/3e635233-ea13-4824-9469-3ce129a96e79" alt="login" width="270" height="603">
<img src="https://github.com/user-attachments/assets/d83881a2-90ed-4ef6-94b7-8e9ae325ad35" alt="home" width="270" height="603">
<img src="https://github.com/user-attachments/assets/29a17643-1518-4cdf-9330-a0cbb557e9ed" alt="bin" width="270" height="603">
<img src="https://github.com/user-attachments/assets/54652b5b-0493-42bb-9854-6c5df2153dc8" alt="settings" width="270" height="603">


## ✨ Features

- **✍️ Block-Based Editor**: Structure your notes with a variety of content blocks, including:
  - Rich Text (with more formatting options to come)
  - Headings
  - To-Do Lists with checkboxes
  - Bulleted Lists
  - Quote blocks for emphasis

- **💯 Offline-First Architecture**: Create, read, edit, and delete pages with zero latency, even without an internet connection. Your data is always available on your device.

- **♻️ Bin & Recovery**: Deleted pages are moved to a bin, where they can be restored or permanently deleted.

- **💾 Robust Local Storage**: Utilizes a **Room Database** to provide a fast, reliable, and queryable local cache for all your pages.

- **🚀 Modern Android Tech Stack**: Built with the latest recommended Android technologies for a scalable and maintainable codebase.

- **☁️ Cloud Sync Ready**: The architecture is designed to seamlessly integrate with a cloud backend (like Firebase or a custom REST API) for data backup and multi-device synchronization.

## 🛠️ Tech Stack & Architecture

PocketPages is built using a modern, scalable architecture that separates concerns and promotes best practices.

- **Language**: **Kotlin** (100% Kotlin codebase)
- **Architecture**: **MVVM (Model-View-ViewModel)** - A clean and testable architecture pattern
  - **View**: Activities and XML Layouts for the UI
  - **ViewModel**: Manages UI-related data and state, surviving configuration changes
  - **Model**: The Repository pattern, which acts as the single source of truth for all app data
- **Asynchronous Programming**: **Kotlin Coroutines** for managing background threads and asynchronous operations like database access
- **Database**: **Room Persistence Library** - A powerful SQLite object mapping library for robust local data storage and caching
- **UI**: 
  - Android XML Layouts with Material Design 3 components
  - RecyclerView for displaying dynamic lists of pages and blocks efficiently
- **Dependency Management**: **Gradle** with Kotlin DSL (.kts)

## 📱 Usage

1. **Creating Pages**: Tap the "+" button to create a new page
2. **Adding Content**: Use the block-based editor to add different types of content
3. **Organizing**: Pages are automatically saved and can be organized in your preferred structure
4. **Offline Access**: All your data is stored locally and accessible without an internet connection

## 🚀 Future Roadmap

PocketPages is an evolving project with many exciting features planned:

### Planned Features

- **[#4] Add More Items**: A "+" button at the end of lists to quickly add new items
- **[#6] Multimedia Blocks**: Support for adding and viewing Images, Videos, and Audio files
- **[#7] Book Feature**: A dedicated view to group pages into a "book" with a page-turning UI
- **[#9] Text Customization**: A rich text formatting toolbar for bold, italics, color, font size, etc.
- **Cloud Synchronization**: Full implementation of Firebase or a custom backend for data backup and sync
- **Search**: A powerful search functionality to find content across all pages
- **Reminders & Scheduling**: Integration with the system calendar for reminders

# Team Member Contributions
## **Team Lead & Core Architecture (Devavrat Verma)**
- •	Contribution:
- o	App scaffolding
- o	MVVM architecture implementation
- o	Core components (Base classes, utilities)
- o	Navigation setup
- o	Code reviews & integration
## **Data Layer & Local Storage (Shrey Mittal)**
- •	Contribution:
- o	Complete Room database
- o	All DAOs and repositories
- o	Data encryption implementation
- o	Local data synchronization logic
## **Authentication & Sync (Dev Asati)**
- •	Contribution:
- o	Complete auth flow
- o	Sync implementation
- o	Conflict resolution UI/UX
## **UI/UX - Core Features (Sidhant Choudhury)**
- •	Contribution:
- o	Page hierarchy UI
- o	Page list with expandable items
- o	Fully functional rich text editor
- o	Accessible UI components
## **UI/UX - Additional Features (Hrishikesh Virupakshi)**
- •	Contribution:
- o	Database views (table/list)
- o	Complete settings UI
- o	Profile management
## **Integration & Testing (Shreejit Paul)**
- •	Contribution:
- o	Share/export functionality
- o	Version history
- o	Testing (unit/integration/UI)
- o	Test suite implementation
