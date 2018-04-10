# Speaker

---

The speaker is a peripheral which allows for Computers to talk (literally)! It can also be crafted with a turtle to make
 a [Talkative Turtle](/turtle_upgrades/talkative_turtle/).

## Events
| Event | Parameter 1 | Parameter 2 | Parameter 3 | Parameter 4 | Description |
|-------|-------------|-------------|-------------|-------------|-------------|
|synthComplete|"synthComplete"|_string_ text|_string_ event_uuid|_boolean_ success|This event is fired after the Text-to-Speech system is finished speaking|

## Functions
| Function | Returns | Description |
|----------|---------|-------------|
|speak(_string_ text, [_number_ range, [_string_ voice, [_number_ pitch, [_number_ pitchRange, [_number_ pitchShift, [_number_ rate, [_number_ volume [, _boolean_ pauseComputerUntilSpeechComplete\]\]\])|_string_ event_uuid|Synthesizes a message to play to nearby players|

## Additional Voices

Some EMBROLA voices are supported:

- us1
- us2
- us3

These can be downloaded from [here](http://tcts.fpms.ac.be/synthesis/mbrola/mbrcopybin.html) and extracted to 
 `<minecraft directory>/mods/peripheralsplusone/mbrola/`. The directory should also contain the EMBROLA executable.