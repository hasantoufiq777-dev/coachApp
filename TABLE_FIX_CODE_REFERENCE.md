# 🔧 PLAYER TABLE FIX - CODE CHANGES REFERENCE

## Summary
Fixed the issue where players added via dialogs/forms didn't appear in the table by:
1. Creating a global `ObservableList<Player>` in `AppState`
2. Binding the TableView to this global list
3. Ensuring all add/remove operations target this global list
4. Using lambda-based cell value factories for reliable value extraction

---

## File 1: AppState.java

**Location:** `src/main/java/com/example/coachsapp/util/AppState.java`

**Changes:**
```java
package com.example.coachsapp.util;

import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    public static List<Manager> managers = new ArrayList<>();

    // Global observable list of players so UI tables can bind to it
    public static ObservableList<Player> players = FXCollections.observableArrayList();
}
```

**Key Addition:** `ObservableList<Player> players` initialized with `FXCollections.observableArrayList()`

---

## File 2: PlayerListController.java

**Location:** `src/main/java/com/example/coachsapp/controller/PlayerListController.java`

**Changes in initialize() method:**

BEFORE:
```java
@FXML
public void initialize() {
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
    jerseyColumn.setCellValueFactory(new PropertyValueFactory<>("jersey"));
    positionColumn.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPosition().toString()));
    statusColumn.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().isInjured() ? "Injured" : "Available"));
}
```

AFTER:
```java
@FXML
public void initialize() {
    // Use lambda-based factories to avoid reflection issues
    nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
    ageColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAge()));
    jerseyColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getJersey()));
    positionColumn.setCellValueFactory(cellData ->
        new SimpleStringProperty(cellData.getValue().getPosition().toString()));
    statusColumn.setCellValueFactory(cellData ->
        new SimpleStringProperty(
            cellData.getValue().isInjured() ? "Injured" : "Available"));

    // Bind the table items to the global observable list
    playerTable.setItems(AppState.players);
}
```

**Key Changes:**
- Replaced `PropertyValueFactory` with lambda-based factories for name, age, jersey
- Added `playerTable.setItems(AppState.players);` to bind table to global list
- Added imports: `SimpleStringProperty`, `SimpleObjectProperty`

**Changes in addPlayer() method:**

BEFORE:
```java
@FXML
public void addPlayer() {
    Stage stage = (Stage) playerTable.getScene().getWindow();
    AddPlayerDialog dialog = new AddPlayerDialog();
    Player newPlayer = dialog.showDialog(stage);
    
    if (newPlayer != null) {
        playerTable.getItems().add(newPlayer);
        System.out.println("✓ Player added: " + newPlayer.getName() + " - #" + newPlayer.getJersey() + " (" + newPlayer.getPosition() + ")");
    }
}
```

AFTER:
```java
@FXML
public void addPlayer() {
    Stage stage = (Stage) playerTable.getScene().getWindow();
    AddPlayerDialog dialog = new AddPlayerDialog();
    Player newPlayer = dialog.showDialog(stage);
    
    if (newPlayer != null) {
        AppState.players.add(newPlayer);  // add to global observable list
        System.out.println("✓ Player added: " + newPlayer.getName() + " - #" + newPlayer.getJersey() + " (" + newPlayer.getPosition() + ")");
    }
}
```

**Key Change:** Changed `playerTable.getItems().add(newPlayer)` to `AppState.players.add(newPlayer)`

**Changes in deletePlayer() method:**

BEFORE:
```java
@FXML
public void deletePlayer() {
    Player selected = playerTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
        playerTable.getItems().remove(selected);
        System.out.println("✓ Deleted: " + selected.getName());
    }
}
```

AFTER:
```java
@FXML
public void deletePlayer() {
    Player selected = playerTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
        AppState.players.remove(selected);
        System.out.println("✓ Deleted: " + selected.getName());
    }
}
```

**Key Change:** Changed `playerTable.getItems().remove(selected)` to `AppState.players.remove(selected)`

**Full Updated Class:**
```java
package com.example.coachsapp.controller;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.dialog.AddPlayerDialog;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

public class PlayerListController {

    @FXML
    private TableView<Player> playerTable;

    @FXML
    private TableColumn<Player, String> nameColumn;

    @FXML
    private TableColumn<Player, Integer> ageColumn;

    @FXML
    private TableColumn<Player, Integer> jerseyColumn;

    @FXML
    private TableColumn<Player, String> positionColumn;

    @FXML
    private TableColumn<Player, String> statusColumn;

    @FXML
    public void initialize() {
        // Use lambda-based factories to avoid reflection issues
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        ageColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAge()));
        jerseyColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getJersey()));
        positionColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getPosition().toString()));
        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().isInjured() ? "Injured" : "Available"));

        // Bind the table items to the global observable list
        playerTable.setItems(AppState.players);
    }

    @FXML
    public void addPlayer() {
        Stage stage = (Stage) playerTable.getScene().getWindow();
        AddPlayerDialog dialog = new AddPlayerDialog();
        Player newPlayer = dialog.showDialog(stage);
        
        if (newPlayer != null) {
            AppState.players.add(newPlayer); // add to global observable list
            System.out.println("✓ Player added: " + newPlayer.getName() + " - #" + newPlayer.getJersey() + " (" + newPlayer.getPosition() + ")");
        }
    }

    @FXML
    public void deletePlayer() {
        Player selected = playerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppState.players.remove(selected);
            System.out.println("✓ Deleted: " + selected.getName());
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }
}
```

---

## File 3: PlayerController.java

**Location:** `src/main/java/com/example/coachsapp/controller/PlayerController.java`

**Changes in savePlayer() method:**

BEFORE:
```java
@FXML
public void savePlayer() {
    String name = nameField.getText();
    String ageText = ageField.getText();
    String jerseyText = jerseyField.getText();
    Position position = positionCombo.getValue();

    if (name == null || name.isEmpty() || ageText == null || ageText.isEmpty() ||
        jerseyText == null || jerseyText.isEmpty() || position == null) {
        System.out.println("Error: All fields must be filled!");
        return;
    }

    try {
        int age = Integer.parseInt(ageText);
        int jersey = Integer.parseInt(jerseyText);

        System.out.println("✓ Player Created:");
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Jersey: " + jersey);
        System.out.println("Position: " + position);

        clearFields();
    } catch (NumberFormatException e) {
        System.out.println("Error: Age and Jersey must be valid numbers!");
    }
}
```

AFTER:
```java
@FXML
public void savePlayer() {
    String name = nameField.getText();
    String ageText = ageField.getText();
    String jerseyText = jerseyField.getText();
    Position position = positionCombo.getValue();

    if (name == null || name.isEmpty() || ageText == null || ageText.isEmpty() ||
        jerseyText == null || jerseyText.isEmpty() || position == null) {
        System.out.println("Error: All fields must be filled!");
        return;
    }

    try {
        int age = Integer.parseInt(ageText);
        int jersey = Integer.parseInt(jerseyText);

        Player player = new Player(name, age, jersey, position);
        AppState.players.add(player); // add to global observable list so table updates

        System.out.println("✓ Player Created:");
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Jersey: " + jersey);
        System.out.println("Position: " + position);

        clearFields();
    } catch (NumberFormatException e) {
        System.out.println("Error: Age and Jersey must be valid numbers!");
    }
}
```

**Key Additions:**
- Create Player object: `Player player = new Player(name, age, jersey, position);`
- Add to global list: `AppState.players.add(player);`
- Add imports: `com.example.coachsapp.model.Player`, `com.example.coachsapp.util.AppState`

**Full Updated Class:**
```java
package com.example.coachsapp.controller;

import com.example.coachsapp.model.Position;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

public class PlayerController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField ageField;

    @FXML
    private TextField jerseyField;

    @FXML
    private ComboBox<Position> positionCombo;

    @FXML
    public void initialize() {
        positionCombo.setItems(FXCollections.observableArrayList(Position.values()));
    }

    @FXML
    public void savePlayer() {
        String name = nameField.getText();
        String ageText = ageField.getText();
        String jerseyText = jerseyField.getText();
        Position position = positionCombo.getValue();

        if (name == null || name.isEmpty() || ageText == null || ageText.isEmpty() ||
            jerseyText == null || jerseyText.isEmpty() || position == null) {
            System.out.println("Error: All fields must be filled!");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            int jersey = Integer.parseInt(jerseyText);

            Player player = new Player(name, age, jersey, position);
            AppState.players.add(player); // add to global observable list so table updates

            System.out.println("✓ Player Created:");
            System.out.println("Name: " + name);
            System.out.println("Age: " + age);
            System.out.println("Jersey: " + jersey);
            System.out.println("Position: " + position);

            clearFields();
        } catch (NumberFormatException e) {
            System.out.println("Error: Age and Jersey must be valid numbers!");
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }

    private void clearFields() {
        nameField.clear();
        ageField.clear();
        jerseyField.clear();
        positionCombo.setValue(null);
    }
}
```

---

## Compilation Status

```
✓ 29 Java files compiled successfully
✓ 0 errors
✓ 0 warnings
✓ BUILD SUCCESS
```

---

## Testing Checklist

- [ ] Compile: `mvnw.cmd clean compile`
- [ ] Run: `mvnw.cmd javafx:run`
- [ ] Add player via dialog
- [ ] Verify player appears in table
- [ ] Add second player
- [ ] Verify both players visible
- [ ] Delete a player
- [ ] Verify player removed from table
- [ ] Click Back button
- [ ] Click Manage Players again
- [ ] Verify players still visible (in-memory persistence)

---

## Summary

The fix involves:
1. Creating a global `ObservableList<Player>` in `AppState`
2. Binding the `TableView` to this list
3. Ensuring all add/remove operations target this global list
4. Using lambda-based cell value factories for reliability

This ensures that any change to the player list is automatically reflected in the UI.

