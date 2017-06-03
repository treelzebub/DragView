# DragView
A Kotlin port of https://github.com/ebanx/swipe-button

Changes from the original:
- Over a hundred fewer lines of code, thanks to Kotlin :)
- Changed attrs to conform with standard Android attributes 
  - (eg. `inner_text_top_padding` became `inner_text_paddingTop`)
- Added a `button_padding` attr that sets padding on all sides
- `OnStateChangedListener` has separate functions for `onEnabled()` and `onDisabled()`, instead of `onStateChanged(Boolean)`

Coming soon:
- a `Config` object for setting custom animation durations and more!
