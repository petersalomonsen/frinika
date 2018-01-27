When coding an audio process you should keep this in mind:
-----------------------------------------------------------

- You should definitely not do any GUI stuff.
- You shouldn't do anything that will block or otherwise yield to the o/s sheduler.
- You shouldn't instantiate new objects or arrays.
- You shouldn't do anything that isn't absolutely essential for the particular DSP function.

- An AudioProcess should always define its requirements in terms that are natural to its DSP function. Conversions from user defined units such as Hz to, perhaps, radians/s, should be done by some other object in some other thread, perhaps the Swing Event Dispatch Thread.