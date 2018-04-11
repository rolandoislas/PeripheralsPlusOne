# Speaker

---

The speaker is a peripheral which allows for Computers to talk (literally)! It can also be crafted with a turtle to make
 a [Talkative Turtle](/turtle_upgrades/talkative_turtle/).

## Events
| Event | Parameter 1 | Parameter 2 | Parameter 3 | Parameter 4 | Parameter 5 | Description |
|-------|-------------|-------------|-------------|-------------|-------------|-------------|
|synthComplete|"synthComplete"|_string_ text|_string_ event_uuid|_boolean_ success|_string_ error_message|This event is fired after the Text-to-Speech system is finished speaking|

## Functions
| Function | Returns | Description |
|----------|---------|-------------|
|speak(_string_ text, [_number_ range, [_string_ voice, [_number_ pitch, [_number_ pitchRange, [_number_ pitchShift, [_number_ rate, [_number_ volume [, _boolean_ pauseComputerUntilSpeechComplete\]\]\]\]\]\]\]\])|_string_ event_uuid|Synthesizes a message to play to nearby players|
|web(_string_ text, [_number_ range, [_string_ language, [_number_ rate, [_number_ volume [, _boolean_ pauseComputerUntilSpeechComplete [, _string_ apiKey\]\]\]\]\]\])|_string_ event_uuid|Requests synthesized audio from the Voice RSS web service|

## Additional Voices/Languages

### Web

The web function uses the [Voice RSS](http://www.voicerss.org/) for voice synthesis.

Refer to the [documentation](http://www.voicerss.org/api/documentation.aspx) for supported languages.

An API key is required to use this service. A free once can be obtained by registering on their web site. Server
 admins can set this API key in the configuration. Clients can also specify an API key. The order of API key used when
 multiple are found is: function parameter \> server config \> client config.

### Speak

Some MBROLA voices are supported for the speak function:

- us1
- us2
- us3

These can be downloaded from [here](http://tcts.fpms.ac.be/synthesis/mbrola/mbrcopybin.html) and extracted to 
 `<minecraft directory>/mods/peripheralsplusone/mbrola/`. The directory should also contain the MBROLA executable.