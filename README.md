# @sinandsean/react-native-wheel-picker

A high-performance native wheel picker for React Native with smooth scrolling, haptic feedback, and customizable styling.

## Features

- Native implementation for both iOS and Android
- Smooth scroll with momentum and snap-to-item
- Haptic feedback on item change
- Customizable font family
- Optional unit labels (e.g., "kg", "cm")
- Multi-column picker support
- TypeScript support

## Installation

```bash
npm install @sinandsean/react-native-wheel-picker
# or
yarn add @sinandsean/react-native-wheel-picker
```

### iOS

```bash
cd ios && pod install
```

### Android

No additional setup required. The library will auto-link.

## Usage

### Basic Usage

```tsx
import { WheelPicker } from "@sinandsean/react-native-wheel-picker";

function App() {
  const [selectedIndex, setSelectedIndex] = useState(0);
  const items = ["Item 1", "Item 2", "Item 3", "Item 4", "Item 5"];

  return (
    <WheelPicker
      items={items}
      selectedIndex={selectedIndex}
      onValueChange={setSelectedIndex}
    />
  );
}
```

### With Unit Label

```tsx
<WheelPicker
  items={["50", "55", "60", "65", "70", "75", "80"]}
  selectedIndex={selectedIndex}
  unit="kg"
  onValueChange={setSelectedIndex}
/>
```

### Multi-Column Picker

```tsx
import { MultiColumnWheelPicker } from "@sinandsean/react-native-wheel-picker";

function HeightPicker() {
  const [feet, setFeet] = useState(5);
  const [inches, setInches] = useState(6);

  return (
    <MultiColumnWheelPicker
      columns={[
        {
          values: ["4", "5", "6", "7"],
          unit: "ft",
          selectedIndex: feet - 4,
          onSelect: (index) => setFeet(index + 4),
        },
        {
          values: [
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
          ],
          unit: "in",
          selectedIndex: inches,
          onSelect: setInches,
        },
      ]}
    />
  );
}
```

### Custom Font

```tsx
<WheelPicker
  items={items}
  selectedIndex={selectedIndex}
  fontFamily="SFProText-Semibold"
  onValueChange={setSelectedIndex}
/>
```

## Props

### WheelPicker

| Prop            | Type                      | Required | Description                                 |
| --------------- | ------------------------- | -------- | ------------------------------------------- |
| `items`         | `string[]`                | Yes      | Array of items to display                   |
| `selectedIndex` | `number`                  | Yes      | Currently selected item index               |
| `onValueChange` | `(index: number) => void` | No       | Callback when selection changes             |
| `unit`          | `string`                  | No       | Unit label displayed next to selected value |
| `fontFamily`    | `string`                  | No       | Custom font family name                     |
| `style`         | `ViewStyle`               | No       | Container style                             |
| `testID`        | `string`                  | No       | Test ID for e2e testing                     |

### MultiColumnWheelPicker

| Prop         | Type            | Required | Description                    |
| ------------ | --------------- | -------- | ------------------------------ |
| `columns`    | `WheelColumn[]` | Yes      | Array of column configurations |
| `fontFamily` | `string`        | No       | Font family for all columns    |
| `style`      | `ViewStyle`     | No       | Container style                |
| `testID`     | `string`        | No       | Test ID for e2e testing        |

### WheelColumn

| Property        | Type                      | Required | Description                     |
| --------------- | ------------------------- | -------- | ------------------------------- |
| `values`        | `string[]`                | Yes      | Array of values for this column |
| `selectedIndex` | `number`                  | Yes      | Selected index for this column  |
| `onSelect`      | `(index: number) => void` | Yes      | Selection callback              |
| `unit`          | `string`                  | No       | Unit label for this column      |
| `width`         | `number`                  | No       | Flex width (default: 1)         |

## Customization

### Visual Specifications

- Item height: 48dp
- Visible items: 5
- Text size: 24sp
- Selection indicator: Rounded rectangle with 16dp corner radius
- Default text color: `#1C1C1C`
- Selection background: `#F7F9FF`

### Font Setup

**iOS**: Use the PostScript name of the font (e.g., `SFProText-Semibold`)

**Android**: Place font files in `android/app/src/main/assets/fonts/` with matching names (e.g., `SFProText-Semibold.ttf` or `.otf`)

## License

MIT
