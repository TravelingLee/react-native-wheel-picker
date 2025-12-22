import React, { useCallback } from 'react';
import {
  requireNativeComponent,
  Platform,
  View,
  StyleSheet,
  type ViewStyle,
  type StyleProp,
} from 'react-native';

export interface WheelPickerProps {
  /**
   * Array of string items to display in the picker
   */
  items: string[];

  /**
   * Index of the currently selected item
   */
  selectedIndex: number;

  /**
   * Optional unit label displayed next to the selected value
   * @example "kg", "cm", "years"
   */
  unit?: string;

  /**
   * Custom font family name
   * @example "SFProText-Semibold" (iOS) or "SF-Pro-Text-Semibold" (Android)
   */
  fontFamily?: string;

  /**
   * Callback when the selected value changes
   * @param index - The new selected index
   */
  onValueChange?: (index: number) => void;

  /**
   * Style for the picker container
   */
  style?: StyleProp<ViewStyle>;

  /**
   * Test ID for e2e testing
   */
  testID?: string;
}

interface NativeWheelPickerProps {
  items: string[];
  selectedIndex: number;
  unit?: string;
  fontFamily?: string;
  onValueChange?: (event: { nativeEvent: { index: number } }) => void;
  style?: StyleProp<ViewStyle>;
}

const NativeWheelPicker =
  Platform.OS === 'android' || Platform.OS === 'ios'
    ? requireNativeComponent<NativeWheelPickerProps>('WheelPickerView')
    : null;

const PICKER_HEIGHT = 240;

export function WheelPicker({
  items,
  selectedIndex,
  unit,
  fontFamily,
  onValueChange,
  style,
  testID,
}: WheelPickerProps): React.ReactElement | null {
  const handleValueChange = useCallback(
    (event: { nativeEvent: { index: number } }) => {
      onValueChange?.(event.nativeEvent.index);
    },
    [onValueChange]
  );

  if (!NativeWheelPicker) {
    if (__DEV__) {
      console.warn('WheelPicker is only available on iOS and Android');
    }
    return null;
  }

  return (
    <View style={[styles.container, style]} testID={testID}>
      <NativeWheelPicker
        items={items}
        selectedIndex={selectedIndex}
        unit={unit}
        fontFamily={fontFamily}
        onValueChange={handleValueChange}
        style={styles.picker}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    height: PICKER_HEIGHT,
    overflow: 'hidden',
  },
  picker: {
    flex: 1,
  },
});

export default WheelPicker;
