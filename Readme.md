# DevDigest

**DevDigest** is a modern Android news reader built with **Kotlin, Clean Architecture, and Paging 3**.  
The app fetches the latest technology and developer-focused stories from the **Hacker News Algolia API** and presents them in a clean, fast, and highly responsive UI.

The project focuses on **production‑level Android architecture**, demonstrating best practices for modern Android development including **MVI state management, Paging, Room caching, and unidirectional data flow**.

---

## ✨ Features

- Latest developer and technology news
- Infinite scrolling with **Paging 3**
- Offline caching using **Room Database**
- Favorite articles support
- Article reading using **Chrome Custom Tabs**
- Reactive UI powered by **Kotlin Flow**
- Debounced search functionality
- Material Design UI with light and dark themes
- Scroll‑to‑top behavior for long lists
- Clean and modular architecture

---

## 🧠 Architecture

DevDigest follows **Clean Architecture** with clear separation of concerns.

```
presentation
│
├── fragments
├── adapters
├── viewmodels
└── utils

domain
│
├── models
├── repositories
└── usecases

data
│
├── remote
├── local
├── repository
└── paging
```

### Layers

**Presentation**
- Fragments
- ViewModels
- MVI UI State
- RecyclerView adapters

**Domain**
- Business logic
- Use cases
- Repository interfaces
- Pure Kotlin models

**Data**
- Retrofit API
- Room database
- Paging RemoteMediator
- Repository implementations

---

## 🔄 Data Flow

```
API → RemoteMediator → Room Database → PagingSource → ViewModel → UI
```

This ensures:

- smooth scrolling
- automatic pagination
- offline data availability
- reactive UI updates

---

## 📦 Tech Stack

- **Kotlin**
- **Coroutines & Flow**
- **Jetpack Paging 3**
- **Room Database**
- **Retrofit**
- **Hilt Dependency Injection**
- **Material Design Components**
- **Chrome Custom Tabs**

---

## 🌐 API

DevDigest uses the **Hacker News Algolia API**.

Example endpoint:

```
https://hn.algolia.com/api/v1/search?tags=front_page
```

Documentation:

```
https://hn.algolia.com/api
```

---

## 📱 Screens

### Trending News
Shows the latest developer and technology stories with infinite scroll.

### Favorites
Users can save stories and quickly access them later.

### Article View
Articles open inside the app using Chrome Custom Tabs for a seamless reading experience.

---

## ⚡ Key Implementations

### Paging with RemoteMediator

Pagination is handled using **Paging 3 with RemoteMediator**, allowing the app to:

- cache pages locally
- fetch new pages when scrolling
- refresh data efficiently

### Debounced Search

Search queries are processed with:

```
debounce
distinctUntilChanged
```

to avoid unnecessary API calls.

### Scroll‑to‑Top UX

Pressing back when deep in the list scrolls to the top first before exiting, improving navigation in long feeds.

---

## 🛠 Project Goals

This project was built to demonstrate:

- modern Android architecture
- scalable code organization
- real‑world Paging implementation
- clean UI state management
- production‑ready patterns

---

## 🚀 Future Improvements

- Article reading mode with WebView
- Bookmark syncing
- Content filtering
- Improved offline support
- Tablet UI support

---

## 👨‍💻 Author

**Sridhar Prasath**


GitHub  
https://github.com/sridharprasath94
