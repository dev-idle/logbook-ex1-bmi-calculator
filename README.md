# BMI Calculator

COMP1786 logbook, exercise 1: an Android app, written in Java, that calculates body mass index
(BMI) from a weight and height in metric, US or UK units and gives color-coded health category
feedback.

## Requirements

| ID | Requirement | How it is met |
|----|-------------|---------------|
| LB1-01 | Take weight and height and calculate the BMI | `BmiCalculator` divides weight in kilograms by the square of height in meters and rounds to one decimal place |
| LB1-02 | Health category feedback with color coding | The result shows the category on its color, outlines the card in it, and lists all categories on a color-coded scale with the user's highlighted (`BmiCategory`, `CategoryAppearance`) |
| LB1-03 | Support different units for weight and height | Weight in kilograms, pounds, or stones and pounds; height in centimeters, meters, or feet and inches; chosen with toggle buttons. A typed value is converted when the unit changes (`WeightUnit`, `HeightUnit`, `MeasurementConverter`) |
| LB1-04 | Validate that input is a valid number within a reasonable range | `MeasurementValidator` rejects empty fields, text that is not a number, and values outside each unit's range |
| LB1-05 | Display an error message for invalid or out-of-range input | Each problem appears on the field it belongs to and states the accepted range |
| LB1-06 | Clean UI using appropriate views, themes, styles and resources | Material 3 day/night theme, custom styles, and all text, colors and dimensions in resources |

### Beyond the requirements

- Accepted ranges are shown under each field before the user makes a mistake.
- The healthy weight range for the user's height is shown in the selected unit.
- The selected units are remembered. Until the user picks units, they follow the device's region:
  stones and feet in the UK, pounds and feet in the US, kilograms and centimeters elsewhere.
- An About dialog explains BMI and links to the source of the categories.
- The layout works in portrait and landscape, in light and dark mode, and on tablets.
- Screen readers announce the result as one sentence, and units are read as full words.

## Accepted ranges

| Unit | Accepted range |
|------|----------------|
| Kilograms | 10 to 400 kg |
| Pounds | 22 to 880 lb |
| Stones and pounds | 1 st 8 lb to 62 st 12 lb (pounds below 14) |
| Centimeters | 50 to 250 cm |
| Meters | 0.5 to 2.5 m |
| Feet and inches | 1 ft 8 in to 8 ft 2 in (inches below 12) |

The ranges accept any adult while catching typing mistakes, such as an extra digit, or a height
typed in centimeters while meters is selected.

## BMI categories

The categories are the adult categories published by the U.S. Centers for Disease Control and
Prevention (CDC), which match the World Health Organization classification. They apply to adults
aged 20 and over.

| Category | BMI |
|----------|-----|
| Underweight | Below 18.5 |
| Healthy weight | 18.5 to 24.9 |
| Overweight | 25.0 to 29.9 |
| Class 1 obesity | 30.0 to 34.9 |
| Class 2 obesity | 35.0 to 39.9 |
| Class 3 obesity | 40.0 and above |

The BMI is rounded to one decimal place before it is classified, so the category always matches
the number shown: a BMI of 24.96 is shown as 25.0 and classified as overweight.

## Technology

| Item | Version |
|------|---------|
| Language | Java 17 |
| Android Gradle Plugin / Gradle | 9.4.1 / 9.8.0 |
| `compileSdk` / `targetSdk` / `minSdk` | 37 / 37 / 33 (Android 13) |
| UI | Android Views, Material Components 1.14.0, View Binding |
| Architecture | ViewModel and LiveData (Lifecycle 2.11.0) |
| Tests | JUnit 4.13.2, AndroidX Test, Espresso 3.7.0 |

## Project structure

```
app/src/main/java/com/comp1786/logbook/bmi/
├── domain/   Units, conversion, validation and BMI calculation (plain Java, no Android types)
│             Weight and height units share one MeasurementUnit interface, and compound units
│             (stones and pounds, feet and inches) share one CompoundQuantity type.
├── data/     Remembers the selected units (SharedPreferences)
└── ui/       Calculator screen, its view model and state, and the About dialog
```

## Build and run

### Android Studio

1. Open this folder with **File > Open**.
2. Wait for the Gradle sync to finish.
3. Select an emulator or device running Android 13 (API 33) or later and press **Run**.

### Command line

Android Studio's bundled JDK can be used by setting `JAVA_HOME` to its `jbr` folder.

```bash
./gradlew assembleDebug        # Build the APK
./gradlew installDebug         # Install on a connected device or emulator
```

## Tests

```bash
./gradlew testDebugUnitTest            # 69 unit tests for the domain and view model
./gradlew connectedDebugAndroidTest    # Espresso tests; needs a running emulator or device
./gradlew lintDebug                    # Android Lint
```

## References

- CDC, *Adult BMI Categories*:
  https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html
- Android Developers, *Display content edge-to-edge in views*:
  https://developer.android.com/develop/ui/views/layout/edge-to-edge
- Android Developers, *Back up user data with Auto Backup*:
  https://developer.android.com/identity/data/autobackup
- Material Design 3 for Android:
  https://github.com/material-components/material-components-android
- The pound (0.45359237 kg) and the inch (2.54 cm) are exact by the 1959 international yard and
  pound agreement; a stone is 14 pounds.
