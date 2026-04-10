# Technical Documentation - The Long Way

This document provides a technical overview of **The Long Way** Android application, its current implementation status, and internal architecture.

## 1. Project Overview

**The Long Way** is an Android application designed to provide navigation routes that are deliberately longer than the fastest path, encouraging exploration. It leverages modern Android development tools and cloud-based mapping services to deliver a fluid mapping experience.

## 2. Architecture

The project follows the **MVVM (Model-View-ViewModel)** architectural pattern, ensuring a clear separation of concerns and scalability:

- **View (UI)**: Built with **Jetpack Compose**, organized into modular feature-based packages to handle specific UI components and screens.
- **ViewModel**: Manages the UI state and coordinates between the view and the underlying business logic, residing within feature-specific viewmodel layers.
- **Model / Core Services**: Handles core infrastructure including networking (Retrofit), location updates, and hardware sensors, centralized in core logic packages.

## 3. Core Features & Implementation

### 3.1. Interactive Map
- Integrated using **MapLibre SDK for Android**.
- Displays a vector map with custom styles from Stadia Maps.
- Supports user location tracking and dynamic markers for search results.
- **Camera Management**: Smooth animations to zoom into searched locations or center on the user.

### 3.2. Geocoding & Search
- **Autocompletion**: Uses the Stadia Maps (Pelias) `/geocoding/v1/autocomplete` endpoint. Suggestions appear as the user types (with a 300ms debounce to optimize API calls).
- **Search**: Final search performed via the `/geocoding/v1/search` endpoint when the user confirms their query.
- **State Management**: Uses Kotlin Coroutines and `StateFlow` for reactive UI updates.

### 3.3. Routing Logic (In Progress)
- **Routing Mode**: The app can switch between a simple search interface and a dual-input routing interface (Origin/Destination).
- **Directions Support**: Once a destination is found, a dedicated "Directions" button appears to initiate the routing flow.
- **Origin Selection**: Users can manually type an address or use the "Current Location" shortcut, which retrieves data from the `LocationProvider`.

### 3.4. Dynamic Themes
- **Ambient Light Sensor**: The app monitors the device's light sensor via `SensorEventListener`.
- **Automatic Style Switching**: When the ambient light falls below 50 lux, the map style automatically switches to "Alidade Smooth Dark", and reverts to "Alidade Smooth" in brighter conditions.

## 4. Technical Stack

| Component | Library / Service |
|-----------|-------------------|
| **UI** | Jetpack Compose |
| **Map Engine** | MapLibre SDK |
| **Map Data** | Stadia Maps |
| **Networking** | Retrofit + Moshi |
| **Location** | Android Fused Location Provider |
| **Async** | Kotlin Coroutines & Flow |

## 5. Current Implementation Status

### Completed ✅
- Basic Map setup with MapLibre.
- Geocoding/Autocomplete integration with Stadia Maps.
- User location permissions and real-time updates.
- Light sensor-based theme switching.
- Search-to-zoom workflow.
- Routing UI (Origin/Destination input fields).

### Planned 🛠️
- **Valhalla Integration**: Connect to the routing engine to fetch polyline data.
- **"The Long Way" Algorithm**: Implement the core logic to generate explorative routes based on time/distance overhead.
- **Turn-by-Turn Navigation**: Integration with Ferrostar for active guidance.
- **Save/Favorite Routes**: Persistent storage for discovered paths.

---
*Documentation last updated: April 2026*
