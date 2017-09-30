# Articles

A list of articles or online answers that have directly contributed to certain portions of the app.

### Overriding App "Startup" class
Creating a custom Application class to run methods on app start, such as authentication file checking.

- Taken from [Stack Overflow - How can I execute something just once per application start](https://stackoverflow.com/questions/7360846/how-can-i-execute-something-just-once-per-application-start)

### Adding underline to TabLayout
Add a bottom-only line to a TabLayout background by drawing a shape and moving all other sides of the "rectangle" out of the drawing boundaries.

- Taken from: [Stack Overflow - Android TabLayout how to make two underlines](https://stackoverflow.com/questions/37676014/android-tablayout-how-to-make-two-underlines)

### Screen Utility Functions
 Several various Screen utility functions for determining current size, orientation, etc.

 - Taken from: [AlvinAlexander - How to determine Android screen sizes/dimensions/orientation](https://alvinalexander.com/android/how-to-determine-android-screen-size-dimensions-orientation)

### Access fragments in a TabLayout
Enable accessing specific fragments in a TabLayout (since they are created programmatically) in order to execute their (_public_) functions.

- Taken from: [Stack Overflow - Calling a Fragment method from an Activity Android tabs](https://stackoverflow.com/questions/25629042/calling-a-fragment-method-from-an-activity-android-tabs)

### Access Fragment XML elements from Fragment
In order to use `findViewById()` from inside a Fragment, a reference to a View is needed (ie. the parent View). This View reference can be found (and stored) inside the constructor, and then used like `parentView.findViewById()`.

### Create Snackbar in a Fragment
Snackbars need a view (ie. a context or anchor) to attach to, but fragments will occasionally disappear. In these cases, or other cases where the Snackbar should attach to the "app" as a whole, use a provided `id`.

```
View snackbarRoot = getActivity().findViewById(android.R.id.content);
```

### Hide the Soft Keyboard
The Soft Keyboard can be hidden by first checking for the currently focused view and then hiding the keyboard (using the `InputMethodManager` class).

- Taken from [Stack Overflow - Close/Hide the Android soft keyboard](https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard)

### Access the Android shell
Viewing the app's private files is possible through the use of the Android shell (ADB). Navigate to the project directory and run `adb shell` in a terminal. Inside the shell, type `run-as ca.kendallroth.mileageapp` to set the shell permission, the list the directory files (`ls files/`).

### Pass data between Activities
Passing data to a new Activity is as simple as properly using `intent.putExtras()` when creating the Intent - passing data back from the same Activity is also quite simple. Rather than call `startActivity` we call `startActivityForResult()` which will call the class's overriden `onActivityResult()` method when the Activity returns.

- Taken from : [Stack Overflow - How to pass data from 2nd activity to 1st activity when pressed back android](https://stackoverflow.com/questions/14292398/how-to-pass-data-from-2nd-activity-to-1st-activity-when-pressed-back-android)
