import Foundation
import React

@objc(WheelPickerViewManager)
class WheelPickerViewManager: RCTViewManager {

    override func view() -> UIView! {
        return WheelPickerViewWrapper()
    }

    override static func requiresMainQueueSetup() -> Bool {
        return true
    }

    override func constantsToExport() -> [AnyHashable : Any]! {
        return [:]
    }
}

@objc(WheelPickerViewWrapper)
class WheelPickerViewWrapper: UIView {

    private let wheelPicker = WheelPickerView()

    @objc var items: [String] = [] {
        didSet {
            wheelPicker.setItems(items)
        }
    }

    @objc var selectedIndex: NSNumber = 0 {
        didSet {
            wheelPicker.setSelectedIndex(selectedIndex.intValue)
        }
    }

    @objc var unit: String? {
        didSet {
            wheelPicker.setUnit(unit)
        }
    }

    @objc var fontFamily: String? {
        didSet {
            wheelPicker.setFontFamily(fontFamily)
        }
    }

    @objc var onValueChange: RCTDirectEventBlock?

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupView()
    }

    private func setupView() {
        addSubview(wheelPicker)

        wheelPicker.onValueChange = { [weak self] index in
            self?.onValueChange?(["index": index])
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        wheelPicker.frame = bounds
    }
}
