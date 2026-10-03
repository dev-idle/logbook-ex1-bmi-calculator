# BMI Calculator

COMP1786 logbook, exercise 1: an Android app, written in Java, that calculates body mass index
(BMI) from a weight and height in metric, US or UK units and gives color-coded health category
feedback.

## Requirements

| ID | Requirement | How it is met |
|----|-------------|---------------|
| LB1-01 | Take weight and height and calculate the BMI | `BmiCalculator` divides weight in kilograms by the square of height in meters and rounds to one decimal place |
| LB1-02 | Health category feedback with color coding | The result shows the category on its color, points to the BMI on a color-coded gauge, and lists all categories with the user's row tinted in its color (`BmiCategory`, `CategoryAppearance`, `BmiGaugeView`) |
| LB1-03 | Support different units for weight and height | Weight in kilograms, pounds, or stones and pounds; height in centimeters, meters, or feet and inches; chosen on a selector whose pill slides to the chosen unit. A typed value is converted when the unit changes (`WeightUnit`, `HeightUnit`, `MeasurementConverter`) |
| LB1-04 | Validate that input is a valid number within a reasonable range | `MeasurementValidator` rejects empty fields, text that is not a number, and values outside each unit's range |
| LB1-05 | Display an error message for invalid or out-of-range input | Each problem appears on the field it belongs to and says how to fix it; out-of-range messages state the accepted range |
| LB1-06 | Clean UI using appropriate views, themes, styles and resources | Material 3 day/night theme with a calm blue color scheme, custom styles, Material motion, and all text, colors and dimensions in resources |

### Beyond the requirements

- Accepted ranges are shown under each field before the user makes a mistake.
- The healthy weight range for the user's height is shown in the selected unit.
- The selected units are remembered. Until the user picks units, they follow the device's region:
  stones and feet in the UK, pounds and feet in the US, kilograms and centimeters elsewhere.
- An About dialog explains BMI and links to the source of the categories.
- The number keyboard and a digit filter stop letters and signs from being typed at all.
- Unit changes animate: the selected pill slides, and the fields fade through when the number
  of input fields changes.
- The adaptive launcher icon, a person on a scale beside a height arrow, has a monochrome layer
  for Android 13 themed icons.
- The layout works in portrait and landscape, in light and dark mode, and on tablets.
- Screen readers announce the result as one sentence, and the unit buttons are read as full
  words.
- Validation messages follow Shneiderman's guidelines for error messages: precise, positive, and
  saying how to fix the problem.

## Accepted ranges

| Unit | Accepted range |
|------|----------------|
| Kilograms | 10 to 400 kg |
| Pounds | 22 to 881.8 lb |
| Stones and pounds | 1 st 8 lb to 62 st 13.8 lb (pounds below 14) |
| Centimeters | 50 to 250 cm |
| Meters | 0.5 to 2.5 m |
| Feet and inches | 1 ft 7.7 in to 8 ft 2.4 in (inches below 12) |

The ranges accept any adult while catching typing mistakes, such as an extra digit, or a height
typed in centimeters while meters is selected. The imperial limits are the metric limits
converted, so switching units never turns an accepted value into an error. A value outside the
range is cleared, not converted, when the unit changes.

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
the number shown: a BMI of 24.96 is shown as 25.0 and classified as overweight. The healthy weight
range uses the same rounding, so its ends agree with the category shown.

## Technology

| Item | Version |
|------|---------|
| Language | Java 17 |
| Android Gradle Plugin / Gradle | 9.4.1 / 9.8.0 |
| `compileSdk` / `targetSdk` / `minSdk` | 37 / 37 / 33 (Android 13) |
| UI | Android Views, Material Components 1.14.0, View Binding |
| Colors | Material 3 scheme generated from the seed #1E6FB8 with material-color-utilities |
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
./gradlew testDebugUnitTest            # 77 unit tests for the domain, view model and gauge
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
- Material color utilities, used to generate the color scheme:
  https://github.com/material-foundation/material-color-utilities
- Material Design, *Motion: easing and duration*:
  https://m3.material.io/styles/motion/easing-and-duration
- Shneiderman, B., Plaisant, C., Cohen, M., Jacobs, S., Elmqvist, N. and Diakopoulos, N. (2016)
  *Designing the User Interface: Strategies for Effective Human-Computer Interaction*. 6th edn.
  Pearson. Its guidelines for error messages shaped the validation messages.
- The pound (0.45359237 kg) and the inch (2.54 cm) are exact by the 1959 international yard and
  pound agreement; a stone is 14 pounds.
