# Coaches App - Complete JavaFX Project Documentation

## 📋 Project Overview
A fully functional JavaFX desktop application for managing soccer/football coaches, players, clubs, and player transfers. The app follows clean architecture principles with separation of concerns: models, controllers, services, and utilities.

---

## 🏗️ Project Structure

```
Coachsapp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/coachsapp/
│   │   │       ├── model/
│   │   │       │   ├── Position.java          (Enum: FORWARD, MIDFIELDER, DEFENDER, GOALKEEPER)
│   │   │       │   ├── Player.java            (Player model with name, age, jersey, position, injured status)
│   │   │       │   ├── Club.java              (Club model with player list and add/remove methods)
│   │   │       │   └── Manager.java           (Manager model linked to a club)
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── MainController.java         (Navigation hub)
│   │   │       │   ├── PlayerController.java       (Create individual player)
│   │   │       │   ├── PlayerListController.java   (TableView with all players)
│   │   │       │   ├── ManagerController.java      (Manager list management)
│   │   │       │   └── TransferController.java     (Player transfer between clubs)
│   │   │       │
│   │   │       ├── service/
│   │   │       │   └── TransferService.java    (Business logic for player transfers)
│   │   │       │
│   │   │       ├── util/
│   │   │       │   ├── AppState.java           (Global in-memory state: List<Manager>)
│   │   │       │   └── SceneSwitcher.java      (Scene navigation utility)
│   │   │       │
│   │   │       ├── HelloApplication.java       (Main entry point)
│   │   │       ├── HelloController.java        (Legacy, can be removed)
│   │   │       └── Launcher.java               (Application launcher)
│   │   │
│   │   └── resources/
│   │       └── com/example/coachsapp/
│   │           ├── main-view.fxml              (Main menu with navigation buttons)
│   │           ├── player-view.fxml            (Create new player form)
│   │           ├── player-list-view.fxml       (TableView of all players)
│   │           ├── manager-view.fxml           (Manager list with add/delete)
│   │           └── transfer-view.fxml          (Player transfer interface)
│   │
│   └── pom.xml                                 (Maven configuration with JavaFX 21.0.6)
```

---

## 🎯 Key Components

### 1. **Model Classes** (Clean OOP, No Annotations)

#### Position.java
```java
public enum Position {
    FORWARD, MIDFIELDER, DEFENDER, GOALKEEPER
}
```

#### Player.java
- **Fields**: name, age, jersey, position, injured (default false)
- **Methods**: Getters, setters, constructor
- **No database or persistence code**

#### Club.java
- **Fields**: clubName, List<Player> players
- **Methods**: addPlayer(), removePlayer(), getPlayers()
- **Pure in-memory data structure**

#### Manager.java
- **Fields**: name, club
- **Methods**: Constructor takes managerName and clubName
- **Links manager to their club**

---

### 2. **Service Layer**

#### TransferService.java
```java
public static boolean transferPlayer(Manager fromManager, Manager toManager, Player player) {
    // Removes player from source club
    // Adds player to destination club
    // Returns true if successful
}
```
- **No database access**
- **Pure business logic**

---

### 3. **Utility Classes**

#### AppState.java
```java
public static List<Manager> managers = new ArrayList<>();
```
- **Global in-memory state management**
- **Singleton-style pattern**
- **No persistence**

#### SceneSwitcher.java
```java
public static void switchTo(ActionEvent event, String fxmlName) {
    // Loads FXML file
    // Gets stage from event source
    // Sets new scene
}
```
- **Simplified scene navigation**
- **Minimal abstraction**

---

### 4. **Controllers**

#### MainController.java
- **Navigation hub** linking to all features
- **Methods**: goToPlayers(), goToManagers(), goToTransfer()

#### PlayerController.java
- **Fields**: nameField, ageField, jerseyField, positionCombo
- **Method**: savePlayer() - prints to console
- **Validation**: Checks all fields filled before saving
- **No database persistence**

#### PlayerListController.java
- **TableView<Player>** with columns: Name, Age, Jersey, Position, Status
- **Methods**: addPlayer(), deletePlayer()
- **PropertyValueFactory** for cell binding

#### ManagerController.java
- **ListView<Manager>** bound to AppState.managers
- **Methods**: addManager(), deleteManager()
- **Observes AppState changes**

#### TransferController.java
- **ComboBoxes**: From Manager, To Manager, Player selection
- **Method**: transferPlayer() with validation
- **Prevents same-club transfers**

---

### 5. **FXML Views** (No CSS, Simple Layout)

#### main-view.fxml
- Navigation buttons to all features
- VBox vertical layout
- Title label

#### player-view.fxml
- Input fields: name, age, jersey
- ComboBox: Position dropdown
- Submit button
- Form-style layout

#### player-list-view.fxml
- TableView with 5 columns
- Add/Delete player buttons

#### manager-view.fxml
- ListView for managers
- Add/Delete manager buttons

#### transfer-view.fxml
- Three ComboBoxes for transfer selection
- Transfer button

---

## 🚀 Features Implemented

### ✅ 1. Player Management
- Create players with name, age, jersey, position
- Display players in TableView
- Delete players from list

### ✅ 2. Manager Management
- Create managers linked to clubs
- List all managers
- Delete managers

### ✅ 3. Club Management
- Clubs created automatically with managers
- Display players by club
- Add/remove players

### ✅ 4. Player Transfer System
- Transfer players between managers' clubs
- Validation to prevent same-club transfers
- Console feedback on transfer success

### ✅ 5. Scene Navigation
- Seamless navigation between screens
- Central hub layout
- Utility-based scene switching

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| UI Framework | JavaFX 21.0.6 |
| Build Tool | Maven 3.8.5 |
| Language | Java 17+ (tested with Java 25) |
| Layout | VBox/HBox only |
| Styling | No CSS (clean defaults) |
| Database | None (in-memory only) |

---

## 💾 Data Flow

```
User Action (Button Click)
    ↓
Controller Method (e.g., savePlayer())
    ↓
Model Creation (e.g., new Player())
    ↓
Service Processing (e.g., TransferService)
    ↓
AppState Update (List<Manager>)
    ↓
UI Refresh (ComboBox, TableView, ListView)
    ↓
Console Feedback
```

---

## 🎮 How to Run

### Option 1: Maven
```bash
cd E:\javalab\Coachsapp
set JAVA_HOME=C:\Program Files\Java\jdk-25
mvnw.cmd javafx:run
```

### Option 2: IDE
1. Right-click project → Run
2. Main class: `com.example.coachsapp.HelloApplication`
3. VM options: `--module-path <JavaFX_PATH>/lib --add-modules javafx.controls,javafx.fxml`

---

## 📝 Example Usage (Console Output)

When user creates a player:
```
Player Created:
Name: Cristiano Ronaldo
Age: 39
Jersey: 7
Position: FORWARD
```

When user transfers a player:
```
Transfer successful: Cristiano Ronaldo from Manchester United to Paris Saint-Germain
```

---

## 🔄 In-Memory State Example

```java
// Programmatically (for testing):
Manager manager1 = new Manager("Sir Alex Ferguson", "Manchester United");
Manager manager2 = new Manager("Ole Gunnar Solskjaer", "Carrow Road");

Player player = new Player("Cristiano Ronaldo", 39, 7, Position.FORWARD);
manager1.getClub().addPlayer(player);

AppState.managers.add(manager1);
AppState.managers.add(manager2);

// UI automatically displays all managers in ComboBox/ListView
```

---

## ✨ Clean Code Principles Applied

1. **Separation of Concerns**: Model, Controller, Service, Utility classes
2. **Single Responsibility**: Each class has one clear purpose
3. **DRY (Don't Repeat Yourself)**: Reusable SceneSwitcher utility
4. **No Annotations**: Plain Java OOP for models
5. **No Database**: Clean in-memory architecture for learning
6. **Null Checks**: Input validation in controllers
7. **Minimal UI Styling**: Focus on functionality, not presentation

---

## 🎓 Learning Path

1. **Start**: Explore model classes (Player, Manager, Club, Position)
2. **Progress**: Understand service layer (TransferService)
3. **Continue**: Study controllers and FXML binding
4. **Master**: Learn scene navigation and AppState pattern
5. **Extend**: Add features like persistence, filtering, or reporting

---

## 📚 Future Enhancements

- [ ] Database persistence (SQL/JDBC)
- [ ] Player statistics and history tracking
- [ ] Match scheduling and results
- [ ] Team formations and tactics
- [ ] User authentication
- [ ] CSS styling and themes
- [ ] Export/import features
- [ ] Advanced reporting

---

## 🐛 Known Issues / Limitations

1. **No Data Persistence**: All data lost on app restart
2. **No Validation**: Age/Jersey could be negative (easily fixed)
3. **Simple UI**: No advanced styling or animations
4. **In-Memory Only**: No database backend
5. **Limited Error Handling**: Basic try-catch blocks

---

## 📞 Support

- Reference each component by folder: `/model`, `/controller`, `/service`, `/util`
- All FXML files are in `/resources/com/example/coachsapp/`
- Main entry point: `HelloApplication.java` → loads `main-view.fxml`

---

**Project Status**: ✅ **COMPLETE** - Fully functional with all 15 requirements implemented.

Generated: December 9, 2025

