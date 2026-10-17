# BMI Calculator

Android app for COMP1786 Logbook Exercise 1. It calculates the body mass
index (BMI) of adults aged 20 and over and classifies it with the WHO adult
categories.

## Features

- Weight in kg, lb or st lb and height in cm, m or ft in, chosen separately.
  The app remembers each choice and converts the entered value when the unit
  changes.
- Inline validation errors for empty, invalid and out-of-range input.
- Result card with:
  - the BMI to two decimal places and its WHO category
  - how far the weight is from the normal range
  - a color scale and the category table
  - the normal weight range for the height
- A caution when an entry is outside the range of the NHS adult calculator.
- Light and dark themes, a two-column landscape layout, and a result that is
  kept after rotation.
- About BMI dialog with a link to the WHO fact sheet.

## Calculation

BMI = weight (kg) / height (m)² [1]. The app rounds the result half up to two
decimal places, the precision of the WHO table, and classifies the rounded
value [2]:

| Category        | BMI           |
| --------------- | ------------- |
| Underweight     | below 18.50   |
| Normal range    | 18.50–24.99   |
| Preobese        | 25.00–29.99   |
| Obese class I   | 30.00–34.99   |
| Obese class II  | 35.00–39.99   |
| Obese class III | 40.00 or more |

These categories are for adults aged 20 and older [3]. WHO assesses children
and teenagers aged 5 to 19 with BMI-for-age instead [1].

Unit conversion is exact: 1 lb = 0.45359237 kg, 1 in = 0.0254 m and
1 st = 14 lb [4][5]. All calculations use `BigDecimal`, so no floating-point
rounding is introduced.

## Input limits

- Accepted: 5–650 kg and 50–280 cm, with the same limits in every unit. They
  cover the shortest, tallest and heaviest adults recorded by Guinness World
  Records [6][7][8][9].
- Caution: outside 25.4–317.5 kg (4–50 st) or 139.7–243.8 cm
  (4 ft 7 in – 8 ft), the range of the NHS adult calculator [10].

## Build and run

- Android Studio with its bundled JDK, AGP 9.4.1 and Gradle 9.6.0.
- minSdk 33, targetSdk 37, Java 11.
- Tested on a Pixel 7 Pro emulator running API 33.

Open the project in Android Studio and run the `app` configuration, or build
from the command line:

```
./gradlew assembleDebug
```

## Project structure

| File                        | Purpose                                         |
| --------------------------- | ----------------------------------------------- |
| `MainActivity.java`         | Input, unit switching, result and About dialog  |
| `BmiCalculator.java`        | BMI formula and normal weight range             |
| `BmiCategory.java`          | WHO categories and their colors                 |
| `MeasurementUnit.java`      | Units, exact conversion factors and limits      |
| `MeasurementValidator.java` | Input validation                                |

## Sources

1. WHO. Obesity and overweight. Fact sheet.
   https://www.who.int/news-room/fact-sheets/detail/obesity-and-overweight
2. WHO. Obesity: preventing and managing the global epidemic. Technical Report
   Series 894, 2000, Table 2.1. https://iris.who.int/handle/10665/42330
3. CDC. Adult BMI categories.
   https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html
4. NIST. Special Publication 811, Appendix B.9.
   https://www.nist.gov/pml/special-publication-811/nist-guide-si-appendix-b-conversion-factors/nist-guide-si-appendix-b9
5. Weights and Measures Act 1985, Schedule 1.
   https://www.legislation.gov.uk/ukpga/1985/72/schedule/1
6. Guinness World Records. Shortest man ever.
   https://www.guinnessworldrecords.com/world-records/67521-shortest-man-ever
7. Guinness World Records. Afshin Esmaeil Ghaderzadeh is confirmed as the new
   shortest man in the world, 2022.
   https://www.guinnessworldrecords.com/news/2022/12/afshin-esmaeil-ghaderzadeh-is-confirmed-as-the-new-shortest-man-in-the-world-730105
8. Guinness World Records. Tallest man ever.
   https://www.guinnessworldrecords.com/world-records/tallest-man-ever
9. Guinness World Records. Heaviest man.
   https://www.guinnessworldrecords.com/world-records/heaviest-man
10. NHS. BMI calculator for adults.
    https://www.nhs.uk/health-assessment-tools/calculate-your-body-mass-index/calculate-bmi-for-adults
